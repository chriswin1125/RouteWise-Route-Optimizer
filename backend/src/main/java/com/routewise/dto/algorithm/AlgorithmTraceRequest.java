package com.routewise.dto.algorithm;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record AlgorithmTraceRequest(
        String graphPreset, // e.g., "SAMPLE_LOGISTICS_NETWORK", "COMPLEX_GRID", "CUSTOM"
        GraphDto customGraph,

        @NotBlank(message = "Source node is required")
        String sourceNode,

        @NotBlank(message = "Destination node is required")
        String destinationNode,

        String algorithm // "DIJKSTRA" or "ASTAR"
) {
    public String resolvedAlgorithm() {
        return (algorithm != null && !algorithm.isBlank()) ? algorithm.toUpperCase() : "DIJKSTRA";
    }
}
