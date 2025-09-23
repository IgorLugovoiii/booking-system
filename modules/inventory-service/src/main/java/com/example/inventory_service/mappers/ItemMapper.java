package com.example.inventory_service.mappers;

import com.example.inventory_service.dtos.ItemRequest;
import com.example.inventory_service.dtos.ItemResponse;
import com.example.inventory_service.models.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Item toItem(ItemRequest itemRequest);
    ItemResponse toItemResponse(Item item);
}
