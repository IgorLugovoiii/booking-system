package com.example.auth_service.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UpdateRoleRequest {
    @Schema(description = "New user role", example = "ADMIN/USER")
    private String newRole;
}
