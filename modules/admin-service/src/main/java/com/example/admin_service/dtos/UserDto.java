package com.example.admin_service.dtos;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String role;
}
