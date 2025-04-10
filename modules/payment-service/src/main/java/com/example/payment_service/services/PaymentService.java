package com.example.payment_service.services;

import com.example.payment_service.dtos.PaymentRequest;
import com.example.payment_service.dtos.PaymentResponse;
import com.example.payment_service.kafka.PaymentEvent;
import com.example.payment_service.kafka.PaymentProducer;
import com.example.payment_service.models.Payment;
import com.example.payment_service.models.enums.PaymentStatus;
import com.example.payment_service.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentProducer paymentProducer;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, PaymentProducer paymentProducer){
        this.paymentRepository = paymentRepository;
        this.paymentProducer = paymentProducer;
    }

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

}
