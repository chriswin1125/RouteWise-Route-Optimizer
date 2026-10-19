package com.routewise.algorithm;

import com.routewise.dto.algorithm.TraceStepDto;

import java.util.*;

/**
 * A* (A-Star) Shortest Path Algorithm on a weighted graph with an admissible Euclidean / Haversine heuristic.
 *
 * Computational Complexity:
 * - Time Complexity: O(|E| log |V|) in the best/typical case with an informative admissible heuristic;
 *   degrades to O((|V| + |E|) log |V|) (equivalent to Dijkstra) in the worst case when h(n) = 0.
 * - Space Complexity: O(|V|) for open and closed priority lists, gScore, and fScore tables.
 *
 * Comparison to Dijkstra:
 * While Dijkstra explores vertices uniformly in all radial directions (expanding contours of equal cost g(n)),
 * A* prioritizes nodes that minimize f(n) = g(n) + h(n), steering the search beam directly towards the destination.
 * Because the straight-line heuristic never overestimates the true travel cost (admissibility property),
 * A* is mathematically guaranteed to find an optimal shortest path while examining significantly fewer vertices.
 */
public class AStarAlgorithm {

    public record AStarResult(
            boolean pathFound,
            List<String> path,
            double totalCost,
            Map<String, Double> gScores,
            Map<String, String> predecessors,
            List<TraceStepDto> traceSteps
    ) {}

    private static class AStarNode implements Comparable<AStarNode> {
        final String nodeId;
        final double fScore;
        final double gScore;

        AStarNode(String nodeId, double fScore, double gScore) {
            this.nodeId = nodeId;
            this.fScore = fScore;
            this.gScore = gScore;
        }

        @Override
        public int compareTo(AStarNode other) {
            int cmp = Double.compare(this.fScore, other.fScore);
            if (cmp == 0) {
                // Tie breaker: prefer node closer to target (higher gScore or lower remaining h)
                return Double.compare(other.gScore, this.gScore);
            }
            return cmp;
        }
    }

    /**
     * Executes A* search from source to destination.
     *
     * @param graph The weighted graph
     * @param sourceId The starting vertex identifier
     * @param targetId The destination vertex identifier
     * @param recordTrace Whether to capture fine-grained trace steps for visualizer
     * @return AStarResult containing path, total cost, and execution details
     */
    public AStarResult findShortestPath(Graph graph, String sourceId, String targetId, boolean recordTrace) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        if (sourceId == null || !graph.containsNode(sourceId)) {
            throw new IllegalArgumentException("Source node '" + sourceId + "' does not exist in the graph");
        }
        if (targetId == null || !graph.containsNode(targetId)) {
            throw new IllegalArgumentException("Target node '" + targetId + "' does not exist in the graph");
        }

        Node targetNode = graph.getNode(targetId);

        Map<String, Double> gScore = new HashMap<>();
        Map<String, Double> fScore = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        Set<String> closedSet = new HashSet<>();
        Set<String> unvisited = new HashSet<>();
        PriorityQueue<AStarNode> openSet = new PriorityQueue<>();
        List<TraceStepDto> traceSteps = recordTrace ? new ArrayList<>() : Collections.emptyList();

        for (Node node : graph.getNodes()) {
            gScore.put(node.getId(), Double.POSITIVE_INFINITY);
            fScore.put(node.getId(), Double.POSITIVE_INFINITY);
            unvisited.add(node.getId());
        }

        Node startNode = graph.getNode(sourceId);
        double initialH = computeHeuristic(startNode, targetNode);
        gScore.put(sourceId, 0.0);
        fScore.put(sourceId, initialH);
        openSet.offer(new AStarNode(sourceId, initialH, 0.0));

        int stepCounter = 1;

        if (recordTrace) {
            traceSteps.add(new TraceStepDto(
                    stepCounter++,
                    "A* initialized. Start: " + sourceId + ", Target: " + targetId + ", Heuristic h(start)=" + String.format(Locale.US, "%.2f", initialH),
                    sourceId,
                    null,
                    0.0,
                    new HashSet<>(closedSet),
                    new HashSet<>(unvisited),
                    new HashMap<>(gScore),
                    new HashMap<>(predecessors),
                    extractPqState(openSet),
                    List.of(sourceId)
            ));
        }

