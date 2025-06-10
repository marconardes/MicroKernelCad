package com.cad.dxflib.common;

import java.util.Objects;

public class Point3D {
    public final double x;
    public final double y;
    public final double z;

    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Construtor para criar Point3D a partir de Point2D (z = 0.0)
    public Point3D(Point2D point2D) {
        this(point2D.x, point2D.y, 0.0);
    }


    @Override
    public boolean equals(Object o) {
    if (this == o) {
        return true;
    }
    if (o == null || getClass() != o.getClass()) {
        return false;
    }
        Point3D point3D = (Point3D) o;
        return Double.compare(point3D.x, x) == 0 &&
               Double.compare(point3D.y, y) == 0 &&
               Double.compare(point3D.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Point3D{" +
               "x=" + x +
               ", y=" + y +
               ", z=" + z +
               '}';
    }
}
