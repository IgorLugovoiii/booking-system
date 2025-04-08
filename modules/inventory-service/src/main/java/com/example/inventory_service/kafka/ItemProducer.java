package com.example.inventory_service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ItemProducer {
    private static final String ITEM_TOPIC = "item-events";
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public ItemProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendItemCreatedEvent(String itemJson) {
        kafkaTemplate.send(ITEM_TOPIC, "item.created", itemJson);
    }

    public void sendItemUpdatedEvent(String itemJson) {
        kafkaTemplate.send(ITEM_TOPIC, "item.updated", itemJson);
    }

    public void sendItemDeletedEvent(String itemJson) {
        kafkaTemplate.send(ITEM_TOPIC, "item.deleted", itemJson);
    }
}
