package com.routewise.dto.route;

import java.time.OffsetDateTime;
import java.util.List;

public record RouteResponse(
        Long id,
        String name,
        String depotAddress,
        Double depotLatitude,
        Double depotLongitude,
        String endDepotAddress,
        Double endDepotLatitude,
        Double endDepotLongitude,
        Boolean roundTrip,
        String optimizationMethod,
        Double totalDistanceKm,
        Double estimatedDurationMin,
        String routeGeometry,
        Boolean isEstimated,
        Double baselineDistanceKm,
        Double baselineDurationMin,
        Double distanceSavedKm,
        Double savingsPercentage,
        List<StopDto> stops,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
