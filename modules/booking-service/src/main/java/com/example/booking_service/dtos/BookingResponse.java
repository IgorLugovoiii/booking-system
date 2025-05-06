package com.example.booking_service.dtos;

import com.example.booking_service.models.enums.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingResponse {
    @Schema(description = "Booking id", example = "1")
    private Long id;
    @Schema(description = "User id", example = "1")
    private Long userId;
    @Schema(description = "Item id", example = "1")
    private Long itemId;
    @Schema(description = "Time, when booking is made")
    private LocalDateTime bookingDate;
    @Schema(description = "Booking status", example = "PENDING, CONFIRMED, CANCELLED")
    private BookingStatus status;

}
