package com.example.movieticketbooking.dto;

import com.example.movieticketbooking.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/** Safe view of a user: the password hash is never included. */
public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        List<String> roles,
        LocalDateTime createdAt) {

    public static UserResponse from(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .sorted()
                .toList();
        return new UserResponse(user.getId(), user.getName(), user.getEmail(),
                user.getPhone(), roleNames, user.getCreatedAt());
    }
}
