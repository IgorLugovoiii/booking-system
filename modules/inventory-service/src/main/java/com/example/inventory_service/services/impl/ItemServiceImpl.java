package com.example.inventory_service.services.impl;

import com.example.inventory_service.dtos.ItemRequest;
import com.example.inventory_service.dtos.ItemResponse;
import com.example.inventory_service.kafka.ItemProducer;
import com.example.inventory_service.mappers.ItemEventMapper;
import com.example.inventory_service.mappers.ItemMapper;
import com.example.inventory_service.models.Item;
import com.example.inventory_service.repositories.ItemRepository;
import com.example.inventory_service.services.api.ItemService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemProducer itemProducer;
    private final ItemCacheService itemCacheService;
    private final ItemMapper itemMapper;
    private final ItemEventMapper itemEventMapper;

    @Transactional(readOnly = true)
    @CircuitBreaker(name = "itemService")
    @Retry(name = "itemService")
    @RateLimiter(name = "itemService")
    public ItemResponse findById(Long itemId) throws JsonProcessingException {
        Item cachedItem = itemCacheService.getItem(itemId);
        if (cachedItem != null) {
            return itemMapper.toItemResponse(cachedItem);
        }
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        itemCacheService.cacheItem(item);
        return itemMapper.toItemResponse(item);
    }

    @Transactional(readOnly = true)
    @CircuitBreaker(name = "itemService")
    @Retry(name = "itemService")
    @RateLimiter(name = "itemService")
    public List<ItemResponse> findAll() {
        return itemRepository.findAll().stream()
                .map(itemMapper::toItemResponse)
                .toList();
    }

    @Transactional
    @CircuitBreaker(name = "itemService")
    @Retry(name = "itemService")
    @RateLimiter(name = "itemService")
    public ItemResponse createItem(ItemRequest itemRequest) throws JsonProcessingException {
        Item item = itemMapper.toItem(itemRequest);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        itemRepository.save(item);
        itemCacheService.cacheItem(item);
        itemProducer.sendEvent(itemEventMapper.toCreatedEvent(item));
        return itemMapper.toItemResponse(item);
    }

    @Transactional
    @CircuitBreaker(name = "itemService")
    @Retry(name = "itemService")
    @RateLimiter(name = "itemService")
    public ItemResponse updateItem(Long id, ItemRequest itemRequest) throws JsonProcessingException {
        Item item = itemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        item.setName(itemRequest.getName());
        item.setDescription(itemRequest.getDescription());
        item.setCategory(itemRequest.getCategory());
        item.setPrice(itemRequest.getPrice());
        item.setAvailable(itemRequest.getAvailable());
        item.setUpdatedAt(LocalDateTime.now());

        itemRepository.save(item);
        itemCacheService.cacheItem(item);
        itemProducer.sendEvent(itemEventMapper.toUpdatedEvent(item));
        return itemMapper.toItemResponse(item);
    }

    @Transactional
    @CircuitBreaker(name = "itemService")
    @Retry(name = "itemService")
    @RateLimiter(name = "itemService")
    public void deleteById(Long id) throws JsonProcessingException {
        Item item = itemRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        itemProducer.sendEvent(itemEventMapper.toDeletedEvent(item));

        itemCacheService.evictItem(id);
        itemRepository.deleteById(id);
    }

}
