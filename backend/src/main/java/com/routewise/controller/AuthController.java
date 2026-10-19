package com.routewise.controller;

import com.routewise.dto.auth.AuthResponse;
import com.routewise.dto.auth.LoginRequest;
import com.routewise.dto.auth.RegisterRequest;
import com.routewise.dto.auth.UserResponse;
import com.routewise.security.UserPrincipal;
import com.routewise.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration, authentication, and profile settings")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and receive JWT access token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile and default preferences")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        UserResponse response = authService.getCurrentUser(principal.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/preferences")
    @Operation(summary = "Update user default depot and UI preferences")
    public ResponseEntity<UserResponse> updatePreferences(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> payload
    ) {
        String depotAddress = (String) payload.get("defaultDepotAddress");
        Double depotLat = payload.get("defaultDepotLat") != null ? Double.valueOf(payload.get("defaultDepotLat").toString()) : null;
        Double depotLng = payload.get("defaultDepotLng") != null ? Double.valueOf(payload.get("defaultDepotLng").toString()) : null;
        String distanceUnit = (String) payload.get("distanceUnit");
        String theme = (String) payload.get("theme");

        UserResponse response = authService.updatePreferences(principal.getId(), depotAddress, depotLat, depotLng, distanceUnit, theme);
        return ResponseEntity.ok(response);
    }
}
