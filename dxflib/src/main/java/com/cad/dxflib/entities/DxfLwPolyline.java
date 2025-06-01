package com.cad.dxflib.entities;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point2D; // LWPOLYLINE vertices are 2D with an elevation
import com.cad.dxflib.common.Point3D; // For converting Point2D + elevation
import com.cad.dxflib.math.Bounds;
import java.util.ArrayList;
import java.util.List;

public class DxfLwPolyline extends AbstractDxfEntity {
    private List<Point2D> vertices;
    private double elevation = 0.0;
    private boolean closed = false;
    private double constantWidth = 0.0; // Default, can be overridden by vertex width

    // Note: Bulge (group code 42) for arcs within LWPOLYLINE is complex.
    // We might store it as a list of doubles parallel to vertices or embed in a Vertex class.
    // For now, let's just store the vertices and basic properties.
    // Consider a private static class Vertex { Point2D point; double bulge; double startWidth; double endWidth; } later.
    private List<Double> bulges; // One bulge value for each vertex *before* the arc segment

    public DxfLwPolyline() {
        this.vertices = new ArrayList<>();
        this.bulges = new ArrayList<>();
    }

    public void addVertex(Point2D vertex) {
        this.vertices.add(vertex);
        // To keep vertices and bulges lists in sync if addVertex(Point2D) is used,
        // we should add a default bulge value. Or, make this method private/protected
        // and only expose addVertex(Point2D, double bulge) publicly.
        // For now, let's add a default 0.0 bulge.
        this.bulges.add(0.0);
    }
     public void addVertex(Point2D vertex, double bulge) {
        this.vertices.add(vertex);
        this.bulges.add(bulge); // This assumes one bulge per vertex. DXF stores bulge on the *start* vertex of the arc segment.
    }


    public List<Point2D> getVertices() {
        return vertices;
    }

    public List<Double> getBulges() {
        return bulges;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public double getConstantWidth() {
        return constantWidth;
    }

    public void setConstantWidth(double constantWidth) {
        this.constantWidth = constantWidth;
    }

    public int getNumberOfVertices() {
        return this.vertices.size();
    }

    @Override
    public EntityType getType() {
        return EntityType.LWPOLYLINE;
    }

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
        return bounds;
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
