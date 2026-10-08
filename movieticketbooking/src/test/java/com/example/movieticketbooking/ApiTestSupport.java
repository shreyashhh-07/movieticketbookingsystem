package com.example.movieticketbooking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Helper methods shared by the API integration tests. */
abstract class ApiTestSupport {

    protected static final String ADMIN_EMAIL = "admin@movie.com";
    protected static final String ADMIN_PASSWORD = "Admin@123";
    protected static final String USER_PASSWORD = "secret123";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    protected String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@test.com";
    }

    protected String adminToken() throws Exception {
        return login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    protected String login(String email, String password) throws Exception {
        Map<String, String> body = Map.of("email", email, "password", password);
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    /** Registers a new normal user and returns a JWT token for it. */
    protected String registerAndLogin(String email) throws Exception {
        Map<String, String> body = Map.of("name", "Test User", "email", email, "password", USER_PASSWORD);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated());
        return login(email, USER_PASSWORD);
    }

    protected Map<String, Object> moviePayload(String title, String genre, int seats, String price) {
        Map<String, Object> movie = new LinkedHashMap<>();
        movie.put("title", title);
        movie.put("genre", genre);
        movie.put("language", "English");
        movie.put("duration", 120);
        movie.put("rating", 8.5);
        movie.put("ticketPrice", new BigDecimal(price));
        movie.put("description", "Test movie");
        movie.put("releaseDate", "2024-05-01");
        movie.put("availableSeats", seats);
        return movie;
    }

    /** Creates a movie as admin and returns its id. */
    protected long createMovie(String adminToken, String title, int seats, String price) throws Exception {
        String response = mockMvc.perform(post("/api/movies")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(moviePayload(title, "Drama", seats, price))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}
