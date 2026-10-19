package com.routewise.service;

import com.routewise.dto.dashboard.DashboardAnalyticsResponse;
import com.routewise.dto.dashboard.DashboardSummaryResponse;
import com.routewise.dto.route.RouteResponse;
import com.routewise.entity.DeliveryOrder;
import com.routewise.entity.DeliveryPriority;
import com.routewise.entity.DeliveryStatus;
import com.routewise.entity.Route;
import com.routewise.mapper.EntityDtoMapper;
import com.routewise.repository.DeliveryOrderRepository;
import com.routewise.repository.RouteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final RouteRepository routeRepository;
    private final DeliveryOrderRepository deliveryOrderRepository;
    private final EntityDtoMapper mapper;

    public DashboardService(
            RouteRepository routeRepository,
            DeliveryOrderRepository deliveryOrderRepository,
            EntityDtoMapper mapper
    ) {
        this.routeRepository = routeRepository;
        this.deliveryOrderRepository = deliveryOrderRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(Long userId) {
        List<DeliveryOrder> deliveries = deliveryOrderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        long totalDeliveries = deliveries.size();

        Map<String, Long> statusMap = new HashMap<>();
        for (DeliveryStatus s : DeliveryStatus.values()) {
            statusMap.put(s.name(), 0L);
        }
        for (DeliveryOrder d : deliveries) {
            statusMap.put(d.getStatus().name(), statusMap.get(d.getStatus().name()) + 1L);
        }

        Map<String, Long> priorityMap = new HashMap<>();
        for (DeliveryPriority p : DeliveryPriority.values()) {
            priorityMap.put(p.name(), 0L);
        }
        for (DeliveryOrder d : deliveries) {
            priorityMap.put(d.getPriority().name(), priorityMap.get(d.getPriority().name()) + 1L);
        }

        long routesOptimized = routeRepository.countByUserId(userId);
        Double sumSaved = routeRepository.sumDistanceSavedByUserId(userId);
        Double avgSavings = routeRepository.avgSavingsPercentageByUserId(userId);

        List<RouteResponse> recentRoutes = routeRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 5))
                .getContent()
                .stream()
                .map(mapper::toRouteResponse)
                .collect(Collectors.toList());

        return new DashboardSummaryResponse(
                totalDeliveries,
                statusMap.get(DeliveryStatus.PENDING.name()),
                statusMap.get(DeliveryStatus.IN_TRANSIT.name()),
                statusMap.get(DeliveryStatus.DELIVERED.name()),
                routesOptimized,
                sumSaved != null ? Math.round(sumSaved * 100.0) / 100.0 : 0.0,
                avgSavings != null ? Math.round(avgSavings * 100.0) / 100.0 : 0.0,
                recentRoutes,
                statusMap,
                priorityMap
        );
    }

    @Transactional(readOnly = true)
    public DashboardAnalyticsResponse getAnalytics(Long userId) {
        List<Route> routes = routeRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // Group by day for daily savings (past 7 days)
        Map<String, Double> dailyDistance = new LinkedHashMap<>();
        Map<String, Long> dailyCount = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            String label = d.format(formatter);
            dailyDistance.put(label, 0.0);
            dailyCount.put(label, 0L);
        }

        for (Route r : routes) {
            LocalDate routeDate = r.getCreatedAt().toLocalDate();
            String label = routeDate.format(formatter);
            if (dailyDistance.containsKey(label)) {
                dailyDistance.put(label, dailyDistance.get(label) + (r.getDistanceSavedKm() != null ? r.getDistanceSavedKm() : 0.0));
                dailyCount.put(label, dailyCount.get(label) + 1L);
            }
        }

        List<DashboardAnalyticsResponse.DailySavingsDto> dailySavings = new ArrayList<>();
        dailyDistance.forEach((date, savedKm) -> {
            dailySavings.add(new DashboardAnalyticsResponse.DailySavingsDto(
                    date,
                    Math.round(savedKm * 10.0) / 10.0,
                    dailyCount.get(date)
            ));
        });

        // Group by algorithm performance
        Map<String, List<Route>> byAlgorithm = routes.stream()
                .collect(Collectors.groupingBy(Route::getOptimizationMethod));

        List<DashboardAnalyticsResponse.AlgorithmPerformanceDto> algorithmPerformance = new ArrayList<>();
        byAlgorithm.forEach((algo, list) -> {
            double avgSavings = list.stream()
                    .mapToDouble(r -> r.getSavingsPercentage() != null ? r.getSavingsPercentage() : 0.0)
                    .average()
                    .orElse(0.0);
            double avgDuration = list.stream()
                    .mapToDouble(r -> r.getEstimatedDurationMin() != null ? r.getEstimatedDurationMin() : 0.0)
                    .average()
                    .orElse(0.0);

            algorithmPerformance.add(new DashboardAnalyticsResponse.AlgorithmPerformanceDto(
                    algo,
                    list.size(),
                    Math.round(avgSavings * 10.0) / 10.0,
                    Math.round(avgDuration * 10.0) / 10.0
            ));
        });

        // Distance distribution buckets (< 10km, 10-25km, 25-50km, 50km+)
        long under10 = routes.stream().filter(r -> r.getTotalDistanceKm() < 10).count();
        long between10And25 = routes.stream().filter(r -> r.getTotalDistanceKm() >= 10 && r.getTotalDistanceKm() < 25).count();
        long between25And50 = routes.stream().filter(r -> r.getTotalDistanceKm() >= 25 && r.getTotalDistanceKm() < 50).count();
        long over50 = routes.stream().filter(r -> r.getTotalDistanceKm() >= 50).count();

        List<DashboardAnalyticsResponse.DistanceDistributionDto> distanceDistribution = List.of(
                new DashboardAnalyticsResponse.DistanceDistributionDto("< 10 km", under10),
                new DashboardAnalyticsResponse.DistanceDistributionDto("10 - 25 km", between10And25),
                new DashboardAnalyticsResponse.DistanceDistributionDto("25 - 50 km", between25And50),
                new DashboardAnalyticsResponse.DistanceDistributionDto("50+ km", over50)
        );

        return new DashboardAnalyticsResponse(dailySavings, algorithmPerformance, distanceDistribution);
    }
}
