package com.example.admin_service.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {
    @Schema(description = "User id", example = "1")
    private Long id;
    @Schema(description = "Username", example = "user")
    private String username;
    @Schema(description = "Role of the user", example = "ADMIN/USER")
    private String role;
}
