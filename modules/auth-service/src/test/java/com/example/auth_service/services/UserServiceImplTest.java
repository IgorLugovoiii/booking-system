package com.example.auth_service.services;

import com.example.auth_service.kafka.AuthProducer;
import com.example.auth_service.kafka.UserEvent;
import com.example.auth_service.models.User;
import com.example.auth_service.models.enums.Role;
import com.example.auth_service.repositories.UserRepository;
import com.example.auth_service.services.impl.UserServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthProducer authProducer;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    private User user;

    @BeforeEach
    void setUp(){
        user = User.builder()
                .id(1L)
                .username("john")
                .role(Role.USER)
                .build();
    }

    @Test
    void givenUsersExist_whenFindAll_thenReturnUserList(){
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> users = userServiceImpl.findAll();

        assertThat(users.getFirst().getUsername()).isEqualTo(user.getUsername());
        assertThat(users).hasSize(1);
        verify(userRepository).findAll();
    }

    @Test
    void givenNoUsers_whenFindAll_thenReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> users = userServiceImpl.findAll();

        assertThat(users).isEmpty();
        verify(userRepository).findAll();
    }

    @Test
    void givenUserExists_whenFindById_thenReturnUser(){
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        Optional<User> foundUser = userServiceImpl.findUserById(user.getId());

        assertThat(foundUser).isNotEmpty().isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(user.getId()) ;
        assertThat(foundUser.get().getUsername()).isEqualTo(user.getUsername());

        verify(userRepository).findById(user.getId());
    }

    @Test
    void givenUserDoesNotExist_whenFindById_thenReturnEmptyOptional(){
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<User> foundUser = userServiceImpl.findUserById(99L);

        assertThat(foundUser).isEmpty();
        verify(userRepository).findById(99L);
    }

    @Test
    void givenUserExists_whenUpdateUserRole_thenRoleUpdatedAndEventSent() throws JsonProcessingException {
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        User updatedUser = userServiceImpl.updateUserRole(user.getId(), Role.ADMIN.name());

        assertThat(updatedUser.getRole()).isEqualTo(Role.ADMIN);
        verify(authProducer).sendEvent(any(UserEvent.class));
        verify(userRepository).save(user);
    }

    @Test
    void givenUserDoesNotExist_whenUpdateUserRole_thenThrowException() throws JsonProcessingException {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userServiceImpl.updateUserRole(99L, Role.ADMIN.name()))
                .isInstanceOf(EntityNotFoundException.class);

        verify(userRepository, never()).save(any());
        verify(authProducer, never()).sendEvent(any());
    }


    @Test
    void givenInvalidRole_whenUpdateUserRole_thenThrowException() throws JsonProcessingException {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userServiceImpl.updateUserRole(user.getId(), "INVALID_ROLE"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).save(any());
        verify(authProducer, never()).sendEvent(any());
    }

    @Test
    void givenUserExists_whenDeleteUser_thenDeletedAndEventSent() throws JsonProcessingException {
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(any());

        userServiceImpl.deleteUserById(user.getId());

        verify(userRepository, times(1)).deleteById(any());
        verify(authProducer, times(1)).sendEvent(any());
    }

    @Test
    void givenUserDoesNotExist_whenDeleteUser_thenThrowException() throws JsonProcessingException {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userServiceImpl.deleteUserById(1L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(userRepository, never()).deleteById(any());
        verify(authProducer, never()).sendEvent(any());
    }
}
