package com.routewise.dto.route;

public record AlternativeRouteDto(
        String algorithmName,
        String title,
        Double totalDistanceKm,
        Double estimatedDurationMin,
        Double savingsPercentage,
        String routeGeometry,
        String description
) {}
