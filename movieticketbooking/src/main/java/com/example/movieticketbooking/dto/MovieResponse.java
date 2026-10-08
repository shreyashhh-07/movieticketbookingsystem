package com.example.movieticketbooking.dto;

import com.example.movieticketbooking.entity.Movie;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovieResponse(
        Long id,
        String title,
        String genre,
        String language,
        Integer duration,
        Double rating,
        BigDecimal ticketPrice,
        String description,
        LocalDate releaseDate,
        Integer availableSeats) {

    public static MovieResponse from(Movie movie) {
        return new MovieResponse(movie.getId(), movie.getTitle(), movie.getGenre(),
                movie.getLanguage(), movie.getDuration(), movie.getRating(),
                movie.getTicketPrice(), movie.getDescription(), movie.getReleaseDate(),
                movie.getAvailableSeats());
    }
}
