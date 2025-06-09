package com.cad.dxflib.entities;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.math.Bounds;

/**
 * Represents a CIRCLE entity in a DXF file.
 * A circle is defined by a center point and a radius.
 */
public class DxfCircle extends AbstractDxfEntity {
    private Point3D center; // Center point of the circle (codes 10, 20, 30)
    private double radius;  // Radius of the circle (code 40)

    /**
     * Constructs a new DxfCircle.
     * The center is initialized to (0,0,0) and the radius to 1.0.
     */
    public DxfCircle() {
        super();
        this.center = new Point3D(0, 0, 0);
        this.radius = 1.0; // Default radius
    }

    /**
     * Gets the center point of the circle.
     * @return The center point.
     */
    public Point3D getCenter() {
        return center;
    }

    /**
     * Sets the center point of the circle.
     * @param center The new center point. Defaults to (0,0,0) if null.
     */
    public void setCenter(Point3D center) {
        this.center = center != null ? center : new Point3D(0,0,0);
    }

    /**
     * Gets the radius of the circle.
     * @return The radius.
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Sets the radius of the circle.
     * @param radius The new radius. Must be positive.
     * @throws IllegalArgumentException if the radius is not positive.
     */
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

    /**
     * Calculates the bounding box of the circle.
     * The bounds are determined by the center point and the radius.
     * @return A {@link Bounds} object representing the circle's extents.
     */
    @Override
    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        if (center != null && radius > 0) {
            bounds.addToBounds(center.x - radius, center.y - radius, center.z);
            bounds.addToBounds(center.x + radius, center.y + radius, center.z);
        }
        return bounds.isValid() ? bounds : null;
    }

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
