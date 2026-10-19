package com.routewise.dto.route;

import jakarta.validation.constraints.NotNull;

public record ShortestPathRequest(
        @NotNull(message = "Start latitude is required")
        Double startLat,

        @NotNull(message = "Start longitude is required")
        Double startLng,

        String startAddress,

        @NotNull(message = "End latitude is required")
        Double endLat,

        @NotNull(message = "End longitude is required")
        Double endLng,

        String endAddress,

        String algorithm // "DIJKSTRA" or "ASTAR"
) {
    public String resolvedAlgorithm() {
        return (algorithm != null && !algorithm.isBlank()) ? algorithm.toUpperCase() : "DIJKSTRA";
    }
}
