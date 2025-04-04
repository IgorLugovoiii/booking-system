package com.example.booking_service.models;

import com.example.booking_service.models.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "item_id")
    private Long itemId;
    @Column(name = "booking_date")
    private LocalDateTime bookingDate;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;
}
