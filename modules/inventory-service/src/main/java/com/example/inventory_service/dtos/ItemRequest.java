package com.example.inventory_service.dtos;

import lombok.Data;

@Data
public class ItemRequest {
    private String name;
    private String description;
    private String category;
    private Double price;
    private Boolean available;
}
