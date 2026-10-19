package com.routewise.controller;

import com.routewise.dto.common.PagedResponse;
import com.routewise.dto.route.*;
import com.routewise.security.UserPrincipal;
import com.routewise.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
@Tag(name = "Routes", description = "Endpoints for route optimization, comparison, persistence, and shortest paths")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping("/optimize")
    @Operation(summary = "Calculate an optimized multi-stop delivery route")
    public ResponseEntity<RouteResponse> optimizeRoute(@Valid @RequestBody OptimizeRouteRequest request) {
        RouteResponse response = routeService.optimize(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/compare")
    @Operation(summary = "Compare baseline order vs optimized routes with distance and duration metrics")
    public ResponseEntity<RouteComparisonResponse> compareRoutes(@Valid @RequestBody CompareRouteRequest request) {
        RouteComparisonResponse response = routeService.compare(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/shortest-path")
    @Operation(summary = "Calculate point-to-point shortest path using Dijkstra or A*")
    public ResponseEntity<RouteResponse> shortestPath(@Valid @RequestBody ShortestPathRequest request) {
        RouteResponse response = routeService.calculateShortestPath(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Save an optimized route to the user's permanent history")
    public ResponseEntity<RouteResponse> saveRoute(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RouteSaveRequest request
    ) {
        RouteResponse response = routeService.saveRoute(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get list of saved routes for current user with pagination and search")
    public ResponseEntity<PagedResponse<RouteResponse>> getUserRoutes(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String property = sortParts[0].trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));
        PagedResponse<RouteResponse> response = routeService.getUserRoutes(principal.getId(), search, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed information for a saved route")
    public ResponseEntity<RouteResponse> getRouteById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        RouteResponse response = routeService.getRouteById(id, principal.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing saved route")
    public ResponseEntity<RouteResponse> updateRoute(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RouteSaveRequest request
    ) {
        RouteResponse response = routeService.updateRoute(id, principal.getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a saved route")
    public ResponseEntity<Void> deleteRoute(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        routeService.deleteRoute(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
