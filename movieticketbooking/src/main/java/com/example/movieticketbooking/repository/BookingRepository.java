package com.example.movieticketbooking.repository;

import com.example.movieticketbooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByBookingTimeDesc(Long userId);

    boolean existsByMovieId(Long movieId);

    boolean existsByUserId(Long userId);
}
