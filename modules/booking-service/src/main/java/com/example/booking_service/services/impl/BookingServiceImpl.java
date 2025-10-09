package com.example.booking_service.services.impl;

import com.example.booking_service.dtos.BookingRequest;
import com.example.booking_service.dtos.BookingResponse;
import com.example.booking_service.dtos.BookingSearchParams;
import com.example.booking_service.kafka.BookingProducer;
import com.example.booking_service.mapper.BookingEventMapper;
import com.example.booking_service.mapper.BookingMapper;
import com.example.booking_service.models.Booking;
import com.example.booking_service.models.enums.BookingStatus;
import com.example.booking_service.repositories.BookingRepository;
import com.example.booking_service.services.api.BookingService;
import com.example.booking_service.services.utils.SpecificationUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingProducer bookingProducer;
    private final BookingMapper bookingMapper;
    private final BookingEventMapper bookingEventMapper;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest bookingRequest) throws JsonProcessingException {
        Booking booking = bookingMapper.toBooking(bookingRequest);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);
        sendKafkaEventSafely(()-> {
            try {
                bookingProducer.sendEvent(bookingEventMapper.toCreatedEvent(savedBooking));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        return bookingMapper.toBookingResponse(savedBooking);
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) throws JsonProcessingException {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException::new);

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());

        bookingRepository.save(booking);

        sendKafkaEventSafely(()-> {
            try {
                bookingProducer.sendEvent(bookingEventMapper.toCanceledEvent(booking));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingByUserId(Long userId) {
        return bookingRepository.findByUserId(userId).stream().map(bookingMapper::toBookingResponse).toList();
    }

    @Override
    @Transactional
    public BookingResponse updateBooking(Long bookingId, BookingRequest bookingRequest) throws JsonProcessingException {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException::new);

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update a cancelled booking");
        }

        booking.setItemId(bookingRequest.getItemId());
        booking.setUserId(bookingRequest.getUserId());
        booking.setBookingDate(bookingRequest.getBookingDate());
        booking.setUpdatedAt(LocalDateTime.now());

        sendKafkaEventSafely(()-> {
            try {
                bookingProducer.sendEvent(bookingEventMapper.toUpdatedEvent(booking));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        return bookingMapper.toBookingResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(Long bookingId) throws JsonProcessingException {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException::new);

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be confirmed");
        }
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(LocalDateTime.now());

        sendKafkaEventSafely(()->{
            try {
                bookingProducer.sendEvent(bookingEventMapper.toConfirmedEvent(booking));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        return bookingMapper.toBookingResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> searchBookings(BookingSearchParams bookingSearchParams, Pageable pageable) {
        Specification<Booking> spec = Specification
                .<Booking>where(SpecificationUtils.equal("userId", bookingSearchParams.getUserId()))
                .and(SpecificationUtils.equal("itemId", bookingSearchParams.getItemId()))
                .and(SpecificationUtils.equal("bookingStatus", bookingSearchParams.getStatus()))
                .and(SpecificationUtils.between("bookingDate", bookingSearchParams.getFromDate(), bookingSearchParams.getToDate()));

        return bookingRepository.findAll(spec, pageable).map(bookingMapper::toBookingResponse);
    }

    @CircuitBreaker(name = "bookingService")
    @Retry(name = "bookingService")
    @RateLimiter(name = "bookingService")
    private void sendKafkaEventSafely(Runnable runnable){
        runnable.run();
    }
}
