package com.example.admin_service.configs;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor requestInterceptor(){
        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getCredentials() != null) {
                String token = authentication.getCredentials().toString();
                requestTemplate.header("Authorization", "Bearer " + token);
            }
        };
    }
    /*конфігурація для клієнта Feign,
     яка додає JWT токен до кожного запиту від admin-service до auth-service
     RequestInterceptor — спеціальний інтерфейс Feign,
     який дозволяє перехоплювати й змінювати HTTP-запит перед його надсиланням

     адмін-сервіс перехоплює запит, що потім змінити його, додати jwt token у заголовок
     і потім коли адмін сервіс робить запит до іншого сервісу,
     той сервіс перевіряє що за користува і чи він аунтифікований*/
}
