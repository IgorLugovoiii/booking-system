package com.example.booking_service.controllers;

import com.example.booking_service.dtos.BookingRequest;
import com.example.booking_service.dtos.BookingResponse;
import com.example.booking_service.services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest bookingRequest) {
        return new ResponseEntity<>(bookingService.createBooking(bookingRequest), HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<BookingResponse>> getUserBookings(@PathVariable Long userId) {
        return new ResponseEntity<>(bookingService.getBookingByUserId(userId), HttpStatus.OK);
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{bookingId}/update")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable Long bookingId,
                                                         @RequestBody BookingRequest bookingRequest) {
        return new ResponseEntity<>(bookingService.updateBooking(bookingId, bookingRequest), HttpStatus.OK);
    }

    @PutMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long bookingId){
        return new ResponseEntity<>(bookingService.confirmBooking(bookingId), HttpStatus.OK);
    }
}
