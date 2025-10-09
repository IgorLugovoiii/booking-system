package com.example.inventory_service.services.impl;

import com.example.inventory_service.dtos.ItemRequest;
import com.example.inventory_service.dtos.ItemResponse;
import com.example.inventory_service.dtos.ItemSearchParams;
import com.example.inventory_service.kafka.ItemProducer;
import com.example.inventory_service.mappers.ItemEventMapper;
import com.example.inventory_service.mappers.ItemMapper;
import com.example.inventory_service.models.Item;
import com.example.inventory_service.repositories.ItemRepository;
import com.example.inventory_service.services.api.ItemService;
import com.example.inventory_service.services.utils.SpecificationUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    @Override
    @Transactional(readOnly = true)
    public ItemResponse findById(Long itemId) throws JsonProcessingException {
        Item cachedItem = itemCacheService.getItem(itemId);
        if (cachedItem != null) {
            return itemMapper.toItemResponse(cachedItem);
        }
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        itemCacheService.cacheItem(item);
        return itemMapper.toItemResponse(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> findAll() {
        return itemRepository.findAll().stream()
                .map(itemMapper::toItemResponse)
                .toList();
    }

    @Override
    @Transactional
    public ItemResponse createItem(ItemRequest itemRequest) throws JsonProcessingException {
        Item item = itemMapper.toItem(itemRequest);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        itemRepository.save(item);
        itemCacheService.cacheItem(item);
        sendKafkaEventSafely(()-> {
            try {
                itemProducer.sendEvent(itemEventMapper.toCreatedEvent(item));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        return itemMapper.toItemResponse(item);
    }

    @Override
    @Transactional
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
        sendKafkaEventSafely(()-> {
            try {
                itemProducer.sendEvent(itemEventMapper.toUpdatedEvent(item));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        return itemMapper.toItemResponse(item);
    }

    @Override
    @Transactional
    public void deleteById(Long id)  {
        Item item = itemRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        sendKafkaEventSafely(()-> {
            try {
                itemProducer.sendEvent(itemEventMapper.toDeletedEvent(item));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        itemCacheService.evictItem(id);
        itemRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemResponse> searchItem(ItemSearchParams itemSearchParams, Pageable pageable) {
        Specification<Item> spec = Specification
                .<Item>where(SpecificationUtils.iLike("name", itemSearchParams.getName()))
                .and(SpecificationUtils.iLike("category", itemSearchParams.getCategory()))
                .and(SpecificationUtils.between("price", itemSearchParams.getMinPrice(), itemSearchParams.getMaxPrice()))
                .and(SpecificationUtils.equal("available", itemSearchParams.getAvailable()));

        return itemRepository.findAll(spec, pageable).map(itemMapper::toItemResponse);
    }

    @CircuitBreaker(name = "kafkaProducer")
    @Retry(name = "kafkaProducer")
    @RateLimiter(name = "itemService")
    private void sendKafkaEventSafely(Runnable runnable) {
        runnable.run();
    }
}