package com.example.admin_service.services;

import com.example.admin_service.clients.AuthServiceClient;
import com.example.admin_service.dtos.UpdateRoleRequest;
import com.example.admin_service.dtos.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {
    @Mock
    private AuthServiceClient authServiceClient;
    @InjectMocks
    private AdminService adminService;

    private UserDto userDto;
    private UpdateRoleRequest updateRoleRequest;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("username");
        userDto.setRole("USER");

        updateRoleRequest = new UpdateRoleRequest();
        updateRoleRequest.setNewRole("ADMIN");
    }

    @Test
    public void testGetAllUsers() {
        when(authServiceClient.getAllUsers()).thenReturn(List.of(userDto));

        List<UserDto> users = adminService.getAllUsers();

        assertNotNull(users);
        assertEquals(userDto.getUsername(), users.getFirst().getUsername());
        assertEquals(1, users.size());
        verify(authServiceClient, times(1)).getAllUsers();
    }

    @Test
    public void testUpdateUserRole() {
        doNothing().when(authServiceClient).updateUserRole(1L, updateRoleRequest);

        adminService.updateUserRole(1L, updateRoleRequest);

        verify(authServiceClient, times(1)).updateUserRole(1L, updateRoleRequest);
    }

    @Test
    public void testDeleteUserById() {
        doNothing().when(authServiceClient).deleteUser(1L);

        adminService.deleteUserById(1L);

        verify(authServiceClient, times(1)).deleteUser(1L);
    }
}
