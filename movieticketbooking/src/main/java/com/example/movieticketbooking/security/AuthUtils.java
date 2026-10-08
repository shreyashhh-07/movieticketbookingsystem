package com.example.movieticketbooking.security;

import com.example.movieticketbooking.enums.RoleName;
import org.springframework.security.core.Authentication;

public final class AuthUtils {

    private AuthUtils() {
    }

    public static boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> RoleName.ROLE_ADMIN.name().equals(authority.getAuthority()));
    }
}
