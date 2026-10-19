package com.routewise.algorithm;

import java.util.*;

public class Graph {
    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final Map<String, List<Edge>> adjacencyList = new LinkedHashMap<>();

    public void addNode(Node node) {
        nodes.put(node.getId(), node);
        adjacencyList.putIfAbsent(node.getId(), new ArrayList<>());
    }

    public Node getOrCreateNode(String id, double x, double y) {
        return nodes.computeIfAbsent(id, k -> {
            Node n = new Node(id, x, y);
            adjacencyList.putIfAbsent(id, new ArrayList<>());
            return n;
        });
    }

    public Node getNode(String id) {
        return nodes.get(id);
    }

    public boolean containsNode(String id) {
        return nodes.containsKey(id);
    }

    public Collection<Node> getNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    public void addEdge(String sourceId, String targetId, double weight, boolean directed) {
        Node src = nodes.get(sourceId);
        Node tgt = nodes.get(targetId);
        if (src == null || tgt == null) {
            throw new IllegalArgumentException("Both nodes must exist in the graph before adding an edge");
        }
        Edge edge = new Edge(src, tgt, weight, directed);
        adjacencyList.computeIfAbsent(sourceId, k -> new ArrayList<>()).add(edge);
        if (!directed) {
            Edge reverseEdge = new Edge(tgt, src, weight, false);
            adjacencyList.computeIfAbsent(targetId, k -> new ArrayList<>()).add(reverseEdge);
        }
    }

    public List<Edge> getNeighbors(String nodeId) {
        return adjacencyList.getOrDefault(nodeId, Collections.emptyList());
    }

    public List<Edge> getAllEdges() {
        List<Edge> edges = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (List<Edge> list : adjacencyList.values()) {
            for (Edge e : list) {
                if (e.isDirected()) {
                    edges.add(e);
                } else {
                    String key = e.getSource().getId().compareTo(e.getTarget().getId()) < 0
                            ? e.getSource().getId() + "-" + e.getTarget().getId()
                            : e.getTarget().getId() + "-" + e.getSource().getId();
                    if (seen.add(key)) {
                        edges.add(e);
                    }
                }
            }
        }
        return edges;
    }

    public int getVertexCount() {
        return nodes.size();
    }
}
