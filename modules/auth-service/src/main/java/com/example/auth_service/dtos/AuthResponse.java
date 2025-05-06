package com.example.auth_service.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AuthResponse {
    @Schema(description = "Token that is given after login in")
    private String token;
}
