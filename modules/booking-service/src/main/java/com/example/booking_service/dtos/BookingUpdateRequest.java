package com.example.booking_service.dtos;

import com.example.booking_service.models.enums.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingUpdateRequest {
    @Schema(description = "User id", example = "1")
    private Long userId;
    @Schema(description = "Item id", example = "1")
    private Long itemId;
    @Schema(description = "Booking date")
    private LocalDateTime bookingDate;
    @Schema(description = "Booking status")
    private BookingStatus bookingStatus;
}
