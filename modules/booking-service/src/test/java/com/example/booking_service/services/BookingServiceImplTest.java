package com.example.booking_service.services;

import com.example.booking_service.dtos.BookingRequest;
import com.example.booking_service.dtos.BookingResponse;
import com.example.booking_service.dtos.BookingUpdateRequest;
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
    void givenPendingBooking_whenUpdateBooking_thenFieldsAreUpdatedAndEventSent() throws JsonProcessingException {
        BookingUpdateRequest request = BookingUpdateRequest.builder()
                .userId(2L)
                .itemId(3L)
                .bookingDate(LocalDateTime.now().plusDays(1))
                .bookingStatus(BookingStatus.PENDING)
                .build();

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toBookingResponse(any())).thenReturn(bookingResponse);
        when(bookingEventMapper.toUpdatedEvent(any())).thenReturn(bookingEvent);

        BookingResponse response = bookingServiceImpl.updateBooking(booking.getId(), request);

        assertThat(response).isNotNull();
        assertThat(booking.getUserId()).isEqualTo(request.getUserId());
        assertThat(booking.getItemId()).isEqualTo(request.getItemId());

        verify(bookingRepository).save(any(Booking.class));
        verify(bookingProducer).sendEvent(bookingEvent);
    }

    @Test
    void givenBooking_whenUpdateStatusToCancelled_thenStatusUpdated() throws JsonProcessingException {
        BookingUpdateRequest request = BookingUpdateRequest.builder()
                .bookingStatus(BookingStatus.CANCELLED).build();

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingEventMapper.toCanceledEvent(any())).thenReturn(bookingEvent);

        bookingServiceImpl.updateBooking(booking.getId(), request);

        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);

        verify(bookingRepository).save(booking);
        verify(bookingProducer).sendEvent(bookingEvent);
    }

    @Test
    void givenAlreadyCancelledBooking_whenUpdateStatusToCancelled_thenThrowsException() throws JsonProcessingException {
        booking.setBookingStatus(BookingStatus.CANCELLED);

        BookingUpdateRequest request = BookingUpdateRequest.builder()
                .bookingStatus(BookingStatus.CANCELLED).build();

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingServiceImpl.updateBooking(booking.getId(), request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot modify cancelled booking");

        verify(bookingRepository, never()).save(any());
        verify(bookingProducer, never()).sendEvent(any());
    }

    @Test
    void givenPendingBooking_whenUpdateStatusToConfirmed_thenStatusUpdatedAndEventSent() throws JsonProcessingException {
        BookingUpdateRequest request = BookingUpdateRequest.builder().bookingStatus(BookingStatus.CONFIRMED).build();

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingEventMapper.toConfirmedEvent(booking)).thenReturn(bookingEvent);
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(bookingResponse);

        BookingResponse response = bookingServiceImpl.updateBooking(booking.getId(), request);
        response.setStatus(booking.getBookingStatus());

        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);

        verify(bookingRepository).save(booking);
        verify(bookingProducer).sendEvent(bookingEvent);
    }

    @Test
    void givenNonPendingBooking_whenUpdateStatusToConfirmed_thenThrowsException() {
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        BookingUpdateRequest request = BookingUpdateRequest.builder().bookingStatus(BookingStatus.CONFIRMED).build();

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingServiceImpl.updateBooking(booking.getId(), request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only pending bookings can be confirmed");
    }

    @Test
    void givenNonExistentBooking_whenUpdateBooking_thenThrowsEntityNotFoundException() {
        BookingUpdateRequest request = BookingUpdateRequest.builder().bookingStatus(BookingStatus.CONFIRMED).build();
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingServiceImpl.updateBooking(booking.getId(), request))
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
