package com.routewise.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.routewise.dto.location.LocationSearchDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.*;

@Component
public class NominatimClient {

    private static final Logger log = LoggerFactory.getLogger(NominatimClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String nominatimBaseUrl;

    // Built-in curated geographic locations for fast local fallback and testing
    private static final List<LocationSearchDto> CURATED_LOCATIONS = List.of(
            new LocationSearchDto("loc-1", "Central Hub, 500 Howard St, San Francisco, CA", 37.7885, -122.3995, "depot", "San Francisco", "CA", "USA"),
            new LocationSearchDto("loc-2", "Embarcadero Tech Labs, 201 Mission St, San Francisco, CA", 37.7915, -122.3972, "commercial", "San Francisco", "CA", "USA"),
            new LocationSearchDto("loc-3", "Fishermans Wharf Gift Shop, 2801 Leavenworth St, San Francisco, CA", 37.8080, -122.4177, "retail", "San Francisco", "CA", "USA"),
            new LocationSearchDto("loc-4", "Mission District Medical Clinic, 2450 Mission St, San Francisco, CA", 37.7587, -122.4191, "healthcare", "San Francisco", "CA", "USA"),
            new LocationSearchDto("loc-5", "Blue Bottle Coffee Roastery, 315 Linden St, San Francisco, CA", 37.7763, -122.4231, "cafe", "San Francisco", "CA", "USA"),
            new LocationSearchDto("loc-6", "SF General Supplies, 1001 Potrero Ave, San Francisco, CA", 37.7558, -122.4047, "warehouse", "San Francisco", "CA", "USA"),
            new LocationSearchDto("loc-7", "Chinatown Culinary Mart, 744 Washington St, San Francisco, CA", 37.7951, -122.4055, "retail", "San Francisco", "CA", "USA"),
            new LocationSearchDto("loc-8", "Presidio Community Center, 210 Lincoln Blvd, San Francisco, CA", 37.7997, -122.4638, "amenity", "San Francisco", "CA", "USA"),
            new LocationSearchDto("loc-9", "Sunset District Hardware, 1320 9th Ave, San Francisco, CA", 37.7634, -122.4662, "retail", "San Francisco", "CA", "USA"),
            new LocationSearchDto("loc-10", "South Bay Distribution Center, 1500 S 10th St, San Jose, CA", 37.3190, -121.8680, "depot", "San Jose", "CA", "USA"),
            new LocationSearchDto("loc-11", "Oakland Maritime Hub, 530 Water St, Oakland, CA", 37.7955, -122.2798, "logistics", "Oakland", "CA", "USA")
    );

    public NominatimClient(
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper,
            @Value("${routewise.routing.nominatim-url:https://nominatim.openstreetmap.org}") String nominatimBaseUrl,
            @Value("${routewise.routing.nominatim-timeout-ms:3000}") int timeoutMs
    ) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();
        this.objectMapper = objectMapper;
        this.nominatimBaseUrl = nominatimBaseUrl.replaceAll("/$", "");
    }

    public List<LocationSearchDto> search(String query) {
        if (query == null || query.trim().length() < 2) {
            return Collections.emptyList();
        }

        String trimmed = query.trim();

        try {
            String url = UriComponentsBuilder.fromHttpUrl(nominatimBaseUrl + "/search")
                    .queryParam("q", trimmed)
                    .queryParam("format", "json")
                    .queryParam("addressdetails", "1")
                    .queryParam("limit", "7")
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "RouteWise-App/1.0 (contact@routewise.dev)");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray() && root.size() > 0) {
                    List<LocationSearchDto> results = new ArrayList<>();
                    for (JsonNode item : root) {
                        String placeId = item.path("place_id").asText();
                        String displayName = item.path("display_name").asText();
                        double lat = item.path("lat").asDouble();
                        double lon = item.path("lon").asDouble();
                        String type = item.path("type").asText("location");

                        JsonNode addressNode = item.path("address");
                        String city = addressNode.path("city").asText(
                                addressNode.path("town").asText(addressNode.path("municipality").asText(""))
                        );
                        String state = addressNode.path("state").asText("");
                        String country = addressNode.path("country").asText("");

                        results.add(new LocationSearchDto(placeId, displayName, lat, lon, type, city, state, country));
                    }
                    return results;
                }
            }
        } catch (Exception ex) {
            log.warn("Nominatim Geocoding API unavailable: {}. Falling back to internal curated locations.", ex.getMessage());
        }

        // Fallback filter over curated locations
        List<LocationSearchDto> matches = new ArrayList<>();
        String lowerQuery = trimmed.toLowerCase();
        for (LocationSearchDto loc : CURATED_LOCATIONS) {
            if (loc.displayName().toLowerCase().contains(lowerQuery)
                    || loc.city().toLowerCase().contains(lowerQuery)
                    || loc.state().toLowerCase().contains(lowerQuery)) {
                matches.add(loc);
            }
        }

        // Return matching curated locations if any, otherwise empty list
        return matches;
    }
}
