package com.example.inventory_service.services;

import com.example.inventory_service.dtos.ItemRequest;
import com.example.inventory_service.dtos.ItemResponse;
import com.example.inventory_service.kafka.ItemEvent;
import com.example.inventory_service.kafka.ItemProducer;
import com.example.inventory_service.mappers.ItemEventMapper;
import com.example.inventory_service.mappers.ItemMapper;
import com.example.inventory_service.models.Item;
import com.example.inventory_service.repositories.ItemRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemProducer itemProducer;
    @Mock
    private ItemCacheService itemCacheService;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private ItemEventMapper itemEventMapper;
    @InjectMocks
    private ItemService itemService;

    private Item item;
    private ItemRequest itemRequest;

    @BeforeEach
    public void setUp() {
        item = Item.builder()
                .id(1L)
                .name("Test Item")
                .description("Description of test item")
                .category("Category 1")
                .price(100.0)
                .available(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        itemRequest = ItemRequest.builder()
                .name("Test Item")
                .description("Description of test item")
                .category("Category 1")
                .price(100.0)
                .available(true)
                .build();

        ItemResponse itemResponse = ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .category(item.getCategory())
                .price(item.getPrice())
                .available(item.isAvailable())
                .build();
        // lenient для уникання UnnecessaryStubbingException
        lenient().when(itemMapper.toItem(any(ItemRequest.class))).thenReturn(item);
        lenient().when(itemMapper.toItemResponse(any(Item.class))).thenReturn(itemResponse);

        ItemEvent itemEvent = ItemEvent.builder()
                .eventType("item.created")
                .itemId(item.getId())
                .name(item.getName())
                .category(item.getCategory())
                .available(item.isAvailable())
                .price(item.getPrice())
                .build();

        //lenient() не вимагає, щоб кожен мок був використаний, а лише вимагає ті, що треба при виконані конкретного тесту
        lenient().when(itemEventMapper.toCreatedEvent(any(Item.class))).thenReturn(itemEvent);
        lenient().when(itemEventMapper.toUpdatedEvent(any(Item.class))).thenReturn(itemEvent);
        lenient().when(itemEventMapper.toDeletedEvent(any(Item.class))).thenReturn(itemEvent);
    }

    @Test
    void testCreateItem_ShouldSaveAndReturnResponse() throws JsonProcessingException {
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponse response = itemService.createItem(itemRequest);

        assertNotNull(response);
        assertEquals(item.getName(), response.getName());
        verify(itemRepository, times(1)).save(any(Item.class));
        verify(itemProducer, times(1)).sendEvent(any());
        verify(itemCacheService, times(1)).cacheItem(any());
    }

    @Test
    void testUpdateItem_ShouldUpdateAndSave() throws JsonProcessingException {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        ItemResponse response = itemService.updateItem(item.getId(), itemRequest);

        assertNotNull(response);
        assertEquals(item.getName(), response.getName());
        verify(itemRepository, times(1)).save(any());
        verify(itemProducer, times(1)).sendEvent(any());
        verify(itemCacheService, times(1)).cacheItem(any());
    }

    @Test
    void testUpdateItem_ShouldReturnException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.updateItem(1L, itemRequest));
    }

    @Test
    void testUpdateItem_WhenNotFound_ShouldNotCallProducer() throws JsonProcessingException {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.updateItem(1L, itemRequest));

        verify(itemProducer, never()).sendEvent(any());
    }

    @Test
    void testDeleteItem_WhenFound() throws JsonProcessingException {
        doNothing().when(itemCacheService).evictItem(1L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        doNothing().when(itemRepository).deleteById(1L);

        itemService.deleteById(1L);

        verify(itemRepository, times(1)).deleteById(1L);
        verify(itemProducer, times(1)).sendEvent(any());
        verify(itemCacheService).evictItem(1L);
    }

    @Test
    void testDeleteItem_WhenNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.deleteById(1L));
    }

    @Test
    void testFindById_WhenFound() throws JsonProcessingException {
        when(itemCacheService.getItem(1L)).thenReturn(null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemResponse response = itemService.findById(1L);

        assertNotNull(response);
        assertEquals(item.getName(), response.getName());
        verify(itemRepository, times(1)).findById(1L);
        verify(itemCacheService).getItem(1L);
    }

    @Test
    void testFindById_WhenNotFound() {
        when(itemRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.findById(1L));
    }

    @Test
    void testFindAll_WhenFound() {
        when(itemRepository.findAll()).thenReturn(List.of(item));

        List<ItemResponse> response = itemService.findAll();

        assertNotNull(response);
        assertEquals(item.getName(), response.getFirst().getName());
        verify(itemRepository, times(1)).findAll();
    }
}
