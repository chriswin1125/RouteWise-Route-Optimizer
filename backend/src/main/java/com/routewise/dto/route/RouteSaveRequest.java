package com.routewise.dto.route;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RouteSaveRequest(
        @NotBlank(message = "Route name is required")
        String name,

        @NotBlank(message = "Depot address is required")
        String depotAddress,

        @NotNull(message = "Depot latitude is required")
        Double depotLatitude,

        @NotNull(message = "Depot longitude is required")
        Double depotLongitude,

        String endDepotAddress,
        Double endDepotLatitude,
        Double endDepotLongitude,

        Boolean roundTrip,

        @NotBlank(message = "Optimization method is required")
        String optimizationMethod,

        @NotNull(message = "Total distance is required")
        Double totalDistanceKm,

        @NotNull(message = "Estimated duration is required")
        Double estimatedDurationMin,

        String routeGeometry,
        Boolean isEstimated,
        Double baselineDistanceKm,
        Double baselineDurationMin,
        Double distanceSavedKm,
        Double savingsPercentage,

        @NotEmpty(message = "At least one stop is required")
        @Valid
        List<StopDto> stops
) {}
