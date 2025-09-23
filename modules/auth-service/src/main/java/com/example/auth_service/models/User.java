package com.example.auth_service.models;

import com.example.auth_service.models.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @Schema(description = "User id", example = "1")
    private Long id;
    @Size(min = 0, max = 30, message = "Incorrect username, length must be from 0 to 30")
    @Column(name = "username", nullable = false)
    @Schema(description = "Username", example = "username")
    private String username;
    @Column(name = "email", unique = true)
    @Email(message = "Invalid email format")
    @Schema(description = "User email", example = "example@gmail.com")
    private String email;
    @Column(name = "password", nullable = false)
    @Schema(description = "User password", example = "password123")
    private String password;
    @Enumerated(EnumType.STRING)
    @Schema(description = "User role", example = "ADMIN/USER")
    private Role role;
}
