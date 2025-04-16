package com.example.inventory_service.services;

import com.example.inventory_service.models.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ItemCacheService {
    private final RedisTemplate<String, Item> redisTemplate;

    @Autowired
    public ItemCacheService(RedisTemplate<String, Item> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Item getItem(Long id) {
        return redisTemplate.opsForValue().get("item:" + id);
    }

    public void cacheItem(Item item) {
        redisTemplate.opsForValue().set("item:" + item.getId(), item, Duration.ofMinutes(10));
    }

    public void evictItem(Long id) {
        redisTemplate.delete("item:" + id);
    }
}

