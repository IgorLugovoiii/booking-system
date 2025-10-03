package com.example.booking_service.services.api;

import com.example.booking_service.dtos.BookingRequest;
import com.example.booking_service.dtos.BookingResponse;

import com.example.booking_service.dtos.BookingSearchParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest bookingRequest) throws JsonProcessingException;

    void cancelBooking(Long bookingId) throws JsonProcessingException;

    List<BookingResponse> getBookingByUserId(Long userId);

    BookingResponse updateBooking(Long bookingId, BookingRequest bookingRequest) throws JsonProcessingException;

    BookingResponse confirmBooking(Long bookingId) throws JsonProcessingException;

    Page<BookingResponse> searchBookings(BookingSearchParams bookingSearchParams, Pageable pageable);
}
