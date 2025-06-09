package com.cad.dxflib.entities;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point2D;
import com.cad.dxflib.math.Bounds;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an LWPOLYLINE (Lightweight Polyline) entity in a DXF file.
 * LWPOLYLINEs are 2D polylines that can have segments of lines or arcs (defined by bulges).
 * They have a single elevation value for all vertices.
 */
public class DxfLwPolyline extends AbstractDxfEntity {
    private final List<Point2D> vertices; // List of 2D vertices (codes 10, 20 repeated)
    private double elevation = 0.0;       // Polyline elevation (code 38)
    private boolean closed = false;       // Flag if polyline is closed (code 70, bit 1)
    private double constantWidth = 0.0;   // Constant width for all segments (code 43)
                                          // If 0, segments can have individual start/end widths (not fully supported here yet).
    private final List<Double> bulges;    // List of bulge values (code 42, repeated). One bulge per vertex,
                                          // defining an arc segment starting at that vertex and ending at the next.
                                          // A bulge of 0 means a straight line segment.

    /**
     * Constructs a new, empty DxfLwPolyline.
     * Initializes vertex and bulge lists. Sets default elevation and constantWidth to 0.0,
     * and closed to false.
     */
    public DxfLwPolyline() {
        super();
        this.vertices = new ArrayList<>();
        this.bulges = new ArrayList<>();
    }

    /**
     * Adds a vertex to the polyline with a default bulge of 0 (straight segment to next vertex).
     * This method is primarily for convenience; for polylines with arcs,
     * use {@link #addVertex(Point2D, double)}.
     * @param vertex The 2D vertex point to add.
     */
    public void addVertex(Point2D vertex) {
        if (vertex != null) {
            this.vertices.add(vertex);
            this.bulges.add(0.0); // Default bulge for a straight segment
        }
    }

    /**
     * Adds a vertex to the polyline with an associated bulge value.
     * The bulge value defines the arc of the segment starting at this vertex
     * and ending at the next vertex. A bulge of 0 indicates a straight line segment.
     * @param vertex The 2D vertex point to add.
     * @param bulge The bulge value.
     */
     public void addVertex(Point2D vertex, double bulge) {
        if (vertex != null) {
            this.vertices.add(vertex);
            this.bulges.add(bulge);
        }
    }

    /**
     * Gets an unmodifiable list of 2D vertices for this polyline.
     * @return An unmodifiable list of {@link Point2D} vertices.
     */
    public List<Point2D> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    /**
     * Gets an unmodifiable list of bulge values for this polyline.
     * Each bulge corresponds to a vertex and defines the arc segment starting at that vertex.
     * @return An unmodifiable list of bulge values.
     */
    public List<Double> getBulges() {
        return Collections.unmodifiableList(bulges);
    }

    /**
     * Gets the elevation of the polyline. All vertices share this Z-coordinate.
     * @return The elevation.
     */
    public double getElevation() {
        return elevation;
    }

    /**
     * Sets the elevation of the polyline.
     * @param elevation The new elevation.
     */
    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    /**
     * Checks if the polyline is closed.
     * @return true if closed, false otherwise.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Sets whether the polyline is closed.
     * @param closed true to close the polyline, false otherwise.
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    /**
     * Gets the constant width for all segments of the polyline.
     * If 0, segments might have individual widths (not fully supported in this basic model).
     * @return The constant width.
     */
    public double getConstantWidth() {
        return constantWidth;
    }

    /**
     * Sets the constant width for all segments of the polyline.
     * @param constantWidth The new constant width.
     */
    public void setConstantWidth(double constantWidth) {
        this.constantWidth = constantWidth;
    }

    /**
     * Gets the number of vertices in the polyline.
     * @return The number of vertices.
     */
    public int getNumberOfVertices() {
        return this.vertices.size();
    }

    @Override
    public EntityType getType() {
        return EntityType.LWPOLYLINE;
    }

    /**
     * Calculates the bounding box of the LWPOLYLINE.
     * This implementation considers the X and Y coordinates of the vertices and the polyline's elevation.
     * It does not currently account for the curvature introduced by bulge values, so the bounds
     * might be smaller than the true extents if arcs are present.
     * @return A {@link Bounds} object. Returns invalid bounds if no vertices.
     */
    @Override
    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        if (vertices != null && !vertices.isEmpty()) {
            for (Point2D p : vertices) {
                bounds.addToBounds(p.x, p.y, this.elevation);
            }
            // TODO: For more accuracy, if bulges are present, the arc segments
            // formed by bulges should also be considered in the bounds calculation.
        }
        return bounds.isValid() ? bounds : null;
    }

    @Override
    public String toString() {
        return "DxfLwPolyline{" +
               "vertices=" + vertices.size() +
               ", closed=" + closed +
               ", layer='" + layerName + '\'' +
               ", color=" + color +
               '}';
    }
}
