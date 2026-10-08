package com.example.movieticketbooking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest extends ApiTestSupport {

    @Test
    void register_createsUserWithoutExposingPassword() throws Exception {
        Map<String, String> body = Map.of("name", "Asha", "email", uniqueEmail(),
                "password", USER_PASSWORD, "phone", "9876543210");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void register_withDuplicateEmail_returns409() throws Exception {
        String email = uniqueEmail();
        registerAndLogin(email);

        Map<String, String> body = Map.of("name", "Again", "email", email, "password", USER_PASSWORD);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));
    }

    @Test
    void register_withInvalidData_returns400WithFieldErrors() throws Exception {
        Map<String, String> body = Map.of("name", "", "email", "not-an-email", "password", "123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    void login_withCorrectCredentials_returnsJwtToken() throws Exception {
        String email = uniqueEmail();
        registerAndLogin(email);

        Map<String, String> body = Map.of("email", email, "password", USER_PASSWORD);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value(email));
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        String email = uniqueEmail();
        registerAndLogin(email);

        Map<String, String> body = Map.of("email", email, "password", "wrong-password");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void protectedEndpoint_withInvalidToken_returns401() throws Exception {
        mockMvc.perform(get("/api/bookings").header("Authorization", "Bearer not.a.real.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoint_asNormalUser_returns403() throws Exception {
        String userToken = registerAndLogin(uniqueEmail());

        mockMvc.perform(get("/api/admin/users").header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void adminEndpoint_asAdmin_returnsUsersWithoutPasswords() throws Exception {
        mockMvc.perform(get("/api/admin/users").header("Authorization", bearer(adminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void userProfile_canBeReadAndUpdatedByOwner() throws Exception {
        String userToken = registerAndLogin(uniqueEmail());

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Updated Name", "phone", "9123456780"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));

        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("9123456780"));
    }
}
