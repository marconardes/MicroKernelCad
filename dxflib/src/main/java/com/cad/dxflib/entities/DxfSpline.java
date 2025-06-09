package com.cad.dxflib.entities;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.math.Bounds;

import java.util.ArrayList;
import java.util.List;

public class DxfSpline extends AbstractDxfEntity {

    private Point3D normalVector = new Point3D(0, 0, 1); // Default Z-axis
    private int flags; // code 70
    private int degree; // code 71
    private int numberOfKnots; // code 72
    private int numberOfControlPoints; // code 73
    private int numberOfFitPoints; // code 74

    private List<Double> knots = new ArrayList<>(); // code 40 (repeated)
    private List<Point3D> controlPoints = new ArrayList<>(); // codes 10, 20, 30 (repeated)
    private List<Point3D> fitPoints = new ArrayList<>(); // codes 11, 21, 31 (repeated)

    private double knotTolerance = 0.0000001; // code 42 (default)
    private double controlPointTolerance = 0.0000001; // code 43 (default)
    private double fitTolerance = 0.0000000001; // code 44 (default)

    public DxfSpline() {
        super(EntityType.SPLINE);
    }

    public Point3D getNormalVector() {
        return normalVector;
    }

    public void setNormalVector(Point3D normalVector) {
        this.normalVector = normalVector;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public int getNumberOfKnots() {
        return numberOfKnots;
    }

    public void setNumberOfKnots(int numberOfKnots) {
        this.numberOfKnots = numberOfKnots;
    }

    public int getNumberOfControlPoints() {
        return numberOfControlPoints;
    }

    public void setNumberOfControlPoints(int numberOfControlPoints) {
        this.numberOfControlPoints = numberOfControlPoints;
    }

    public int getNumberOfFitPoints() {
        return numberOfFitPoints;
    }

    public void setNumberOfFitPoints(int numberOfFitPoints) {
        this.numberOfFitPoints = numberOfFitPoints;
    }

    public List<Double> getKnots() {
        return knots;
    }

    public void addKnot(double knot) {
        this.knots.add(knot);
    }

    public List<Point3D> getControlPoints() {
        return controlPoints;
    }

    public void addControlPoint(Point3D controlPoint) {
        this.controlPoints.add(controlPoint);
    }

    public List<Point3D> getFitPoints() {
        return fitPoints;
    }

    public void addFitPoint(Point3D fitPoint) {
        this.fitPoints.add(fitPoint);
    }

    public double getKnotTolerance() {
        return knotTolerance;
    }

    public void setKnotTolerance(double knotTolerance) {
        this.knotTolerance = knotTolerance;
    }

    public double getControlPointTolerance() {
        return controlPointTolerance;
    }

    public void setControlPointTolerance(double controlPointTolerance) {
        this.controlPointTolerance = controlPointTolerance;
    }

    public double getFitTolerance() {
        return fitTolerance;
    }

    public void setFitTolerance(double fitTolerance) {
        this.fitTolerance = fitTolerance;
    }

    @Override
    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        if (!controlPoints.isEmpty()) {
            for (Point3D cp : controlPoints) {
                bounds.addPoint(cp);
            }
        } else if (!fitPoints.isEmpty()) {
            // If only fit points are available, use them for bounds.
            // This might be less accurate for the actual spline shape than control points.
            for (Point3D fp : fitPoints) {
                bounds.addPoint(fp);
            }
        }
        // Note: This is an approximation. For a more precise bounding box,
        // the spline would need to be evaluated or its properties (like convex hull of control points) used.
        return bounds.isValid() ? bounds : null;
    }

    @Override
    public String toString() {
        return "DxfSpline{" +
                "layerName='" + getLayerName() + '\'' +
                ", color=" + getColor() +
                ", normalVector=" + normalVector +
                ", flags=" + flags +
                ", degree=" + degree +
                ", numberOfKnots=" + numberOfKnots +
                ", numberOfControlPoints=" + numberOfControlPoints +
                ", numberOfFitPoints=" + numberOfFitPoints +
                ", knots.size=" + knots.size() +
                ", controlPoints.size=" + controlPoints.size() +
                ", fitPoints.size=" + fitPoints.size() +
                '}';
    }
}
