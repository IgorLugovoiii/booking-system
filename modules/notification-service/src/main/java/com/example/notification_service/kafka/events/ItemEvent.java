package com.example.notification_service.kafka.events;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemEvent {
    private Long itemId;
    private String name;
    private String category;
    private Double price;
    private Boolean available;
    private String eventType;
}
