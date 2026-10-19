package com.routewise.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routewise.algorithm.AStarAlgorithm;
import com.routewise.algorithm.DijkstraAlgorithm;
import com.routewise.algorithm.Graph;
import com.routewise.algorithm.Node;
import com.routewise.dto.common.PagedResponse;
import com.routewise.dto.route.*;
import com.routewise.entity.*;
import com.routewise.exception.ResourceNotFoundException;
import com.routewise.exception.UnauthorizedAccessException;
import com.routewise.integration.OsrmClient;
import com.routewise.mapper.EntityDtoMapper;
import com.routewise.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;
    private final RouteHistoryRepository routeHistoryRepository;
    private final DeliveryOrderRepository deliveryOrderRepository;
    private final UserRepository userRepository;
    private final RoutingEngineService routingEngineService;
    private final OsrmClient osrmClient;
    private final EntityDtoMapper mapper;
    private final ObjectMapper objectMapper;

    public RouteService(
            RouteRepository routeRepository,
            RouteStopRepository routeStopRepository,
            RouteHistoryRepository routeHistoryRepository,
            DeliveryOrderRepository deliveryOrderRepository,
            UserRepository userRepository,
            RoutingEngineService routingEngineService,
            OsrmClient osrmClient,
            EntityDtoMapper mapper,
            ObjectMapper objectMapper
    ) {
        this.routeRepository = routeRepository;
        this.routeStopRepository = routeStopRepository;
        this.routeHistoryRepository = routeHistoryRepository;
        this.deliveryOrderRepository = deliveryOrderRepository;
        this.userRepository = userRepository;
        this.routingEngineService = routingEngineService;
        this.osrmClient = osrmClient;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    public RouteResponse optimize(OptimizeRouteRequest request) {
        return routingEngineService.optimizeRoute(request);
    }

    public RouteComparisonResponse compare(CompareRouteRequest request) {
        return routingEngineService.compareRoutes(request);
    }

    @Transactional
    public RouteResponse saveRoute(Long userId, RouteSaveRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Route route = new Route();
        route.setUser(user);
        route.setName(request.name());
        route.setDepotAddress(request.depotAddress());
        route.setDepotLatitude(request.depotLatitude());
        route.setDepotLongitude(request.depotLongitude());
        route.setEndDepotAddress(request.endDepotAddress());
        route.setEndDepotLatitude(request.endDepotLatitude());
        route.setEndDepotLongitude(request.endDepotLongitude());
        route.setRoundTrip(request.roundTrip() == null || request.roundTrip());
        route.setOptimizationMethod(request.optimizationMethod());
        route.setTotalDistanceKm(request.totalDistanceKm());
        route.setEstimatedDurationMin(request.estimatedDurationMin());
        route.setRouteGeometry(request.routeGeometry());
        route.setIsEstimated(request.isEstimated() != null && request.isEstimated());
        route.setBaselineDistanceKm(request.baselineDistanceKm());
        route.setBaselineDurationMin(request.baselineDurationMin());
        route.setDistanceSavedKm(request.distanceSavedKm() != null ? request.distanceSavedKm() : 0.0);
        route.setSavingsPercentage(request.savingsPercentage() != null ? request.savingsPercentage() : 0.0);

        Route savedRoute = routeRepository.save(route);

        int seq = 1;
        for (StopDto s : request.stops()) {
            RouteStop stop = new RouteStop();
            stop.setRoute(savedRoute);
            stop.setStopSequence(seq++);
            stop.setAddress(s.address());
            stop.setLatitude(s.latitude());
            stop.setLongitude(s.longitude());
            stop.setCustomerName(s.customerName());
            stop.setContactPhone(s.contactPhone());
            stop.setDistanceToNextKm(s.distanceToNextKm());
            stop.setDurationToNextMin(s.durationToNextMin());

            if (s.deliveryOrderId() != null) {
                deliveryOrderRepository.findByIdAndUserId(s.deliveryOrderId(), userId).ifPresent(order -> {
                    stop.setDeliveryOrder(order);
                    order.setAssignedRoute(savedRoute);
                    order.setStatus(DeliveryStatus.ASSIGNED);
                    deliveryOrderRepository.save(order);
                });
            }

            routeStopRepository.save(stop);
            savedRoute.addStop(stop);
        }

        // Record history event
        RouteHistory history = new RouteHistory(
                user,
                savedRoute,
                "CREATED",
                "Route '" + savedRoute.getName() + "' saved with " + request.stops().size() + " stops."
        );
        routeHistoryRepository.save(history);

        return mapper.toRouteResponse(savedRoute);
    }

    @Transactional(readOnly = true)
    public PagedResponse<RouteResponse> getUserRoutes(Long userId, String query, Pageable pageable) {
        Page<Route> page = routeRepository.searchRoutes(userId, query, pageable);
        return PagedResponse.of(page.map(mapper::toRouteResponse));
    }

    @Transactional(readOnly = true)
    public RouteResponse getRouteById(Long routeId, Long userId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + routeId));

        if (!route.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You do not have access to this route");
        }

        return mapper.toRouteResponse(route);
    }

    @Transactional
    public RouteResponse updateRoute(Long routeId, Long userId, RouteSaveRequest request) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + routeId));

        if (!route.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You do not have access to this route");
        }

        route.setName(request.name());
        route.setDepotAddress(request.depotAddress());
        route.setDepotLatitude(request.depotLatitude());
        route.setDepotLongitude(request.depotLongitude());
        route.setEndDepotAddress(request.endDepotAddress());
        route.setEndDepotLatitude(request.endDepotLatitude());
        route.setEndDepotLongitude(request.endDepotLongitude());
        route.setRoundTrip(request.roundTrip() == null || request.roundTrip());
        route.setOptimizationMethod(request.optimizationMethod());
        route.setTotalDistanceKm(request.totalDistanceKm());
        route.setEstimatedDurationMin(request.estimatedDurationMin());
        route.setRouteGeometry(request.routeGeometry());
        route.setIsEstimated(request.isEstimated() != null && request.isEstimated());
        route.setBaselineDistanceKm(request.baselineDistanceKm());
        route.setBaselineDurationMin(request.baselineDurationMin());
        route.setDistanceSavedKm(request.distanceSavedKm() != null ? request.distanceSavedKm() : 0.0);
        route.setSavingsPercentage(request.savingsPercentage() != null ? request.savingsPercentage() : 0.0);

        // Replace stops
        routeStopRepository.deleteByRouteId(route.getId());
        route.getStops().clear();

        int seq = 1;
        for (StopDto s : request.stops()) {
            RouteStop stop = new RouteStop();
            stop.setRoute(route);
            stop.setStopSequence(seq++);
            stop.setAddress(s.address());
            stop.setLatitude(s.latitude());
            stop.setLongitude(s.longitude());
            stop.setCustomerName(s.customerName());
            stop.setContactPhone(s.contactPhone());
            stop.setDistanceToNextKm(s.distanceToNextKm());
            stop.setDurationToNextMin(s.durationToNextMin());

            if (s.deliveryOrderId() != null) {
                deliveryOrderRepository.findByIdAndUserId(s.deliveryOrderId(), userId).ifPresent(order -> {
                    stop.setDeliveryOrder(order);
                    order.setAssignedRoute(route);
                    deliveryOrderRepository.save(order);
                });
            }

            routeStopRepository.save(stop);
            route.addStop(stop);
        }

        Route updated = routeRepository.save(route);

        RouteHistory history = new RouteHistory(
                route.getUser(),
                updated,
                "UPDATED",
                "Route updated with " + request.stops().size() + " stops."
        );
        routeHistoryRepository.save(history);

        return mapper.toRouteResponse(updated);
    }

    @Transactional
    public void deleteRoute(Long routeId, Long userId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + routeId));

        if (!route.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You do not have access to this route");
        }

        // Unlink assigned deliveries
        List<DeliveryOrder> assignedOrders = deliveryOrderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (DeliveryOrder order : assignedOrders) {
            if (order.getAssignedRoute() != null && order.getAssignedRoute().getId().equals(routeId)) {
                order.setAssignedRoute(null);
                if (order.getStatus() == DeliveryStatus.ASSIGNED) {
                    order.setStatus(DeliveryStatus.PENDING);
                }
                deliveryOrderRepository.save(order);
            }
        }

        routeRepository.delete(route);
    }

    public RouteResponse calculateShortestPath(ShortestPathRequest request) {
        List<OsrmClient.Coordinate> coords = List.of(
                new OsrmClient.Coordinate(request.startLat(), request.startLng()),
                new OsrmClient.Coordinate(request.endLat(), request.endLng())
        );

        OsrmClient.RoadRoutingResult roadResult = osrmClient.getRouteGeometry(coords);

        List<StopDto> stops = List.of(
                new StopDto(null, null, 1, request.endAddress() != null ? request.endAddress() : "Destination",
                        request.endLat(), request.endLng(), null, null, roadResult.distanceKm(), roadResult.durationMinutes())
        );

        return new RouteResponse(
                null,
                "Shortest Path (" + request.resolvedAlgorithm() + ")",
                request.startAddress() != null ? request.startAddress() : "Origin",
                request.startLat(),
                request.startLng(),
                request.endAddress() != null ? request.endAddress() : "Destination",
                request.endLat(),
                request.endLng(),
                false,
                request.resolvedAlgorithm(),
                Math.round(roadResult.distanceKm() * 100.0) / 100.0,
                Math.round(roadResult.durationMinutes() * 100.0) / 100.0,
                roadResult.geometryGeoJson(),
                roadResult.isEstimated(),
                Math.round(roadResult.distanceKm() * 100.0) / 100.0,
                Math.round(roadResult.durationMinutes() * 100.0) / 100.0,
                0.0,
                0.0,
                stops,
                null,
                null
        );
    }
}
