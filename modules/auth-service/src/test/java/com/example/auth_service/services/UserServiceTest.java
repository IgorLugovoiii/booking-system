package com.example.auth_service.services;

import com.example.auth_service.kafka.AuthProducer;
import com.example.auth_service.models.User;
import com.example.auth_service.models.enums.Role;
import com.example.auth_service.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthProducer authProducer;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp(){
        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setRole(Role.USER);
    }

    @Test
    void testFindAllUsers(){
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> users = userService.findAll();

        assertEquals(user.getUsername(), users.getFirst().getUsername());
        assertEquals(1, users.size());
    }

    @Test
    void testFindById(){
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.findUserById(1L);

        assertNotNull(foundUser);
        assertTrue(foundUser.isPresent());
        assertEquals(user.getId(), foundUser.get().getId());
        assertEquals(user.getUsername(), foundUser.get().getUsername());
    }

    @Test
    void testUpdateUserRole(){
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        User updatedUser = userService.updateUserRole(user.getId(), Role.ADMIN.name());

        assertEquals(Role.ADMIN, updatedUser.getRole());
        verify(authProducer, times(1)).sendUserRoleUpdateEvent(any());
    }

    @Test
    void testUpdateUserRole_UserNotFound_ShouldThrowException() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.updateUserRole(99L, Role.ADMIN.name()));
    }

    @Test
    void testDeleteUserById(){
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(any());

        userService.deleteUserById(user.getId());

        verify(userRepository, times(1)).deleteById(any());
        verify(authProducer, times(1)).sendUserDeletedEvent(any());
    }

    @Test
    void testDeleteUserById_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.deleteUserById(1L));

        verify(userRepository, never()).deleteById(any());
        verify(authProducer, never()).sendUserDeletedEvent(any());
    }
}
