package com.example.auth_service.services;

import com.example.auth_service.dtos.AuthResponse;
import com.example.auth_service.kafka.AuthProducer;
import com.example.auth_service.kafka.UserEvent;
import com.example.auth_service.models.User;
import com.example.auth_service.models.enums.Role;
import com.example.auth_service.repositories.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class UserService {
    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    private final UserRepository userRepository;
    private final AuthProducer authProducer;

    @Autowired
    public UserService(UserRepository userRepository, AuthProducer authProducer) {
        this.userRepository = userRepository;
        this.authProducer = authProducer;
    }

    @Transactional(readOnly = true)
    @CircuitBreaker(name = "authService", fallbackMethod = "findAllFallback")
    @Retry(name = "authService")
    @RateLimiter(name = "authService")
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    @CircuitBreaker(name = "authService", fallbackMethod = "findUserByIdFallback")
    @Retry(name = "authService")
    @RateLimiter(name = "authService")
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    @CircuitBreaker(name = "authService", fallbackMethod = "updateUserFallback")
    @Retry(name = "authService")
    @RateLimiter(name = "authService")
    public User updateUserRole(Long id, String newRole) {
        User user = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        user.setRole(Role.valueOf(newRole));
        authProducer.sendUserRoleUpdateEvent(new UserEvent(
                user.getId(),
                "user.role.updated",
                user.getUsername(),
                user.getRole().name()
        ));
        return userRepository.save(user);
    }

    @Transactional
    @CircuitBreaker(name = "authService", fallbackMethod = "deleteUserByIdFallback")
    @Retry(name = "authService")
    @RateLimiter(name = "authService")
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        userRepository.deleteById(id);
        authProducer.sendUserDeletedEvent(new UserEvent(
                user.getId(),
                "user.deleted",
                user.getUsername(),
                user.getRole().name()
        ));
    }

    public List<User> findAllFallback(Throwable t) {
        logger.severe("Fallback triggered in findAllFallback: " + t.getMessage());
        throw new IllegalStateException("Fallback: can`t find all users");
    }

    public Optional<User> findUserByIdFallback(Long id, Throwable t) {
        logger.severe("Fallback triggered in findUserByIdFallback: " + t.getMessage());
        throw new IllegalStateException("Fallback: can`t find user with id: " + id);
    }

    public User updateUserRoleFallback(Long id, String newRole, Throwable t) {
        logger.severe("Fallback triggered in updateUserRoleFallback: " + t.getMessage());
        throw new IllegalStateException("Fallback: can`t update role for user with id: " + id);
    }

    public void deleteUserByIdFallback(Long id, Throwable t) {
        logger.severe("Fallback triggered in deleteUserByIdFallback: " + t.getMessage());
        throw new IllegalStateException("Fallback: can`t delete user with id: " + id);
    }
}
