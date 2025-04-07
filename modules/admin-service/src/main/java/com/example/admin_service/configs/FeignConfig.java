package com.example.admin_service.configs;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor requestInterceptor(){
        return requestTemplate -> {
            String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
            requestTemplate.header("Authorization", "Bearer " + token);
        };
    }
    /*конфігурація для клієнта Feign,
     яка додає JWT токен до кожного запиту від admin-service до auth-service
     RequestInterceptor — спеціальний інтерфейс Feign,
     який дозволяє перехоплювати й змінювати HTTP-запит перед його надсиланням*/
}
