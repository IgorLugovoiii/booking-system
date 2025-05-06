package com.example.admin_service.services;

import com.example.admin_service.clients.AuthServiceClient;
import com.example.admin_service.dtos.UpdateRoleRequest;
import com.example.admin_service.dtos.UserDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.logging.Logger;

@Service
public class AdminService {
    private final AuthServiceClient authServiceClient;
    private static final Logger logger = Logger.getLogger(AdminService.class.getName());

    @Autowired
    public AdminService(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @Transactional(readOnly = true)
    @CircuitBreaker(name = "adminService", fallbackMethod = "getAllUsersFallback")//Вимикає зовнішній сервіс при помилках
    @Retry(name = "adminService")//Повторює запити при помилках
    @RateLimiter(name = "adminService")//Обмежує кількість викликів за час
    public List<UserDto> getAllUsers() {
        return authServiceClient.getAllUsers();
    }

    @Transactional
    @CircuitBreaker(name = "adminService", fallbackMethod = "updateUserRoleFallback")
    @Retry(name = "adminService")
    @RateLimiter(name = "adminService")
    public void updateUserRole(Long id, UpdateRoleRequest updateRoleRequest) {
        authServiceClient.updateUserRole(id, updateRoleRequest);

    }

    @Transactional
    @CircuitBreaker(name = "adminService", fallbackMethod = "deleteUserFallback")
    @Retry(name = "adminService")
    @RateLimiter(name = "adminService")
    public void deleteUserById(Long id) {
        authServiceClient.deleteUser(id);
    }

    public List<UserDto> getAllUsersFallback(Throwable t) {
        logger.severe("Error fetching all users: " + t.getMessage());
        throw new IllegalStateException("Fallback: admin service is unavailable: " + t.getMessage());
    }

    public void updateUserRoleFallback(Long id, UpdateRoleRequest updateRoleRequest, Throwable t) {
        logger.severe("Error updating role for user with id " + id + ": " + t.getMessage());
        throw new IllegalStateException("Fallback: can't update role for user with id: " + id);
    }


    public void deleteUserFallback(Long id, Throwable t) { // обов'язково робити однакову сигнатуру як в методі а потім throwable
        logger.severe("Error deleting user with id " + id + ": " + t.getMessage());
        throw new IllegalStateException("Fallback: can't delete user with id: " + id);
    }
}
