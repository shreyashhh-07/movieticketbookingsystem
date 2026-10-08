package com.example.movieticketbooking.config;

import com.example.movieticketbooking.security.CustomUserDetailsService;
import com.example.movieticketbooking.security.JwtAuthenticationFilter;
import com.example.movieticketbooking.security.JwtService;
import com.example.movieticketbooking.security.SecurityErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final SecurityErrorHandler securityErrorHandler;

    public SecurityConfig(JwtService jwtService,
                          CustomUserDetailsService userDetailsService,
                          SecurityErrorHandler securityErrorHandler) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.securityErrorHandler = securityErrorHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless JWT API: no browser session or cookie, so CSRF protection is not needed.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler))
                .authorizeHttpRequests(auth -> auth
                        // public
                        .requestMatchers("/api/auth/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/movies", "/api/movies/**").permitAll()
                        // admin only
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/movies").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                        // everything else (bookings, users/me, ...) needs a valid login
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
