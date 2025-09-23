package com.example.booking_service.services;

import com.example.booking_service.dtos.BookingRequest;
import com.example.booking_service.dtos.BookingResponse;
import com.example.booking_service.kafka.BookingEvent;
import com.example.booking_service.kafka.BookingProducer;
import com.example.booking_service.mapper.BookingEventMapper;
import com.example.booking_service.mapper.BookingMapper;
import com.example.booking_service.models.Booking;
import com.example.booking_service.models.enums.BookingStatus;
import com.example.booking_service.repositories.BookingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
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
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private BookingEventMapper bookingEventMapper;

    @InjectMocks
    private BookingService bookingService;

    private Booking booking;
    private BookingRequest bookingRequest;
    private BookingResponse bookingResponse;
    private BookingEvent bookingEvent;

    @BeforeEach
    void setUp() {
        bookingRequest = BookingRequest.builder()
                .userId(1L)
                .itemId(1L)
                .bookingDate(LocalDateTime.now())
                .build();

        booking = Booking.builder()
                .id(1L)
                .userId(1L)
                .itemId(1L)
                .bookingDate(bookingRequest.getBookingDate())
                .bookingStatus(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();


        bookingResponse = BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .itemId(booking.getItemId())
                .bookingDate(booking.getBookingDate())
                .status(booking.getBookingStatus())
                .build();

        // lenient для уникання UnnecessaryStubbingException
        lenient().when(bookingMapper.toBooking(any(BookingRequest.class))).thenReturn(booking);
        lenient().when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(bookingResponse);

        bookingEvent = BookingEvent.builder()
                .eventType("booking.created")
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .itemId(booking.getItemId())
                .bookingTime(booking.getBookingDate())
                .build();

        //lenient() не вимагає, щоб кожен мок був використаний, а лише вимагає ті, що треба при виконані конкретного тесту
        lenient().when(bookingEventMapper.toCreatedEvent(any(Booking.class))).thenReturn(bookingEvent);
        lenient().when(bookingEventMapper.toUpdatedEvent(any(Booking.class))).thenReturn(bookingEvent);
        lenient().when(bookingEventMapper.toCanceledEvent(any(Booking.class))).thenReturn(bookingEvent);
        lenient().when(bookingEventMapper.toConfirmedEvent(any(Booking.class))).thenReturn(bookingEvent);
    }


    @Test
    void createBooking_ShouldSaveAndReturnResponse() throws JsonProcessingException {
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingResponse response = bookingService.createBooking(bookingRequest);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        verify(bookingProducer).sendEvent(any());
    }

    @Test
    void cancelBooking_ShouldSetStatusToCancelled() throws JsonProcessingException {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.cancelBooking(1L);

        assertEquals(BookingStatus.CANCELLED, booking.getBookingStatus());
        verify(bookingProducer).sendEvent(any());
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
    void updateBooking_ShouldUpdateAndReturnResponse() throws JsonProcessingException {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingResponse response = bookingService.updateBooking(1L, bookingRequest);

        assertEquals(BookingStatus.PENDING, response.getStatus());
        verify(bookingProducer).sendEvent(any());
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
    void confirmBooking_shouldSetStatusToConfirmed() throws JsonProcessingException {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        //thenReturn завжди повертає одне й те саме значення.
        //thenAnswer дозволяє написати логіку, яка виконується при кожному виклику.
        when(bookingMapper.toBookingResponse(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            BookingResponse r = new BookingResponse();
            r.setId(b.getId());
            r.setUserId(b.getUserId());
            r.setItemId(b.getItemId());
            r.setBookingDate(b.getBookingDate());
            r.setStatus(b.getBookingStatus());
            return r;
        });

        BookingResponse response = bookingService.confirmBooking(1L);

        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
        verify(bookingProducer).sendEvent(any());
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
