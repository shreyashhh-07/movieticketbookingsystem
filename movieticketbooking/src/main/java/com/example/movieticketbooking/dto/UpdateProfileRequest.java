package com.example.movieticketbooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be exactly 10 digits")
        String phone) {
}
