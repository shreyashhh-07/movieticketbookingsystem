package com.example.movieticketbooking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MovieControllerTest extends ApiTestSupport {

    @Test
    void createMovie_asAdmin_returns201() throws Exception {
        mockMvc.perform(post("/api/movies")
                        .header("Authorization", bearer(adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(moviePayload("Created Movie", "Drama", 40, "180.00"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Created Movie"))
                .andExpect(jsonPath("$.availableSeats").value(40));
    }

    @Test
    void getMovies_isPublic_andGetById_returnsMovie() throws Exception {
        long id = createMovie(adminToken(), "Public Movie " + UUID.randomUUID(), 10, "100.00");

        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());

        mockMvc.perform(get("/api/movies/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.ticketPrice").value(100.0));
    }

    @Test
    void getMovieById_whenMissing_returns404WithErrorBody() throws Exception {
        mockMvc.perform(get("/api/movies/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Movie with id 999999 not found"))
                .andExpect(jsonPath("$.path").value("/api/movies/999999"));
    }

    @Test
    void updateMovie_asAdmin_changesAllFields() throws Exception {
        String token = adminToken();
        long id = createMovie(token, "Before Update", 10, "100.00");

        Map<String, Object> updated = moviePayload("After Update", "Thriller", 25, "300.00");
        mockMvc.perform(put("/api/movies/" + id)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("After Update"))
                .andExpect(jsonPath("$.genre").value("Thriller"))
                .andExpect(jsonPath("$.availableSeats").value(25))
                .andExpect(jsonPath("$.ticketPrice").value(300.0));
    }

    @Test
    void deleteMovie_asAdmin_returns204_thenMovieIsGone() throws Exception {
        String token = adminToken();
        long id = createMovie(token, "To Delete", 10, "100.00");

        mockMvc.perform(delete("/api/movies/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/movies/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void createMovie_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(moviePayload("No Token", "Drama", 10, "100.00"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createMovie_asNormalUser_returns403() throws Exception {
        String userToken = registerAndLogin(uniqueEmail());

        mockMvc.perform(post("/api/movies")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(moviePayload("Not Allowed", "Drama", 10, "100.00"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteMovie_asNormalUser_returns403() throws Exception {
        long id = createMovie(adminToken(), "Protected Movie", 10, "100.00");
        String userToken = registerAndLogin(uniqueEmail());

        mockMvc.perform(delete("/api/movies/" + id).header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createMovie_withInvalidData_returns400WithFieldErrors() throws Exception {
        Map<String, Object> invalid = moviePayload("", "Drama", -1, "-5.00");
        invalid.put("rating", 11.0);
        invalid.put("duration", 0);

        mockMvc.perform(post("/api/movies")
                        .header("Authorization", bearer(adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.title").exists())
                .andExpect(jsonPath("$.validationErrors.rating").exists())
                .andExpect(jsonPath("$.validationErrors.duration").exists())
                .andExpect(jsonPath("$.validationErrors.ticketPrice").exists())
                .andExpect(jsonPath("$.validationErrors.availableSeats").exists());
    }

    @Test
    void createMovie_withMalformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/movies")
                        .header("Authorization", bearer(adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ this is not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMovies_canBeFilteredByGenreAndSearchedByTitle() throws Exception {
        String token = adminToken();
        String uniqueGenre = "Genre" + UUID.randomUUID().toString().substring(0, 8);
        String uniqueTitle = "Searchable" + UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(post("/api/movies")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(moviePayload(uniqueTitle, uniqueGenre, 10, "100.00"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/movies").param("genre", uniqueGenre.toUpperCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value(uniqueTitle));

        mockMvc.perform(get("/api/movies/search").param("title", uniqueTitle.substring(0, 12).toLowerCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(uniqueTitle));
    }
}
