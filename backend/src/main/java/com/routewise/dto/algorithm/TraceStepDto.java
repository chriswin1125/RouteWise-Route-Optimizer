package com.routewise.dto.algorithm;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record TraceStepDto(
        int stepNumber,
        String description,
        String currentNode,
        String examiningNeighbor,
        Double tentativeDistanceThroughCurrent,
        Set<String> visitedNodes,
        Set<String> unvisitedNodes,
        Map<String, Double> shortestDistances,
        Map<String, String> predecessors,
        List<String> priorityQueueState,
        List<String> currentPathCandidate
) {}
