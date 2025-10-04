package com.example.inventory_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemSearchParams {
    private String name;
    private String category;
    private double minPrice;
    private double maxPrice;
    private Boolean available;
}
