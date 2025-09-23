package com.example.inventory_service.services;

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

    public Item getItem(Long id) throws JsonProcessingException {
        String json = redisTemplate.opsForValue().get("item:" + id);
        if (json == null) {
            return null;
        }
        return objectMapper.readValue(json, Item.class);
    }

    public void cacheItem(Item item) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(item);
        redisTemplate.opsForValue().set("item:" + item.getId(), json, Duration.ofMinutes(10));
    }

    public void evictItem(Long id) {
        redisTemplate.delete("item:" + id);
    }
}

