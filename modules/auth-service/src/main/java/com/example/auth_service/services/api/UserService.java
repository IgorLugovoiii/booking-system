package com.example.auth_service.services.api;

import com.example.auth_service.models.User;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();

    Optional<User> findUserById(Long id);

    User updateUserRole(Long id, String newRole) throws JsonProcessingException;

    void deleteUserById(Long id) throws JsonProcessingException;
}
