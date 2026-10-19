package com.routewise.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.routewise.algorithm.HaversineDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Client for interacting with the Open Source Routing Machine (OSRM) REST API.
 * Provides road network distance matrices and route geometries with robust straight-line fallback.
 */
@Component
public class OsrmClient {

    private static final Logger log = LoggerFactory.getLogger(OsrmClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String osrmBaseUrl;

    public record Coordinate(double latitude, double longitude) {}

    public record RoadRoutingResult(
            double distanceKm,
            double durationMinutes,
            String geometryGeoJson,
            boolean isEstimated,
            String routingSource
    ) {}

    public record DistanceMatrixResult(
            double[][] distanceMatrixKm,
            double[][] durationMatrixMin,
            boolean isEstimated,
            String routingSource
    ) {}

    public OsrmClient(
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper,
            @Value("${routewise.routing.osrm-url:https://router.project-osrm.org}") String osrmBaseUrl,
            @Value("${routewise.routing.osrm-timeout-ms:4000}") int timeoutMs
    ) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();
        this.objectMapper = objectMapper;
        this.osrmBaseUrl = osrmBaseUrl.replaceAll("/$", "");
    }

    /**
     * Obtains pairwise road distances and travel times between multiple points.
     * Falls back gracefully to Haversine distance if OSRM service is unavailable.
     */
    public DistanceMatrixResult getDistanceMatrix(List<Coordinate> coordinates) {
        int n = coordinates.size();
        if (n == 0) {
            return new DistanceMatrixResult(new double[0][0], new double[0][0], false, "EMPTY");
        }

        try {
            // Build OSRM table query: /table/v1/driving/{lon1},{lat1};{lon2},{lat2}...
            StringBuilder coordsParam = new StringBuilder();
            for (int i = 0; i < n; i++) {
                if (i > 0) coordsParam.append(";");
                Coordinate c = coordinates.get(i);
                // OSRM expects longitude first, then latitude!
                coordsParam.append(String.format(Locale.US, "%.6f,%.6f", c.longitude(), c.latitude()));
            }

            String url = osrmBaseUrl + "/table/v1/driving/" + coordsParam + "?annotations=distance,duration";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String code = root.path("code").asText("");

                if ("Ok".equalsIgnoreCase(code)) {
                    JsonNode distancesNode = root.path("distances");
                    JsonNode durationsNode = root.path("durations");

                    double[][] distanceMatrix = new double[n][n];
                    double[][] durationMatrix = new double[n][n];

                    for (int i = 0; i < n; i++) {
                        for (int j = 0; j < n; j++) {
                            // OSRM distances are in meters, durations in seconds
                            double meters = distancesNode.get(i).get(j).asDouble(0.0);
                            double seconds = durationsNode.get(i).get(j).asDouble(0.0);
                            distanceMatrix[i][j] = meters / 1000.0; // convert to km
                            durationMatrix[i][j] = seconds / 60.0;  // convert to minutes
                        }
                    }

                    return new DistanceMatrixResult(distanceMatrix, durationMatrix, false, "OSRM_ROAD_NETWORK");
                }
            }
        } catch (Exception ex) {
            log.warn("OSRM Table API unavailable or timed out: {}. Using straight-line Haversine distance matrix fallback.", ex.getMessage());
        }

        // Fallback: Haversine distance matrix
        return computeHaversineMatrix(coordinates);
    }

    /**
     * Retrieves the turn-by-turn road route geometry and total distance/duration for an ordered sequence of coordinates.
     */
    public RoadRoutingResult getRouteGeometry(List<Coordinate> orderedCoordinates) {
        int n = orderedCoordinates.size();
        if (n < 2) {
            return new RoadRoutingResult(0.0, 0.0, "[]", false, "SINGLE_POINT");
        }

        try {
            StringBuilder coordsParam = new StringBuilder();
            for (int i = 0; i < n; i++) {
                if (i > 0) coordsParam.append(";");
                Coordinate c = orderedCoordinates.get(i);
                coordsParam.append(String.format(Locale.US, "%.6f,%.6f", c.longitude(), c.latitude()));
            }

            String url = osrmBaseUrl + "/route/v1/driving/" + coordsParam + "?overview=full&geometries=geojson";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String code = root.path("code").asText("");

                if ("Ok".equalsIgnoreCase(code)) {
                    JsonNode route = root.path("routes").get(0);
                    double distanceMeters = route.path("distance").asDouble();
                    double durationSeconds = route.path("duration").asDouble();
                    JsonNode coordinatesNode = route.path("geometry").path("coordinates");

                    // Convert [lon, lat] pairs from GeoJSON to [[lat, lon], ...] for Leaflet convenience
                    List<List<Double>> leafletCoordinates = new ArrayList<>();
                    if (coordinatesNode.isArray()) {
                        for (JsonNode pt : coordinatesNode) {
                            double lon = pt.get(0).asDouble();
                            double lat = pt.get(1).asDouble();
                            leafletCoordinates.add(List.of(lat, lon));
                        }
                    }

                    String geometryJson = objectMapper.writeValueAsString(leafletCoordinates);
                    return new RoadRoutingResult(
                            distanceMeters / 1000.0,
                            durationSeconds / 60.0,
                            geometryJson,
                            false,
                            "OSRM_ROAD_NETWORK"
                    );
                }
            }
        } catch (Exception ex) {
            log.warn("OSRM Route API unavailable or timed out: {}. Generating straight-line geometry fallback.", ex.getMessage());
        }

        // Fallback: straight-line geometry and Haversine distance
        double totalDistance = 0.0;
        List<List<Double>> fallbackCoordinates = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Coordinate c = orderedCoordinates.get(i);
            fallbackCoordinates.add(List.of(c.latitude(), c.longitude()));
            if (i > 0) {
                Coordinate prev = orderedCoordinates.get(i - 1);
                totalDistance += HaversineDistance.calculateDistanceKm(prev.latitude(), prev.longitude(), c.latitude(), c.longitude());
            }
        }

        double durationMinutes = HaversineDistance.estimateDurationMinutes(totalDistance, 40.0);
        String geometryJson = "[]";
        try {
            geometryJson = objectMapper.writeValueAsString(fallbackCoordinates);
        } catch (Exception ignored) {}

        return new RoadRoutingResult(totalDistance, durationMinutes, geometryJson, true, "HAVERSINE_STRAIGHT_LINE_FALLBACK");
    }

    private DistanceMatrixResult computeHaversineMatrix(List<Coordinate> coordinates) {
        int n = coordinates.size();
        double[][] distances = new double[n][n];
        double[][] durations = new double[n][n];

        for (int i = 0; i < n; i++) {
            Coordinate c1 = coordinates.get(i);
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    distances[i][j] = 0.0;
                    durations[i][j] = 0.0;
                } else {
                    Coordinate c2 = coordinates.get(j);
                    double dist = HaversineDistance.calculateDistanceKm(c1.latitude(), c1.longitude(), c2.latitude(), c2.longitude());
                    distances[i][j] = dist;
                    durations[i][j] = HaversineDistance.estimateDurationMinutes(dist, 40.0);
                }
            }
        }

        return new DistanceMatrixResult(distances, durations, true, "HAVERSINE_STRAIGHT_LINE_FALLBACK");
    }
}
