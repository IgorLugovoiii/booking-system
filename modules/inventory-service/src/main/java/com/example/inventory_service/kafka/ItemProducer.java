package com.example.inventory_service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ItemProducer {
    private static final String ITEM_TOPIC = "item-events";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendEvent(ItemEvent itemEvent) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(itemEvent);
        kafkaTemplate.send(ITEM_TOPIC, itemEvent.getEventType(), json);
    }
}
