package com.example.auth_service.services.impl;

import com.example.auth_service.kafka.AuthProducer;
import com.example.auth_service.kafka.UserEvent;
import com.example.auth_service.models.User;
import com.example.auth_service.models.enums.Role;
import com.example.auth_service.repositories.UserRepository;
import com.example.auth_service.services.api.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AuthProducer authProducer;

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public User updateUserRole(Long id, String newRole) throws JsonProcessingException {
        User user = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        user.setRole(Role.valueOf(newRole));
        sendKafkaEventSafely(() -> {
            try {
                authProducer.sendEvent(new UserEvent(
                        user.getId(),
                        "user.role.updated",
                        user.getUsername(),
                        user.getRole().name()
                ));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) throws JsonProcessingException {
        User user = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        userRepository.deleteById(id);
        sendKafkaEventSafely(() ->
        {
            try {
                authProducer.sendEvent(new UserEvent(
                        user.getId(),
                        "user.deleted",
                        user.getUsername(),
                        user.getRole().name()
                ));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @CircuitBreaker(name = "authService")
    @Retry(name = "authService")
    @RateLimiter(name = "authService")
    private void sendKafkaEventSafely(Runnable runnable) {
        runnable.run();
    }
}