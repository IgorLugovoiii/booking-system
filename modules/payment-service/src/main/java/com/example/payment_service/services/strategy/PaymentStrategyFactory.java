package com.example.payment_service.services.strategy;

import com.example.payment_service.models.enums.PaymentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentStrategyFactory {
    private final Map<String, PaymentStrategy> strategies;

    public PaymentStrategy getStrategy(PaymentType paymentType){
        PaymentStrategy strategy = strategies.get(paymentType.name().toLowerCase() + "Payment");
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment type: " + paymentType);
        }
        return strategy;
    }
}
