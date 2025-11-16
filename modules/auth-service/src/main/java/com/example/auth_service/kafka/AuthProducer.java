package com.example.auth_service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthProducer {
    private static final String TOPIC = "auth-events";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendEvent(UserEvent userEvent) throws JsonProcessingException {
        try {
            String json = objectMapper.writeValueAsString(userEvent);
            kafkaTemplate.send(TOPIC, userEvent.getEventType(), json)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Kafka event sent: type={}, event={}", userEvent.getEventType(), json);
                        } else {
                            log.error("Kafka send failed: {}", ex.getMessage(), ex);
                            throw new RuntimeException("Failed to send Kafka message", ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Kafka serialization/send error: {}", e.getMessage(), e);
            throw e;
        }
    }
}
