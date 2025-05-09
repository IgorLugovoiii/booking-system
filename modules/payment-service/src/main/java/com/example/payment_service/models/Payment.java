package com.example.payment_service.models;

import com.example.payment_service.models.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "booking_id")
    private Long bookingId;
    @Column(name = "amount")
    private Double amount;
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;
}
