package com.example.booking_service.dtos;

import com.example.booking_service.models.enums.BookingStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingResponse {
    private Long id;
    private Long userId;
    private Long itemId;
    private LocalDateTime bookingDate;
    private BookingStatus status;

}
