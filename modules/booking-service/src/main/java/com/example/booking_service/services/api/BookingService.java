package com.example.booking_service.services.api;

import com.example.booking_service.dtos.BookingRequest;
import com.example.booking_service.dtos.BookingResponse;

import com.example.booking_service.dtos.BookingSearchParams;
import com.example.booking_service.dtos.BookingUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest bookingRequest);

    BookingResponse getBookingById(Long userId);

    BookingResponse updateBooking(Long bookingId, BookingUpdateRequest request);

    Page<BookingResponse> searchBookings(BookingSearchParams bookingSearchParams, Pageable pageable);
}
