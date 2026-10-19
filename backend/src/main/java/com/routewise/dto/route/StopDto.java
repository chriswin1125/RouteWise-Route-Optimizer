package com.routewise.dto.route;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StopDto(
        Long id,
        Long deliveryOrderId,
        Integer sequence,

        @NotBlank(message = "Stop address is required")
        String address,

        @NotNull(message = "Latitude is required")
        @Min(value = -90, message = "Latitude must be >= -90")
        @Max(value = 90, message = "Latitude must be <= 90")
        Double latitude,

        @NotNull(message = "Longitude is required")
        @Min(value = -180, message = "Longitude must be >= -180")
        @Max(value = 180, message = "Longitude must be <= 180")
        Double longitude,

        String customerName,
        String contactPhone,
        Double distanceToNextKm,
        Double durationToNextMin
) {
    public StopDto withSequence(int newSequence) {
        return new StopDto(id, deliveryOrderId, newSequence, address, latitude, longitude, customerName, contactPhone, distanceToNextKm, durationToNextMin);
    }

    public StopDto withMetrics(Double distance, Double duration) {
        return new StopDto(id, deliveryOrderId, sequence, address, latitude, longitude, customerName, contactPhone, distance, duration);
    }
}
