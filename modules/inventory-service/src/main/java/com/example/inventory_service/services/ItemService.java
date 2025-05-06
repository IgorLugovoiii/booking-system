package com.example.inventory_service.services;

import com.example.inventory_service.dtos.ItemRequest;
import com.example.inventory_service.dtos.ItemResponse;
import com.example.inventory_service.kafka.ItemEvent;
import com.example.inventory_service.kafka.ItemProducer;
import com.example.inventory_service.models.Item;
import com.example.inventory_service.repositories.ItemRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ItemService {
    private static final Logger logger = Logger.getLogger(ItemService.class.getName());
    private final ItemRepository itemRepository;
    private final ItemProducer itemProducer;
    private final ItemCacheService itemCacheService;

    @Autowired
    public ItemService(ItemRepository itemRepository, ItemProducer itemProducer,
                       ItemCacheService itemCacheService) {
        this.itemRepository = itemRepository;
        this.itemProducer = itemProducer;
        this.itemCacheService = itemCacheService;
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
    @CircuitBreaker(name = "itemService", fallbackMethod = "findByIdFallback")
    @Retry(name = "itemService")
    @RateLimiter(name = "itemService")
    public ItemResponse findById(Long itemId) {
        Item cachedItem = itemCacheService.getItem(itemId);
        if (cachedItem != null) {
            return convertToItemResponse(cachedItem);
        }
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        itemCacheService.cacheItem(item);
        return convertToItemResponse(item);
    }

    @Transactional(readOnly = true)
    @CircuitBreaker(name = "itemService", fallbackMethod = "findAllFallback")
    @Retry(name = "itemService")
    @RateLimiter(name = "itemService")
    public List<ItemResponse> findAll() {
        return itemRepository.findAll().stream()
                .map(this::convertToItemResponse)
                .toList();
    }

    @Transactional
    @CircuitBreaker(name = "itemService", fallbackMethod = "createItemFallback")
    @Retry(name = "itemService")
    @RateLimiter(name = "itemService")
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
        itemCacheService.cacheItem(item);
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
    @CircuitBreaker(name = "itemService", fallbackMethod = "updateItemFallback")
    @Retry(name = "itemService")
    @RateLimiter(name = "itemService")
    public ItemResponse updateItem(Long id, ItemRequest itemRequest) {
        Item item = itemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        item.setName(itemRequest.getName());
        item.setDescription(itemRequest.getDescription());
        item.setCategory(itemRequest.getCategory());
        item.setPrice(itemRequest.getPrice());
        item.setAvailable(itemRequest.getAvailable());
        item.setUpdatedAt(LocalDateTime.now());

        itemRepository.save(item);
        itemCacheService.cacheItem(item);
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
    @CircuitBreaker(name = "itemService", fallbackMethod = "deleteByIdFallback")
    @Retry(name = "itemService")
    @RateLimiter(name = "itemService")
    public void deleteById(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        itemProducer.sendItemDeletedEvent(
                new ItemEvent("item.deleted",
                        item.getId(),
                        item.getName(),
                        item.getCategory(),
                        item.isAvailable(),
                        item.getPrice()
                ));

        itemCacheService.evictItem(id);
        itemRepository.deleteById(id);
    }

    public ItemResponse findByIdFallback(Long itemId, Throwable t) {
        logger.severe("Error fetching item with id " + itemId + ": " + t.getMessage());
        throw new IllegalStateException("Fallback: can't find item with id: " + itemId);
    }

    public List<ItemResponse> findAllFallback(Throwable t) {
        logger.severe("Error fetching all items: " + t.getMessage());
        throw new IllegalStateException("Fallback: can't find all items");
    }

    public ItemResponse createItemFallback(ItemRequest itemRequest, Throwable t) {
        logger.severe("Error creating item: " + t.getMessage());
        throw new IllegalStateException("Fallback: can't create item");
    }

    public ItemResponse updateItemFallback(Long id, ItemRequest itemRequest, Throwable t) {
        logger.severe("Error updating item with id " + id + ": " + t.getMessage());
        throw new IllegalStateException("Fallback: can't update item with id: " + id);
    }

    public void deleteByIdFallback(Long id, Throwable t) {
        logger.severe("Error deleting item with id " + id + ": " + t.getMessage());
        throw new IllegalStateException("Fallback: can't delete item with id: " + id);
    }
}
