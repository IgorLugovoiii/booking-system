package com.example.payment_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentProducer {
    private static final String PAYMENT_TOPIC = "payment-events";
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public PaymentProducer(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate){
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentEvent(PaymentEvent event){
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(PAYMENT_TOPIC, event.getEventType(), json);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
