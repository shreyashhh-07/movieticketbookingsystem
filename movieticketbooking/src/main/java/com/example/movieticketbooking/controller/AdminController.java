package com.example.movieticketbooking.controller;

import com.example.movieticketbooking.dto.BookingResponse;
import com.example.movieticketbooking.dto.UserResponse;
import com.example.movieticketbooking.service.BookingService;
import com.example.movieticketbooking.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Everything under /api/admin/** is restricted to ROLE_ADMIN in SecurityConfig. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final BookingService bookingService;

    public AdminController(UserService userService, BookingService bookingService) {
        this.userService = userService;
        this.bookingService = bookingService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }
}
