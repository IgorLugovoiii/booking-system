package com.example.inventory_service.kafka;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemEvent {
    private String eventType;
    private Long itemId;
    private String name;
    private String category;
    private boolean available;
    private Double price;
}
