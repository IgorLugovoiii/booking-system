package com.example.booking_service.integration;

import com.example.booking_service.dtos.BookingRequest;
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

import static org.hamcrest.Matchers.*;
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

        Booking booking1 = new Booking();
        booking1.setUserId(1L);
        booking1.setItemId(1L);
        booking1.setBookingStatus(BookingStatus.PENDING);
        booking1.setBookingDate(LocalDateTime.now().plusDays(1));

        Booking booking2 = new Booking();
        booking2.setUserId(2L);
        booking2.setItemId(2L);
        booking2.setBookingStatus(BookingStatus.CONFIRMED);
        booking2.setBookingDate(LocalDateTime.now().plusDays(2));

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
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setUserId(3L);
        bookingRequest.setItemId(3L);
        bookingRequest.setBookingDate(LocalDateTime.now().plusDays(3));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemId").value(3L))
                .andExpect(jsonPath("$.userId").value(3L));
    }

    @Test
    public void shouldUpdateBooking() throws Exception {
        Booking existing = bookingRepository.findAll().getFirst();

        BookingRequest updateRequest = new BookingRequest();
        updateRequest.setUserId(existing.getUserId());
        updateRequest.setItemId(existing.getItemId());
        updateRequest.setBookingDate(LocalDateTime.now().plusDays(5));


        mockMvc.perform(put("/api/bookings/" + existing.getId() + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(existing.getUserId()))
                .andExpect(jsonPath("$.itemId").value(existing.getItemId()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    public void shouldCancelBooking() throws Exception {
        Booking booking = bookingRepository.findAll().getFirst();

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/cancel"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/bookings/" + booking.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CANCELLED"));
    }

    @Test
    public void shouldConfirmBooking() throws Exception {
        Booking booking = bookingRepository.findAll().getFirst();

        mockMvc.perform(put("/api/bookings/" + booking.getId() + "/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    public void shouldReturn404WhenConfirmingNonexistentBooking() throws Exception {
        mockMvc.perform(put("/api/bookings/99999/confirm"))
                .andExpect(status().isNotFound());
    }
}
