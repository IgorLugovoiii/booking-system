package com.example.inventory_service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LogProducer {
    private static final String LOG_TOPIC = "logs-topic";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sentLogEvent(Map<String, Object> logMap) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(logMap);
        kafkaTemplate.send(LOG_TOPIC, "log", json);
    }
}