package com.example.booking_service.services;

import com.example.booking_service.dtos.BookingRequest;
import com.example.booking_service.dtos.BookingResponse;
import com.example.booking_service.kafka.BookingProducer;
import com.example.booking_service.mapper.BookingEventMapper;
import com.example.booking_service.mapper.BookingMapper;
import com.example.booking_service.models.Booking;
import com.example.booking_service.models.enums.BookingStatus;
import com.example.booking_service.repositories.BookingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingProducer bookingProducer;
    private final BookingMapper bookingMapper;
    private final BookingEventMapper bookingEventMapper;

    @Autowired
    public BookingService(BookingRepository bookingRepository, BookingProducer bookingProducer,
                          BookingMapper bookingMapper, BookingEventMapper bookingEventMapper) {
        this.bookingRepository = bookingRepository;
        this.bookingProducer = bookingProducer;
        this.bookingMapper = bookingMapper;
        this.bookingEventMapper = bookingEventMapper;
    }

    @Transactional
    @CircuitBreaker(name = "bookingService")
    @Retry(name = "bookingService")
    @RateLimiter(name = "bookingService")
    public BookingResponse createBooking(BookingRequest bookingRequest) throws JsonProcessingException {
        Booking booking = bookingMapper.toBooking(bookingRequest);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);
        bookingProducer.sendEvent(bookingEventMapper.toCreatedEvent(savedBooking));
        return bookingMapper.toBookingResponse(savedBooking);
    }

    @Transactional
    @CircuitBreaker(name = "bookingService")
    @Retry(name = "bookingService")
    @RateLimiter(name = "bookingService")
    public void cancelBooking(Long bookingId) throws JsonProcessingException {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException::new);

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());

        bookingRepository.save(booking);

        bookingProducer.sendEvent(bookingEventMapper.toCanceledEvent(booking));
    }

    @Transactional
    @CircuitBreaker(name = "bookingService")
    @Retry(name = "bookingService")
    @RateLimiter(name = "bookingService")
    public List<BookingResponse> getBookingByUserId(Long userId) {
        return bookingRepository.findByUserId(userId).stream().map(bookingMapper::toBookingResponse).toList();
    }

    @Transactional
    @CircuitBreaker(name = "bookingService")
    @Retry(name = "bookingService")
    @RateLimiter(name = "bookingService")
    public BookingResponse updateBooking(Long bookingId, BookingRequest bookingRequest) throws JsonProcessingException {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException::new);

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update a cancelled booking");
        }

        booking.setItemId(bookingRequest.getItemId());
        booking.setUserId(bookingRequest.getUserId());
        booking.setBookingDate(bookingRequest.getBookingDate());
        booking.setUpdatedAt(LocalDateTime.now());

        bookingProducer.sendEvent(bookingEventMapper.toUpdatedEvent(booking));

        return bookingMapper.toBookingResponse(bookingRepository.save(booking));
    }

    @Transactional
    @CircuitBreaker(name = "bookingService")
    @Retry(name = "bookingService")
    @RateLimiter(name = "bookingService")
    public BookingResponse confirmBooking(Long bookingId) throws JsonProcessingException {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException::new);

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be confirmed");
        }
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(LocalDateTime.now());

        bookingProducer.sendEvent(bookingEventMapper.toConfirmedEvent(booking));

        return bookingMapper.toBookingResponse(bookingRepository.save(booking));
    }
}
