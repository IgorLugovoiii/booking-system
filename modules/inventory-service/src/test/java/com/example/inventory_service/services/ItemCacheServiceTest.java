package com.example.inventory_service.services;

import com.example.inventory_service.models.Item;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock(lenient = true) // Додаємо lenient, щоб уникнути UnnecessaryStubbing
    private ObjectMapper objectMapper;

    private ItemCacheService itemCacheService;
    private Item item;
    private String itemJson;

    @BeforeEach
    public void setUp() {
        itemCacheService = new ItemCacheService(redisTemplate, objectMapper);

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");

        itemJson = "{\"id\":1,\"name\":\"Test Item\"}";
    }

    @Test
    void testGetItem_shouldReturnItemFromCache() throws JsonProcessingException {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("item:1")).thenReturn(itemJson);
        when(objectMapper.readValue(itemJson, Item.class)).thenReturn(item);

        Item result = itemCacheService.getItem(1L);

        assertNotNull(result);
        assertEquals("Test Item", result.getName());
        verify(valueOperations, times(1)).get("item:1");
        verify(objectMapper, times(1)).readValue(itemJson, Item.class);
    }

    @Test
    void testCacheItem_shouldStoreItem() throws JsonProcessingException {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(item)).thenReturn(itemJson);

        itemCacheService.cacheItem(item);

        verify(objectMapper, times(1)).writeValueAsString(item);
        verify(valueOperations, times(1))
                .set(eq("item:1"), eq(itemJson), eq(Duration.ofMinutes(10)));
    }

    @Test
    void testEvictItem_shouldDeleteItemFromCache() {
        itemCacheService.evictItem(1L);

        verify(redisTemplate, times(1)).delete("item:1");
    }
}
