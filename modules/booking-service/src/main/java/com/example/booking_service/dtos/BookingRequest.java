package com.example.booking_service.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequest {
    private Long userId;
    private Long itemId;
    private LocalDateTime bookingDate;
}
