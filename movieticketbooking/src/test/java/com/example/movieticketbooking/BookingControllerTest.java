package com.example.movieticketbooking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingControllerTest extends ApiTestSupport {

    private long bookSeats(String userToken, long movieId, int seats) throws Exception {
        String response = mockMvc.perform(post("/api/bookings")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("movieId", movieId, "numberOfSeats", seats))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void createBooking_calculatesTotalOnServer_andReducesAvailableSeats() throws Exception {
        long movieId = createMovie(adminToken(), "Booking Movie", 50, "200.00");
        String userToken = registerAndLogin(uniqueEmail());

        // the client tries to cheat by sending its own totalAmount / ticketPrice - they must be ignored
        Map<String, Object> body = Map.of("movieId", movieId, "numberOfSeats", 3,
                "totalAmount", 1, "ticketPrice", 1);
        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numberOfSeats").value(3))
                .andExpect(jsonPath("$.ticketPrice").value(200.0))
                .andExpect(jsonPath("$.totalAmount").value(600.0))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.movieId").value(movieId));

        mockMvc.perform(get("/api/movies/" + movieId))
                .andExpect(jsonPath("$.availableSeats").value(47));
    }

    @Test
    void getBooking_andListMyBookings_returnOnlyOwnData() throws Exception {
        long movieId = createMovie(adminToken(), "Listing Movie", 50, "100.00");
        String userToken = registerAndLogin(uniqueEmail());
        long bookingId = bookSeats(userToken, movieId, 2);

        mockMvc.perform(get("/api/bookings/" + bookingId).header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.totalAmount").value(200.0));

        mockMvc.perform(get("/api/bookings").header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(bookingId));
    }

    @Test
    void getBooking_ofAnotherUser_returns403_butAdminCanView() throws Exception {
        long movieId = createMovie(adminToken(), "Private Movie", 50, "100.00");
        String owner = registerAndLogin(uniqueEmail());
        String stranger = registerAndLogin(uniqueEmail());
        long bookingId = bookSeats(owner, movieId, 1);

        mockMvc.perform(get("/api/bookings/" + bookingId).header("Authorization", bearer(stranger)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/bookings/" + bookingId).header("Authorization", bearer(adminToken())))
                .andExpect(status().isOk());
    }

    @Test
    void updateBooking_changesSeats_recalculatesTotal_andAdjustsAvailableSeats() throws Exception {
        long movieId = createMovie(adminToken(), "Update Booking Movie", 20, "150.00");
        String userToken = registerAndLogin(uniqueEmail());
        long bookingId = bookSeats(userToken, movieId, 2);

        mockMvc.perform(put("/api/bookings/" + bookingId)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("numberOfSeats", 4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfSeats").value(4))
                .andExpect(jsonPath("$.totalAmount").value(600.0));

        mockMvc.perform(get("/api/movies/" + movieId))
                .andExpect(jsonPath("$.availableSeats").value(16));
    }

    @Test
    void cancelBooking_returns204_restoresSeats_andCannotBeCancelledTwice() throws Exception {
        long movieId = createMovie(adminToken(), "Cancel Movie", 10, "100.00");
        String userToken = registerAndLogin(uniqueEmail());
        long bookingId = bookSeats(userToken, movieId, 4);

        mockMvc.perform(delete("/api/bookings/" + bookingId).header("Authorization", bearer(userToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/bookings/" + bookingId).header("Authorization", bearer(userToken)))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/api/movies/" + movieId))
                .andExpect(jsonPath("$.availableSeats").value(10));

        mockMvc.perform(delete("/api/bookings/" + bookingId).header("Authorization", bearer(userToken)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_withInvalidSeats_returns400WithFieldError() throws Exception {
        long movieId = createMovie(adminToken(), "Validation Movie", 10, "100.00");
        String userToken = registerAndLogin(uniqueEmail());

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("movieId", movieId, "numberOfSeats", 0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.numberOfSeats").exists());
    }

    @Test
    void createBooking_withMoreSeatsThanAvailable_returns400() throws Exception {
        long movieId = createMovie(adminToken(), "Almost Full Movie", 2, "100.00");
        String userToken = registerAndLogin(uniqueEmail());

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("movieId", movieId, "numberOfSeats", 5))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only 2 seat(s) available for 'Almost Full Movie'"));
    }

    @Test
    void createBooking_forUnknownMovie_returns404() throws Exception {
        String userToken = registerAndLogin(uniqueEmail());

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("movieId", 999999, "numberOfSeats", 1))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMovie_thatHasBookings_returns409() throws Exception {
        String adminToken = adminToken();
        long movieId = createMovie(adminToken, "Booked Movie", 10, "100.00");
        bookSeats(registerAndLogin(uniqueEmail()), movieId, 1);

        mockMvc.perform(delete("/api/movies/" + movieId).header("Authorization", bearer(adminToken)))
                .andExpect(status().isConflict());
    }

    @Test
    void adminCanListAllBookings() throws Exception {
        long movieId = createMovie(adminToken(), "Admin List Movie", 10, "100.00");
        bookSeats(registerAndLogin(uniqueEmail()), movieId, 1);

        mockMvc.perform(get("/api/admin/bookings").header("Authorization", bearer(adminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].movieTitle").exists());
    }
}
