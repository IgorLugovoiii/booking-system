package com.example.inventory_service.services.api;

import com.example.inventory_service.dtos.ItemRequest;
import com.example.inventory_service.dtos.ItemResponse;
import com.example.inventory_service.dtos.ItemSearchParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemService {
    ItemResponse findById(Long itemId);

    List<ItemResponse> findAll();

    ItemResponse createItem(ItemRequest itemRequest);

    ItemResponse updateItem(Long id, ItemRequest itemRequest);

    void deleteById(Long id);

    Page<ItemResponse> searchItem(ItemSearchParams itemSearchParams, Pageable pageable);
}
