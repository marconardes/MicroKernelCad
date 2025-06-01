package com.cad.dxflib.entities;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point3D;

public class DxfArc extends AbstractDxfEntity {
    private Point3D center;
    private double radius;
    private double startAngle;
    private double endAngle;

    public DxfArc() {
        this.center = new Point3D(0, 0, 0);
        this.radius = 1.0;
        this.startAngle = 0.0;
        this.endAngle = 360.0; // Default to a full circle if angles are not specified
    }

    public Point3D getCenter() {
        return center;
    }

    public void setCenter(Point3D center) {
        this.center = center;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Arc radius must be positive.");
        }
        this.radius = radius;
    }

    public double getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(double startAngle) {
        this.startAngle = startAngle;
    }

    public double getEndAngle() {
        return endAngle;
    }

    public void setEndAngle(double endAngle) {
        this.endAngle = endAngle;
    }

    @Override
    public EntityType getType() {
        return EntityType.ARC;
    }

    // getBounds() and transform() to be implemented later

    @Override
    public String toString() {
        return "DxfArc{" +
               "center=" + center +
               ", radius=" + radius +
               ", startAngle=" + startAngle +
               ", endAngle=" + endAngle +
               ", layer='" + layerName + '\'' +
               ", color=" + color +
               '}';
    }
}
