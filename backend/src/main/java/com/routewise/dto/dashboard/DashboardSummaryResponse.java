package com.routewise.dto.dashboard;

import com.routewise.dto.route.RouteResponse;
import java.util.List;
import java.util.Map;

public record DashboardSummaryResponse(
        long totalDeliveries,
        long pendingDeliveries,
        long inTransitDeliveries,
        long completedDeliveries,
        long routesOptimized,
        double totalDistanceSavedKm,
        double averageRouteEfficiencyPercent,
        List<RouteResponse> recentRoutes,
        Map<String, Long> deliveriesByStatus,
        Map<String, Long> deliveriesByPriority
) {}
