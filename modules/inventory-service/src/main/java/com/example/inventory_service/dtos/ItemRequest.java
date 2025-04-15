package com.example.inventory_service.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ItemRequest {
    @NotBlank(message = "Name must not be blank")
    private String name;
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    @NotBlank(message = "Category is required")
    private String category;
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;
    @NotNull(message = "Availability must be specified")
    private Boolean available;
}
