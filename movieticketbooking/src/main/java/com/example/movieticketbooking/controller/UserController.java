package com.example.movieticketbooking.controller;

import com.example.movieticketbooking.dto.UpdateProfileRequest;
import com.example.movieticketbooking.dto.UserResponse;
import com.example.movieticketbooking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Profile management. A user can read/update only their own profile; DELETE is admin-only. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getCurrentUser(authentication));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request,
                                                        Authentication authentication) {
        return ResponseEntity.ok(userService.updateCurrentUser(request, authentication));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(userService.getUserById(id, authentication));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateProfileRequest request,
                                                   Authentication authentication) {
        return ResponseEntity.ok(userService.updateUser(id, request, authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
