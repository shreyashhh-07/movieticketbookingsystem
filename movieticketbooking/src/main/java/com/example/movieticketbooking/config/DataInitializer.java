package com.example.movieticketbooking.config;

import com.example.movieticketbooking.entity.Movie;
import com.example.movieticketbooking.entity.Role;
import com.example.movieticketbooking.entity.User;
import com.example.movieticketbooking.enums.RoleName;
import com.example.movieticketbooking.repository.MovieRepository;
import com.example.movieticketbooking.repository.RoleRepository;
import com.example.movieticketbooking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Runs at start-up: creates the two roles, a default admin account and a few sample movies. */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminName;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           MovieRepository movieRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.name}") String adminName,
                           @Value("${app.admin.email}") String adminEmail,
                           @Value("${app.admin.password}") String adminPassword) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        Role userRole = findOrCreateRole(RoleName.ROLE_USER);
        Role adminRole = findOrCreateRole(RoleName.ROLE_ADMIN);
        createAdminIfMissing(userRole, adminRole);
        createSampleMoviesIfEmpty();
    }

    private Role findOrCreateRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }

    private void createAdminIfMissing(Role userRole, Role adminRole) {
        String email = adminEmail.trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User admin = new User();
        admin.setName(adminName);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.getRoles().add(userRole);
        admin.getRoles().add(adminRole);
        userRepository.save(admin);
        log.info("Default admin account created: {}", email);
    }

    private void createSampleMoviesIfEmpty() {
        if (movieRepository.count() > 0) {
            return;
        }
        movieRepository.saveAll(List.of(
                sampleMovie("Inception", "Sci-Fi", "English", 148, 8.8, "250.00",
                        "A thief who steals secrets from dreams gets a chance to erase his past.",
                        LocalDate.of(2010, 7, 16), 100),
                sampleMovie("3 Idiots", "Comedy", "Hindi", 170, 8.4, "200.00",
                        "Three engineering students challenge the pressure-filled education system.",
                        LocalDate.of(2009, 12, 25), 120),
                sampleMovie("Baahubali: The Beginning", "Action", "Telugu", 159, 8.0, "220.00",
                        "A young man discovers his royal lineage and fights to reclaim a kingdom.",
                        LocalDate.of(2015, 7, 10), 150)));
        log.info("Sample movies inserted");
    }

    private Movie sampleMovie(String title, String genre, String language, int duration, double rating,
                              String ticketPrice, String description, LocalDate releaseDate, int seats) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setGenre(genre);
        movie.setLanguage(language);
        movie.setDuration(duration);
        movie.setRating(rating);
        movie.setTicketPrice(new BigDecimal(ticketPrice));
        movie.setDescription(description);
        movie.setReleaseDate(releaseDate);
        movie.setAvailableSeats(seats);
        return movie;
    }
}
