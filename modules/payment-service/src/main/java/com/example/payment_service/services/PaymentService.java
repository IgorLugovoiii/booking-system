package com.example.payment_service.services;

import com.example.payment_service.dtos.PaymentRequest;
import com.example.payment_service.dtos.PaymentResponse;
import com.example.payment_service.kafka.PaymentEvent;
import com.example.payment_service.kafka.PaymentProducer;
import com.example.payment_service.models.Payment;
import com.example.payment_service.models.enums.PaymentStatus;
import com.example.payment_service.repositories.PaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Service
public class PaymentService {
    private static final Logger logger = Logger.getLogger(PaymentService.class.getName());
    private final PaymentRepository paymentRepository;
    private final PaymentProducer paymentProducer;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, PaymentProducer paymentProducer){
        this.paymentRepository = paymentRepository;
        this.paymentProducer = paymentProducer;
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "processPaymentFallback")
    @Retry(name = "paymentService")
    @RateLimiter(name = "paymentService")
    public PaymentResponse processPayment(PaymentRequest paymentRequest){
        Payment payment = new Payment();
        payment.setBookingId(paymentRequest.getBookingId());
        payment.setUserId(paymentRequest.getUserId());
        payment.setAmount(paymentRequest.getAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        Payment saved = paymentRepository.save(payment);

        paymentProducer.sendPaymentEvent(new PaymentEvent(
                "payment.success",
                saved.getId(),
                saved.getBookingId(),
                saved.getUserId(),
                saved.getAmount(),
                saved.getPaymentDate()
        ));

        return new PaymentResponse(saved.getId(), saved.getPaymentStatus(), saved.getPaymentDate());
    }

    public PaymentResponse processPaymentFallback(PaymentRequest paymentRequest, Throwable t) {
        logger.severe("Error processing payment for booking " + paymentRequest.getBookingId() + ": " + t.getMessage());
        throw new IllegalStateException("Fallback: can't process payment for booking " + paymentRequest.getBookingId());
    }
}
