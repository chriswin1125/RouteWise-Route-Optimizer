package com.routewise.algorithm;

import com.routewise.dto.algorithm.TraceStepDto;

import java.util.*;

/**
 * Dijkstra's Single-Source Shortest Path Algorithm on a weighted graph with nonnegative edge weights.
 *
 * Computational Complexity:
 * - Time Complexity: O((|V| + |E|) log |V|), where |V| is the number of vertices and |E| is the number of edges,
 *   achieved using a binary min-heap (PriorityQueue).
 * - Space Complexity: O(|V| + |E|) to maintain graph adjacency list, distance table, visited set, and predecessors.
 */
public class DijkstraAlgorithm {

    public record DijkstraResult(
            boolean pathFound,
            List<String> path,
            double totalCost,
            Map<String, Double> distances,
            Map<String, String> predecessors,
            List<TraceStepDto> traceSteps
    ) {}

    private static class QueueEntry implements Comparable<QueueEntry> {
        final String nodeId;
        final double distance;

        QueueEntry(String nodeId, double distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }

        @Override
        public int compareTo(QueueEntry other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    /**
     * Executes Dijkstra's algorithm from source to destination.
     *
     * @param graph The weighted graph
     * @param sourceId The starting vertex identifier
     * @param targetId The destination vertex identifier (can be null to find shortest path to all reachable nodes)
     * @param recordTrace Whether to capture fine-grained trace steps for visualizer
     * @return DijkstraResult containing path, total cost, and execution details
     */
    public DijkstraResult findShortestPath(Graph graph, String sourceId, String targetId, boolean recordTrace) {
        Objects.requireNonNull(graph, "Graph cannot be null");
        if (sourceId == null || !graph.containsNode(sourceId)) {
            throw new IllegalArgumentException("Source node '" + sourceId + "' does not exist in the graph");
        }
        if (targetId != null && !graph.containsNode(targetId)) {
            throw new IllegalArgumentException("Target node '" + targetId + "' does not exist in the graph");
        }

        Map<String, Double> distances = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Set<String> unvisited = new HashSet<>();
        PriorityQueue<QueueEntry> pq = new PriorityQueue<>();
        List<TraceStepDto> traceSteps = recordTrace ? new ArrayList<>() : Collections.emptyList();

        for (Node node : graph.getNodes()) {
            distances.put(node.getId(), Double.POSITIVE_INFINITY);
            unvisited.add(node.getId());
        }

        distances.put(sourceId, 0.0);
        pq.offer(new QueueEntry(sourceId, 0.0));

        int stepCounter = 1;

        if (recordTrace) {
            traceSteps.add(new TraceStepDto(
                    stepCounter++,
                    "Initialized distances. Starting node: " + sourceId + " (distance = 0.0). All other nodes = infinity.",
                    sourceId,
                    null,
                    0.0,
                    new HashSet<>(visited),
                    new HashSet<>(unvisited),
                    new HashMap<>(distances),
                    new HashMap<>(predecessors),
                    extractPqState(pq),
                    List.of(sourceId)
            ));
        }

        while (!pq.isEmpty()) {
            QueueEntry current = pq.poll();
            String u = current.nodeId;
            double currentDist = current.distance;

            // Skip if a shorter path to u was already processed
            if (currentDist > distances.get(u)) {
                continue;
            }

            if (!visited.add(u)) {
                continue;
            }
            unvisited.remove(u);

            if (recordTrace) {
                traceSteps.add(new TraceStepDto(
                        stepCounter++,
                        "Visited node " + u + " with confirmed minimum tentative distance = " + String.format(Locale.US, "%.2f", currentDist),
                        u,
                        null,
                        currentDist,
                        new HashSet<>(visited),
                        new HashSet<>(unvisited),
                        new HashMap<>(distances),
                        new HashMap<>(predecessors),
                        extractPqState(pq),
                        reconstructPathTo(u, predecessors)
                ));
            }

            // Early exit if target is reached
            if (targetId != null && u.equals(targetId)) {
                break;
            }

            for (Edge edge : graph.getNeighbors(u)) {
                String v = edge.getTarget().getId();
                if (visited.contains(v)) {
                    continue;
                }

                double weight = edge.getWeight();
                if (weight < 0) {
                    throw new IllegalStateException("Dijkstra does not support negative edge weights: " + edge);
                }

                double newDist = distances.get(u) + weight;

                if (newDist < distances.get(v)) {
                    distances.put(v, newDist);
                    predecessors.put(v, u);
                    pq.offer(new QueueEntry(v, newDist));

                    if (recordTrace) {
                        traceSteps.add(new TraceStepDto(
                                stepCounter++,
                                "Relaxed edge (" + u + " -> " + v + ", weight=" + weight + "). Updated distance to " + v + " = " + String.format(Locale.US, "%.2f", newDist),
                                u,
                                v,
                                newDist,
                                new HashSet<>(visited),
                                new HashSet<>(unvisited),
                                new HashMap<>(distances),
                                new HashMap<>(predecessors),
                                extractPqState(pq),
                                reconstructPathTo(v, predecessors)
                        ));
                    }
                }
            }
        }

        boolean pathFound = targetId != null && distances.get(targetId) < Double.POSITIVE_INFINITY;
        List<String> path = pathFound ? reconstructPathTo(targetId, predecessors) : Collections.emptyList();
        double totalCost = pathFound ? distances.get(targetId) : (targetId == null ? 0.0 : -1.0);

        if (recordTrace && targetId != null) {
            String summary = pathFound
                    ? "Target reached! Shortest path: " + String.join(" -> ", path) + " (Cost: " + String.format(Locale.US, "%.2f", totalCost) + ")"
                    : "No path exists from " + sourceId + " to " + targetId;
            traceSteps.add(new TraceStepDto(
                    stepCounter++,
                    summary,
                    targetId,
                    null,
                    totalCost,
                    new HashSet<>(visited),
                    new HashSet<>(unvisited),
                    new HashMap<>(distances),
                    new HashMap<>(predecessors),
                    extractPqState(pq),
                    path
            ));
        }

        return new DijkstraResult(pathFound, path, totalCost, distances, predecessors, traceSteps);
    }

    private List<String> extractPqState(PriorityQueue<QueueEntry> pq) {
        List<QueueEntry> list = new ArrayList<>(pq);
        Collections.sort(list);
        List<String> result = new ArrayList<>();
        for (QueueEntry e : list) {
            result.add(e.nodeId + "(" + String.format(Locale.US, "%.1f", e.distance) + ")");
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
