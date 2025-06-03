package com.cad.modules.geometry.entities;

import com.cad.dxflib.common.Point2D;
import java.util.Objects;

public class Line2D implements GeometricEntity2D {
    private final Point2D startPoint;
    private final Point2D endPoint;

    public Line2D(Point2D startPoint, Point2D endPoint) {
        Objects.requireNonNull(startPoint, "Start point cannot be null");
        Objects.requireNonNull(endPoint, "End point cannot be null");
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    public Point2D getStartPoint() {
        return startPoint;
    }

    public Point2D getEndPoint() {
        return endPoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line2D line2D = (Line2D) o;
        return startPoint.equals(line2D.startPoint) &&
               endPoint.equals(line2D.endPoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPoint, endPoint);
    }

    @Override
    public String toString() {
        return "Line2D{" +
               "startPoint=" + startPoint +
               ", endPoint=" + endPoint +
               '}';
    }
}
