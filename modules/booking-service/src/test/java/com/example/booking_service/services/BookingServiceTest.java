package com.example.booking_service.services;

import com.example.booking_service.dtos.BookingRequest;
import com.example.booking_service.dtos.BookingResponse;
import com.example.booking_service.kafka.BookingProducer;
import com.example.booking_service.models.Booking;
import com.example.booking_service.models.enums.BookingStatus;
import com.example.booking_service.repositories.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.zookeeper.Op;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingProducer bookingProducer;
    @InjectMocks
    private BookingService bookingService;

    private Booking booking;
    private BookingRequest bookingRequest;

    @BeforeEach
    void setUp() {
        bookingRequest = new BookingRequest();
        bookingRequest.setUserId(1L);
        bookingRequest.setItemId(1L);
        bookingRequest.setBookingDate(LocalDateTime.now());

        booking = new Booking();

        booking.setId(1L);
        booking.setUserId(1L);
        booking.setItemId(1L);
        booking.setBookingDate(bookingRequest.getBookingDate());
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createBooking_ShouldSaveAndReturnResponse() {
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingResponse response = bookingService.createBooking(bookingRequest);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        verify(bookingProducer).sendBookingCreatedEvent(any());
    }

    @Test
    void cancelBooking_ShouldSetStatusToCancelled() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.cancelBooking(1L);

        assertEquals(BookingStatus.CANCELLED, booking.getBookingStatus());
        verify(bookingProducer).sendBookingCanceledEvent(any());
    }

    @Test
    void cancelBooking_AlreadyCancelled_ShouldReturnException() {
        booking.setBookingStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Exception ex = assertThrows(IllegalStateException.class, () -> bookingService.cancelBooking(1L));
        assertEquals("Booking is already cancelled", ex.getMessage());
    }

    @Test
    void cancelBooking_NotFound_ShouldThrowException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> bookingService.cancelBooking(1L));
    }

    @Test
    void getBookingByUserId_ShouldReturnList() {
        when(bookingRepository.findByUserId(1L)).thenReturn(List.of(booking));

        List<BookingResponse> list = bookingService.getBookingByUserId(1L);

        assertEquals(1, list.size());
        assertEquals(1L, list.getFirst().getUserId());
    }

    @Test
    void getBookingByUserId_Empty_ShouldReturnEmptyList() {
        when(bookingRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        List<BookingResponse> list = bookingService.getBookingByUserId(1L);

        assertTrue(list.isEmpty());
    }

    @Test
    void updateBooking_ShouldUpdateAndReturnResponse() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingResponse response = bookingService.updateBooking(1L, bookingRequest);

        assertEquals(BookingStatus.PENDING, response.getStatus());
        verify(bookingProducer).sendBookingUpdatedEvent(any());
    }

    @Test
    void updateBooking_Cancelled_ShouldThrowException() {
        booking.setBookingStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class, () -> bookingService.updateBooking(1L, bookingRequest));
    }

    @Test
    void updateBooking_NotFound_ShouldThrowException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookingService.updateBooking(1L, bookingRequest));
    }

    @Test
    void confirmBooking_shouldSetStatusToConfirmed() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingResponse response = bookingService.confirmBooking(1L);

        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
        verify(bookingProducer).sendBookingConfirmedEvent(any());
    }

    @Test
    void confirmBooking_ConfirmedOrCancelled_ShouldThrowException() {
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class, () -> bookingService.confirmBooking(1L));

        booking.setBookingStatus(BookingStatus.CANCELLED);

        assertThrows(IllegalStateException.class, () -> bookingService.confirmBooking(1L));
    }

    @Test
    void confirmBooking_NotFound_shouldThrowException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookingService.confirmBooking(1L));
    }
}
