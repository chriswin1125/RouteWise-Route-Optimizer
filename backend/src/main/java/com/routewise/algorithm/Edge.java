package com.routewise.algorithm;

import java.util.Objects;

public class Edge {
    private final Node source;
    private final Node target;
    private final double weight;
    private final boolean directed;

    public Edge(Node source, Node target, double weight, boolean directed) {
        if (weight < 0) {
            throw new IllegalArgumentException("Edge weight cannot be negative: " + weight);
        }
        this.source = Objects.requireNonNull(source, "Source node cannot be null");
        this.target = Objects.requireNonNull(target, "Target node cannot be null");
        this.weight = weight;
        this.directed = directed;
    }

    public Edge(Node source, Node target, double weight) {
        this(source, target, weight, false);
    }

    public Node getSource() {
        return source;
    }

    public Node getTarget() {
        return target;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isDirected() {
        return directed;
    }

    @Override
    public String toString() {
        return source.getId() + (directed ? " -> " : " <-> ") + target.getId() + " (" + weight + ")";
    }
}
