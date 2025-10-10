package com.example.payment_service.services;

import com.example.payment_service.dtos.PaymentRequest;
import com.example.payment_service.dtos.PaymentResponse;
import com.example.payment_service.kafka.PaymentEvent;
import com.example.payment_service.kafka.PaymentProducer;
import com.example.payment_service.models.Payment;
import com.example.payment_service.models.enums.PaymentStatus;
import com.example.payment_service.models.enums.PaymentType;
import com.example.payment_service.repositories.PaymentRepository;
import com.example.payment_service.services.impl.PaymentServiceImpl;
import com.example.payment_service.services.strategy.PaymentStrategy;
import com.example.payment_service.services.strategy.PaymentStrategyFactory;
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
public class TestPaymentServiceImpl {

    @Mock
    private PaymentStrategyFactory strategyFactory;

    @Mock
    private PaymentStrategy cardPaymentStrategy;

    @InjectMocks
    private PaymentServiceImpl paymentServiceImpl;

    private PaymentRequest paymentRequest;
    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        paymentRequest = PaymentRequest.builder()
                .userId(1L)
                .bookingId(1L)
                .amount(250.0)
                .paymentType(PaymentType.CREDIT_CARD)
                .build();

        paymentResponse = PaymentResponse.builder()
                .paymentId(1L)
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentDate(LocalDateTime.now())
                .build();
    }

    @Test
    void givenValidPayment_whenProcessingPayment_thenUsesStrategy() throws JsonProcessingException {
        // Мок фабрику, щоб вона повертала потрібну стратегію
        when(strategyFactory.getStrategy(paymentRequest.getPaymentType()))
                .thenReturn(cardPaymentStrategy);

        // Мок саму стратегію
        when(cardPaymentStrategy.pay(paymentRequest)).thenReturn(paymentResponse);

        PaymentResponse response = paymentServiceImpl.processPayment(paymentRequest);

        assertThat(response).isEqualTo(paymentResponse);

        verify(strategyFactory, times(1)).getStrategy(paymentRequest.getPaymentType());
        verify(cardPaymentStrategy, times(1)).pay(paymentRequest);
    }

    @Test
    void givenStrategyThrowsException_thenPropagates() throws JsonProcessingException {
        when(strategyFactory.getStrategy(paymentRequest.getPaymentType()))
                .thenReturn(cardPaymentStrategy);
        when(cardPaymentStrategy.pay(paymentRequest)).thenThrow(new RuntimeException("Kafka failed"));

        assertThatThrownBy(() -> paymentServiceImpl.processPayment(paymentRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Kafka failed");

        verify(strategyFactory).getStrategy(paymentRequest.getPaymentType());
        verify(cardPaymentStrategy).pay(paymentRequest);
    }
}
