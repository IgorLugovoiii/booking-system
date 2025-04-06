package com.example.auth_service.dtos;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}
