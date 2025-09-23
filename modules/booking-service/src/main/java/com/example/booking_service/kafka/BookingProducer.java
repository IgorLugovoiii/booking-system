package com.example.booking_service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingProducer {
    private static final String TOPIC = "booking-events";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendEvent(BookingEvent bookingEvent) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(bookingEvent);
        kafkaTemplate.send(TOPIC, bookingEvent.getEventType(), json);
    }
}
