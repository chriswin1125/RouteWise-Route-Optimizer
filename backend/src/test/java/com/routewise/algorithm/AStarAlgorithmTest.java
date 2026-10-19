package com.routewise.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AStarAlgorithmTest {

    private AStarAlgorithm aStar;
    private DijkstraAlgorithm dijkstra;
    private Graph graph;

    @BeforeEach
    void setUp() {
        aStar = new AStarAlgorithm();
        dijkstra = new DijkstraAlgorithm();
        graph = new Graph();

        // 2D grid graph: (0,0) to (3,3)
        // Nodes with spatial coordinates
        graph.addNode(new Node("Start", 0.0, 0.0));
        graph.addNode(new Node("N1", 1.0, 0.0));
        graph.addNode(new Node("N2", 0.0, 1.0));
        graph.addNode(new Node("N3", 1.0, 1.0));
        graph.addNode(new Node("N4", 2.0, 1.0));
        graph.addNode(new Node("Goal", 2.0, 2.0));
        graph.addNode(new Node("Unreachable", 10.0, 10.0));

        graph.addEdge("Start", "N1", 1.0, false);
        graph.addEdge("Start", "N2", 1.0, false);
        graph.addEdge("N1", "N3", 1.2, false);
        graph.addEdge("N2", "N3", 1.0, false);
        graph.addEdge("N3", "N4", 1.0, false);
        graph.addEdge("N4", "Goal", 1.0, false);
    }

    @Test
    @DisplayName("Should find optimal shortest path matching Dijkstra cost")
    void testAStarFindsOptimalPath() {
        AStarAlgorithm.AStarResult aStarResult = aStar.findShortestPath(graph, "Start", "Goal", true);
        DijkstraAlgorithm.DijkstraResult dijkstraResult = dijkstra.findShortestPath(graph, "Start", "Goal", false);

        assertTrue(aStarResult.pathFound());
        assertEquals(dijkstraResult.totalCost(), aStarResult.totalCost(), 1e-6);
        assertEquals(4.0, aStarResult.totalCost(), 1e-6);
        assertEquals(List.of("Start", "N2", "N3", "N4", "Goal"), aStarResult.path());
    }

    @Test
    @DisplayName("Should return pathFound=false for unreachable node")
    void testUnreachableNode() {
        AStarAlgorithm.AStarResult result = aStar.findShortestPath(graph, "Start", "Unreachable", false);
        assertFalse(result.pathFound());
        assertEquals(-1.0, result.totalCost());
        assertTrue(result.path().isEmpty());
    }

    @Test
    @DisplayName("Should handle start equal to goal")
    void testStartEqualsGoal() {
        AStarAlgorithm.AStarResult result = aStar.findShortestPath(graph, "Start", "Start", false);
        assertTrue(result.pathFound());
        assertEquals(0.0, result.totalCost(), 1e-6);
        assertEquals(List.of("Start"), result.path());
    }

    @Test
    @DisplayName("Should generate step trace with heuristic evaluations")
    void testTraceGeneration() {
        AStarAlgorithm.AStarResult result = aStar.findShortestPath(graph, "Start", "Goal", true);
        assertNotNull(result.traceSteps());
        assertFalse(result.traceSteps().isEmpty());
    }
}
