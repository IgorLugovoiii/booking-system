package com.example.inventory_service.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ItemResponse {
    @Schema(description = "Item id", example = "1")
    private Long id;
    @Schema(description = "Item name", example = "Name")
    private String name;
    @Schema(description = "Item description", example = "Description ... more text")
    private String description;
    @Schema(description = "Item category", example = "Category")
    private String category;
    @Schema(description = "Is item available", example = "true/false")
    private Boolean available;
    @Schema(description = "Price", example = "40.0")
    private Double price;
}
