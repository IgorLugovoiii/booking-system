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
import com.example.booking_service.services.impl.BookingServiceImpl;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingProducer bookingProducer;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private BookingEventMapper bookingEventMapper;

    @InjectMocks
    private BookingServiceImpl bookingServiceImpl;

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

        bookingEvent = BookingEvent.builder()
                .eventType("booking.created")
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .itemId(booking.getItemId())
                .bookingTime(booking.getBookingDate())
                .build();

    }

    @Test
    void givenValidBookingRequest_whenCreateBooking_thenBookingIsSavedAndEventSent() throws JsonProcessingException {
        when(bookingMapper.toBooking(bookingRequest)).thenReturn(booking);
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingEventMapper.toCreatedEvent(booking)).thenReturn(bookingEvent);
        when(bookingMapper.toBookingResponse(booking)).thenReturn(bookingResponse);

        BookingResponse response = bookingServiceImpl.createBooking(bookingRequest);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(bookingRequest.getUserId());
        assertThat(response.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(response.getBookingDate()).isNotNull();

        verify(bookingRepository).save(any(Booking.class));
        verify(bookingProducer).sendEvent(bookingEvent);
    }

    @Test
    void givenExistingBooking_whenUpdateBooking_thenFieldsAreUpdatedAndEventSent() throws JsonProcessingException {
        BookingRequest updatedRequest = BookingRequest.builder()
                .userId(2L)
                .itemId(3L)
                .bookingDate(LocalDateTime.now().plusDays(1))
                .build();

        Booking updatedBooking = Booking.builder()
                .id(booking.getId())
                .userId(updatedRequest.getUserId())
                .itemId(updatedRequest.getItemId())
                .bookingDate(updatedRequest.getBookingDate())
                .bookingStatus(BookingStatus.PENDING)
                .createdAt(booking.getCreatedAt())
                .updatedAt(LocalDateTime.now().plusDays(1))
                .build();

        BookingResponse updatedResponse = BookingResponse.builder()
                .userId(updatedBooking.getUserId())
                .itemId(updatedBooking.getItemId())
                .bookingDate(updatedBooking.getBookingDate())
                .build();

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(updatedBooking);
        when(bookingMapper.toBookingResponse(any())).thenReturn(updatedResponse);
        when(bookingEventMapper.toUpdatedEvent(any())).thenReturn(bookingEvent);

        BookingResponse response = bookingServiceImpl.updateBooking(booking.getId(), updatedRequest);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(updatedRequest.getUserId());
        assertThat(response.getItemId()).isEqualTo(updatedRequest.getItemId());
        assertThat(response.getBookingDate()).isEqualTo(updatedRequest.getBookingDate());

        verify(bookingRepository).save(any(Booking.class));
        verify(bookingProducer).sendEvent(bookingEvent);
    }

    @Test
    void cancelBooking_ShouldSetStatusToCancelled() throws JsonProcessingException {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingEventMapper.toCanceledEvent(booking)).thenReturn(bookingEvent);

        bookingServiceImpl.cancelBooking(booking.getId());

        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);

        verify(bookingRepository).save(booking);
        verify(bookingProducer).sendEvent(bookingEvent);
    }

    @Test
    void givenAlreadyCancelledBooking_whenCancelBooking_thenThrowsIllegalStateException() throws JsonProcessingException {
        booking.setBookingStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingServiceImpl.cancelBooking(booking.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Booking is already cancelled");

        verify(bookingRepository, never()).save(any());
        verify(bookingProducer, never()).sendEvent(any());
    }

    @Test
    void givenPendingBooking_whenConfirmBooking_thenStatusIsConfirmedAndEventSent() throws JsonProcessingException {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingEventMapper.toConfirmedEvent(booking)).thenReturn(bookingEvent);
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(bookingResponse);

        BookingResponse response = bookingServiceImpl.confirmBooking(booking.getId());
        response.setStatus(booking.getBookingStatus());

        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);

        verify(bookingRepository).save(booking);
        verify(bookingProducer).sendEvent(bookingEvent);
    }

    @Test
    void givenNonPendingBooking_whenConfirmBooking_thenThrowsIllegalStateException() {
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingServiceImpl.confirmBooking(booking.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only pending bookings can be confirmed");

        booking.setBookingStatus(BookingStatus.CANCELLED);

        assertThatThrownBy(() -> bookingServiceImpl.confirmBooking(booking.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only pending bookings can be confirmed");
    }

    @Test
    void givenNonExistentBooking_whenConfirmBooking_thenThrowsEntityNotFoundException() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(()-> bookingServiceImpl.confirmBooking(booking.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void givenUserIdWithBookings_whenGetBookingsByUserId_thenReturnsBookingResponses() {
        when(bookingRepository.findByUserId(booking.getUserId())).thenReturn(List.of(booking));
        when(bookingMapper.toBookingResponse(booking)).thenReturn(bookingResponse);

        List<BookingResponse> list = bookingServiceImpl.getBookingByUserId(booking.getUserId());

        assertThat(list).hasSize(1);
        assertThat(list.getFirst().getUserId()).isEqualTo(booking.getUserId());
        assertThat(list.getFirst().getStatus()).isEqualTo(BookingStatus.PENDING);

        verify(bookingRepository).findByUserId(booking.getUserId());
        verify(bookingMapper).toBookingResponse(booking);
    }

    @Test
    void givenUserIdWithoutBookings_whenGetBookingsByUserId_thenReturnsEmptyList() {
        when(bookingRepository.findByUserId(booking.getUserId())).thenReturn(List.of());

        List<BookingResponse> list = bookingServiceImpl.getBookingByUserId(booking.getUserId());

        assertThat(list).isEmpty();

        verify(bookingRepository).findByUserId(booking.getUserId());
        verify(bookingMapper, never()).toBookingResponse(any());
    }
}
