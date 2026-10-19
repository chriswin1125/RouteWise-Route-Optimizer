package com.routewise.controller;

import com.routewise.dto.dashboard.DashboardAnalyticsResponse;
import com.routewise.dto.dashboard.DashboardSummaryResponse;
import com.routewise.security.UserPrincipal;
import com.routewise.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Endpoints for aggregated KPIs, metrics, and visualization analytics")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get high-level summary KPIs (total deliveries, routes optimized, distance saved, recent routes)")
    public ResponseEntity<DashboardSummaryResponse> getSummary(@AuthenticationPrincipal UserPrincipal principal) {
        DashboardSummaryResponse response = dashboardService.getSummary(principal.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get historical analytics time-series and algorithm performance breakdown")
    public ResponseEntity<DashboardAnalyticsResponse> getAnalytics(@AuthenticationPrincipal UserPrincipal principal) {
        DashboardAnalyticsResponse response = dashboardService.getAnalytics(principal.getId());
        return ResponseEntity.ok(response);
    }
}
