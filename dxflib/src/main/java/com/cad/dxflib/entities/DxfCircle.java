package com.cad.dxflib.entities;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.math.Bounds;

public class DxfCircle extends AbstractDxfEntity {
    private Point3D center;
    private double radius;

    public DxfCircle() {
        this.center = new Point3D(0, 0, 0);
        this.radius = 1.0; // Default radius
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
            throw new IllegalArgumentException("Circle radius must be positive.");
        }
        this.radius = radius;
    }

    @Override
    public EntityType getType() {
        return EntityType.CIRCLE;
    }

    @Override
    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        if (center != null && radius > 0) {
            bounds.addToBounds(center.x - radius, center.y - radius, center.z);
            bounds.addToBounds(center.x + radius, center.y + radius, center.z);
        }
        return bounds;
    }

    // @Override
    // public void transform(Object transformContext) {
    //     // Implementation will be added later
    //     // this.center = transformContext.transform(this.center);
    //     // Radius might need scaling if transformContext includes non-uniform scaling
    // }

    @Override
    public String toString() {
        return "DxfCircle{" +
               "center=" + center +
               ", radius=" + radius +
               ", layer='" + layerName + '\'' +
               ", color=" + color +
               '}';
    }
}
