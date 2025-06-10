package com.cad.dxflib.entities;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.math.Bounds;

/**
 * Represents a LINE entity in a DXF file.
 * A line is defined by a start point and an end point.
 */
public class DxfLine extends AbstractDxfEntity {
    private Point3D startPoint; // Start point of the line (codes 10, 20, 30)
    private Point3D endPoint;   // End point of the line (codes 11, 21, 31)

    /**
     * Constructs a new DxfLine.
     * Both start and end points are initialized to (0,0,0).
     */
    public DxfLine() {
        super();
        this.startPoint = new Point3D(0, 0, 0);
        this.endPoint = new Point3D(0, 0, 0);
    }

    /**
     * Gets the start point of the line.
     * @return The start point.
     */
    public Point3D getStartPoint() {
        return startPoint;
    }

    /**
     * Sets the start point of the line.
     * @param startPoint The new start point.
     */
    public void setStartPoint(Point3D startPoint) {
        this.startPoint = startPoint != null ? startPoint : new Point3D(0,0,0);
    }

    /**
     * Gets the end point of the line.
     * @return The end point.
     */
    public Point3D getEndPoint() {
        return endPoint;
    }

    /**
     * Sets the end point of the line.
     * @param endPoint The new end point.
     */
    public void setEndPoint(Point3D endPoint) {
        this.endPoint = endPoint != null ? endPoint : new Point3D(0,0,0);
    }

    @Override
    public EntityType getType() {
        return EntityType.LINE;
    }

    /**
     * Calculates the bounding box of the line.
     * The bounds are determined by the start and end points of the line.
     * @return A {@link Bounds} object representing the line's extents.
     */
    @Override
    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        if (startPoint != null) {
            bounds.addToBounds(this.startPoint);
        }
        if (endPoint != null) {
            bounds.addToBounds(this.endPoint);
        }
        return bounds.isValid() ? bounds : null;
    }


    @Override
    public String toString() {
        return "DxfLine{" +
               "start=" + startPoint +
               ", end=" + endPoint +
               ", layer='" + layerName + '\'' +
               ", color=" + color +
               '}';
    }
}
