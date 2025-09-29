package com.example.inventory_service.services;

import com.example.inventory_service.dtos.ItemRequest;
import com.example.inventory_service.dtos.ItemResponse;
import com.example.inventory_service.kafka.ItemEvent;
import com.example.inventory_service.kafka.ItemProducer;
import com.example.inventory_service.mappers.ItemEventMapper;
import com.example.inventory_service.mappers.ItemMapper;
import com.example.inventory_service.models.Item;
import com.example.inventory_service.repositories.ItemRepository;
import com.example.inventory_service.services.impl.ItemCacheService;
import com.example.inventory_service.services.impl.ItemServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {
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
    private ItemServiceImpl itemServiceImpl;

    private Item item;
    private ItemRequest itemRequest;
    private ItemResponse itemResponse;
    private ItemEvent itemEvent;

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

        itemResponse = ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .category(item.getCategory())
                .price(item.getPrice())
                .available(item.isAvailable())
                .build();

        itemEvent = ItemEvent.builder()
                .eventType("item.created")
                .itemId(item.getId())
                .name(item.getName())
                .category(item.getCategory())
                .available(item.isAvailable())
                .price(item.getPrice())
                .build();
    }

    @Test
    void givenValidItem_whenCreateItem_thenSaveItemAndItemEvent() throws JsonProcessingException {
        when(itemMapper.toItem(itemRequest)).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        doNothing().when(itemCacheService).cacheItem(item);
        when(itemEventMapper.toCreatedEvent(item)).thenReturn(itemEvent);
        when(itemMapper.toItemResponse(item)).thenReturn(itemResponse);

        ItemResponse response = itemServiceImpl.createItem(itemRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(item.getName());
        assertThat(response.getCategory()).isEqualTo(item.getCategory());
        assertThat(response.getPrice()).isEqualTo(item.getPrice());
        assertThat(response.getAvailable()).isEqualTo(item.isAvailable());

        verify(itemRepository).save(any(Item.class));
        verify(itemProducer).sendEvent(any(ItemEvent.class));
    }

    @Test
    void givenExistingItem_whenUpdateItem_thenFieldsAreUpdatedAndEventSent() throws JsonProcessingException {
        ItemRequest updatedRequest = ItemRequest.builder()
                .name("Updated Item")
                .description("Updated description")
                .category("Updated Category")
                .price(200.0)
                .available(false)
                .build();

        Item updatedItem = Item.builder()
                .id(item.getId())
                .name(updatedRequest.getName())
                .description(updatedRequest.getDescription())
                .category(updatedRequest.getCategory())
                .price(updatedRequest.getPrice())
                .available(updatedRequest.getAvailable())
                .createdAt(item.getCreatedAt())
                .updatedAt(LocalDateTime.now().plusDays(1))
                .build();

        ItemResponse updatedResponse = ItemResponse.builder()
                .id(updatedItem.getId())
                .name(updatedItem.getName())
                .description(updatedItem.getDescription())
                .category(updatedItem.getCategory())
                .price(updatedItem.getPrice())
                .available(updatedItem.isAvailable())
                .build();

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);
        when(itemEventMapper.toUpdatedEvent(any(Item.class))).thenReturn(itemEvent);
        when(itemMapper.toItemResponse(any(Item.class))).thenReturn(updatedResponse);

        ItemResponse response = itemServiceImpl.updateItem(item.getId(), updatedRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(updatedRequest.getName());
        assertThat(response.getDescription()).isEqualTo(updatedRequest.getDescription());
        assertThat(response.getCategory()).isEqualTo(updatedRequest.getCategory());
        assertThat(response.getPrice()).isEqualTo(updatedRequest.getPrice());
        assertThat(response.getAvailable()).isEqualTo(updatedRequest.getAvailable());

        verify(itemRepository).save(any(Item.class));
        verify(itemProducer).sendEvent(itemEvent);
    }

    @Test
    void givenItemNotFound_whenUpdateItem_thenThrowEntityNotFoundException() {
        Long nonExistentId = 999L;
        ItemRequest request = ItemRequest.builder()
                .name("Updated")
                .description("Updated desc")
                .category("Updated category")
                .price(200.0)
                .available(true)
                .build();

        when(itemRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemServiceImpl.updateItem(nonExistentId, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(itemRepository).findById(nonExistentId);
        verifyNoMoreInteractions(itemRepository,itemCacheService,itemMapper,itemEventMapper);

    }

    @Test
    void givenExistingItem_whenDeleteById_thenEventSentCacheEvictedAndDeletedFromRepository() throws JsonProcessingException {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.ofNullable(item));
        doNothing().when(itemRepository).deleteById(item.getId());
        when(itemEventMapper.toDeletedEvent(item)).thenReturn(itemEvent);

        itemServiceImpl.deleteById(item.getId());

        verify(itemRepository).deleteById(item.getId());
        verify(itemProducer).sendEvent(any());
        verify(itemCacheService).evictItem(item.getId());
    }

    @Test
    void givenItemNotFound_whenDeleteById_thenThrowEntityNotFoundException() throws JsonProcessingException {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(()-> itemServiceImpl.deleteById(item.getId()))
                .isInstanceOf(EntityNotFoundException.class);

        verify(itemRepository, times(1)).findById(item.getId());
        verifyNoMoreInteractions(itemRepository,itemCacheService);
    }

    @Test
    void givenItemNotInCache_whenFindById_thenFetchFromRepositoryAndCacheIt() throws JsonProcessingException {
        when(itemCacheService.getItem(item.getId())).thenReturn(null);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        doNothing().when(itemCacheService).cacheItem(item);
        when(itemMapper.toItemResponse(item)).thenReturn(itemResponse);

        ItemResponse response = itemServiceImpl.findById(item.getId());

        assertThat(response).isEqualTo(itemResponse);
        verify(itemCacheService).getItem(item.getId());
        verify(itemRepository).findById(item.getId());
        verify(itemCacheService).cacheItem(item);
    }

    @Test
    void givenItemInCache_whenFindById_thenReturnFromCache() throws JsonProcessingException {
        when(itemCacheService.getItem(item.getId())).thenReturn(item);
        when(itemMapper.toItemResponse(item)).thenReturn(itemResponse);

        ItemResponse response = itemServiceImpl.findById(item.getId());

        assertThat(response).isEqualTo(itemResponse);
        verify(itemCacheService).getItem(item.getId());
        verify(itemRepository, never()).findById(any());
    }

    @Test
    void givenItemNotFound_whenFindById_thenThrowEntityNotFoundException() throws JsonProcessingException {
        when(itemCacheService.getItem(item.getId())).thenReturn(null);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(()-> itemServiceImpl.findById(item.getId()))
                .isInstanceOf(EntityNotFoundException.class);

        verify(itemCacheService).getItem(item.getId());
        verify(itemRepository).findById(item.getId());
        verify(itemCacheService, never()).cacheItem(any());
    }

    @Test
    void whenFindAll_thenReturnMappedResponses() {
        when(itemRepository.findAll()).thenReturn(List.of(item));
        when(itemMapper.toItemResponse(item)).thenReturn(itemResponse);

        List<ItemResponse> response = itemServiceImpl.findAll();

        assertThat(response).hasSize(1);
        assertThat(response.getFirst()).isEqualTo(itemResponse);
        verify(itemRepository).findAll();
        verify(itemMapper).toItemResponse(item);
    }

    @Test
    void givenEmptyRepository_whenFindAll_thenReturnEmptyList() {
        when(itemRepository.findAll()).thenReturn(List.of());

        List<ItemResponse> response = itemServiceImpl.findAll();

        assertThat(response).isEmpty();
        verify(itemRepository).findAll();
        verify(itemMapper, never()).toItemResponse(item);
    }
}
