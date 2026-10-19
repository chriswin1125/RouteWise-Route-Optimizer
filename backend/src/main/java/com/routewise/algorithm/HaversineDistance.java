package com.routewise.algorithm;

/**
 * Utility for computing great-circle geographic distance using the Haversine formula.
 * Earth mean radius = 6371.0 kilometers.
 */
public final class HaversineDistance {

    public static final double EARTH_RADIUS_KM = 6371.0;

    private HaversineDistance() {}

    /**
     * Calculates the great-circle distance between two geographic coordinates in kilometers.
     *
     * @param lat1 Latitude of source point in decimal degrees
     * @param lon1 Longitude of source point in decimal degrees
     * @param lat2 Latitude of destination point in decimal degrees
     * @param lon2 Longitude of destination point in decimal degrees
     * @return Distance in kilometers (>= 0.0)
     */
    public static double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        if (Double.compare(lat1, lat2) == 0 && Double.compare(lon1, lon2) == 0) {
            return 0.0;
        }

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2.0) * Math.sin(latDistance / 2.0)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2.0) * Math.sin(lonDistance / 2.0);

        // Clamp 'a' between 0 and 1 to prevent floating point inaccuracy with atan2/sqrt
        a = Math.max(0.0, Math.min(1.0, a));

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Estimates travel duration in minutes based on distance and average speed.
     *
     * @param distanceKm Distance in kilometers
     * @param speedKmh Average speed in km/h (defaults to 40 km/h if <= 0)
     * @return Duration in minutes
     */
    public static double estimateDurationMinutes(double distanceKm, double speedKmh) {
        if (distanceKm <= 0.0) {
            return 0.0;
        }
        double speed = speedKmh > 0 ? speedKmh : 40.0;
        return (distanceKm / speed) * 60.0;
    }
}
