package com.routewise.service;

import com.routewise.dto.location.GeocodeRequest;
import com.routewise.dto.location.GeocodeResponse;
import com.routewise.dto.location.LocationSearchDto;
import com.routewise.integration.NominatimClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {

    private final NominatimClient nominatimClient;

    public LocationService(NominatimClient nominatimClient) {
        this.nominatimClient = nominatimClient;
    }

    public List<LocationSearchDto> search(String query) {
        return nominatimClient.search(query);
    }

    public GeocodeResponse geocode(GeocodeRequest request) {
        if (request == null || request.address() == null || request.address().isBlank()) {
            return GeocodeResponse.failure("Address cannot be empty");
        }

        List<LocationSearchDto> results = nominatimClient.search(request.address());
        if (results.isEmpty()) {
            return GeocodeResponse.failure("Could not geocode address: " + request.address());
        }

        LocationSearchDto best = results.get(0);
        return GeocodeResponse.success(best.displayName(), best.latitude(), best.longitude());
    }

    public LocationSearchDto reverse(double lat, double lon) {
        return nominatimClient.reverseGeocode(lat, lon)
                .orElseGet(() -> new LocationSearchDto(
                        "gps-" + System.currentTimeMillis(),
                        String.format("Current Location (%.4f, %.4f)", lat, lon),
                        lat,
                        lon,
                        "current_location",
                        "",
                        "",
                        ""
                ));
    }
}
