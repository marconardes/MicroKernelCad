package com.cad.modules.geometry.entities;

import com.cad.dxflib.common.Point2D;

public class Circle2D { // Considerar implementar uma interface GeometricEntity no futuro
    private Point2D center;
    private double radius;

    public Circle2D(Point2D center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    public Point2D getCenter() {
        return center;
    }

    public void setCenter(Point2D center) {
        this.center = center;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public String toString() {
        return "Circle2D{center=" + center + ", radius=" + radius + "}";
    }
}
