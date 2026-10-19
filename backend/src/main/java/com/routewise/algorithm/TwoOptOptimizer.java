package com.routewise.algorithm;

import java.util.*;

/**
 * Multi-Stop Route Optimizer using Nearest-Neighbor construction followed by 2-Opt Local Search refinement.
 *
 * Algorithm Details:
 * 1. Nearest Neighbor Heuristic:
 *    - Starts from the starting depot (index 0).
 *    - Greedily selects the closest unvisited delivery stop until all stops are visited.
 *    - Connects to the return depot (if round-trip) or fixed end depot.
 *
 * 2. 2-Opt Local Search:
 *    - Evaluates edge reversals (2-opt move): replaces edges (i, i+1) and (j, j+1) with (i, j) and (i+1, j+1)
 *      by reversing the sub-segment between index i+1 and j.
 *    - Accepts moves that yield a reduction in total distance greater than an epsilon threshold (1e-6).
 *    - Repeats until reaching a local optimum or exceeding maxIterations (500).
 *
 * Note on Optimality:
 * As a polynomial-time heuristic for the NP-hard Traveling Salesperson Problem (TSP), 2-Opt is not
 * guaranteed to find the global optimum; it finds the best local optimum within the 2-exchange neighborhood.
 */
public class TwoOptOptimizer {

    private static final double EPSILON = 1e-6;
    private static final int DEFAULT_MAX_ITERATIONS = 500;

    public record OptimizationResult(
            List<Integer> initialTour,
            List<Integer> optimizedTour,
            double initialDistance,
            double optimizedDistance,
            double distanceSaved,
            double percentageSaved,
            int iterationsPerformed,
            String algorithmUsed
    ) {}

    /**
     * Optimizes a tour given a distance matrix.
     * Index 0 is always the starting depot.
     * Indices 1..N are the delivery stops.
     * If hasFixedEndDepot is true, index N+1 is the fixed end depot.
     * If roundTrip is true (and !hasFixedEndDepot), the tour returns to index 0.
     *
     * @param distanceMatrix Square matrix of distances between all points
     * @param totalStopsCount Number of delivery stops (excluding depots)
     * @param roundTrip Whether route must return to start depot
     * @param hasFixedEndDepot Whether route terminates at a distinct end depot
     * @return OptimizationResult
     */
    public OptimizationResult optimize(double[][] distanceMatrix, int totalStopsCount, boolean roundTrip, boolean hasFixedEndDepot) {
        if (distanceMatrix == null || distanceMatrix.length == 0) {
            throw new IllegalArgumentException("Distance matrix cannot be null or empty");
        }

        int totalPoints = distanceMatrix.length;
        if (totalStopsCount <= 0) {
            List<Integer> emptyTour = Collections.emptyList();
            return new OptimizationResult(emptyTour, emptyTour, 0.0, 0.0, 0.0, 0.0, 0, "HEURISTIC_2OPT");
        }

        // 1. Build initial tour via Nearest Neighbor
        List<Integer> initialTour = buildNearestNeighborTour(distanceMatrix, totalStopsCount, roundTrip, hasFixedEndDepot);
        double initialDistance = calculateTourDistance(initialTour, distanceMatrix);

        if (totalStopsCount <= 2) {
            // Nothing to optimize with 2-opt for 1 or 2 stops
            return new OptimizationResult(
                    new ArrayList<>(initialTour),
                    new ArrayList<>(initialTour),
                    initialDistance,
                    initialDistance,
                    0.0,
                    0.0,
                    0,
                    "HEURISTIC_2OPT"
            );
        }

        // 2. Perform 2-Opt Local Search
        List<Integer> currentTour = new ArrayList<>(initialTour);
        double currentDistance = initialDistance;
        boolean improved = true;
        int iterations = 0;

        // The first element (depot) and last element (depot or fixed end) are fixed
        int lastSwappableIndex = (roundTrip || hasFixedEndDepot) ? currentTour.size() - 2 : currentTour.size() - 1;

        while (improved && iterations < DEFAULT_MAX_ITERATIONS) {
            improved = false;
            iterations++;

            for (int i = 0; i < lastSwappableIndex - 1; i++) {
                for (int j = i + 2; j <= lastSwappableIndex; j++) {
                    int a = currentTour.get(i);
                    int b = currentTour.get(i + 1);
                    int c = currentTour.get(j);
                    int d = (j + 1 < currentTour.size()) ? currentTour.get(j + 1) : -1;

                    double currentEdgeCost = distanceMatrix[a][b];
                    double newEdgeCost = distanceMatrix[a][c];

                    if (d != -1) {
                        currentEdgeCost += distanceMatrix[c][d];
                        newEdgeCost += distanceMatrix[b][d];
                    }

                    double delta = newEdgeCost - currentEdgeCost;

                    if (delta < -EPSILON) {
                        reverseSubTour(currentTour, i + 1, j);
                        currentDistance += delta;
                        improved = true;
                        break;
                    }
                }
                if (improved) {
                    break;
                }
            }
        }

        // Recalculate precisely to avoid floating-point accumulation drift
        double finalDistance = calculateTourDistance(currentTour, distanceMatrix);
        double distanceSaved = Math.max(0.0, initialDistance - finalDistance);
        double percentageSaved = initialDistance > 0.0 ? (distanceSaved / initialDistance) * 100.0 : 0.0;

        return new OptimizationResult(
                initialTour,
                currentTour,
                initialDistance,
                finalDistance,
                distanceSaved,
                percentageSaved,
                iterations,
                "HEURISTIC_2OPT"
        );
    }

    private List<Integer> buildNearestNeighborTour(double[][] matrix, int stopCount, boolean roundTrip, boolean hasFixedEndDepot) {
        List<Integer> tour = new ArrayList<>();
        Set<Integer> unvisited = new HashSet<>();
        for (int i = 1; i <= stopCount; i++) {
            unvisited.add(i);
        }

        int current = 0; // Start at depot
        tour.add(current);

        while (!unvisited.isEmpty()) {
            int nearest = -1;
            double minDistance = Double.POSITIVE_INFINITY;

            for (int next : unvisited) {
                double dist = matrix[current][next];
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = next;
                }
            }

            if (nearest == -1) {
                nearest = unvisited.iterator().next();
            }

            unvisited.remove(nearest);
            tour.add(nearest);
            current = nearest;
        }

        if (hasFixedEndDepot) {
            int endDepotIndex = stopCount + 1;
            tour.add(endDepotIndex);
        } else if (roundTrip) {
            tour.add(0); // Return to start depot
        }

        return tour;
    }

    public static double calculateTourDistance(List<Integer> tour, double[][] matrix) {
        if (tour == null || tour.size() < 2) {
            return 0.0;
        }
        double sum = 0.0;
        for (int i = 0; i < tour.size() - 1; i++) {
            int u = tour.get(i);
            int v = tour.get(i + 1);
            sum += matrix[u][v];
        }
        return sum;
    }

    private void reverseSubTour(List<Integer> tour, int start, int end) {
        while (start < end) {
            int temp = tour.get(start);
            tour.set(start, tour.get(end));
            tour.set(end, temp);
            start++;
            end--;
        }
    }
}
