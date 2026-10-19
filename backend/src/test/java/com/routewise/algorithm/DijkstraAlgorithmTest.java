package com.routewise.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DijkstraAlgorithmTest {

    private DijkstraAlgorithm dijkstra;
    private Graph graph;

    @BeforeEach
    void setUp() {
        dijkstra = new DijkstraAlgorithm();
        graph = new Graph();

        // Nodes
        graph.addNode(new Node("A", 0, 0));
        graph.addNode(new Node("B", 1, 1));
        graph.addNode(new Node("C", 2, 0));
        graph.addNode(new Node("D", 3, 1));
        graph.addNode(new Node("E", 4, 0));
        graph.addNode(new Node("Isolated", 10, 10));

        // Edges: A -> B(4), A -> C(2), C -> B(1), B -> D(5), C -> D(8), C -> E(10), D -> E(2)
        graph.addEdge("A", "B", 4.0, true);
        graph.addEdge("A", "C", 2.0, true);
        graph.addEdge("C", "B", 1.0, true);
        graph.addEdge("B", "D", 5.0, true);
        graph.addEdge("C", "D", 8.0, true);
        graph.addEdge("C", "E", 10.0, true);
        graph.addEdge("D", "E", 2.0, true);
    }

    @Test
    @DisplayName("Should find correct shortest path and minimal cost from A to E")
    void testShortestPathFound() {
        // Expected optimal path: A -> C (2) -> B (1) -> D (5) -> E (2) = total 10.0
        // (Better than A -> C (2) -> E (10) = 12.0)
        DijkstraAlgorithm.DijkstraResult result = dijkstra.findShortestPath(graph, "A", "E", true);

        assertTrue(result.pathFound());
        assertEquals(10.0, result.totalCost(), 1e-6);
        assertEquals(List.of("A", "C", "B", "D", "E"), result.path());
        assertFalse(result.traceSteps().isEmpty());
    }

    @Test
    @DisplayName("Should return pathFound=false for unreachable destination")
    void testUnreachableDestination() {
        DijkstraAlgorithm.DijkstraResult result = dijkstra.findShortestPath(graph, "A", "Isolated", false);

        assertFalse(result.pathFound());
        assertEquals(-1.0, result.totalCost());
        assertTrue(result.path().isEmpty());
    }

    @Test
    @DisplayName("Should handle source same as destination with cost 0.0")
    void testSourceSameAsDestination() {
        DijkstraAlgorithm.DijkstraResult result = dijkstra.findShortestPath(graph, "A", "A", false);

        assertTrue(result.pathFound());
        assertEquals(0.0, result.totalCost());
        assertEquals(List.of("A"), result.path());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when node does not exist in graph")
    void testNonExistentNode() {
        assertThrows(IllegalArgumentException.class, () -> dijkstra.findShortestPath(graph, "NonExistent", "B", false));
        assertThrows(IllegalArgumentException.class, () -> dijkstra.findShortestPath(graph, "A", "NonExistent", false));
    }

    @Test
    @DisplayName("Should reject negative edge weight upon edge creation")
    void testNegativeEdgeWeightRejection() {
        Node n1 = new Node("X", 0, 0);
        Node n2 = new Node("Y", 1, 1);
        assertThrows(IllegalArgumentException.class, () -> new Edge(n1, n2, -5.0));
    }

    @Test
    @DisplayName("Should capture fine-grained trace steps when requested")
    void testTraceStepsCaptured() {
        DijkstraAlgorithm.DijkstraResult result = dijkstra.findShortestPath(graph, "A", "D", true);

        assertTrue(result.pathFound());
        assertTrue(result.traceSteps().size() >= 3);
        assertNotNull(result.traceSteps().get(0).priorityQueueState());
        assertNotNull(result.traceSteps().get(0).shortestDistances());
    }
}
