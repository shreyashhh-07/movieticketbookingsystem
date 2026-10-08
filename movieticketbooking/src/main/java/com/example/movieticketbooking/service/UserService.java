package com.example.movieticketbooking.service;

import com.example.movieticketbooking.dto.UpdateProfileRequest;
import com.example.movieticketbooking.dto.UserResponse;
import com.example.movieticketbooking.entity.User;
import com.example.movieticketbooking.exception.ResourceInUseException;
import com.example.movieticketbooking.exception.ResourceNotFoundException;
import com.example.movieticketbooking.repository.BookingRepository;
import com.example.movieticketbooking.repository.UserRepository;
import com.example.movieticketbooking.security.AuthUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public UserService(UserRepository userRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Authentication authentication) {
        return UserResponse.from(findByEmail(authentication.getName()));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id, Authentication authentication) {
        User user = findById(id);
        ensureSelfOrAdmin(user, authentication);
        return UserResponse.from(user);
    }

    public UserResponse updateCurrentUser(UpdateProfileRequest request, Authentication authentication) {
        return applyUpdate(findByEmail(authentication.getName()), request);
    }

    public UserResponse updateUser(Long id, UpdateProfileRequest request, Authentication authentication) {
        User user = findById(id);
        ensureSelfOrAdmin(user, authentication);
        return applyUpdate(user, request);
    }

    public void deleteUser(Long id) {
        User user = findById(id);
        if (bookingRepository.existsByUserId(id)) {
            throw new ResourceInUseException("User with id " + id + " has bookings and cannot be deleted");
        }
        userRepository.delete(user);
    }

    private UserResponse applyUpdate(User user, UpdateProfileRequest request) {
        user.setName(request.name().trim());
        user.setPhone(request.phone());
        return UserResponse.from(userRepository.save(user));
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found"));
    }

    private void ensureSelfOrAdmin(User user, Authentication authentication) {
        boolean isOwner = user.getEmail().equals(authentication.getName());
        if (!isOwner && !AuthUtils.isAdmin(authentication)) {
            throw new AccessDeniedException("You can only access your own profile");
        }
    }
}
