package com.example.notification_service.kafka;

import lombok.Data;

@Data
public class ItemEvent {
    private Long id;
    private String name;
    private String description;
    private String category;
    private Double price;
    private Boolean available;
    private String eventType;
}
