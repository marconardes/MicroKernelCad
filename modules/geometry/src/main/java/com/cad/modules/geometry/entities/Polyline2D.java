package com.cad.modules.geometry.entities;

import com.cad.dxflib.common.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Polyline2D implements GeometricEntity2D {
    private final List<Point2D> vertices;
    private final boolean closed;

    public Polyline2D(List<Point2D> vertices, boolean closed) {
        Objects.requireNonNull(vertices, "Vertices list cannot be null");
        if (vertices.isEmpty()) {
            throw new IllegalArgumentException("Vertices list cannot be empty");
        }
        // Ensure all points in the list are non-null
        for (Point2D vertex : vertices) {
            Objects.requireNonNull(vertex, "Vertex in list cannot be null");
        }
        this.vertices = Collections.unmodifiableList(new ArrayList<>(vertices)); // Make list unmodifiable
        this.closed = closed;
    }

    public List<Point2D> getVertices() {
        return vertices;
    }

    public boolean isClosed() {
        return closed;
    }

    public int getNumberOfVertices() {
        return vertices.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Polyline2D that = (Polyline2D) o;
        return closed == that.closed &&
               vertices.equals(that.vertices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertices, closed);
    }

    @Override
    public String toString() {
        return "Polyline2D{" +
               "vertices=" + vertices +
               ", closed=" + closed +
               '}';
    }
}
