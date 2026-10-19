package com.routewise.algorithm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HaversineDistanceTest {

    @Test
    @DisplayName("Should return 0.0 for identical coordinates")
    void testIdenticalCoordinates() {
        double dist = HaversineDistance.calculateDistanceKm(37.7749, -122.4194, 37.7749, -122.4194);
        assertEquals(0.0, dist, 1e-9);
    }

    @Test
    @DisplayName("Should accurately calculate distance between London and Paris (~343 km)")
    void testLondonToParis() {
        // London: 51.5074, -0.1278
        // Paris: 48.8566, 2.3522
        double dist = HaversineDistance.calculateDistanceKm(51.5074, -0.1278, 48.8566, 2.3522);
        // True great-circle distance is approximately 343 - 345 km
        assertTrue(dist >= 340.0 && dist <= 348.0, "Expected ~343km but was " + dist);
    }

    @Test
    @DisplayName("Should correctly estimate duration in minutes at given average speed")
    void testDurationEstimation() {
        double distKm = 40.0;
        double speedKmh = 40.0;
        double durationMin = HaversineDistance.estimateDurationMinutes(distKm, speedKmh);
        assertEquals(60.0, durationMin, 1e-6);

        // Zero distance should yield zero duration
        assertEquals(0.0, HaversineDistance.estimateDurationMinutes(0.0, 50.0));
    }
}
