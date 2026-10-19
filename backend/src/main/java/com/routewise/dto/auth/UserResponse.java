package com.routewise.dto.auth;

import java.time.OffsetDateTime;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        String companyName,
        String role,
        OffsetDateTime createdAt,
        String defaultDepotAddress,
        Double defaultDepotLat,
        Double defaultDepotLng,
        String distanceUnit,
        String theme
) {}
