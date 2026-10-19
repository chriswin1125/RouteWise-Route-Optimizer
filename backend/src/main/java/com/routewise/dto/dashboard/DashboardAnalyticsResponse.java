package com.routewise.dto.dashboard;

import java.util.List;

public record DashboardAnalyticsResponse(
        List<DailySavingsDto> dailySavings,
        List<AlgorithmPerformanceDto> algorithmPerformance,
        List<DistanceDistributionDto> distanceDistribution
) {
    public record DailySavingsDto(
            String date,
            double distanceSavedKm,
            long routesCount
    ) {}

    public record AlgorithmPerformanceDto(
            String algorithm,
            long routeCount,
            double averageSavingsPercentage,
            double averageDurationMin
    ) {}

    public record DistanceDistributionDto(
            String range,
            long count
    ) {}
}
