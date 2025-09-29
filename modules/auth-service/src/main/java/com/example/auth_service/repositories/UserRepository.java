package com.example.auth_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.auth_service.models.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByUsername(String username);
    Optional<User> findUserByEmail(String email);
}
