package com.example.inventory_service.services;

import com.example.inventory_service.models.Item;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ItemCacheService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public ItemCacheService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public Item getItem(Long id) {
        String json = redisTemplate.opsForValue().get("item:" + id);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Item.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize Item", e);
        }
    }

    public void cacheItem(Item item) {
        try {
            String json = objectMapper.writeValueAsString(item);
            redisTemplate.opsForValue().set("item:" + item.getId(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize Item", e);
        }
    }

    public void evictItem(Long id) {
        redisTemplate.delete("item:" + id);
    }
}

