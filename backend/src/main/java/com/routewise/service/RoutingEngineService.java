package com.routewise.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routewise.algorithm.HeldKarpTsp;
import com.routewise.algorithm.TwoOptOptimizer;
import com.routewise.dto.route.*;
import com.routewise.integration.OsrmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoutingEngineService {

    private static final Logger log = LoggerFactory.getLogger(RoutingEngineService.class);

    private final OsrmClient osrmClient;
    private final TwoOptOptimizer twoOptOptimizer;
    private final HeldKarpTsp heldKarpTsp;
    private final ObjectMapper objectMapper;
    private final int exactMaxStops;

    public RoutingEngineService(
            OsrmClient osrmClient,
            ObjectMapper objectMapper,
            @Value("${routewise.routing.exact-algorithm-max-stops:12}") int exactMaxStops
    ) {
        this.osrmClient = osrmClient;
        this.twoOptOptimizer = new TwoOptOptimizer();
        this.heldKarpTsp = new HeldKarpTsp();
        this.objectMapper = objectMapper;
        this.exactMaxStops = exactMaxStops;
    }

    public RouteResponse optimizeRoute(OptimizeRouteRequest request) {
        boolean roundTrip = request.isRoundTrip();
        boolean hasFixedEndDepot = request.endDepotLatitude() != null && request.endDepotLongitude() != null;
        List<StopDto> inputStops = request.stops();
        int n = inputStops.size();

        // Build list of coordinates
        List<OsrmClient.Coordinate> allPoints = new ArrayList<>();
        // Index 0: Start Depot
        allPoints.add(new OsrmClient.Coordinate(request.depotLatitude(), request.depotLongitude()));

        // Indices 1..N: Delivery stops
        for (StopDto s : inputStops) {
            allPoints.add(new OsrmClient.Coordinate(s.latitude(), s.longitude()));
        }

        // Index N+1: End Depot (if specified)
        if (hasFixedEndDepot) {
            allPoints.add(new OsrmClient.Coordinate(request.endDepotLatitude(), request.endDepotLongitude()));
        }

        // Fetch Distance Matrix
        OsrmClient.DistanceMatrixResult matrixResult = osrmClient.getDistanceMatrix(allPoints);
        double[][] distMatrix = matrixResult.distanceMatrixKm();
        double[][] durationMatrix = matrixResult.durationMatrixMin();

        // 1. Calculate Baseline Route (unoptimized sequence: 0 -> 1 -> 2 -> ... -> N -> end)
        List<Integer> baselineTour = new ArrayList<>();
        baselineTour.add(0);
        for (int i = 1; i <= n; i++) {
            baselineTour.add(i);
        }
        if (hasFixedEndDepot) {
            baselineTour.add(n + 1);
        } else if (roundTrip) {
            baselineTour.add(0);
        }

        double baselineDistance = TwoOptOptimizer.calculateTourDistance(baselineTour, distMatrix);
        double baselineDuration = TwoOptOptimizer.calculateTourDistance(baselineTour, durationMatrix);

        // 2. Perform Optimization
        String requestedAlgorithm = request.resolvedAlgorithm();
        String actualAlgorithmUsed = requestedAlgorithm;
        List<Integer> optimizedTour;
        double optimizedDistance;

        if ("EXACT_HELD_KARP".equalsIgnoreCase(requestedAlgorithm) && n <= exactMaxStops) {
            try {
                HeldKarpTsp.ExactTspResult exactResult = heldKarpTsp.solve(distMatrix, n, roundTrip, hasFixedEndDepot);
                optimizedTour = exactResult.optimalTour();
                optimizedDistance = exactResult.optimalDistance();
                actualAlgorithmUsed = "EXACT_HELD_KARP";
            } catch (Exception ex) {
                log.warn("Held-Karp exact solver failed or exceeded threshold, falling back to 2-Opt: {}", ex.getMessage());
                TwoOptOptimizer.OptimizationResult heuristicResult = twoOptOptimizer.optimize(distMatrix, n, roundTrip, hasFixedEndDepot);
                optimizedTour = heuristicResult.optimizedTour();
                optimizedDistance = heuristicResult.optimizedDistance();
                actualAlgorithmUsed = "HEURISTIC_2OPT (Fallback from Exact)";
            }
        } else {
            TwoOptOptimizer.OptimizationResult heuristicResult = twoOptOptimizer.optimize(distMatrix, n, roundTrip, hasFixedEndDepot);
            optimizedTour = heuristicResult.optimizedTour();
            optimizedDistance = heuristicResult.optimizedDistance();
            actualAlgorithmUsed = "HEURISTIC_2OPT";
        }

        // 3. Reconstruct ordered stops with leg distances and durations
        List<StopDto> orderedStops = new ArrayList<>();
        List<OsrmClient.Coordinate> orderedCoordinates = new ArrayList<>();
        orderedCoordinates.add(allPoints.get(0)); // Depot

        int stopSequence = 1;
        for (int i = 1; i < optimizedTour.size(); i++) {
            int pointIndex = optimizedTour.get(i);
            int prevIndex = optimizedTour.get(i - 1);

            double legDistance = distMatrix[prevIndex][pointIndex];
            double legDuration = durationMatrix[prevIndex][pointIndex];

            if (pointIndex >= 1 && pointIndex <= n) {
                StopDto originalStop = inputStops.get(pointIndex - 1);
                StopDto updatedStop = originalStop.withSequence(stopSequence++)
                        .withMetrics(legDistance, legDuration);
                orderedStops.add(updatedStop);
                orderedCoordinates.add(allPoints.get(pointIndex));
            } else {
                // Returned to depot or fixed end
                orderedCoordinates.add(allPoints.get(pointIndex));
            }
        }

        // 4. Retrieve road network route geometry for ordered path
        OsrmClient.RoadRoutingResult roadResult = osrmClient.getRouteGeometry(orderedCoordinates);

        // Prefer actual road distance from geometry if available, else matrix sum
        double totalDistance = roadResult.distanceKm() > 0 ? roadResult.distanceKm() : optimizedDistance;
        double totalDuration = roadResult.durationMinutes() > 0 ? roadResult.durationMinutes()
                : TwoOptOptimizer.calculateTourDistance(optimizedTour, durationMatrix);

        double distanceSaved = Math.max(0.0, baselineDistance - totalDistance);
        double savingsPercentage = baselineDistance > 0.0 ? (distanceSaved / baselineDistance) * 100.0 : 0.0;

        String routeName = request.name() != null && !request.name().isBlank() ? request.name() : "Optimized Route";

        return new RouteResponse(
                null,
                routeName,
                request.depotAddress(),
                request.depotLatitude(),
                request.depotLongitude(),
                request.endDepotAddress(),
                request.endDepotLatitude(),
                request.endDepotLongitude(),
                roundTrip,
                actualAlgorithmUsed,
                roundToTwoDecimals(totalDistance),
                roundToTwoDecimals(totalDuration),
                roadResult.geometryGeoJson(),
                roadResult.isEstimated() || matrixResult.isEstimated(),
                roundToTwoDecimals(baselineDistance),
                roundToTwoDecimals(baselineDuration),
                roundToTwoDecimals(distanceSaved),
                roundToTwoDecimals(savingsPercentage),
                orderedStops,
                null,
                null
        );
    }

    public RouteComparisonResponse compareRoutes(CompareRouteRequest request) {
        // Compute baseline (as entered order)
        OptimizeRouteRequest baselineRequest = new OptimizeRouteRequest(
                "Baseline Route (Original Order)",
                request.depotAddress(),
                request.depotLatitude(),
                request.depotLongitude(),
                request.endDepotAddress(),
                request.endDepotLatitude(),
                request.endDepotLongitude(),
                request.roundTrip(),
                "BASELINE",
                request.stops()
        );

        // Optimize using 2-Opt
        OptimizeRouteRequest opt2OptRequest = new OptimizeRouteRequest(
                "2-Opt Heuristic Optimized",
                request.depotAddress(),
                request.depotLatitude(),
                request.depotLongitude(),
                request.endDepotAddress(),
                request.endDepotLatitude(),
                request.endDepotLongitude(),
                request.roundTrip(),
                "HEURISTIC_2OPT",
                request.stops()
        );

        RouteResponse optimized2Opt = optimizeRoute(opt2OptRequest);

        // Build baseline RouteResponse directly using baseline geometry
        List<OsrmClient.Coordinate> baselineCoordinates = new ArrayList<>();
        baselineCoordinates.add(new OsrmClient.Coordinate(request.depotLatitude(), request.depotLongitude()));
        for (StopDto s : request.stops()) {
            baselineCoordinates.add(new OsrmClient.Coordinate(s.latitude(), s.longitude()));
        }
        if (request.endDepotLatitude() != null && request.endDepotLongitude() != null) {
            baselineCoordinates.add(new OsrmClient.Coordinate(request.endDepotLatitude(), request.endDepotLongitude()));
        } else if (request.isRoundTrip()) {
            baselineCoordinates.add(new OsrmClient.Coordinate(request.depotLatitude(), request.depotLongitude()));
        }

        OsrmClient.RoadRoutingResult baselineRoad = osrmClient.getRouteGeometry(baselineCoordinates);
        double baselineDistance = optimized2Opt.baselineDistanceKm() != null ? optimized2Opt.baselineDistanceKm() : baselineRoad.distanceKm();
        double baselineDuration = optimized2Opt.baselineDurationMin() != null ? optimized2Opt.baselineDurationMin() : baselineRoad.durationMinutes();

        RouteResponse baselineResponse = new RouteResponse(
                null,
                "Baseline Route (Original Sequence)",
                request.depotAddress(),
                request.depotLatitude(),
                request.depotLongitude(),
                request.endDepotAddress(),
                request.endDepotLatitude(),
                request.endDepotLongitude(),
                request.roundTrip(),
                "BASELINE_ORIGINAL",
                roundToTwoDecimals(baselineDistance),
                roundToTwoDecimals(baselineDuration),
                baselineRoad.geometryGeoJson(),
                baselineRoad.isEstimated(),
                roundToTwoDecimals(baselineDistance),
                roundToTwoDecimals(baselineDuration),
                0.0,
                0.0,
                request.stops(),
                null,
                null
        );

        double distanceDiff = Math.max(0.0, baselineDistance - optimized2Opt.totalDistanceKm());
        double durationDiff = Math.max(0.0, baselineDuration - optimized2Opt.estimatedDurationMin());
        double savingsPct = baselineDistance > 0 ? (distanceDiff / baselineDistance) * 100.0 : 0.0;

        List<AlternativeRouteDto> alternatives = new ArrayList<>();
        alternatives.add(new AlternativeRouteDto(
                "HEURISTIC_2OPT",
                "2-Opt Heuristic Optimization",
                optimized2Opt.totalDistanceKm(),
                optimized2Opt.estimatedDurationMin(),
                savingsPct,
                optimized2Opt.routeGeometry(),
                "Iteratively removes crossing paths by reversing tour segments. Scales well for 50+ delivery stops."
        ));

        if (request.stops().size() <= exactMaxStops) {
            try {
                OptimizeRouteRequest exactRequest = new OptimizeRouteRequest(
                        "Held-Karp Exact TSP",
                        request.depotAddress(),
                        request.depotLatitude(),
                        request.depotLongitude(),
                        request.endDepotAddress(),
                        request.endDepotLatitude(),
                        request.endDepotLongitude(),
                        request.roundTrip(),
                        "EXACT_HELD_KARP",
                        request.stops()
                );
                RouteResponse exactRoute = optimizeRoute(exactRequest);
                double exactSavings = baselineDistance > 0 ? Math.max(0.0, (baselineDistance - exactRoute.totalDistanceKm()) / baselineDistance * 100.0) : 0.0;
                alternatives.add(new AlternativeRouteDto(
                        "EXACT_HELD_KARP",
                        "Held-Karp Dynamic Programming (Exact Global Optimum)",
                        exactRoute.totalDistanceKm(),
                        exactRoute.estimatedDurationMin(),
                        roundToTwoDecimals(exactSavings),
                        exactRoute.routeGeometry(),
                        "Solves the TSP to proven mathematical optimality using bitmask dynamic programming. Ideal for <= 12 stops."
                ));
            } catch (Exception ex) {
                log.warn("Exact Held-Karp comparison skipped: {}", ex.getMessage());
            }
        }

        String explanation = "Multi-stop route optimization solves the Traveling Salesperson Problem (TSP) by finding the optimal "
                + "visitation permutation across all stops. In contrast, shortest-path algorithms (such as Dijkstra or A*) "
                + "only find the minimum path between a single origin and a single destination. RouteWise combines both: "
                + "using road routing engines (or Dijkstra/A* on road graphs) for leg distances, and 2-Opt / Held-Karp to optimize the global sequence.";

        return new RouteComparisonResponse(
                baselineResponse,
                optimized2Opt,
                roundToTwoDecimals(distanceDiff),
                roundToTwoDecimals(durationDiff),
                roundToTwoDecimals(savingsPct),
                explanation,
                alternatives
        );
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
