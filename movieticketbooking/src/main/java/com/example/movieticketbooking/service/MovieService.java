package com.example.movieticketbooking.service;

import com.example.movieticketbooking.dto.MovieRequest;
import com.example.movieticketbooking.dto.MovieResponse;
import com.example.movieticketbooking.entity.Movie;
import com.example.movieticketbooking.exception.ResourceInUseException;
import com.example.movieticketbooking.exception.ResourceNotFoundException;
import com.example.movieticketbooking.repository.BookingRepository;
import com.example.movieticketbooking.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class MovieService {

    private final MovieRepository movieRepository;
    private final BookingRepository bookingRepository;

    public MovieService(MovieRepository movieRepository, BookingRepository bookingRepository) {
        this.movieRepository = movieRepository;
        this.bookingRepository = bookingRepository;
    }

    public MovieResponse createMovie(MovieRequest request) {
        Movie movie = new Movie();
        copyRequestToMovie(request, movie);
        return MovieResponse.from(movieRepository.save(movie));
    }

    /** Lists movies; every filter is optional and filters can be combined. */
    @Transactional(readOnly = true)
    public List<MovieResponse> searchMovies(String title, String genre, String language) {
        return movieRepository.search(containsPattern(title), exactPattern(genre), exactPattern(language))
                .stream()
                .map(MovieResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MovieResponse getMovieById(Long id) {
        return MovieResponse.from(findById(id));
    }

    public MovieResponse updateMovie(Long id, MovieRequest request) {
        Movie movie = findById(id);
        copyRequestToMovie(request, movie);
        return MovieResponse.from(movieRepository.save(movie));
    }

    public void deleteMovie(Long id) {
        Movie movie = findById(id);
        if (bookingRepository.existsByMovieId(id)) {
            throw new ResourceInUseException("Movie with id " + id + " has bookings and cannot be deleted");
        }
        movieRepository.delete(movie);
    }

    private Movie findById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie with id " + id + " not found"));
    }

    private void copyRequestToMovie(MovieRequest request, Movie movie) {
        movie.setTitle(request.title().trim());
        movie.setGenre(request.genre().trim());
        movie.setLanguage(request.language().trim());
        movie.setDuration(request.duration());
        movie.setRating(request.rating());
        movie.setTicketPrice(request.ticketPrice());
        movie.setDescription(request.description());
        movie.setReleaseDate(request.releaseDate());
        movie.setAvailableSeats(request.availableSeats());
    }

    private String containsPattern(String value) {
        return "%" + normalize(value) + "%";
    }

    private String exactPattern(String value) {
        String normalized = normalize(value);
        return normalized.isEmpty() ? "%" : normalized;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
