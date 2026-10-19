package com.routewise.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TwoOptOptimizerTest {

    private TwoOptOptimizer optimizer;

    @BeforeEach
    void setUp() {
        optimizer = new TwoOptOptimizer();
    }

    @Test
    @DisplayName("Should improve criss-crossing tour using 2-opt swaps")
    void testTwoOptImprovesCrossedTour() {
        // Depot (0,0), Stop1 (0,2), Stop2 (2,0), Stop3 (2,2)
        // Crossing tour: Depot(0) -> Stop3(3) -> Stop1(1) -> Stop2(2) -> Depot(0)
        // Optimal tour: 0 -> 1 -> 3 -> 2 -> 0 or 0 -> 2 -> 3 -> 1 -> 0
        double[][] dist = new double[][] {
                {0.0, 2.0, 2.0, 2.82}, // 0: Depot
                {2.0, 0.0, 2.82, 2.0}, // 1: Stop 1
                {2.0, 2.82, 0.0, 2.0}, // 2: Stop 2
                {2.82, 2.0, 2.0, 0.0}  // 3: Stop 3
        };

        TwoOptOptimizer.OptimizationResult result = optimizer.optimize(dist, 3, true, false);

        assertNotNull(result);
        assertTrue(result.optimizedDistance() <= result.initialDistance());
        assertTrue(result.distanceSaved() >= 0.0);
        assertTrue(result.percentageSaved() >= 0.0);
        assertEquals(0, (int) result.optimizedTour().get(0)); // starts at depot
        assertEquals(0, (int) result.optimizedTour().get(result.optimizedTour().size() - 1)); // ends at depot
    }

    @Test
    @DisplayName("Should handle single delivery stop gracefully without error")
    void testSingleStop() {
        double[][] dist = new double[][] {
                {0.0, 5.0},
                {5.0, 0.0}
        };

        TwoOptOptimizer.OptimizationResult result = optimizer.optimize(dist, 1, true, false);

        assertEquals(List.of(0, 1, 0), result.optimizedTour());
        assertEquals(10.0, result.optimizedDistance(), 1e-6);
        assertEquals(0.0, result.distanceSaved(), 1e-6);
    }

    @Test
    @DisplayName("Should handle fixed end depot correctly")
    void testFixedEndDepot() {
        // 0: Start Depot, 1: Stop 1, 2: Stop 2, 3: Fixed End Depot
        double[][] dist = new double[][] {
                {0.0, 2.0, 4.0, 8.0},
                {2.0, 0.0, 2.0, 6.0},
                {4.0, 2.0, 0.0, 4.0},
                {8.0, 6.0, 4.0, 0.0}
        };

        TwoOptOptimizer.OptimizationResult result = optimizer.optimize(dist, 2, false, true);

        assertEquals(0, (int) result.optimizedTour().get(0));
        assertEquals(3, (int) result.optimizedTour().get(result.optimizedTour().size() - 1));
        assertEquals(8.0, result.optimizedDistance(), 1e-6); // 0 -> 1(2) -> 2(2) -> 3(4) = 8
    }

    @Test
    @DisplayName("Should handle zero-distance and duplicate locations safely")
    void testZeroDistanceSafely() {
        double[][] zeroMatrix = new double[][] {
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0}
        };

        TwoOptOptimizer.OptimizationResult result = optimizer.optimize(zeroMatrix, 2, true, false);

        assertEquals(0.0, result.optimizedDistance(), 1e-6);
        assertEquals(0.0, result.distanceSaved(), 1e-6);
        assertEquals(0.0, result.percentageSaved(), 1e-6);
    }
}
