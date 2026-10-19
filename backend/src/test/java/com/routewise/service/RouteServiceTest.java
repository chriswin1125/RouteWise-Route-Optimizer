package com.routewise.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routewise.dto.route.RouteResponse;
import com.routewise.dto.route.RouteSaveRequest;
import com.routewise.dto.route.StopDto;
import com.routewise.entity.Route;
import com.routewise.entity.User;
import com.routewise.exception.UnauthorizedAccessException;
import com.routewise.integration.OsrmClient;
import com.routewise.mapper.EntityDtoMapper;
import com.routewise.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private RouteStopRepository routeStopRepository;

    @Mock
    private RouteHistoryRepository routeHistoryRepository;

    @Mock
    private DeliveryOrderRepository deliveryOrderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoutingEngineService routingEngineService;

    @Mock
    private OsrmClient osrmClient;

    private EntityDtoMapper mapper;
    private ObjectMapper objectMapper;
    private RouteService routeService;

    private User user1;
    private User user2;
    private Route route1;

    @BeforeEach
    void setUp() {
        mapper = new EntityDtoMapper();
        objectMapper = new ObjectMapper();
        routeService = new RouteService(
                routeRepository, routeStopRepository, routeHistoryRepository,
                deliveryOrderRepository, userRepository, routingEngineService,
                osrmClient, mapper, objectMapper
        );

        user1 = new User("user1@test.com", "hash", "User One", "Company 1");
        user1.setId(1L);

        user2 = new User("user2@test.com", "hash", "User Two", "Company 2");
        user2.setId(2L);

        route1 = new Route();
        route1.setId(100L);
        route1.setUser(user1);
        route1.setName("Downtown Route");
        route1.setDepotAddress("Hub 1");
        route1.setDepotLatitude(37.78);
        route1.setDepotLongitude(-122.40);
        route1.setTotalDistanceKm(15.5);
        route1.setEstimatedDurationMin(35.0);
        route1.setOptimizationMethod("HEURISTIC_2OPT");
    }

    @Test
    @DisplayName("Should save a route with stops and history entry")
    void testSaveRoute() {
        RouteSaveRequest request = new RouteSaveRequest(
                "Downtown Route", "Hub 1", 37.78, -122.40, null, null, null,
                true, "HEURISTIC_2OPT", 15.5, 35.0, "[]", false,
                20.0, 50.0, 4.5, 22.5,
                List.of(new StopDto(null, null, 1, "Stop 1", 37.79, -122.41, "Cust A", "123", 5.0, 10.0))
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(routeRepository.save(any(Route.class))).thenAnswer(inv -> {
            Route r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });

        RouteResponse response = routeService.saveRoute(1L, request);

        assertNotNull(response);
        assertEquals("Downtown Route", response.name());
        verify(routeRepository, times(1)).save(any(Route.class));
        verify(routeHistoryRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should prevent cross-user route access (route ownership isolation)")
    void testUnauthorizedRouteAccess() {
        when(routeRepository.findById(100L)).thenReturn(Optional.of(route1));

        assertThrows(UnauthorizedAccessException.class, () -> routeService.getRouteById(100L, 2L));
        assertThrows(UnauthorizedAccessException.class, () -> routeService.deleteRoute(100L, 2L));
    }
}
