package com.example.inventory_service.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ItemRequest {
    @NotBlank(message = "Name must not be blank")
    @Schema(description = "Item name", example = "Name")
    private String name;
    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Schema(description = "Item description", example = "Something, description, ...")
    private String description;
    @NotBlank(message = "Category is required")
    @Schema(description = "Item category", example = "Category")
    private String category;
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Schema(description = "Item price", example = "20.0")
    private Double price;
    @NotNull(message = "Availability must be specified")
    @Schema(description = "Is item available", example = "true/false")
    private Boolean available;
}
