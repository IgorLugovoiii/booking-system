package com.example.booking_service.controllers;

import com.example.booking_service.dtos.BookingRequest;
import com.example.booking_service.dtos.BookingResponse;
import com.example.booking_service.dtos.BookingSearchParams;
import com.example.booking_service.dtos.BookingUpdateRequest;
import com.example.booking_service.services.api.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Booking controller", description = "Controller, for managing bookings")
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "A method for creating booking")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking created"),
            @ApiResponse(responseCode = "403", description = "Unauthorized"),
            @ApiResponse(responseCode = "400", description = "Invalid credentials")
    })
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest bookingRequest) {
        return new ResponseEntity<>(bookingService.createBooking(bookingRequest), HttpStatus.CREATED);
    }

    @Operation(summary = "Return all bookings by user id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking by user id found"),
            @ApiResponse(responseCode = "403", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<List<BookingResponse>> getUserBookings(
            @Parameter(description = "ID of the user to find all his bookings", example = "1")
            @PathVariable Long userId) {
        return new ResponseEntity<>(bookingService.getBookingByUserId(userId), HttpStatus.OK);
    }

    @Operation(description = "Update booking by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created"),
            @ApiResponse(responseCode = "403", description = "Unauthorized"),
            @ApiResponse(responseCode = "400", description = "Invalid credentials")
    })
    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> updateBooking(
            @Parameter(description = "ID of the booking to update", example = "1")
            @PathVariable Long bookingId, @RequestBody BookingUpdateRequest request) {
        return new ResponseEntity<>(bookingService.updateBooking(bookingId, request), HttpStatus.OK);
    }

    @Operation(description = "Searching bookings with different params")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bookings found"),
            @ApiResponse(responseCode = "403", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "None of bookings with those params not found")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<BookingResponse>> searchBookings(
            BookingSearchParams bookingSearchParams,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<BookingResponse> bookingResponses = bookingService.searchBookings(bookingSearchParams, PageRequest.of(page, size));
        return new ResponseEntity<>(bookingResponses, HttpStatus.OK);
    }
}
