package com.example.notification_service.kafka.consumers;

import com.example.notification_service.dtos.NotificationRequest;
import com.example.notification_service.kafka.events.PaymentEvent;
import com.example.notification_service.services.api.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventConsumer extends BaseEventConsumer<PaymentEvent> {
    public PaymentEventConsumer(ObjectMapper objectMapper, NotificationService notificationService) {
        super(objectMapper, notificationService);
    }

    @KafkaListener(topics = "${spring.kafka.topics.payment}", groupId = "notification-group")
    public void consume(String message) throws JsonProcessingException {
        super.consume(message);
    }

    @Override
    protected Class<PaymentEvent> getEventClass() {
        return PaymentEvent.class;
    }

    @Override
    protected NotificationRequest buildNotification(PaymentEvent event) {
        try {
            String subject = switch (event.getEventType()) {
                case "payment.successful" -> String.format(
                        "Payment successful! Price: %.2f hrn. Date: %s",
                        event.getAmount(),
                        event.getPaymentDate()
                );
                default -> "Payment failed. Something is wrong";
            };

            String msg = "Event: " + event.getEventType() + " for payment with id: "
                    + event.getPaymentId() + ", user id: " + event.getUserId() + " and price: " + event.getAmount();

            return new NotificationRequest("qeadzc4065@gmail.com", "payment.success", msg);
        } catch (Exception e) {
            throw new KafkaMessageReceiveException("Failed to receive payment event", e);
        }
    }
}
