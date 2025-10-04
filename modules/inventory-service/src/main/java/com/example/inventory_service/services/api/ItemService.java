package com.example.inventory_service.services.api;

import com.example.inventory_service.dtos.ItemRequest;
import com.example.inventory_service.dtos.ItemResponse;
import com.example.inventory_service.dtos.ItemSearchParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemService {
    ItemResponse findById(Long itemId) throws JsonProcessingException;

    List<ItemResponse> findAll();

    ItemResponse createItem(ItemRequest itemRequest) throws JsonProcessingException;

    ItemResponse updateItem(Long id, ItemRequest itemRequest) throws JsonProcessingException;

    void deleteById(Long id) throws JsonProcessingException;

    Page<ItemResponse> searchItem(ItemSearchParams itemSearchParams, Pageable pageable);
}
