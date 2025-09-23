package com.example.payment_service.services;

import com.example.payment_service.dtos.PaymentRequest;
import com.example.payment_service.dtos.PaymentResponse;
import com.example.payment_service.kafka.PaymentEvent;
import com.example.payment_service.kafka.PaymentProducer;
import com.example.payment_service.models.Payment;
import com.example.payment_service.models.enums.PaymentStatus;
import com.example.payment_service.repositories.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestPaymentService {
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentProducer paymentProducer;
    @InjectMocks
    private PaymentService paymentService;

    private Payment payment;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        payment = Payment.builder()
                .id(1L)
                .userId(1L)
                .bookingId(1L)
                .amount(250.0)
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentDate(LocalDateTime.now())
                .build();

        paymentRequest = PaymentRequest.builder()
                .userId(1L)
                .bookingId(1L)
                .amount(250.0)
                .build();
    }

    @Test
    void testProcessPayment_ShouldSaveAndSendEven() throws JsonProcessingException {
        when(paymentRepository.save(any())).thenReturn(payment);

        PaymentResponse paymentResponse = paymentService.processPayment(paymentRequest);

        assertNotNull(paymentResponse);
        assertEquals(payment.getId(), paymentResponse.getPaymentId());
        assertEquals(PaymentStatus.SUCCESS, paymentResponse.getPaymentStatus());

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentProducer, times(1)).sendPaymentEvent(any(PaymentEvent.class));
    }
}
