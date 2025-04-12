package com.example.inventory_service.services;

import com.example.inventory_service.dtos.ItemRequest;
import com.example.inventory_service.dtos.ItemResponse;
import com.example.inventory_service.kafka.ItemProducer;
import com.example.inventory_service.models.Item;
import com.example.inventory_service.repositories.ItemRepository;
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
    @InjectMocks
    private ItemService itemService;

    private Item item;
    private ItemRequest itemRequest;

    @BeforeEach
    public void setUp() {
        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Description of test item");
        item.setCategory("Category 1");
        item.setPrice(100.0);
        item.setAvailable(true);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        itemRequest = new ItemRequest();
        itemRequest.setName("Test Item");
        itemRequest.setDescription("Description of test item");
        itemRequest.setCategory("Category 1");
        itemRequest.setPrice(100.0);
        itemRequest.setAvailable(true);
    }

    @Test
    void testCreateItem_ShouldSaveAndReturnResponse() {
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponse response = itemService.createItem(itemRequest);

        assertNotNull(response);
        assertEquals(item.getName(), response.getName());
        verify(itemRepository, times(1)).save(any(Item.class));
        verify(itemProducer, times(1)).sendItemCreatedEvent(any());
    }

    @Test
    void testUpdateItem_ShouldUpdateAndSave() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        ItemResponse response = itemService.updateItem(item.getId(), itemRequest);

        assertNotNull(response);
        assertEquals(item.getName(), response.getName());
        verify(itemRepository, times(1)).save(any());
        verify(itemProducer, times(1)).sendItemUpdatedEvent(any());
    }

    @Test
    void testUpdateItem_ShouldReturnException(){
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, ()->itemService.updateItem(1L,itemRequest));
    }

    @Test
    void testUpdateItem_WhenNotFound_ShouldNotCallProducer() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.updateItem(1L, itemRequest));

        verify(itemProducer, never()).sendItemUpdatedEvent(any());
    }

    @Test
    void testDeleteItem_WhenFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        doNothing().when(itemRepository).deleteById(1L);

        itemService.deleteById(1L);

        verify(itemRepository, times(1)).deleteById(1L);
        verify(itemProducer, times(1)).sendItemDeletedEvent(any());
    }

    @Test
    void testDeleteItem_WhenNotFound(){
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,()->itemService.deleteById(1L));
    }

    @Test
    void testFindById_WhenFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemResponse response = itemService.findById(1L);

        assertNotNull(response);
        assertEquals(item.getName(), response.getName());
        verify(itemRepository, times(1)).findById(1L);
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
