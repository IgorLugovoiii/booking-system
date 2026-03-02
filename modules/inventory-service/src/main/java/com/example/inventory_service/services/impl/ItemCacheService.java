package com.example.inventory_service.services.impl;

import com.example.inventory_service.models.Item;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ItemCacheService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public Item getItem(Long id) {
        try {
            String json = redisTemplate.opsForValue().get("item:" + id);
            return (json == null) ? null : objectMapper.readValue(json, Item.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void cacheItem(Item item){
        try {
            String json = objectMapper.writeValueAsString(item);
            redisTemplate.opsForValue().set("item:" + item.getId(), json, Duration.ofMinutes(10));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void evictItem(Long id) {
        redisTemplate.delete("item:" + id);
    }
}

