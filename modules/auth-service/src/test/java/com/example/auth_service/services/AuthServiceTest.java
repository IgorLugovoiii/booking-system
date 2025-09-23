package com.example.auth_service.services;

import com.example.auth_service.dtos.AuthRequest;
import com.example.auth_service.dtos.AuthResponse;
import com.example.auth_service.dtos.RegisterRequest;
import com.example.auth_service.kafka.AuthProducer;
import com.example.auth_service.models.User;
import com.example.auth_service.models.enums.Role;
import com.example.auth_service.repositories.UserRepository;
import com.example.auth_service.utils.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private AuthProducer authProducer;

    @InjectMocks
    private AuthService authService;

    @Test
    void registration_shouldReturnAuthResponseWithToken() throws JsonProcessingException {
        RegisterRequest request = RegisterRequest.builder()
                .username("john")
                .email("john@example.com")
                .password("password")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .username("john")
                .email("john@example.com")
                .role(Role.USER)
                .password("encodedPassword")
                .build();

        when(bCryptPasswordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(any(), any())).thenReturn("jwt-token");

        AuthResponse response = authService.registration(request);

        assertEquals("jwt-token", response.getToken());
        verify(authProducer).sendEvent(any());
    }

    @Test
    void authenticate_shouldReturnAuthResponseWithToken() {
        AuthRequest authRequest = AuthRequest.builder()
                .username("john")
                .password("password")
                .build();

        User user = User.builder()
                .username("john")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findUserByUsername("john")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(), any())).thenReturn("jwt-token");

        AuthResponse response = authService.authenticate(authRequest);

        assertEquals("jwt-token", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
