package com.example.inventory_service.services.api;

import com.example.inventory_service.dtos.ItemRequest;
import com.example.inventory_service.dtos.ItemResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface ItemService {
    ItemResponse findById(Long itemId) throws JsonProcessingException;

    List<ItemResponse> findAll();

    ItemResponse createItem(ItemRequest itemRequest) throws JsonProcessingException;

    ItemResponse updateItem(Long id, ItemRequest itemRequest) throws JsonProcessingException;

    void deleteById(Long id) throws JsonProcessingException;
}