        boolean found = false;

        while (!openSet.isEmpty()) {
            AStarNode current = openSet.poll();
            String u = current.nodeId;

            if (closedSet.contains(u)) {
                continue;
            }

            closedSet.add(u);
            unvisited.remove(u);

            if (recordTrace) {
                traceSteps.add(new TraceStepDto(
                        stepCounter++,
                        "Selected node " + u + " with f=" + String.format(Locale.US, "%.2f", current.fScore) + " (g=" + String.format(Locale.US, "%.2f", current.gScore) + ")",
                        u,
                        null,
                        current.gScore,
                        new HashSet<>(closedSet),
                        new HashSet<>(unvisited),
                        new HashMap<>(gScore),
                        new HashMap<>(predecessors),
                        extractPqState(openSet),
                        reconstructPathTo(u, predecessors)
                ));
            }

            if (u.equals(targetId)) {
                found = true;
                break;
            }

            Node uNode = graph.getNode(u);

            for (Edge edge : graph.getNeighbors(u)) {
                String v = edge.getTarget().getId();
                if (closedSet.contains(v)) {
                    continue;
                }

                double weight = edge.getWeight();
                double tentativeG = gScore.get(u) + weight;

                if (tentativeG < gScore.get(v)) {
                    predecessors.put(v, u);
                    gScore.put(v, tentativeG);
                    Node vNode = edge.getTarget();
                    double h = computeHeuristic(vNode, targetNode);
                    double newF = tentativeG + h;
                    fScore.put(v, newF);

                    openSet.offer(new AStarNode(v, newF, tentativeG));

                    if (recordTrace) {
                        traceSteps.add(new TraceStepDto(
                                stepCounter++,
                                "Evaluated neighbor " + v + " via " + u + ": tentative g=" + String.format(Locale.US, "%.2f", tentativeG)
                                        + ", h=" + String.format(Locale.US, "%.2f", h) + ", total f=" + String.format(Locale.US, "%.2f", newF),
                                u,
                                v,
                                tentativeG,
                                new HashSet<>(closedSet),
                                new HashSet<>(unvisited),
                                new HashMap<>(gScore),
                                new HashMap<>(predecessors),
                                extractPqState(openSet),
                                reconstructPathTo(v, predecessors)
                        ));
                    }
                }
            }
        }

        List<String> path = found ? reconstructPathTo(targetId, predecessors) : Collections.emptyList();
        double totalCost = found ? gScore.get(targetId) : -1.0;

        if (recordTrace) {
            String summary = found
                    ? "Goal reached! Shortest path: " + String.join(" -> ", path) + " (Total Cost: " + String.format(Locale.US, "%.2f", totalCost) + ")"
                    : "No path exists from " + sourceId + " to " + targetId;
            traceSteps.add(new TraceStepDto(
                    stepCounter++,
                    summary,
                    targetId,
                    null,
                    totalCost,
                    new HashSet<>(closedSet),
                    new HashSet<>(unvisited),
                    new HashMap<>(gScore),
                    new HashMap<>(predecessors),
                    extractPqState(openSet),
                    path
            ));
        }

        return new AStarResult(found, path, totalCost, gScore, predecessors, traceSteps);
    }

    /**
     * Admissible Euclidean straight-line distance heuristic in 2D coordinate space.
     */
    private double computeHeuristic(Node a, Node b) {
        if (a == null || b == null) return 0.0;
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private List<String> extractPqState(PriorityQueue<AStarNode> pq) {
        List<AStarNode> list = new ArrayList<>(pq);
        Collections.sort(list);
        List<String> result = new ArrayList<>();
        for (AStarNode n : list) {
            result.add(n.nodeId + "(f=" + String.format(Locale.US, "%.1f", n.fScore) + ", g=" + String.format(Locale.US, "%.1f", n.gScore) + ")");
        }
        return result;
    }

    private List<String> reconstructPathTo(String targetId, Map<String, String> predecessors) {
        LinkedList<String> path = new LinkedList<>();
        String curr = targetId;
        while (curr != null) {
            path.addFirst(curr);
            curr = predecessors.get(curr);
        }
        return path;
    }
}
