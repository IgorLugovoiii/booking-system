package com.example.booking_service.kafka;

import com.example.booking_service.exception.KafkaMessageSendException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BookingProducer {
    private static final String TOPIC = "booking-events";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public BookingProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendBookingCreatedEvent(BookingEvent bookingEvent) {
        try {
            String json = objectMapper.writeValueAsString(bookingEvent);
            kafkaTemplate.send(TOPIC, "booking.created", json);
        } catch (Exception e) {
            throw new KafkaMessageSendException("Failed to send booking.created event", e);
        }
    }

    public void sendBookingUpdatedEvent(BookingEvent bookingEvent) {
        try {
            String json = objectMapper.writeValueAsString(bookingEvent);
            kafkaTemplate.send(TOPIC, "booking.updated", json);
        } catch (Exception e) {
            throw new KafkaMessageSendException("Failed to send booking.updated event", e);
        }
    }

    public void sendBookingCanceledEvent(BookingEvent bookingEvent) {
        try {
            String json = objectMapper.writeValueAsString(bookingEvent);
            kafkaTemplate.send(TOPIC, "booking.deleted", json);
        } catch (Exception e) {
            throw new KafkaMessageSendException("Failed to send booking.deleted event", e);
        }
    }

    public void sendBookingConfirmedEvent(BookingEvent bookingEvent) {
        try {
            String json = objectMapper.writeValueAsString(bookingEvent);
            kafkaTemplate.send(TOPIC, "booking.confirmed", json);
        } catch (Exception e) {
            throw new KafkaMessageSendException("Failed to send booking.confirmed event", e);
        }
    }

}
