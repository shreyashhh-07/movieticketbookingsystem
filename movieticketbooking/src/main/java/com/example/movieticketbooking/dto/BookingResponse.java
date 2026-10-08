package com.example.movieticketbooking.dto;

import com.example.movieticketbooking.entity.Booking;
import com.example.movieticketbooking.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long userId,
        String userName,
        Long movieId,
        String movieTitle,
        Integer numberOfSeats,
        BigDecimal ticketPrice,
        BigDecimal totalAmount,
        BookingStatus status,
        LocalDateTime bookingTime) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(booking.getId(),
                booking.getUser().getId(), booking.getUser().getName(),
                booking.getMovie().getId(), booking.getMovie().getTitle(),
                booking.getNumberOfSeats(), booking.getTicketPrice(),
                booking.getTotalAmount(), booking.getStatus(), booking.getBookingTime());
    }
}
