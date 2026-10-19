package com.routewise.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeldKarpTspTest {

    private HeldKarpTsp heldKarp;

    @BeforeEach
    void setUp() {
        heldKarp = new HeldKarpTsp();
    }

    @Test
    @DisplayName("Should find proven mathematically optimal tour for 4 delivery stops")
    void testExactOptimalityFourStops() {
        // Depot (0,0), S1 (0,1), S2 (1,1), S3 (1,0), S4 (2,0)
        double[][] dist = new double[][] {
                {0, 1, 1.41, 1, 2},
                {1, 0, 1, 1.41, 2.23},
                {1.41, 1, 0, 1, 1.41},
                {1, 1.41, 1, 0, 1},
                {2, 2.23, 1.41, 1, 0}
        };

        HeldKarpTsp.ExactTspResult result = heldKarp.solve(dist, 4, true, false);

        assertTrue(result.exactSolutionFound());
        assertEquals(0, (int) result.optimalTour().get(0));
        assertEquals(0, (int) result.optimalTour().get(result.optimalTour().size() - 1));
        // Must visit all 4 stops
        assertTrue(result.optimalTour().contains(1));
        assertTrue(result.optimalTour().contains(2));
        assertTrue(result.optimalTour().contains(3));
        assertTrue(result.optimalTour().contains(4));
        assertEquals(6, result.optimalTour().size()); // 0 -> s1 -> s2 -> s3 -> s4 -> 0
    }

    @Test
    @DisplayName("Should enforce maximum stop limit to prevent combinatorial explosion")
    void testStopLimitEnforcement() {
        double[][] dummy = new double[16][16];
        assertThrows(IllegalArgumentException.class, () -> heldKarp.solve(dummy, 15, true, false));
    }

    @Test
    @DisplayName("Should handle 1 stop exactly")
    void testSingleStopExact() {
        double[][] dist = new double[][] {
                {0, 4},
                {4, 0}
        };

        HeldKarpTsp.ExactTspResult result = heldKarp.solve(dist, 1, true, false);
        assertEquals(8.0, result.optimalDistance(), 1e-6);
    }
}
