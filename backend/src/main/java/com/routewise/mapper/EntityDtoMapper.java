package com.routewise.mapper;

import com.routewise.dto.auth.UserResponse;
import com.routewise.dto.delivery.DeliveryOrderResponse;
import com.routewise.dto.route.RouteResponse;
import com.routewise.dto.route.StopDto;
import com.routewise.entity.DeliveryOrder;
import com.routewise.entity.Route;
import com.routewise.entity.RouteStop;
import com.routewise.entity.User;
import com.routewise.entity.UserPreference;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityDtoMapper {

    public UserResponse toUserResponse(User user) {
        if (user == null) return null;
        UserPreference pref = user.getPreference();

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getCompanyName(),
                user.getRole(),
                user.getCreatedAt(),
                pref != null ? pref.getDefaultDepotAddress() : null,
                pref != null ? pref.getDefaultDepotLat() : null,
                pref != null ? pref.getDefaultDepotLng() : null,
                pref != null ? pref.getDistanceUnit() : "km",
                pref != null ? pref.getTheme() : "light"
        );
    }

    public DeliveryOrderResponse toDeliveryOrderResponse(DeliveryOrder order) {
        if (order == null) return null;

        Long routeId = order.getAssignedRoute() != null ? order.getAssignedRoute().getId() : null;
        String routeName = order.getAssignedRoute() != null ? order.getAssignedRoute().getName() : null;

        return new DeliveryOrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getCustomerName(),
                order.getAddress(),
                order.getLatitude(),
                order.getLongitude(),
                order.getContactPhone(),
                order.getNotes(),
                order.getPriority(),
                order.getStatus(),
                routeId,
                routeName,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public StopDto toStopDto(RouteStop stop) {
        if (stop == null) return null;
        Long deliveryOrderId = stop.getDeliveryOrder() != null ? stop.getDeliveryOrder().getId() : null;
        return new StopDto(
                stop.getId(),
                deliveryOrderId,
                stop.getStopSequence(),
                stop.getAddress(),
                stop.getLatitude(),
                stop.getLongitude(),
                stop.getCustomerName(),
                stop.getContactPhone(),
                stop.getDistanceToNextKm(),
                stop.getDurationToNextMin()
        );
    }

    public RouteResponse toRouteResponse(Route route) {
        if (route == null) return null;

        List<StopDto> stopDtos = route.getStops() != null
                ? route.getStops().stream().map(this::toStopDto).collect(Collectors.toList())
                : Collections.emptyList();

        return new RouteResponse(
                route.getId(),
                route.getName(),
                route.getDepotAddress(),
                route.getDepotLatitude(),
                route.getDepotLongitude(),
                route.getEndDepotAddress(),
                route.getEndDepotLatitude(),
                route.getEndDepotLongitude(),
                route.getRoundTrip(),
                route.getOptimizationMethod(),
                route.getTotalDistanceKm(),
                route.getEstimatedDurationMin(),
                route.getRouteGeometry(),
                route.getIsEstimated(),
                route.getBaselineDistanceKm(),
                route.getBaselineDurationMin(),
                route.getDistanceSavedKm(),
                route.getSavingsPercentage(),
                stopDtos,
                route.getCreatedAt(),
                route.getUpdatedAt()
        );
    }
}
