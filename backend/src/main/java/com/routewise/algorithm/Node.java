package com.routewise.algorithm;

import java.util.Objects;

public class Node {
    private final String id;
    private final String label;
    private final double x;
    private final double y;

    public Node(String id, String label, double x, double y) {
        this.id = id;
        this.label = label != null ? label : id;
        this.x = x;
        this.y = y;
    }

    public Node(String id, double x, double y) {
        this(id, id, x, y);
    }

    public Node(String id) {
        this(id, id, 0.0, 0.0);
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(id, node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Node{" + id + "}";
    }
}
