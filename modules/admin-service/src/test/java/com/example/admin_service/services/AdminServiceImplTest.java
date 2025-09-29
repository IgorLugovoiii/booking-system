package com.example.admin_service.services;

import com.example.admin_service.clients.AuthServiceClient;
import com.example.admin_service.dtos.UpdateRoleRequest;
import com.example.admin_service.dtos.UserDto;
import com.example.admin_service.services.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceImplTest {
    @Mock
    private AuthServiceClient authServiceClient;
    @InjectMocks
    private AdminServiceImpl adminServiceImpl;

    private UserDto userDto;
    private UpdateRoleRequest updateRoleRequest;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .username("username")
                .role("USER")
                .build();

        updateRoleRequest = UpdateRoleRequest.builder().newRole("ADMIN").build();
    }

    @Test
    public void givenUsersExist_whenGetAllUsers_thenReturnUsers() {
        when(authServiceClient.getAllUsers()).thenReturn(List.of(userDto));

        List<UserDto> users = adminServiceImpl.getAllUsers();

        assertThat(users).isNotEmpty().hasSize(1);
        assertThat(users.getFirst().getUsername()).isEqualTo(userDto.getUsername());
        assertThat(users.size()).isEqualTo(1);

        verify(authServiceClient).getAllUsers();
    }

    @Test
    public void givenValidRequest_whenUpdateUserRole_thenDelegatesToClient() {
        doNothing().when(authServiceClient).updateUserRole(userDto.getId(), updateRoleRequest);

        adminServiceImpl.updateUserRole(userDto.getId(), updateRoleRequest);

        verify(authServiceClient).updateUserRole(userDto.getId(), updateRoleRequest);
    }

    @Test
    public void givenValidId_whenDeleteUserById_thenDelegatesToClient() {
        doNothing().when(authServiceClient).deleteUser(userDto.getId());

        adminServiceImpl.deleteUserById(userDto.getId());

        verify(authServiceClient).deleteUser(userDto.getId());
    }

    @Test
    public void givenClientThrows_whenGetAllUsers_thenPropagatesException(){
        when(authServiceClient.getAllUsers()).thenThrow(new RuntimeException());

        assertThatThrownBy(()-> adminServiceImpl.getAllUsers())
                .isInstanceOf(RuntimeException.class);

        verify(authServiceClient).getAllUsers();
    }

    @Test
    public void givenClientThrows_whenUpdateUserRole_thenPropagatesException(){
        doThrow(new RuntimeException()).when(authServiceClient).updateUserRole(userDto.getId(), updateRoleRequest);

        assertThatThrownBy(() -> adminServiceImpl.updateUserRole(userDto.getId(), updateRoleRequest))
                .isInstanceOf(RuntimeException.class);

        verify(authServiceClient).updateUserRole(userDto.getId(), updateRoleRequest);
    }

    @Test
    public void givenClientThrows_whenDeleteUserById_thenPropagatesException(){
        doThrow(new RuntimeException()).when(authServiceClient).deleteUser(userDto.getId());

        assertThatThrownBy(() -> adminServiceImpl.deleteUserById(userDto.getId()))
                .isInstanceOf(RuntimeException.class);

        verify(authServiceClient).deleteUser(userDto.getId());
    }
}
