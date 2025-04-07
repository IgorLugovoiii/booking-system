package com.example.auth_service.services;

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
    @Autowired
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    @Transactional(readOnly = true)
    public List<User> findAll(){
        return userRepository.findAll();
    }
    @Transactional(readOnly = true)
    public Optional<User> findUserById(Long id){
        return userRepository.findById(id);
    }
    @Transactional
    public User updateUserRole(Long id, String newRole){
        User user = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        user.setRole(Role.valueOf(newRole));
        return userRepository.save(user);
    }
    @Transactional
    public void deleteUserById(Long id){
        userRepository.deleteById(id);
    }
}
