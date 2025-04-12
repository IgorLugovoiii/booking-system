package com.example.inventory_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemEvent {
    private String eventType;
    private Long itemId;
    private String name;
    private String category;
    private boolean available;
    private Double price;
}
