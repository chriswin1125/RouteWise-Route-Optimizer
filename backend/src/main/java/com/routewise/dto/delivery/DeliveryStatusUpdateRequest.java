package com.routewise.dto.delivery;

import com.routewise.entity.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

public record DeliveryStatusUpdateRequest(
        @NotNull(message = "Status is required")
        DeliveryStatus status
) {}
