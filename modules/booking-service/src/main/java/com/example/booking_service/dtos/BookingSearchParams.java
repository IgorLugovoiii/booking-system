package com.example.booking_service.dtos;

import com.example.booking_service.models.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingSearchParams {
    private BookingStatus status;
    private Long userId;
    private Long itemId;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}
