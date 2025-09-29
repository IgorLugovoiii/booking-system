package com.example.admin_service.controllers;

import com.example.admin_service.dtos.UpdateRoleRequest;
import com.example.admin_service.dtos.UserDto;
import com.example.admin_service.services.api.AdminService;
import com.example.admin_service.services.impl.AdminServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin controller", description = "Controller for managing users, only for admins usage")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/allUsers")
    @Operation(summary = "Get all users")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of users",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = UserDto.class))
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden - only admins allowed"),
            @ApiResponse(responseCode = "404", description = "Users not found")
    })
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return new ResponseEntity<>(adminService.getAllUsers(), HttpStatus.OK);
    }

    @PutMapping("/updateRole/{id}")
    @Operation(summary = "Update user role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User role updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden - only admins allowed"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> updateRole(
            @Parameter(description = "ID of the user to update", example = "5")
            @PathVariable Long id, @RequestBody UpdateRoleRequest updateRoleRequest) {
        adminService.updateUserRole(id, updateRoleRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - only admins allowed"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUserById(
            @Parameter(description = "ID of the user to delete", example = "10")
            @PathVariable Long id) {
        adminService.deleteUserById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
