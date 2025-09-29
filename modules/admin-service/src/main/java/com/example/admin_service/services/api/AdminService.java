package com.example.admin_service.services.api;

import com.example.admin_service.dtos.UpdateRoleRequest;
import com.example.admin_service.dtos.UserDto;

import java.util.List;

public interface AdminService {
    List<UserDto> getAllUsers();

    void updateUserRole(Long id, UpdateRoleRequest updateRoleRequest);

    void deleteUserById(Long id);
}
