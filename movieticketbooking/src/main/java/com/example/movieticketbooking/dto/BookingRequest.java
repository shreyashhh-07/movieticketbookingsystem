package com.example.movieticketbooking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Only the movie and the seat count come from the client.
 * Price and total amount are calculated on the server.
 */
public record BookingRequest(
        @NotNull(message = "Movie id is required")
        Long movieId,

        @NotNull(message = "Number of seats is required")
        @Min(value = 1, message = "At least 1 seat must be booked")
        @Max(value = 10, message = "At most 10 seats can be booked at once")
        Integer numberOfSeats) {
}
