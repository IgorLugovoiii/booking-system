package com.example.auth_service.services;

import com.example.auth_service.kafka.AuthProducer;
import com.example.auth_service.kafka.UserEvent;
import com.example.auth_service.models.User;
import com.example.auth_service.models.enums.Role;
import com.example.auth_service.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AuthProducer authProducer;

    @Autowired
    public UserService(UserRepository userRepository, AuthProducer authProducer) {
        this.userRepository = userRepository;
        this.authProducer = authProducer;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
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
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
        User user = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        authProducer.sendUserDeletedEvent(new UserEvent(
                user.getId(),
                "user.deleted",
                user.getUsername(),
                user.getRole().name()
        ));
    }
}
