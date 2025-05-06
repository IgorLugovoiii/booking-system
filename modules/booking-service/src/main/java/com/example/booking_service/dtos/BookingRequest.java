package com.example.booking_service.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequest {
    @Schema(description = "User id", example = "1")
    private Long userId;
    @Schema(description = "Item id", example = "1")
    private Long itemId;
    @Schema(description = "Booking date")
    private LocalDateTime bookingDate;
}
