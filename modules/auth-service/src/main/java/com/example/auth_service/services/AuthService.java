package com.example.auth_service.services;

import com.example.auth_service.dtos.AuthRequest;
import com.example.auth_service.dtos.AuthResponse;
import com.example.auth_service.dtos.RegisterRequest;
import com.example.auth_service.kafka.AuthProducer;
import com.example.auth_service.kafka.UserEvent;
import com.example.auth_service.models.User;
import com.example.auth_service.models.enums.Role;
import com.example.auth_service.repositories.UserRepository;
import com.example.auth_service.utils.JwtUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.logging.Logger;

@Service
public class AuthService {
    private static final Logger logger = Logger.getLogger(AuthService.class.getName());
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuthProducer authProducer;

    @Autowired
    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, BCryptPasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, AuthProducer authProducer) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.authProducer = authProducer;
    }

    private AuthResponse authResponse(User user) {
        AuthResponse authResponse = new AuthResponse();
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        authResponse.setToken(token);
        return authResponse;
    }


    @Transactional
    @CircuitBreaker(name = "authService", fallbackMethod = "registrationFallback")
    @Retry(name = "authService")
    @RateLimiter(name = "authService")
    public AuthResponse registration(RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userRepository.save(user);
        authProducer.sendUserRegisteredEvent(new UserEvent(
                user.getId(),
                "user.registered",
                user.getUsername(),
                user.getRole().name()
        ));

        return authResponse(user);
    }

    @Transactional
    @CircuitBreaker(name = "authService", fallbackMethod = "loginFallback")
    @Retry(name = "authService")
    @RateLimiter(name = "authService")
    public AuthResponse authenticate(AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException | UsernameNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Authentication failed", ex);
        }

        User user = userRepository.findUserByUsername(authRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return authResponse(user);
    }

    public AuthResponse registrationFallback(Throwable t) {
        logger.severe("Fallback triggered in registrationFallback: " + t.getMessage());
        throw new IllegalStateException("Fallback: can`t register user");
    }

    public AuthResponse loginFallback(Throwable t) {
        logger.severe("Fallback triggered in LoginFallback: " + t.getMessage());
        throw new IllegalStateException("Fallback: can`t login user");
    }
}
