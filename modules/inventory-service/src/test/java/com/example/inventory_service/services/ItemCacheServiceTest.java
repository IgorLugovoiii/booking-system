package com.example.inventory_service.services;

import com.example.inventory_service.models.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ItemCacheServiceTest {
    @Mock
    private RedisTemplate<String, Item> redisTemplate;
    @Mock
    private ValueOperations<String, Item> valueOperations;
    @InjectMocks
    private ItemCacheService itemCacheService;

    private Item item;

    @BeforeEach
    public void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        itemCacheService = new ItemCacheService(redisTemplate, valueOperations);

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
    }

    @Test
    void testGetItem_shouldReturnItemFromCache() {
        when(valueOperations.get("item:1")).thenReturn(item);

        Item result = itemCacheService.getItem(1L);

        assertNotNull(result);
        assertEquals("Test Item", result.getName());
        verify(valueOperations, times(1)).get("item:1");
    }

    @Test
    void testCacheItem_shouldStoreItem() {
        itemCacheService.cacheItem(item);

        verify(valueOperations, times(1))
                .set(eq("item:1"), eq(item), eq(Duration.ofMinutes(10)));
    }

    @Test
    void testEvictItem_shouldDeleteItemFromCache() {
        itemCacheService.evictItem(1L);

        verify(redisTemplate, times(1)).delete("item:1");
    }
}
