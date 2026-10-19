package com.routewise.algorithm;

import java.util.*;

/**
 * Exact Traveling Salesperson Problem (TSP) Solver using the Held-Karp Dynamic Programming Algorithm.
 *
 * Mathematical Complexity:
 * - Time Complexity: O(N^2 * 2^N), where N is the number of delivery stops.
 *   Compared to exhaustive brute-force search O(N!), Held-Karp solves the TSP exponentially faster
 *   by caching sub-tour solutions in a memoization table indexed by (bitmask, ending_vertex).
 * - Space Complexity: O(N * 2^N) for the DP table and parent backtracking pointers.
 *
 * Limitation & Guardrail:
 * Safe for N <= 14 stops. Exceeding this threshold causes factorial memory and CPU explosion,
 * in which case the system safely falls back to the 2-Opt heuristic with a clear log and label.
 */
public class HeldKarpTsp {

    public static final int MAX_SUPPORTED_STOPS = 14;

    public record ExactTspResult(
            List<Integer> optimalTour,
            double optimalDistance,
            boolean exactSolutionFound,
            String algorithmUsed
    ) {}

    /**
     * Solves the TSP exactly for N stops.
     * Index 0 = start depot.
     * Indices 1..N = delivery stops.
     * If hasFixedEndDepot, index N+1 = fixed end depot.
     *
     * @param distanceMatrix Square matrix of pairwise distances
     * @param n Number of delivery stops (1..N)
     * @param roundTrip Whether to return to start depot
     * @param hasFixedEndDepot Whether to terminate at distinct fixed end depot
     * @return ExactTspResult
     */
    public ExactTspResult solve(double[][] distanceMatrix, int n, boolean roundTrip, boolean hasFixedEndDepot) {
        if (n <= 0) {
            return new ExactTspResult(Collections.emptyList(), 0.0, true, "EXACT_HELD_KARP");
        }

        if (n > MAX_SUPPORTED_STOPS) {
            throw new IllegalArgumentException("Stop count " + n + " exceeds exact Held-Karp maximum limit of " + MAX_SUPPORTED_STOPS);
        }

        if (n == 1) {
            List<Integer> tour = new ArrayList<>();
            tour.add(0);
            tour.add(1);
            if (hasFixedEndDepot) {
                tour.add(2);
            } else if (roundTrip) {
                tour.add(0);
            }
            double dist = TwoOptOptimizer.calculateTourDistance(tour, distanceMatrix);
            return new ExactTspResult(tour, dist, true, "EXACT_HELD_KARP");
        }

        int totalMasks = 1 << n;
        // dp[mask][u]: min cost to visit stops in mask, ending at stop u (0-indexed relative to stops, so stop 1 is index 0)
        double[][] dp = new double[totalMasks][n];
        int[][] parent = new int[totalMasks][n];

        for (int mask = 0; mask < totalMasks; mask++) {
            Arrays.fill(dp[mask], Double.POSITIVE_INFINITY);
            Arrays.fill(parent[mask], -1);
        }

        // Base cases: single stops visited directly from depot (index 0)
        for (int u = 0; u < n; u++) {
            int stopIndex = u + 1;
            int mask = 1 << u;
            dp[mask][u] = distanceMatrix[0][stopIndex];
        }

        // DP transitions: iterate by mask size
        for (int mask = 1; mask < totalMasks; mask++) {
            for (int u = 0; u < n; u++) {
                if ((mask & (1 << u)) == 0 || dp[mask][u] == Double.POSITIVE_INFINITY) {
                    continue;
                }

                int uStop = u + 1;

                // Try visiting an unvisited stop v
                for (int v = 0; v < n; v++) {
                    if ((mask & (1 << v)) != 0) {
                        continue;
                    }

                    int nextMask = mask | (1 << v);
                    int vStop = v + 1;
                    double newDist = dp[mask][u] + distanceMatrix[uStop][vStop];

                    if (newDist < dp[nextMask][v]) {
                        dp[nextMask][v] = newDist;
                        parent[nextMask][v] = u;
                    }
                }
            }
        }

        // Find optimal last stop that connects to return depot or fixed end depot
        int fullMask = totalMasks - 1;
        double bestDistance = Double.POSITIVE_INFINITY;
        int bestLastStop = -1;

        int finalDestinationIndex = hasFixedEndDepot ? (n + 1) : (roundTrip ? 0 : -1);

        for (int u = 0; u < n; u++) {
            int uStop = u + 1;
            double totalCost = dp[fullMask][u];
            if (finalDestinationIndex >= 0) {
                totalCost += distanceMatrix[uStop][finalDestinationIndex];
            }

            if (totalCost < bestDistance) {
                bestDistance = totalCost;
                bestLastStop = u;
            }
        }

        if (bestLastStop == -1) {
            // Fallback if unreachable
            throw new IllegalStateException("Failed to find valid TSP tour");
        }

        // Backtrack to reconstruct tour
        List<Integer> stopOrder = new ArrayList<>();
        int currMask = fullMask;
        int currStop = bestLastStop;

        while (currStop != -1) {
            stopOrder.add(currStop + 1);
            int prev = parent[currMask][currStop];
            currMask ^= (1 << currStop);
            currStop = prev;
        }

        Collections.reverse(stopOrder);

        List<Integer> fullTour = new ArrayList<>();
        fullTour.add(0); // Depot
        fullTour.addAll(stopOrder);
        if (hasFixedEndDepot) {
            fullTour.add(n + 1);
        } else if (roundTrip) {
            fullTour.add(0);
        }

        return new ExactTspResult(fullTour, bestDistance, true, "EXACT_HELD_KARP");
    }
}
