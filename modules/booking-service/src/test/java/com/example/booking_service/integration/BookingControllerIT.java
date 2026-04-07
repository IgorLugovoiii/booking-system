package com.example.booking_service.integration;

import com.example.booking_service.dtos.BookingRequest;
import com.example.booking_service.dtos.BookingUpdateRequest;
import com.example.booking_service.models.Booking;
import com.example.booking_service.models.enums.BookingStatus;
import com.example.booking_service.repositories.BookingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class BookingControllerIT {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    public void setup() {
        bookingRepository.deleteAll();

        Booking booking1 = Booking.builder()
                .id(1L)
                .userId(1L)
                .bookingStatus(BookingStatus.PENDING)
                .bookingDate(LocalDateTime.now().plusDays(1))
                .build();

        Booking booking2 = Booking.builder()
                .userId(2L)
                .id(2L)
                .bookingStatus(BookingStatus.CONFIRMED)
                .bookingDate(LocalDateTime.now().plusDays(2))
                .build();

        bookingRepository.saveAll(List.of(booking1, booking2));
    }

    @Test
    public void shouldReturnBookingByUser() throws Exception {
        Long userId = bookingRepository.findAll().getFirst().getUserId();

        mockMvc.perform(get("/api/bookings/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].status").value(BookingStatus.PENDING.name()));
    }


    @Test
    public void shouldCreateBooking() throws Exception {
        BookingRequest bookingRequest = BookingRequest.builder()
                .userId(3L)
                .itemId(3L)
                .bookingDate(LocalDateTime.now().plusDays(3))
                .build();

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemId").value(3L))
                .andExpect(jsonPath("$.userId").value(3L));
    }

    @Test
    public void shouldUpdateBooking() throws Exception {
        Booking booking = bookingRepository.findAll().getFirst();

        BookingUpdateRequest request = BookingUpdateRequest.builder()
                .userId(booking.getUserId())
                .itemId(booking.getItemId())
                .bookingDate(LocalDateTime.now().plusDays(5))
                .build();

        mockMvc.perform(patch("/api/bookings/" + booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(booking.getUserId()))
                .andExpect(jsonPath("$.itemId").value(booking.getItemId()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    public void shouldCancelBooking() throws Exception {
        Booking booking = bookingRepository.findAll().getFirst();

        BookingUpdateRequest request = BookingUpdateRequest.builder()
                .bookingStatus(BookingStatus.CANCELLED)
                .build();

        mockMvc.perform(patch("/api/bookings/" + booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/bookings/" + booking.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CANCELLED"));
    }

    @Test
    public void shouldConfirmBooking() throws Exception {
        Booking booking = bookingRepository.findAll().getFirst();

        BookingUpdateRequest request = BookingUpdateRequest.builder()
                .bookingStatus(BookingStatus.CONFIRMED)
                .build();

        mockMvc.perform(patch("/api/bookings/" + booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    public void shouldReturn404WhenConfirmingNonexistentBooking() throws Exception {
        BookingUpdateRequest request = BookingUpdateRequest.builder()
                .bookingStatus(BookingStatus.CONFIRMED)
                .build();

        mockMvc.perform(patch("/api/bookings/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
