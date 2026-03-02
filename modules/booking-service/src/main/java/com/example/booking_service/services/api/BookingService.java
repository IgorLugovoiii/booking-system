package com.example.booking_service.services.api;

import com.example.booking_service.dtos.BookingRequest;
import com.example.booking_service.dtos.BookingResponse;

import com.example.booking_service.dtos.BookingSearchParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest bookingRequest);

    void cancelBooking(Long bookingId);

    List<BookingResponse> getBookingByUserId(Long userId);

    BookingResponse updateBooking(Long bookingId, BookingRequest bookingRequest);

    BookingResponse confirmBooking(Long bookingId);

    Page<BookingResponse> searchBookings(BookingSearchParams bookingSearchParams, Pageable pageable);
}
