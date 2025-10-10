package com.example.payment_service.services.strategy;

import com.example.payment_service.dtos.PaymentRequest;
import com.example.payment_service.dtos.PaymentResponse;
import com.example.payment_service.kafka.PaymentEvent;
import com.example.payment_service.kafka.PaymentProducer;
import com.example.payment_service.models.Payment;
import com.example.payment_service.models.enums.PaymentStatus;
import com.example.payment_service.repositories.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service("creditCardPayment")
@RequiredArgsConstructor
public class CardPaymentStrategy implements PaymentStrategy {
    private final PaymentRepository paymentRepository;
    private final PaymentProducer paymentProducer;

    @Override
    public PaymentResponse pay(PaymentRequest paymentRequest) throws JsonProcessingException {
        Payment payment = Payment.builder()
                .bookingId(paymentRequest.getBookingId())
                .userId(paymentRequest.getUserId())
                .amount(paymentRequest.getAmount())
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentDate(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);

        sendKafkaEventSafely(() ->
        {
            try {
                paymentProducer.sendPaymentEvent(new PaymentEvent(
                        "payment.success",
                        saved.getId(),
                        saved.getBookingId(),
                        saved.getUserId(),
                        saved.getAmount(),
                        saved.getPaymentDate()
                ));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        return new PaymentResponse(saved.getId(), saved.getPaymentStatus(), saved.getPaymentDate());
    }


    @CircuitBreaker(name = "paymentService")
    @Retry(name = "paymentService")
    @RateLimiter(name = "paymentService")
    private void sendKafkaEventSafely(Runnable runnable) {
        runnable.run();
    }
}
