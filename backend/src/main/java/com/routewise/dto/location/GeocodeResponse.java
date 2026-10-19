package com.routewise.dto.location;

public record GeocodeResponse(
        String formattedAddress,
        Double latitude,
        Double longitude,
        boolean success,
        String message
) {
    public static GeocodeResponse success(String address, Double lat, Double lng) {
        return new GeocodeResponse(address, lat, lng, true, "Address geocoded successfully");
    }

    public static GeocodeResponse failure(String message) {
        return new GeocodeResponse(null, null, null, false, message);
    }
}
