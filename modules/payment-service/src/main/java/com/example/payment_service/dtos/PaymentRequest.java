package com.example.payment_service.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PaymentRequest {
    @Schema(description = "Booking id", example = "1")
    private Long bookingId;
    @Schema(description = "User id", example = "1")
    private Long userId;
    @Schema(description = "Price", example = "10.0")
    private Double amount;
}
