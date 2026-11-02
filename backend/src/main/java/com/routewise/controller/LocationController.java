package com.routewise.controller;

import com.routewise.dto.location.GeocodeRequest;
import com.routewise.dto.location.GeocodeResponse;
import com.routewise.dto.location.LocationSearchDto;
import com.routewise.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@Tag(name = "Locations", description = "Endpoints for geocoding and address auto-completion")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/search")
    @Operation(summary = "Search addresses and places via OpenStreetMap Nominatim with debounced suggestions")
    public ResponseEntity<List<LocationSearchDto>> searchLocations(@RequestParam String q) {
        List<LocationSearchDto> results = locationService.search(q);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/geocode")
    @Operation(summary = "Geocode a text address into geographic coordinates")
    public ResponseEntity<GeocodeResponse> geocodeAddress(@Valid @RequestBody GeocodeRequest request) {
        GeocodeResponse response = locationService.geocode(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reverse")
    @Operation(summary = "Reverse geocode latitude and longitude into human-readable address")
    public ResponseEntity<LocationSearchDto> reverseGeocode(@RequestParam double lat, @RequestParam double lon) {
        LocationSearchDto result = locationService.reverse(lat, lon);
        return ResponseEntity.ok(result);
    }
}
