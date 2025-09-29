package com.example.inventory_service.services;

import com.example.inventory_service.models.Item;
import com.example.inventory_service.services.impl.ItemCacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ItemCacheServiceTest {
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks

    private ItemCacheService itemCacheService;
    private Item item;
    private String itemJson;

    @BeforeEach
    public void setUp() {
        itemCacheService = new ItemCacheService(redisTemplate, objectMapper);

        item = Item.builder()
                .id(1L)
                .name("TestItem")
                .build();

        itemJson = "{\"id\":1,\"name\":\"Test Item\"}";
    }

    @Test
    void givenItemInCache_whenGetItem_thenReturnItem() throws JsonProcessingException {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("item:1")).thenReturn(itemJson);
        when(objectMapper.readValue(itemJson, Item.class)).thenReturn(item);

        Item result = itemCacheService.getItem(1L);

        assertThat(result).isNotNull()
                .extracting(Item::getName)
                .isEqualTo("TestItem");
        verify(valueOperations).get("item:1");
        verify(objectMapper).readValue(itemJson, Item.class);
        verifyNoMoreInteractions(valueOperations, objectMapper, redisTemplate);
    }

    @Test
    void givenItem_whenCacheItem_thenStoredInRedis() throws JsonProcessingException {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(item)).thenReturn(itemJson);

        itemCacheService.cacheItem(item);

        verify(objectMapper).writeValueAsString(item);
        verify(valueOperations).set(eq("item:1"), eq(itemJson), eq(Duration.ofMinutes(10)));
        verifyNoMoreInteractions(valueOperations, objectMapper, redisTemplate);
    }

    @Test
    void givenItemId_whenEvictItem_thenDeleteFromRedis() {
        itemCacheService.evictItem(1L);

        verify(redisTemplate).delete("item:1");
        verifyNoMoreInteractions(valueOperations, objectMapper, redisTemplate);
    }
}
