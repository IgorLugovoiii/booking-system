package com.example.payment_service.services;

import com.example.payment_service.dtos.PaymentRequest;
import com.example.payment_service.dtos.PaymentResponse;
import com.example.payment_service.kafka.PaymentEvent;
import com.example.payment_service.kafka.PaymentProducer;
import com.example.payment_service.models.Payment;
import com.example.payment_service.models.enums.PaymentStatus;
import com.example.payment_service.repositories.PaymentRepository;
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
        payment = new Payment();
        payment.setId(1L);
        payment.setUserId(1L);
        payment.setBookingId(1L);
        payment.setAmount(250.0);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaymentDate(LocalDateTime.now());

        paymentRequest = new PaymentRequest();
        paymentRequest.setUserId(1L);
        paymentRequest.setBookingId(1L);
        paymentRequest.setAmount(250.0);
    }

    @Test
    void testProcessPayment_ShouldSaveAndSendEven() {
        when(paymentRepository.save(any())).thenReturn(payment);

        PaymentResponse paymentResponse = paymentService.processPayment(paymentRequest);

        assertNotNull(paymentResponse);
        assertEquals(payment.getId(), paymentResponse.getPaymentId());
        assertEquals(PaymentStatus.SUCCESS, paymentResponse.getPaymentStatus());

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentProducer, times(1)).sendPaymentEvent(any(PaymentEvent.class));
    }
}
