package com.cad.modules.geometry.entities;

import com.cad.dxflib.common.Point2D;
import java.util.Objects;

public class Arc2D implements GeometricEntity2D {
    private final Point2D center;
    private final double radius;
    private final double startAngle; // Degrees
    private final double endAngle;   // Degrees

    public Arc2D(Point2D center, double radius, double startAngle, double endAngle) {
        Objects.requireNonNull(center, "Center point cannot be null");
        if (radius <= 0) {
            throw new IllegalArgumentException("Radius must be positive");
        }
        this.center = center;
        this.radius = radius;
        this.startAngle = startAngle;
        this.endAngle = endAngle;
    }

    public Point2D getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    public double getStartAngle() {
        return startAngle;
    }

    public double getEndAngle() {
        return endAngle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arc2D arc2D = (Arc2D) o;
        return Double.compare(arc2D.radius, radius) == 0 &&
               Double.compare(arc2D.startAngle, startAngle) == 0 &&
               Double.compare(arc2D.endAngle, endAngle) == 0 &&
               center.equals(arc2D.center);
    }

    @Override
    public int hashCode() {
        return Objects.hash(center, radius, startAngle, endAngle);
    }

    @Override
    public String toString() {
        return "Arc2D{" +
               "center=" + center +
               ", radius=" + radius +
               ", startAngle=" + startAngle +
               ", endAngle=" + endAngle +
               '}';
    }
}
