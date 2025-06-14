package com.cad.dxflib.common;

import java.util.Objects;

public class Point2D {
    public final double x;
    public final double y;

    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distanceTo(Point2D other) {
        double deltaX = other.x - this.x;
        double deltaY = other.y - this.y;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    @Override
    public boolean equals(Object o) {
    if (this == obj) {
        return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
        return false;
    }
        Point2D point2D = (Point2D) o;
        return Double.compare(point2D.x, x) == 0 &&
               Double.compare(point2D.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Point2D{" +
               "x=" + x +
               ", y=" + y +
               '}';
    }
}
