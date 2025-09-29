package com.example.auth_service.services.impl;

import com.example.auth_service.dtos.AuthRequest;
import com.example.auth_service.dtos.AuthResponse;
import com.example.auth_service.dtos.RegisterRequest;
import com.example.auth_service.kafka.AuthProducer;
import com.example.auth_service.kafka.UserEvent;
import com.example.auth_service.models.User;
import com.example.auth_service.models.enums.Role;
import com.example.auth_service.repositories.UserRepository;
import com.example.auth_service.services.api.AuthService;
import com.example.auth_service.utils.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuthProducer authProducer;

    private AuthResponse authResponse(User user) {
        AuthResponse authResponse = new AuthResponse();
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        authResponse.setToken(token);
        return authResponse;
    }


    @Transactional
    @CircuitBreaker(name = "authService")
    @Retry(name = "authService")
    @RateLimiter(name = "authService")
    public AuthResponse registration(RegisterRequest registerRequest) throws JsonProcessingException {
        if(userRepository.findUserByUsername(registerRequest.getUsername()).isPresent()){
            throw new IllegalStateException("User already exists");
        }

        if(userRepository.findUserByEmail(registerRequest.getEmail()).isPresent()){
            throw new IllegalStateException("Email already registered");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userRepository.save(user);
        authProducer.sendEvent(new UserEvent(
                user.getId(),
                "user.registered",
                user.getUsername(),
                user.getRole().name()
        ));

        return authResponse(user);
    }

    @Transactional
    @CircuitBreaker(name = "authService")
    @Retry(name = "authService")
    @RateLimiter(name = "authService")
    public AuthResponse authenticate(AuthRequest authRequest) {
        if (authRequest.getUsername().isEmpty() || authRequest.getPassword().isEmpty()){
            throw new IllegalStateException("Authentication failed");
        }

        User user = userRepository.findUserByUsername(authRequest.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

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

        return authResponse(user);
    }
}
