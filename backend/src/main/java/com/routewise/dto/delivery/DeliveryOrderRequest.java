package com.routewise.dto.delivery;

import com.routewise.entity.DeliveryPriority;
import com.routewise.entity.DeliveryStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeliveryOrderRequest(
        @NotBlank(message = "Customer name is required")
        String customerName,

        @NotBlank(message = "Address is required")
        String address,

        @NotNull(message = "Latitude is required")
        @Min(value = -90, message = "Latitude must be >= -90")
        @Max(value = 90, message = "Latitude must be <= 90")
        Double latitude,

        @NotNull(message = "Longitude is required")
        @Min(value = -180, message = "Longitude must be >= -180")
        @Max(value = 180, message = "Longitude must be <= 180")
        Double longitude,

        String contactPhone,
        String notes,
        DeliveryPriority priority,
        DeliveryStatus status
) {}
