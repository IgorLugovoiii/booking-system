package com.example.auth_service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthProducer {
    private static final String TOPIC = "auth-events";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendEvent(UserEvent userEvent) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(userEvent);
        kafkaTemplate.send(TOPIC, userEvent.getEventType(), json);
    }
}
