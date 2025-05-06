package com.example.booking_service.services;

import com.example.booking_service.dtos.BookingRequest;
import com.example.booking_service.dtos.BookingResponse;
import com.example.booking_service.kafka.BookingEvent;
import com.example.booking_service.kafka.BookingProducer;
import com.example.booking_service.models.Booking;
import com.example.booking_service.models.enums.BookingStatus;
import com.example.booking_service.repositories.BookingRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Service
public class BookingService {
    private static final Logger logger = Logger.getLogger(BookingService.class.getName());
    private final BookingRepository bookingRepository;
    private final BookingProducer bookingProducer;

    @Autowired
    public BookingService(BookingRepository bookingRepository, BookingProducer bookingProducer) {
        this.bookingRepository = bookingRepository;
        this.bookingProducer = bookingProducer;
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
    @CircuitBreaker(name = "bookingService", fallbackMethod = "createBookingFallback")
    @Retry(name = "bookingService")
    @RateLimiter(name = "bookingService")
    public BookingResponse createBooking(BookingRequest bookingRequest) {
        Booking booking = new Booking();
        booking.setUserId(bookingRequest.getUserId());
        booking.setItemId(bookingRequest.getItemId());
        booking.setBookingDate(bookingRequest.getBookingDate());
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);
        bookingProducer.sendBookingCreatedEvent(new BookingEvent(
                "booking.created",
                booking.getId(),
                booking.getUserId(),
                booking.getItemId(),
                booking.getCreatedAt()
        ));
        return convertToBookingResponse(savedBooking);
    }

    @Transactional
    @CircuitBreaker(name = "bookingService", fallbackMethod = "cancelBookingFallback")
    @Retry(name = "bookingService")
    @RateLimiter(name = "bookingService")
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException::new);

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());

        bookingRepository.save(booking);

        bookingProducer.sendBookingCanceledEvent(new BookingEvent(
                "booking.canceled",
                booking.getId(),
                booking.getUserId(),
                booking.getItemId(),
                booking.getCreatedAt()
        ));
    }

    @Transactional
    @CircuitBreaker(name = "bookingService", fallbackMethod = "getBookingByUserIdBookingFallback")
    @Retry(name = "bookingService")
    @RateLimiter(name = "bookingService")
    public List<BookingResponse> getBookingByUserId(Long userId) {
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(this::convertToBookingResponse)
                .toList();
    }

    @Transactional
    @CircuitBreaker(name = "bookingService", fallbackMethod = "updateBookingFallback")
    @Retry(name = "bookingService")
    @RateLimiter(name = "bookingService")
    public BookingResponse updateBooking(Long bookingId, BookingRequest bookingRequest) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException::new);

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update a cancelled booking");
        }

        booking.setItemId(bookingRequest.getItemId());
        booking.setUserId(bookingRequest.getUserId());
        booking.setBookingDate(bookingRequest.getBookingDate());
        booking.setUpdatedAt(LocalDateTime.now());

        bookingProducer.sendBookingUpdatedEvent(new BookingEvent(
                "booking.updated",
                booking.getId(),
                booking.getUserId(),
                booking.getItemId(),
                booking.getCreatedAt()
        ));

        return convertToBookingResponse(bookingRepository.save(booking));
    }

    @Transactional
    @CircuitBreaker(name = "bookingService", fallbackMethod = "confirmBookingFallback")
    @Retry(name = "bookingService")
    @RateLimiter(name = "bookingService")
    public BookingResponse confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException::new);

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be confirmed");
        }
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(LocalDateTime.now());

        bookingProducer.sendBookingConfirmedEvent(new BookingEvent(
                "booking.confirmed",
                booking.getId(),
                booking.getUserId(),
                booking.getItemId(),
                booking.getCreatedAt()
        ));

        return convertToBookingResponse(bookingRepository.save(booking));
    }

    public BookingResponse createBookingFallback(BookingRequest bookingRequest, Throwable t) {
        logger.severe("Fallback triggered in createBookingFallback: " + t.getMessage());
        throw new IllegalStateException("Fallback: can't create booking");
    }

    public void cancelBookingFallback(Long bookingId, Throwable t) {
        logger.severe("Fallback triggered in cancelBookingFallback: " + t.getMessage());
        throw new IllegalStateException("Fallback: can't cancel booking with id: " + bookingId);
    }

    public List<BookingResponse> getBookingByUserIdBookingFallback(Long userId, Throwable t) {
        logger.severe("Fallback triggered in getBookingByUserIdBookingFallback: " + t.getMessage());
        throw new IllegalStateException("Fallback: can't find bookings for user with id: " + userId);
    }

    public BookingResponse updateBookingFallback(Long bookingId, BookingRequest bookingRequest, Throwable t) {
        logger.severe("Fallback triggered in updateBookingFallback: " + t.getMessage());
        throw new IllegalStateException("Fallback: can't update booking with id: " + bookingId);
    }

    public BookingResponse confirmBookingFallback(Long bookingId, Throwable t) {
        logger.severe("Fallback triggered in confirmBookingFallback: " + t.getMessage());
        throw new IllegalStateException("Fallback: can't confirm booking with id: " + bookingId);
    }
}
