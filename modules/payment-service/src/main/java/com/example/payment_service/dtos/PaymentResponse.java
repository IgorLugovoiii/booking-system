package com.example.payment_service.dtos;

import com.example.payment_service.models.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PaymentResponse {
    @Schema(description = "Payment id", example = "1")
    private Long paymentId;
    @Schema(description = "Payment status", example = "SUCCESS", allowableValues = {"SUCCESS", "FAIL"})
    private PaymentStatus paymentStatus;
    @Schema(description = "Payment date")
    private LocalDateTime paymentDate;
}
