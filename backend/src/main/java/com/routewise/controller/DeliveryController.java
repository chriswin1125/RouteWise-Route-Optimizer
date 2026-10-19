package com.routewise.controller;

import com.routewise.dto.common.PagedResponse;
import com.routewise.dto.delivery.DeliveryOrderRequest;
import com.routewise.dto.delivery.DeliveryOrderResponse;
import com.routewise.dto.delivery.DeliveryStatusUpdateRequest;
import com.routewise.entity.DeliveryPriority;
import com.routewise.entity.DeliveryStatus;
import com.routewise.security.UserPrincipal;
import com.routewise.service.DeliveryService;
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

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@Tag(name = "Deliveries", description = "Endpoints for managing delivery orders, priorities, and assignments")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping
    @Operation(summary = "Create a new delivery order")
    public ResponseEntity<DeliveryOrderResponse> createDelivery(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DeliveryOrderRequest request
    ) {
        DeliveryOrderResponse response = deliveryService.createDelivery(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List and search delivery orders with filtering and pagination")
    public ResponseEntity<PagedResponse<DeliveryOrderResponse>> getDeliveries(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) DeliveryStatus status,
            @RequestParam(required = false) DeliveryPriority priority,
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
        PagedResponse<DeliveryOrderResponse> response = deliveryService.getDeliveries(principal.getId(), status, priority, search, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all unassigned pending deliveries for current user")
    public ResponseEntity<List<DeliveryOrderResponse>> getPendingDeliveries(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<DeliveryOrderResponse> response = deliveryService.getPendingDeliveries(principal.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get delivery order by ID")
    public ResponseEntity<DeliveryOrderResponse> getDeliveryById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        DeliveryOrderResponse response = deliveryService.getDeliveryById(id, principal.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing delivery order")
    public ResponseEntity<DeliveryOrderResponse> updateDelivery(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DeliveryOrderRequest request
    ) {
        DeliveryOrderResponse response = deliveryService.updateDelivery(id, principal.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update status of a delivery order")
    public ResponseEntity<DeliveryOrderResponse> updateStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DeliveryStatusUpdateRequest request
    ) {
        DeliveryOrderResponse response = deliveryService.updateStatus(id, principal.getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a delivery order")
    public ResponseEntity<Void> deleteDelivery(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        deliveryService.deleteDelivery(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
