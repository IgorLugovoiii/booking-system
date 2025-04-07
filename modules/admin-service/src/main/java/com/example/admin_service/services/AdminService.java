package com.example.admin_service.services;

import com.example.admin_service.clients.AuthServiceClient;
import com.example.admin_service.dtos.UpdateRoleRequest;
import com.example.admin_service.dtos.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {
    private final AuthServiceClient authServiceClient;
    @Autowired
    public AdminService(AuthServiceClient authServiceClient){
        this.authServiceClient = authServiceClient;
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers(){
        return authServiceClient.getAllUsers();
    }

    @Transactional
    public void updateUserRole(Long id, UpdateRoleRequest updateRoleRequest){
        authServiceClient.updateUserRole(id, updateRoleRequest);
    }

    @Transactional
    public void deleteUserById(Long id){
        authServiceClient.deleteUser(id);
    }
}
