package com.example.inventory_service.dtos;

import lombok.Data;

@Data
public class ItemResponse {
    private Long id;
    private String name;
    private String description;
    private String category;
    private Boolean available;
    private Double price;
}
