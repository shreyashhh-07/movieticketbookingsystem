package com.example.movieticketbooking.controller;

import com.example.movieticketbooking.dto.MovieRequest;
import com.example.movieticketbooking.dto.MovieResponse;
import com.example.movieticketbooking.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** GET endpoints are public; POST/PUT/DELETE are restricted to ADMIN in SecurityConfig. */
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.createMovie(request));
    }

    @GetMapping
    public ResponseEntity<List<MovieResponse>> getMovies(@RequestParam(required = false) String title,
                                                         @RequestParam(required = false) String genre,
                                                         @RequestParam(required = false) String language) {
        return ResponseEntity.ok(movieService.searchMovies(title, genre, language));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieResponse>> searchByTitle(@RequestParam String title) {
        return ResponseEntity.ok(movieService.searchMovies(title, null, null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieResponse> updateMovie(@PathVariable Long id,
                                                     @Valid @RequestBody MovieRequest request) {
        return ResponseEntity.ok(movieService.updateMovie(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
