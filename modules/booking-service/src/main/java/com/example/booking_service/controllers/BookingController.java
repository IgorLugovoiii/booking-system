package com.example.booking_service.controllers;

import com.example.booking_service.dtos.BookingRequest;
import com.example.booking_service.dtos.BookingResponse;
import com.example.booking_service.services.BookingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest bookingRequest) throws JsonProcessingException {
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

    @Operation(description = "Cancel booking")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Booking canceled"),
            @ApiResponse(responseCode = "403", description = "Unauthorized"),
            @ApiResponse(responseCode = "400", description = "Invalid credentials")
    })
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(
            @Parameter(description = "ID of the booking to find by id", example = "1")
            @PathVariable Long bookingId) throws JsonProcessingException {
        bookingService.cancelBooking(bookingId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(description = "Update booking by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created"),
            @ApiResponse(responseCode = "403", description = "Unauthorized"),
            @ApiResponse(responseCode = "400", description = "Invalid credentials")
    })
    @PutMapping("/{bookingId}/update")
    public ResponseEntity<BookingResponse> updateBooking(
            @Parameter(description = "ID of the booking to update", example = "1")
            @PathVariable Long bookingId, @RequestBody BookingRequest bookingRequest) throws JsonProcessingException {
        return new ResponseEntity<>(bookingService.updateBooking(bookingId, bookingRequest), HttpStatus.OK);
    }

    @Operation(description = "Confirm booking")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created"),
            @ApiResponse(responseCode = "403", description = "Unauthorized")
    })
    @PutMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(
            @Parameter(description = "ID of the booking to delete", example = "1")
            @PathVariable Long bookingId) throws JsonProcessingException {
        return new ResponseEntity<>(bookingService.confirmBooking(bookingId), HttpStatus.OK);
    }
}
