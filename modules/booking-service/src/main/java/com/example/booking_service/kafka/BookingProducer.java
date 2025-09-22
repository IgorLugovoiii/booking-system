package com.example.booking_service.kafka;

import com.example.booking_service.exception.KafkaMessageSendException;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    public void sendEvent(BookingEvent bookingEvent) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(bookingEvent);
        kafkaTemplate.send(TOPIC, bookingEvent.getEventType(), json);
    }
}
