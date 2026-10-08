package com.example.movieticketbooking;

import com.example.movieticketbooking.dto.BookingRequest;
import com.example.movieticketbooking.dto.BookingResponse;
import com.example.movieticketbooking.entity.Booking;
import com.example.movieticketbooking.entity.Movie;
import com.example.movieticketbooking.entity.User;
import com.example.movieticketbooking.enums.BookingStatus;
import com.example.movieticketbooking.exception.BadRequestException;
import com.example.movieticketbooking.repository.BookingRepository;
import com.example.movieticketbooking.repository.MovieRepository;
import com.example.movieticketbooking.repository.UserRepository;
import com.example.movieticketbooking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit test of the business rules in BookingService (repositories are mocked, no database needed). */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

    private Authentication authentication;
    private Movie movie;

    @BeforeEach
    void setUp() {
        authentication = new UsernamePasswordAuthenticationToken(
                "student@test.com", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        User user = new User();
        user.setName("Student");
        user.setEmail("student@test.com");
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));

        movie = new Movie();
        movie.setTitle("Unit Test Movie");
        movie.setTicketPrice(new BigDecimal("250.00"));
        movie.setAvailableSeats(10);
        when(movieRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(movie));
    }

    @Test
    void createBooking_totalAmountIsSeatsTimesPrice_andSeatsAreReserved() {
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.createBooking(new BookingRequest(1L, 3), authentication);

        assertEquals(0, new BigDecimal("750.00").compareTo(response.totalAmount()));
        assertEquals(BookingStatus.CONFIRMED, response.status());
        assertEquals(7, movie.getAvailableSeats());
    }

    @Test
    void createBooking_withNotEnoughSeats_throwsBadRequest_andSavesNothing() {
        assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(new BookingRequest(1L, 11), authentication));

        verify(bookingRepository, never()).save(any(Booking.class));
        assertEquals(10, movie.getAvailableSeats());
    }
}
