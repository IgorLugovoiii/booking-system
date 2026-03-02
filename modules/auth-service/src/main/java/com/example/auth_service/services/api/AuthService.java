package com.example.auth_service.services.api;

import com.example.auth_service.dtos.AuthRequest;
import com.example.auth_service.dtos.AuthResponse;
import com.example.auth_service.dtos.RegisterRequest;

public interface AuthService {
    AuthResponse registration(RegisterRequest registerRequest);
    AuthResponse authenticate(AuthRequest authRequest);
}
