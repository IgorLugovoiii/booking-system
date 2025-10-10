package com.example.auth_service.services;

import com.example.auth_service.dtos.AuthRequest;
import com.example.auth_service.dtos.AuthResponse;
import com.example.auth_service.dtos.RegisterRequest;
import com.example.auth_service.kafka.AuthProducer;
import com.example.auth_service.models.User;
import com.example.auth_service.models.enums.Role;
import com.example.auth_service.repositories.UserRepository;
import com.example.auth_service.services.impl.AuthServiceImpl;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {
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
    private AuthServiceImpl authServiceImpl;

    @Test
    void givenValidRegisterRequest_whenRegistration_thenReturnAuthResponseWithToken() throws JsonProcessingException {
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
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("jwt-token");

        AuthResponse response = authServiceImpl.registration(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(authProducer).sendEvent(any());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void givenValidAuthRequest_whenAuthenticate_thenReturnAuthResponseWithToken() {
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
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("jwt-token");

        AuthResponse response = authServiceImpl.authenticate(authRequest);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(userRepository).findUserByUsername("john");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void givenExistingUser_whenRegistration_thenThrowIllegalStateException() throws JsonProcessingException {
        RegisterRequest request = RegisterRequest.builder()
                .username("john")
                .email("john@example.com")
                .password("password")
                .build();

        User existingUser = User.builder()
                .id(1L)
                .username("john")
                .email("john@example.com")
                .role(Role.USER)
                .password("encodedPassword")
                .build();

        when(userRepository.findUserByUsername("john")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authServiceImpl.registration(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User already exists");

        verify(userRepository).findUserByUsername("john");
        verify(userRepository, never()).save(any());
        verify(authProducer, never()).sendEvent(any());
    }

    @Test
    void givenNonExistingUser_whenAuthenticate_thenThrowEntityNotFoundException() {
        AuthRequest authRequest = AuthRequest.builder()
                .username("john")
                .password("password")
                .build();

        when(userRepository.findUserByUsername("john")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authServiceImpl.authenticate(authRequest))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findUserByUsername("john");
        verify(authenticationManager, never()).authenticate(any());
        verify(jwtUtil, never()).generateToken(any(), any(),any());
    }

}
