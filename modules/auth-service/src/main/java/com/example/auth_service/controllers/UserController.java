package com.example.auth_service.controllers;

import com.example.auth_service.dtos.UpdateRoleRequest;
import com.example.auth_service.models.User;
import com.example.auth_service.services.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User controller", description = "Controller for managing users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get all users")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of users",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = User.class))
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Users not found")
    })
    @GetMapping
    public ResponseEntity<List<User>> findAllUsers(){
        return new ResponseEntity<>(userService.findAll(), HttpStatus.OK);
    }

    @Operation(summary = "Find user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> findUserById(
            @Parameter(description = "ID of the user to find", example = "1")
            @PathVariable Long id){
        return new ResponseEntity<>(userService.findUserById(id).orElseThrow(EntityNotFoundException::new), HttpStatus.OK);
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Update user role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User role updated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<User> updateUserRole(
            @Parameter(description = "ID of the user to update", example = "1")
            @PathVariable Long id, @RequestBody UpdateRoleRequest newRole) throws JsonProcessingException {
        return new ResponseEntity<>(userService.updateUserRole(id, newRole.getNewRole()), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUserById(
            @Parameter(description = "ID of the user to delete", example = "1")
            @PathVariable Long id) throws JsonProcessingException {
        userService.deleteUserById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
