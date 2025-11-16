package com.example.payment_service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProducer {
    private static final String TOPIC = "payment-events";
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendPaymentEvent(PaymentEvent event) throws JsonProcessingException {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, event.getEventType(), json)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Kafka event sent: type={}, event={}", event.getEventType(), json);
                        } else {
                            log.error("Kafka send failed: {}", ex.getMessage(), ex);
                            throw new RuntimeException("Failed to send Kafka message", ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Kafka serialization/send error: {}", e.getMessage(), e);
            throw e;
        }
    }
}
