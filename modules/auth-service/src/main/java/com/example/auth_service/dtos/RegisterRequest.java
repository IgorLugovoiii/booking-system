package com.example.auth_service.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 1, max = 256, message = "Username must be at least 6 characters and less than 256")
    @Schema(description = "Username", example = "username")
    private String username;
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Schema(description = "User email", example = "example@gmail.com")
    private String email;
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "User password", example = "password123")
    private String password;
}
