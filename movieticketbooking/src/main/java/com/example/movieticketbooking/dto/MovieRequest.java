package com.example.movieticketbooking.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovieRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must be at most 150 characters")
        String title,

        @NotBlank(message = "Genre is required")
        @Size(max = 50, message = "Genre must be at most 50 characters")
        String genre,

        @NotBlank(message = "Language is required")
        @Size(max = 50, message = "Language must be at most 50 characters")
        String language,

        @NotNull(message = "Duration (in minutes) is required")
        @Positive(message = "Duration must be greater than 0")
        Integer duration,

        @NotNull(message = "Rating is required")
        @DecimalMin(value = "0.0", message = "Rating must be at least 0")
        @DecimalMax(value = "10.0", message = "Rating must be at most 10")
        Double rating,

        @NotNull(message = "Ticket price is required")
        @Positive(message = "Ticket price must be greater than 0")
        BigDecimal ticketPrice,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        LocalDate releaseDate,

        @NotNull(message = "Available seats is required")
        @PositiveOrZero(message = "Available seats cannot be negative")
        Integer availableSeats) {
}
