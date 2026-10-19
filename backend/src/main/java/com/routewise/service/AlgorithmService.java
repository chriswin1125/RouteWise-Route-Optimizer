package com.routewise.service;

import com.routewise.algorithm.AStarAlgorithm;
import com.routewise.algorithm.DijkstraAlgorithm;
import com.routewise.algorithm.Graph;
import com.routewise.algorithm.Node;
import com.routewise.dto.algorithm.AlgorithmTraceRequest;
import com.routewise.dto.algorithm.AlgorithmTraceResponse;
import com.routewise.dto.algorithm.GraphDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AlgorithmService {

    private final DijkstraAlgorithm dijkstra = new DijkstraAlgorithm();
    private final AStarAlgorithm aStar = new AStarAlgorithm();

    public AlgorithmTraceResponse traceAlgorithm(AlgorithmTraceRequest request) {
        Graph graph;
        GraphDto graphDto;

        if ("CUSTOM".equalsIgnoreCase(request.graphPreset()) && request.customGraph() != null) {
            graphDto = request.customGraph();
            graph = buildGraphFromDto(graphDto);
        } else {
            graphDto = getSampleLogisticsNetworkDto();
            graph = buildGraphFromDto(graphDto);
        }

        String algorithm = request.resolvedAlgorithm();
        long startTime = System.nanoTime();

        if ("ASTAR".equalsIgnoreCase(algorithm)) {
            AStarAlgorithm.AStarResult result = aStar.findShortestPath(graph, request.sourceNode(), request.destinationNode(), true);
            long elapsed = System.nanoTime() - startTime;

            AlgorithmTraceResponse.AlgorithmComplexityDto complexity = new AlgorithmTraceResponse.AlgorithmComplexityDto(
                    "O(|E| log |V|) typical, O((|V| + |E|) log |V|) worst-case",
                    "O(|V|) for Open/Closed sets, gScore, and fScore mappings",
                    "A* directs search toward the goal using an admissible Euclidean heuristic h(n) <= d(n, target). It explores fewer nodes than Dijkstra while guaranteeing an optimal path."
            );

            return new AlgorithmTraceResponse(
                    "ASTAR",
                    graphDto,
                    request.sourceNode(),
                    request.destinationNode(),
                    result.pathFound(),
                    result.path(),
                    result.totalCost() >= 0 ? Math.round(result.totalCost() * 100.0) / 100.0 : null,
                    result.traceSteps(),
                    result.traceSteps().size(),
                    elapsed,
                    complexity
            );
        } else {
            DijkstraAlgorithm.DijkstraResult result = dijkstra.findShortestPath(graph, request.sourceNode(), request.destinationNode(), true);
            long elapsed = System.nanoTime() - startTime;

            AlgorithmTraceResponse.AlgorithmComplexityDto complexity = new AlgorithmTraceResponse.AlgorithmComplexityDto(
                    "O((|V| + |E|) log |V|) with Binary Min-Heap (PriorityQueue)",
                    "O(|V| + |E|) for Adjacency List, Distances, Visited set, and Predecessors",
                    "Dijkstra greedily expands vertices in order of increasing confirmed distance from the source. It guarantees global shortest paths across non-negative weighted graphs."
            );

            return new AlgorithmTraceResponse(
                    "DIJKSTRA",
                    graphDto,
                    request.sourceNode(),
                    request.destinationNode(),
                    result.pathFound(),
                    result.path(),
                    result.totalCost() >= 0 ? Math.round(result.totalCost() * 100.0) / 100.0 : null,
                    result.traceSteps(),
                    result.traceSteps().size(),
                    elapsed,
                    complexity
            );
        }
    }

    public GraphDto getSampleGraph() {
        return getSampleLogisticsNetworkDto();
    }

    private Graph buildGraphFromDto(GraphDto dto) {
        Graph graph = new Graph();
        for (GraphDto.GraphNodeDto n : dto.nodes()) {
            graph.addNode(new Node(n.id(), n.label(), n.x(), n.y()));
        }
        for (GraphDto.GraphEdgeDto e : dto.edges()) {
            graph.addEdge(e.source(), e.target(), e.weight(), e.directed());
        }
        return graph;
    }

    private GraphDto getSampleLogisticsNetworkDto() {
        // Standard metropolitan logistics graph with 7 distribution nodes
        List<GraphDto.GraphNodeDto> nodes = List.of(
                new GraphDto.GraphNodeDto("Depot", "Main Depot", 100, 250),
                new GraphDto.GraphNodeDto("HubA", "North Hub A", 260, 100),
                new GraphDto.GraphNodeDto("HubB", "Central Hub B", 300, 250),
                new GraphDto.GraphNodeDto("HubC", "South Hub C", 260, 400),
                new GraphDto.GraphNodeDto("Zone1", "Delivery Zone 1", 500, 100),
                new GraphDto.GraphNodeDto("Zone2", "Delivery Zone 2", 520, 260),
                new GraphDto.GraphNodeDto("Zone3", "Delivery Zone 3", 500, 410),
                new GraphDto.GraphNodeDto("Terminal", "Port Terminal", 700, 250)
        );

        List<GraphDto.GraphEdgeDto> edges = List.of(
                new GraphDto.GraphEdgeDto("e1", "Depot", "HubA", 4.0, false),
                new GraphDto.GraphEdgeDto("e2", "Depot", "HubB", 3.0, false),
                new GraphDto.GraphEdgeDto("e3", "Depot", "HubC", 7.0, false),
                new GraphDto.GraphEdgeDto("e4", "HubA", "HubB", 2.0, false),
                new GraphDto.GraphEdgeDto("e5", "HubA", "Zone1", 5.0, false),
                new GraphDto.GraphEdgeDto("e6", "HubB", "Zone1", 6.0, false),
                new GraphDto.GraphEdgeDto("e7", "HubB", "Zone2", 3.0, false),
                new GraphDto.GraphEdgeDto("e8", "HubB", "HubC", 3.0, false),
                new GraphDto.GraphEdgeDto("e9", "HubC", "Zone3", 4.0, false),
                new GraphDto.GraphEdgeDto("e10", "Zone1", "Terminal", 6.0, false),
                new GraphDto.GraphEdgeDto("e11", "Zone2", "Terminal", 4.0, false),
                new GraphDto.GraphEdgeDto("e12", "Zone3", "Terminal", 5.0, false),
                new GraphDto.GraphEdgeDto("e13", "Zone2", "Zone3", 2.0, false)
        );

        return new GraphDto(nodes, edges);
    }
}
