package com.example.admin_service.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UpdateRoleRequest {
    @Schema(description = "Enter new user role", example = "ADMIN/USER")
    private String newRole;
}
