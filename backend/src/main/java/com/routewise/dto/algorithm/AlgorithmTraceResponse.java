package com.routewise.dto.algorithm;

import java.util.List;
import java.util.Map;

public record AlgorithmTraceResponse(
        String algorithm,
        GraphDto graph,
        String sourceNode,
        String destinationNode,
        boolean pathFound,
        List<String> shortestPath,
        Double totalCost,
        List<TraceStepDto> steps,
        int totalStepsCount,
        long executionTimeNanos,
        AlgorithmComplexityDto complexity
) {
    public record AlgorithmComplexityDto(
            String timeComplexity,
            String spaceComplexity,
            String description
    ) {}
}
