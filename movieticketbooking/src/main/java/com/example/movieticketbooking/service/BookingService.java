package com.example.movieticketbooking.service;

import com.example.movieticketbooking.dto.BookingRequest;
import com.example.movieticketbooking.dto.BookingResponse;
import com.example.movieticketbooking.dto.BookingUpdateRequest;
import com.example.movieticketbooking.entity.Booking;
import com.example.movieticketbooking.entity.Movie;
import com.example.movieticketbooking.entity.User;
import com.example.movieticketbooking.enums.BookingStatus;
import com.example.movieticketbooking.exception.BadRequestException;
import com.example.movieticketbooking.exception.ResourceNotFoundException;
import com.example.movieticketbooking.repository.BookingRepository;
import com.example.movieticketbooking.repository.MovieRepository;
import com.example.movieticketbooking.repository.UserRepository;
import com.example.movieticketbooking.security.AuthUtils;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          MovieRepository movieRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.movieRepository = movieRepository;
        this.userRepository = userRepository;
    }

    /** Books seats for the logged-in user. The price and total are taken from the movie, not from the client. */
    public BookingResponse createBooking(BookingRequest request, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));
        Movie movie = findMovieForUpdate(request.movieId());

        reserveSeats(movie, request.numberOfSeats());

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setMovie(movie);
        booking.setNumberOfSeats(request.numberOfSeats());
        booking.setTicketPrice(movie.getTicketPrice());
        booking.setTotalAmount(calculateTotal(movie.getTicketPrice(), request.numberOfSeats()));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingTime(LocalDateTime.now());

        return BookingResponse.from(bookingRepository.save(booking));
    }

    /** Bookings of the logged-in user, newest first. */
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Logged-in user not found"));
        return bookingRepository.findByUserIdOrderByBookingTimeDesc(user.getId()).stream()
                .map(BookingResponse::from)
                .toList();
    }

    /** All bookings of all users (admin only - enforced in SecurityConfig). */
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "bookingTime")).stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id, Authentication authentication) {
        return BookingResponse.from(findAuthorizedBooking(id, authentication));
    }

    /** Changes the number of seats of a confirmed booking and recalculates the total. */
    public BookingResponse updateBooking(Long id, BookingUpdateRequest request, Authentication authentication) {
        Booking booking = findAuthorizedBooking(id, authentication);
        ensureConfirmed(booking, "updated");

        Movie movie = findMovieForUpdate(booking.getMovie().getId());
        int seatDifference = request.numberOfSeats() - booking.getNumberOfSeats();
        if (seatDifference > 0) {
            reserveSeats(movie, seatDifference);
        } else {
            movie.setAvailableSeats(movie.getAvailableSeats() - seatDifference);
        }

        booking.setNumberOfSeats(request.numberOfSeats());
        booking.setTotalAmount(calculateTotal(booking.getTicketPrice(), request.numberOfSeats()));
        return BookingResponse.from(bookingRepository.save(booking));
    }

    /** Cancels a booking: seats are given back to the movie and the booking is kept with status CANCELLED. */
    public void cancelBooking(Long id, Authentication authentication) {
        Booking booking = findAuthorizedBooking(id, authentication);
        ensureConfirmed(booking, "cancelled");

        Movie movie = findMovieForUpdate(booking.getMovie().getId());
        movie.setAvailableSeats(movie.getAvailableSeats() + booking.getNumberOfSeats());
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    private Booking findAuthorizedBooking(Long id, Authentication authentication) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with id " + id + " not found"));
        boolean isOwner = booking.getUser().getEmail().equals(authentication.getName());
        if (!isOwner && !AuthUtils.isAdmin(authentication)) {
            throw new AccessDeniedException("You can only access your own bookings");
        }
        return booking;
    }

    private Movie findMovieForUpdate(Long movieId) {
        return movieRepository.findByIdForUpdate(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie with id " + movieId + " not found"));
    }

    private void reserveSeats(Movie movie, int seats) {
        if (seats > movie.getAvailableSeats()) {
            throw new BadRequestException("Only " + movie.getAvailableSeats()
                    + " seat(s) available for '" + movie.getTitle() + "'");
        }
        movie.setAvailableSeats(movie.getAvailableSeats() - seats);
    }

    private void ensureConfirmed(Booking booking, String action) {
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Booking " + booking.getId() + " is already "
                    + booking.getStatus() + " and cannot be " + action);
        }
    }

    private BigDecimal calculateTotal(BigDecimal ticketPrice, int numberOfSeats) {
        return ticketPrice.multiply(BigDecimal.valueOf(numberOfSeats));
    }
}
