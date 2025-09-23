package com.example.inventory_service.integration;

import com.example.inventory_service.models.Item;
import com.example.inventory_service.repositories.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class ItemControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    private static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();

        Item item1 = Item.builder()
                .name("Book")
                .category("Books")
                .price(25.0)
                .build();

        Item item2 = Item.builder()
                .name("Book2")
                .category("Books")
                .price(1000.0)
                .build();

        itemRepository.save(item1);
        itemRepository.save(item2);
    }

    @Test
    void shouldReturnAllItems() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) //$ — це корінь JSON-об'єкта, тобто уся відповідь.
                .andExpect(jsonPath("$[0].name", not(empty())))
                .andExpect(jsonPath("$[1].price", greaterThan(0.0)));
    }

    @Test
    void shouldReturnItemById() throws Exception {
        Long id = itemRepository.findAll().getFirst().getId();

        mockMvc.perform(get("/api/items/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", not(empty())))
                .andExpect(jsonPath("$.price", greaterThan(0.0)));
    }

    @Test
    void shouldReturn404ForMissingItem() throws Exception {
        mockMvc.perform(get("/api/items/" + 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateItem() throws Exception {
        Item item = Item.builder()
                .id(3L)
                .name("Item3")
                .category("Books")
                .price(10.00)
                .build();

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Item3"))
                .andExpect(jsonPath("$.price", greaterThan(0.0)));
    }

    @Test
    void shouldUpdateItem() throws Exception {
        Item item = itemRepository.findAll().getFirst();
        item.setName("Updated item");
        item.setPrice(20.00);

        mockMvc.perform(put("/api/items/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated item"))
                .andExpect(jsonPath("$.price").value(20.00));
    }

    @Test
    void shouldReturn404WhenUpdatingMissingItem() throws Exception {
        Item item = itemRepository.findAll().getFirst();
        item.setName("Updated item");
        item.setPrice(20.00);

        mockMvc.perform(put("/api/items/" + 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteItem() throws Exception {
        Long id = itemRepository.findAll().getFirst().getId();
        mockMvc.perform(delete("/api/items/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingMissingItem() throws Exception {
        mockMvc.perform(delete("/api/items/4"))
                .andExpect(status().isNotFound());
    }
}
