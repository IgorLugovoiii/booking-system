package com.example.auth_service.services.api;

import com.example.auth_service.dtos.AuthRequest;
import com.example.auth_service.dtos.AuthResponse;
import com.example.auth_service.dtos.RegisterRequest;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface AuthService {
    AuthResponse registration(RegisterRequest registerRequest) throws JsonProcessingException;
    AuthResponse authenticate(AuthRequest authRequest);
}
