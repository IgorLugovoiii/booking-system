package com.example.auth_service.models;

import com.example.auth_service.models.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Table(name = "users")
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Size(min = 0, max = 30, message = "Incorrect username, length must be from 0 to 30")
    @Column(name = "username", nullable = false)
    private String username;
    @Column(name = "email", unique = true)
    @Email(message = "Invalid email format")
    private String email;
    @Column(name = "password", nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
}
