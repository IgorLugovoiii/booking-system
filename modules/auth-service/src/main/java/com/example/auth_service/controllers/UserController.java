package com.example.auth_service.controllers;

import com.example.auth_service.dtos.UpdateRoleRequest;
import com.example.auth_service.models.User;
import com.example.auth_service.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> findAllUsers(){
        return new ResponseEntity<>(userService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> findUserById(@PathVariable Long id){
        return new ResponseEntity<>(userService.findUserById(id).orElseThrow(EntityNotFoundException::new), HttpStatus.OK);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<User> updateUserRole(@PathVariable Long id, @RequestBody UpdateRoleRequest newRole){
        return new ResponseEntity<>(userService.updateUserRole(id, newRole.getNewRole()), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id){
        userService.deleteUserById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
