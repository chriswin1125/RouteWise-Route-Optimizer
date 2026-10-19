package com.routewise.controller;

import com.routewise.dto.algorithm.AlgorithmTraceRequest;
import com.routewise.dto.algorithm.AlgorithmTraceResponse;
import com.routewise.dto.algorithm.GraphDto;
import com.routewise.service.AlgorithmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/algorithms")
@Tag(name = "Algorithms", description = "Endpoints for step-by-step Dijkstra and A* graph algorithm visualization")
public class AlgorithmController {

    private final AlgorithmService algorithmService;

    public AlgorithmController(AlgorithmService algorithmService) {
        this.algorithmService = algorithmService;
    }

    @PostMapping("/dijkstra/trace")
    @Operation(summary = "Execute Dijkstra's algorithm step-by-step on a weighted graph with priority queue inspection")
    public ResponseEntity<AlgorithmTraceResponse> traceDijkstra(@Valid @RequestBody AlgorithmTraceRequest request) {
        AlgorithmTraceRequest dijkstraReq = new AlgorithmTraceRequest(
                request.graphPreset(),
                request.customGraph(),
                request.sourceNode(),
                request.destinationNode(),
                "DIJKSTRA"
        );
        AlgorithmTraceResponse response = algorithmService.traceAlgorithm(dijkstraReq);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/a-star/shortest-path")
    @Operation(summary = "Execute A* search step-by-step with Euclidean heuristic evaluation")
    public ResponseEntity<AlgorithmTraceResponse> traceAStar(@Valid @RequestBody AlgorithmTraceRequest request) {
        AlgorithmTraceRequest aStarReq = new AlgorithmTraceRequest(
                request.graphPreset(),
                request.customGraph(),
                request.sourceNode(),
                request.destinationNode(),
                "ASTAR"
        );
        AlgorithmTraceResponse response = algorithmService.traceAlgorithm(aStarReq);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sample-graph")
    @Operation(summary = "Get the standard sample logistics network graph topology")
    public ResponseEntity<GraphDto> getSampleGraph() {
        GraphDto graph = algorithmService.getSampleGraph();
        return ResponseEntity.ok(graph);
    }
}
