package com.routewise.dto.algorithm;

import java.util.List;

public record GraphDto(
        List<GraphNodeDto> nodes,
        List<GraphEdgeDto> edges
) {
    public record GraphNodeDto(
            String id,
            String label,
            double x,
            double y
    ) {}

    public record GraphEdgeDto(
            String id,
            String source,
            String target,
            double weight,
            boolean directed
    ) {}
}
