package com.example.booking_service.services;

import com.example.booking_service.dtos.BookingRequest;
import com.example.booking_service.dtos.BookingResponse;
import com.example.booking_service.models.Booking;
import com.example.booking_service.models.enums.BookingStatus;
import com.example.booking_service.repositories.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    private BookingResponse convertToBookingResponse(Booking booking) {
        BookingResponse bookingResponse = new BookingResponse();
        bookingResponse.setId(booking.getId());
        bookingResponse.setItemId(booking.getItemId());
        bookingResponse.setUserId(booking.getUserId());
        bookingResponse.setBookingDate(booking.getBookingDate());
        bookingResponse.setStatus(booking.getBookingStatus());
        return bookingResponse;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest bookingRequest) {
        Booking booking = new Booking();
        booking.setUserId(bookingRequest.getUserId());
        booking.setItemId(bookingRequest.getItemId());
        booking.setBookingDate(bookingRequest.getBookingDate());
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);
        return convertToBookingResponse(savedBooking);
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException::new);

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking already cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());

        bookingRepository.save(booking);
    }

    @Transactional
    public List<BookingResponse> getBookingByUserId(Long userId) {
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(this::convertToBookingResponse)
                .toList();
    }

    @Transactional
    public BookingResponse updateBooking(Long bookingId, BookingRequest bookingRequest) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException::new);

        if(booking.getBookingStatus() == BookingStatus.CANCELLED){
            throw new IllegalStateException("Cannot update a cancelled booking");
        }

        booking.setItemId(bookingRequest.getItemId());
        booking.setUserId(bookingRequest.getUserId());
        booking.setBookingDate(bookingRequest.getBookingDate());
        booking.setUpdatedAt(LocalDateTime.now());

        return convertToBookingResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException::new);

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be confirmed");
        }
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(LocalDateTime.now());

        return convertToBookingResponse(bookingRepository.save(booking));
    }
}
