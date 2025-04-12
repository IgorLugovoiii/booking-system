package com.example.inventory_service.services;

import com.example.inventory_service.dtos.ItemRequest;
import com.example.inventory_service.dtos.ItemResponse;
import com.example.inventory_service.kafka.ItemEvent;
import com.example.inventory_service.kafka.ItemProducer;
import com.example.inventory_service.models.Item;
import com.example.inventory_service.repositories.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemProducer itemProducer;

    @Autowired
    public ItemService(ItemRepository itemRepository, ItemProducer itemProducer) {
        this.itemRepository = itemRepository;
        this.itemProducer = itemProducer;
    }

    private ItemResponse convertToItemResponse(Item item) {
        ItemResponse itemResponse = new ItemResponse();
        itemResponse.setId(item.getId());
        itemResponse.setName(item.getName());
        itemResponse.setDescription(item.getDescription());
        itemResponse.setCategory(item.getCategory());
        itemResponse.setPrice(item.getPrice());
        itemResponse.setAvailable(item.isAvailable());

        return itemResponse;
    }

    @Transactional(readOnly = true)
    public ItemResponse findById(Long itemId) {
        return convertToItemResponse(itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new));
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> findAll() {
        return itemRepository.findAll().stream()
                .map(this::convertToItemResponse)
                .toList();
    }

    @Transactional
    public ItemResponse createItem(ItemRequest itemRequest) {
        Item item = new Item();
        item.setName(itemRequest.getName());
        item.setDescription(itemRequest.getDescription());
        item.setCategory(itemRequest.getCategory());
        item.setPrice(itemRequest.getPrice());
        item.setAvailable(itemRequest.getAvailable());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        itemRepository.save(item);
        itemProducer.sendItemCreatedEvent(
                new ItemEvent("item.created",
                        item.getId(),
                        item.getName(),
                        item.getCategory(),
                        item.isAvailable(),
                        item.getPrice()
                ));
        return convertToItemResponse(item);
    }

    @Transactional
    public ItemResponse updateItem(Long id, ItemRequest itemRequest) {
        Item item = itemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        item.setName(itemRequest.getName());
        item.setDescription(itemRequest.getDescription());
        item.setCategory(itemRequest.getCategory());
        item.setPrice(itemRequest.getPrice());
        item.setAvailable(itemRequest.getAvailable());
        item.setUpdatedAt(LocalDateTime.now());

        itemRepository.save(item);
        itemProducer.sendItemUpdatedEvent(
                new ItemEvent("item.updated",
                        item.getId(),
                        item.getName(),
                        item.getCategory(),
                        item.isAvailable(),
                        item.getPrice()
                ));
        return convertToItemResponse(item);
    }

    @Transactional
    public void deleteById(Long id) {
        itemRepository.deleteById(id);
        Item item = itemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        itemProducer.sendItemDeletedEvent(
                new ItemEvent("item.deleted",
                        item.getId(),
                        item.getName(),
                        item.getCategory(),
                        item.isAvailable(),
                        item.getPrice()
                ));
    }
}
