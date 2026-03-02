package com.example.auth_service.services.api;

import com.example.auth_service.models.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();

    Optional<User> findUserById(Long id);

    User updateUserRole(Long id, String newRole);

    void deleteUserById(Long id);
}
