package com.routewise.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OsrmClientTest {

    private OsrmClient osrmClient;

    @BeforeEach
    void setUp() {
        // Point to unreachable localhost port to force fallback handling
        osrmClient = new OsrmClient(
                new RestTemplateBuilder(),
                new ObjectMapper(),
                "http://127.0.0.1:54321", // non-existent server
                500 // 500ms timeout
        );
    }

    @Test
    @DisplayName("Should fall back to Haversine straight-line distance matrix when OSRM is unreachable")
    void testOsrmFallbackDistanceMatrix() {
        List<OsrmClient.Coordinate> coords = List.of(
                new OsrmClient.Coordinate(37.7749, -122.4194),
                new OsrmClient.Coordinate(37.7885, -122.3995)
        );

        OsrmClient.DistanceMatrixResult result = osrmClient.getDistanceMatrix(coords);

        assertNotNull(result);
        assertTrue(result.isEstimated(), "Should be marked as estimated when fallback is used");
        assertEquals("HAVERSINE_STRAIGHT_LINE_FALLBACK", result.routingSource());
        assertEquals(2, result.distanceMatrixKm().length);
        assertEquals(0.0, result.distanceMatrixKm()[0][0], 1e-6);
        assertTrue(result.distanceMatrixKm()[0][1] > 0.0);
    }

    @Test
    @DisplayName("Should fall back to straight-line geometry when OSRM Route service is unreachable")
    void testOsrmFallbackRouteGeometry() {
        List<OsrmClient.Coordinate> coords = List.of(
                new OsrmClient.Coordinate(37.7749, -122.4194),
                new OsrmClient.Coordinate(37.7885, -122.3995)
        );

        OsrmClient.RoadRoutingResult result = osrmClient.getRouteGeometry(coords);

        assertNotNull(result);
        assertTrue(result.isEstimated());
        assertEquals("HAVERSINE_STRAIGHT_LINE_FALLBACK", result.routingSource());
        assertTrue(result.distanceKm() > 0.0);
        assertTrue(result.durationMinutes() > 0.0);
        assertNotNull(result.geometryGeoJson());
    }
}
