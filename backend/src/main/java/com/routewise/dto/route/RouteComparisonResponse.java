package com.routewise.dto.route;

import java.util.List;

public record RouteComparisonResponse(
        RouteResponse baselineRoute,
        RouteResponse optimizedRoute,
        Double distanceDifferenceKm,
        Double durationDifferenceMin,
        Double savingsPercentage,
        String comparisonExplanation,
        List<AlternativeRouteDto> alternatives
) {}
