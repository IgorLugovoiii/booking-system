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
    private final ValueOperations<String, Item> valueOperations;

    @Autowired
    public ItemCacheService(RedisTemplate<String, Item> redisTemplate, ValueOperations<String, Item> valueOperations) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
    }

    public Item getItem(Long id) {
        return valueOperations.get("item:" + id); // item: - ключ, важливо його писати правильно, без зайвих пробілів, id - це json об'єкт з певним id
    }

    public void cacheItem(Item item) {
        valueOperations.set("item:" + item.getId(), item, Duration.ofMinutes(10));
    }

    public void evictItem(Long id) {
        redisTemplate.delete("item:" + id);
    }
}
