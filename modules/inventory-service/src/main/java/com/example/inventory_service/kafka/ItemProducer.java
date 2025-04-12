package com.example.inventory_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class ItemProducer {
    private static final String ITEM_TOPIC = "item-events";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public ItemProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendItemCreatedEvent(ItemEvent itemEvent) {
        try {
            String json = objectMapper.writeValueAsString(itemEvent);
            kafkaTemplate.send(ITEM_TOPIC, "item.created", json);
        } catch (Exception e) {
            throw new KafkaException("Failed to send item.created event", e);
        }

    }

    public void sendItemUpdatedEvent(ItemEvent itemEvent) {
        try {
            String json = objectMapper.writeValueAsString(itemEvent);
            kafkaTemplate.send(ITEM_TOPIC, "item.updated", json);
        } catch (Exception e) {
            throw new KafkaException("Failed to send item.updated event", e);
        }

    }

    public void sendItemDeletedEvent(ItemEvent itemEvent) {
        try {
            String json = objectMapper.writeValueAsString(itemEvent);
            kafkaTemplate.send(ITEM_TOPIC, "item.deleted", json);
        } catch (Exception e) {
            throw new KafkaException("Failed to send item.deleted event", e);
        }
    }
}
