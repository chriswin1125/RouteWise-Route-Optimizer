package com.routewise.dto.route;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CompareRouteRequest(
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

        @NotEmpty(message = "At least two delivery stops are required for comparison")
        @Valid
        List<StopDto> stops
) {
    public boolean isRoundTrip() {
        return roundTrip == null || roundTrip;
    }
}
