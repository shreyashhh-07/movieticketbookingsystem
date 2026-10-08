package com.example.movieticketbooking.service;

import com.example.movieticketbooking.dto.AuthResponse;
import com.example.movieticketbooking.dto.LoginRequest;
import com.example.movieticketbooking.dto.RegisterRequest;
import com.example.movieticketbooking.dto.UserResponse;
import com.example.movieticketbooking.entity.Role;
import com.example.movieticketbooking.entity.User;
import com.example.movieticketbooking.enums.RoleName;
import com.example.movieticketbooking.exception.DuplicateResourceException;
import com.example.movieticketbooking.exception.ResourceNotFoundException;
import com.example.movieticketbooking.repository.RoleRepository;
import com.example.movieticketbooking.repository.UserRepository;
import com.example.movieticketbooking.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /** Public registration always creates a normal user (ROLE_USER); admins cannot be self-registered. */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email '" + email + "' is already registered");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("ROLE_USER is missing from the database"));

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.getRoles().add(userRole);

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        // Throws BadCredentialsException (-> 401) when the email or password is wrong.
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return AuthResponse.bearer(jwtService.generateToken(email), UserResponse.from(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
