package com.example.admin_service.integration;

import com.example.admin_service.dtos.UpdateRoleRequest;
import com.example.admin_service.dtos.UserDto;
import com.example.admin_service.services.impl.AdminServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
public class AdminControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminServiceImpl adminServiceImpl;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldReturnAllUsers() throws Exception {
        List<UserDto> users = List.of(
                new UserDto(1L, "Igor", "USER"),
                new UserDto(2L, "Anna", "ADMIN")
        );

        when(adminServiceImpl.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/admin/allUsers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                        .andExpect(jsonPath("$[0].username").value("Igor"))
                        .andExpect(jsonPath("$[1].role").value("ADMIN"));

        verify(adminServiceImpl, times(1)).getAllUsers();
    }

    @Test
    public void shouldUpdateUserRole() throws Exception {
        UpdateRoleRequest req = new UpdateRoleRequest();
        req.setNewRole("ADMIN");

        mockMvc.perform(put("/api/admin/updateRole/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(adminServiceImpl, times(1)).updateUserRole(eq(1L), eq(req));
    }

    @Test
    public void shouldReturn404WhenUpdatingNonexistentUser() throws Exception {
        UpdateRoleRequest req = new UpdateRoleRequest();
        req.setNewRole("USER");
        doThrow(new EntityNotFoundException()).when(adminServiceImpl).updateUserRole(eq(1L), eq(req));

        mockMvc.perform(put("/api/admin/updateRole/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/admin/5"))
                .andExpect(status().isNoContent());

        verify(adminServiceImpl, times(1)).deleteUserById(5L);
    }

    @Test
    public void shouldReturn404WhenDeletingNonexistentUser() throws Exception {
        doThrow(new EntityNotFoundException()).when(adminServiceImpl).deleteUserById(42L);

        mockMvc.perform(delete("/api/admin/42"))
                .andExpect(status().isNotFound());
    }
}
