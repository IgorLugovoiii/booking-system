package com.example.auth_service.kafka;

import com.example.auth_service.exceptions.KafkaMessageSendException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuthProducer {
    private static final String TOPIC = "auth-events";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuthProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendUserRoleUpdateEvent(UserEvent userEvent) {
        try {
            String json = objectMapper.writeValueAsString(userEvent);
            kafkaTemplate.send(TOPIC, "user.role.updated", json);
        } catch (Exception e) {
            throw new KafkaMessageSendException("Failed to send user.role.updated event", e);
        }
    }

    public void sendUserDeletedEvent(UserEvent userEvent) {
        try {
            String json = objectMapper.writeValueAsString(userEvent);
            kafkaTemplate.send(TOPIC, "user.deleted", json);
        } catch (Exception e) {
            throw new KafkaMessageSendException("Failed to send user.deleted event", e);
        }
    }

    public void sendUserRegisteredEvent(UserEvent userEvent) {
        try {
            String json = objectMapper.writeValueAsString(userEvent);
            kafkaTemplate.send(TOPIC, "user.registered", json);
        } catch (Exception e) {
            throw new KafkaMessageSendException("Failed to send user.registered event", e);
        }
    }
}
