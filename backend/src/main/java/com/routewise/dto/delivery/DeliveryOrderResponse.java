package com.routewise.dto.delivery;

import com.routewise.entity.DeliveryPriority;
import com.routewise.entity.DeliveryStatus;
import java.time.OffsetDateTime;

public record DeliveryOrderResponse(
        Long id,
        Long userId,
        String customerName,
        String address,
        Double latitude,
        Double longitude,
        String contactPhone,
        String notes,
        DeliveryPriority priority,
        DeliveryStatus status,
        Long assignedRouteId,
        String assignedRouteName,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
