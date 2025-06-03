package com.cad.modules.geometry.entities;

import com.cad.dxflib.common.Point2D;
import java.util.Objects;

public class Circle2D implements GeometricEntity2D {
    private final Point2D center;
    private final double radius;

    public Circle2D(Point2D center, double radius) {
        Objects.requireNonNull(center, "Center point cannot be null");
        if (radius <= 0) {
            throw new IllegalArgumentException("Radius must be positive");
        }
        this.center = center;
        this.radius = radius;
    }

    public Point2D getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Circle2D circle2D = (Circle2D) o;
        return Double.compare(circle2D.radius, radius) == 0 &&
               center.equals(circle2D.center);
    }

    @Override
    public int hashCode() {
        return Objects.hash(center, radius);
    }

    @Override
    public String toString() {
        return "Circle2D{" +
               "center=" + center +
               ", radius=" + radius +
               '}';
    }
}
