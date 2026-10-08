package com.example.movieticketbooking;

import com.example.movieticketbooking.repository.MovieRepository;
import com.example.movieticketbooking.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class MovieticketbookingApplicationTests {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Test
    void contextLoadsAndDefaultDataIsSeeded() {
        assertEquals(2, roleRepository.count(), "ROLE_USER and ROLE_ADMIN must exist");
        assertTrue(movieRepository.count() >= 3, "sample movies must be inserted");
    }
}
