package com.example.admin_service.clients;

import com.example.admin_service.configs.FeignConfig;
import com.example.admin_service.dtos.UpdateRoleRequest;
import com.example.admin_service.dtos.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "auth-service", configuration = FeignConfig.class)
public interface AuthServiceClient {
    @GetMapping("/api/users")
    List<UserDto> getAllUsers();
    @PutMapping("/api/users/{id}/role")
    void updateUserRole(@PathVariable Long id, @RequestBody UpdateRoleRequest request);
    @DeleteMapping("/api/users/{id}")
    void deleteUser(@PathVariable Long id);
}
