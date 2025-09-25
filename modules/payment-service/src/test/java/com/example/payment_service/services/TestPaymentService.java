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

import static org.assertj.core.api.Assertions.*;
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
    void givenValidPayment_whenProcessingPayment_thenMakesPaymentAndPaymentEventIsSend() throws JsonProcessingException {
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse response = paymentService.processPayment(paymentRequest);

        assertThat(response)
                .isNotNull()
                .extracting(PaymentResponse::getPaymentId, PaymentResponse::getPaymentStatus)
                .containsExactly(payment.getId(), PaymentStatus.SUCCESS);

        assertThat(response.getPaymentDate()).isEqualToIgnoringNanos(payment.getPaymentDate());

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentProducer, times(1)).sendPaymentEvent(any(PaymentEvent.class));
        verifyNoMoreInteractions(paymentRepository, paymentProducer);
    }

    @Test
    void givenRepositoryThrowsException_whenProcessPayment_thenExceptionPropagated() throws JsonProcessingException {
        when(paymentRepository.save(any(Payment.class))).thenThrow(new RuntimeException());

        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(RuntimeException.class);

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verifyNoInteractions(paymentProducer);
    }

    @Test
    void givenZeroAmount_whenProcessPayment_thenPaymentSavedWithZeroAmount() throws JsonProcessingException {
        PaymentRequest zeroAmountRequest = PaymentRequest.builder()
                .userId(1L)
                .bookingId(1L)
                .amount(0.0)
                .build();

        Payment zeroPayment = Payment.builder()
                .id(2L)
                .userId(1L)
                .bookingId(1L)
                .amount(0.0)
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentDate(LocalDateTime.now())
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(zeroPayment);

        PaymentResponse response = paymentService.processPayment(zeroAmountRequest);

        assertThat(response.getPaymentId()).isEqualTo(zeroPayment.getId());
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(response.getPaymentDate()).isEqualTo(zeroPayment.getPaymentDate());

        verify(paymentRepository).save(any(Payment.class));
        verify(paymentProducer).sendPaymentEvent(any(PaymentEvent.class));
    }

    @Test
    void givenNullUserId_whenProcessPayment_thenThrowsException() {
        PaymentRequest invalidRequest = PaymentRequest.builder()
                .userId(null)
                .bookingId(1L)
                .amount(100.0)
                .build();

        assertThatThrownBy(() -> paymentService.processPayment(invalidRequest))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void givenNullBookingId_whenProcessPayment_thenThrowsException() {
        PaymentRequest invalidRequest = PaymentRequest.builder()
                .userId(1L)
                .bookingId(null)
                .amount(100.0)
                .build();

        assertThatThrownBy(() -> paymentService.processPayment(invalidRequest))
                .isInstanceOf(NullPointerException.class);
    }
}
