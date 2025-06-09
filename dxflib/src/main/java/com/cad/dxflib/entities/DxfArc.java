package com.cad.dxflib.entities;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.math.Bounds;

/**
 * Represents an ARC entity in a DXF file.
 * An arc is defined by a center point, radius, start angle, and end angle.
 * Angles are typically in degrees, with 0 degrees along the positive X-axis,
 * and increasing counter-clockwise.
 */
public class DxfArc extends AbstractDxfEntity {
    private Point3D center;     // Center point of the arc (codes 10, 20, 30)
    private double radius;      // Radius of the arc (code 40)
    private double startAngle;  // Start angle in degrees (code 50)
    private double endAngle;    // End angle in degrees (code 51)

    /**
     * Constructs a new DxfArc.
     * Initializes center to (0,0,0), radius to 1.0, startAngle to 0.0, and endAngle to 360.0 (a full circle).
     */
    public DxfArc() {
        super();
        this.center = new Point3D(0, 0, 0);
        this.radius = 1.0;
        this.startAngle = 0.0;
        this.endAngle = 360.0;
    }

    /**
     * Gets the center point of the arc.
     * @return The center point.
     */
    public Point3D getCenter() {
        return center;
    }

    /**
     * Sets the center point of the arc.
     * @param center The new center point. Defaults to (0,0,0) if null.
     */
    public void setCenter(Point3D center) {
        this.center = center != null ? center : new Point3D(0,0,0);
    }

    /**
     * Gets the radius of the arc.
     * @return The radius.
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Sets the radius of the arc.
     * @param radius The new radius. Must be positive.
     * @throws IllegalArgumentException if the radius is not positive.
     */
    public void setRadius(double radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Arc radius must be positive.");
        }
        this.radius = radius;
    }

    /**
     * Gets the start angle of the arc in degrees.
     * @return The start angle.
     */
    public double getStartAngle() {
        return startAngle;
    }

    /**
     * Sets the start angle of the arc in degrees.
     * @param startAngle The new start angle.
     */
    public void setStartAngle(double startAngle) {
        this.startAngle = startAngle;
    }

    /**
     * Gets the end angle of the arc in degrees.
     * @return The end angle.
     */
    public double getEndAngle() {
        return endAngle;
    }

    /**
     * Sets the end angle of the arc in degrees.
     * @param endAngle The new end angle.
     */
    public void setEndAngle(double endAngle) {
        this.endAngle = endAngle;
    }

    @Override
    public EntityType getType() {
        return EntityType.ARC;
    }

    /**
     * Calculates the bounding box of the arc.
     * This implementation provides a simplified bounding box that encompasses the full circle
     * from which the arc is derived. A more precise calculation would involve determining
     * the actual extents based on the start and end angles.
     * @return A {@link Bounds} object representing the arc's extents.
     */
    @Override
    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        if (center != null && radius > 0) {
            // Simplified bounds: takes the full circle containing the arc.
            // TODO: More precise calculation would involve checking start/end points
            // and points at 0, 90, 180, 270 degrees if they fall within the arc span.
            bounds.addToBounds(center.x - radius, center.y - radius, center.z);
            bounds.addToBounds(center.x + radius, center.y + radius, center.z);
        }
        return bounds.isValid() ? bounds : null;
    }

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
