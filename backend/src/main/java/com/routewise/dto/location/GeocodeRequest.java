package com.routewise.dto.location;

import jakarta.validation.constraints.NotBlank;

public record GeocodeRequest(
        @NotBlank(message = "Address is required")
        String address
) {}
