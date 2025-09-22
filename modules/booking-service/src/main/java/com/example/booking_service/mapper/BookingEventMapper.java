package com.example.booking_service.mapper;

import com.example.booking_service.kafka.BookingEvent;
import com.example.booking_service.models.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingEventMapper {
    @Mapping(target = "eventType", constant = "booking.created")
    BookingEvent toCreatedEvent(Booking booking);

    @Mapping(target = "eventType", constant = "booking.updated")
    BookingEvent toUpdatedEvent(Booking booking);

    @Mapping(target = "eventType", constant = "booking.canceled")
    BookingEvent toCanceledEvent(Booking booking);

    @Mapping(target = "eventType", constant = "booking.confirmed")
    BookingEvent toConfirmedEvent(Booking booking);

}
