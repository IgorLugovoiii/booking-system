package com.example.api_gateway.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/prometheus").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/error").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2-> oauth2
                        .jwt(
                                jwtSpec -> jwtSpec //змінити реалм
                                        .jwkSetUri("http://keycloak:8080/realms/booking-system/protocol/openid-connect/certs")
                        ))
                .build();
    }
}