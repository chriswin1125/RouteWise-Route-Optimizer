package com.routewise.dto.route;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OptimizeRouteRequest(
        String name,

        @NotNull(message = "Depot address is required")
        String depotAddress,

        @NotNull(message = "Depot latitude is required")
        Double depotLatitude,

        @NotNull(message = "Depot longitude is required")
        Double depotLongitude,

        String endDepotAddress,
        Double endDepotLatitude,
        Double endDepotLongitude,

        Boolean roundTrip,

        String algorithm, // "HEURISTIC_2OPT" or "EXACT_HELD_KARP"

        @NotEmpty(message = "At least one delivery stop is required")
        @Valid
        List<StopDto> stops
) {
    public boolean isRoundTrip() {
        return roundTrip == null || roundTrip;
    }

    public String resolvedAlgorithm() {
        return algorithm != null && !algorithm.isBlank() ? algorithm : "HEURISTIC_2OPT";
    }
}
