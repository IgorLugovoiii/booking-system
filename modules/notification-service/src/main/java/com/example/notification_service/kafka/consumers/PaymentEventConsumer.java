package com.example.notification_service.kafka.consumers;

import com.example.notification_service.dtos.NotificationRequest;
import com.example.notification_service.exception.KafkaMessageReceiveException;
import com.example.notification_service.kafka.events.PaymentEvent;
import com.example.notification_service.services.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventConsumer {
    private final NotificationService notificationService;

    @Autowired
    public PaymentEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-group")
    public void handlePaymentEvent(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PaymentEvent paymentEvent = objectMapper.readValue(message, PaymentEvent.class);
            if ("payment.success".equals(paymentEvent.getEventType())) {
                String msg = String.format(
                        "Payment successful! Price: %.2f hrn. Date: %s",
                        paymentEvent.getAmount(),
                        paymentEvent.getPaymentDate()
                );

                NotificationRequest request = new NotificationRequest("qeadzc4065@gmail.com",
                        "payment.success",
                        msg
                );

                notificationService.sendNotification(request);
            }
        } catch (Exception e) {
            throw new KafkaMessageReceiveException("Failed to receive payment event" , e);
        }
    }
}
