package com.example.booking_service.mapper;

import com.example.booking_service.dtos.BookingRequest;
import com.example.booking_service.dtos.BookingResponse;
import com.example.booking_service.models.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "bookingStatus", ignore = true)
    Booking toBooking(BookingRequest bookingRequest);

    @Mapping(source = "bookingStatus", target = "status")
    BookingResponse toBookingResponse(Booking booking);
}
