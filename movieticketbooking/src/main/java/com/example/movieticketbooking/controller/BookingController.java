package com.example.movieticketbooking.controller;

import com.example.movieticketbooking.dto.BookingRequest;
import com.example.movieticketbooking.dto.BookingResponse;
import com.example.movieticketbooking.dto.BookingUpdateRequest;
import com.example.movieticketbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Every endpoint needs a login; users can only touch their own bookings (checked in BookingService). */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request,
                                                         Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(request, authentication));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getMyBookings(Authentication authentication) {
        return ResponseEntity.ok(bookingService.getMyBookings(authentication));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(bookingService.getBookingById(id, authentication));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable Long id,
                                                         @Valid @RequestBody BookingUpdateRequest request,
                                                         Authentication authentication) {
        return ResponseEntity.ok(bookingService.updateBooking(id, request, authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id, Authentication authentication) {
        bookingService.cancelBooking(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
