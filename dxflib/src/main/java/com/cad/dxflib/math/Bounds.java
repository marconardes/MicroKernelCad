package com.cad.dxflib.math;

import com.cad.dxflib.common.Point3D;

public class Bounds {
    private double minX = Double.POSITIVE_INFINITY;
    private double minY = Double.POSITIVE_INFINITY;
    private double minZ = Double.POSITIVE_INFINITY; // Though less used for 2D SVG viewBox
    private double maxX = Double.NEGATIVE_INFINITY;
    private double maxY = Double.NEGATIVE_INFINITY;
    private double maxZ = Double.NEGATIVE_INFINITY; // Though less used for 2D SVG viewBox
    private boolean initialized = false;

    public void addToBounds(Point3D point) {
        if (point == null) {
            return;
        }
        updateBounds(point.x, point.y, point.z);
    }

    public void addToBounds(double x, double y, double z) {
        updateBounds(x, y, z);
    }

    public void addToBounds(double x, double y) { // Convenience for 2D
        updateBounds(x, y, 0); // Assume z=0 for 2D points if not specified
    }

    public void addToBounds(Bounds other) {
        if (other != null && other.isValid()) {
            updateBounds(other.minX, other.minY, other.minZ);
            updateBounds(other.maxX, other.maxY, other.maxZ);
        }
    }

    private void updateBounds(double x, double y, double z) {
        if (x < minX) {
            minX = x;
        }
        if (y < minY) {
            minY = y;
        }
        if (z < minZ) {
            minZ = z;
        }
        if (x > maxX) {
            maxX = x;
        }
        if (y > maxY) {
            maxY = y;
        }
        if (z > maxZ) {
            maxZ = z;
        }
        initialized = true;
    }

    public boolean isValid() {
        return initialized &&
               minX != Double.POSITIVE_INFINITY &&
               maxX != Double.NEGATIVE_INFINITY &&
               minY != Double.POSITIVE_INFINITY &&
               maxY != Double.NEGATIVE_INFINITY;
    }

    public double getMinX() { return isValid() ? minX : 0.0; }
    public double getMinY() { return isValid() ? minY : 0.0; }
    public double getMinZ() { return isValid() ? minZ : 0.0; }
    public double getMaxX() { return isValid() ? maxX : 0.0; }
    public double getMaxY() { return isValid() ? maxY : 0.0; }
    public double getMaxZ() { return isValid() ? maxZ : 0.0; }

    public double getWidth() {
        return isValid() ? maxX - minX : 0.0;
    }

    public double getHeight() {
        return isValid() ? maxY - minY : 0.0;
    }

    public double getDepth() {
         return isValid() ? maxZ - minZ : 0.0;
    }

    @Override
    public String toString() {
        if (!isValid()) {
            return "Bounds{invalid}";
        }
        return "Bounds{" +
               "minX=" + minX + ", minY=" + minY + ", minZ=" + minZ +
               ", maxX=" + maxX + ", maxY=" + maxY + ", maxZ=" + maxZ +
               '}';
    }
}
