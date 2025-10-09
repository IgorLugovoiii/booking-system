package com.example.admin_service.services.impl;

import com.example.admin_service.clients.AuthServiceClient;
import com.example.admin_service.dtos.UpdateRoleRequest;
import com.example.admin_service.dtos.UserDto;
import com.example.admin_service.services.api.AdminService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@CircuitBreaker(name = "adminService")//Вимикає зовнішній сервіс при помилках
@Retry(name = "adminService")//Повторює запити при помилках
@RateLimiter(name = "adminService")//Обмежує кількість викликів за час
public class AdminServiceImpl implements AdminService {
    private final AuthServiceClient authServiceClient;

    @Override
    public List<UserDto> getAllUsers() {
        return authServiceClient.getAllUsers();
    }

    @Override
    public void updateUserRole(Long id, UpdateRoleRequest updateRoleRequest) {
        authServiceClient.updateUserRole(id, updateRoleRequest);

    }

    @Override
    public void deleteUserById(Long id) {
        authServiceClient.deleteUser(id);
    }
}
