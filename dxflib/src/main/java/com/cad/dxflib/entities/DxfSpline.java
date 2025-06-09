package com.cad.dxflib.entities;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.math.Bounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a SPLINE entity in a DXF file.
 * Splines are complex curves defined by control points, knots, and degree.
 */
public class DxfSpline extends AbstractDxfEntity {

    private Point3D normalVector = new Point3D(0, 0, 1); // Default Z-axis (code 210, 220, 230)
    private int flags; // Spline flags (code 70: 1=closed, 2=periodic, 4=rational, 8=planar, 16=linear)
    private int degree; // Degree of the spline curve (code 71)
    private int numberOfKnots; // Number of knot values (code 72)
    private int numberOfControlPoints; // Number of control points (code 73)
    private int numberOfFitPoints; // Number of fit points (code 74)

    private final List<Double> knots = new ArrayList<>(); // Knot values (code 40, repeated)
    private final List<Point3D> controlPoints = new ArrayList<>(); // Control points (codes 10,20,30 repeated)
    private final List<Point3D> fitPoints = new ArrayList<>(); // Fit points (codes 11,21,31 repeated)

    private double knotTolerance = 0.0000001; // Knot tolerance (code 42)
    private double controlPointTolerance = 0.0000001; // Control point tolerance (code 43)
    private double fitTolerance = 0.0000000001; // Fit tolerance (code 44)

    /**
     * Constructs a new DxfSpline.
     * Initializes lists for knots, control points, and fit points.
     * Sets default normal vector to (0,0,1) and default tolerances.
     */
    public DxfSpline() {
        super(); // Calls AbstractDxfEntity's no-arg constructor
    }

    @Override
    public EntityType getType() {
        return EntityType.SPLINE;
    }

    /**
     * Gets the normal vector of the spline. Defaults to (0,0,1) if not specified.
     * @return The normal vector as a Point3D.
     */
    public Point3D getNormalVector() {
        return normalVector;
    }

    /**
     * Sets the normal vector of the spline.
     * @param normalVector The normal vector. If null, the default (0,0,1) might be assumed by some setters or operations.
     */
    public void setNormalVector(Point3D normalVector) {
        this.normalVector = normalVector != null ? normalVector : new Point3D(0,0,1);
    }

    /**
     * Gets the spline flags.
     * Bit-coded: 1=Closed, 2=Periodic, 4=Rational, 8=Planar, 16=Linear (planar bit is derived).
     * @return The flags integer.
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Sets the spline flags.
     * @param flags The flags integer.
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

    /**
     * Gets the degree of the spline curve.
     * @return The degree.
     */
    public int getDegree() {
        return degree;
    }

    /**
     * Sets the degree of the spline curve.
     * @param degree The degree.
     */
    public void setDegree(int degree) {
        this.degree = degree;
    }

    /**
     * Gets the declared number of knot values.
     * @return The number of knots.
     */
    public int getNumberOfKnots() {
        return numberOfKnots;
    }

    /**
     * Sets the declared number of knot values.
     * @param numberOfKnots The number of knots.
     */
    public void setNumberOfKnots(int numberOfKnots) {
        this.numberOfKnots = numberOfKnots;
    }

    /**
     * Gets the declared number of control points.
     * @return The number of control points.
     */
    public int getNumberOfControlPoints() {
        return numberOfControlPoints;
    }

    /**
     * Sets the declared number of control points.
     * @param numberOfControlPoints The number of control points.
     */
    public void setNumberOfControlPoints(int numberOfControlPoints) {
        this.numberOfControlPoints = numberOfControlPoints;
    }

    /**
     * Gets the declared number of fit points.
     * Usually 0 if control points are used.
     * @return The number of fit points.
     */
    public int getNumberOfFitPoints() {
        return numberOfFitPoints;
    }

    /**
     * Sets the declared number of fit points.
     * @param numberOfFitPoints The number of fit points.
     */
    public void setNumberOfFitPoints(int numberOfFitPoints) {
        this.numberOfFitPoints = numberOfFitPoints;
    }

    /**
     * Gets an unmodifiable list of knot values for the spline.
     * @return An unmodifiable list of knot values.
     */
    public List<Double> getKnots() {
        return Collections.unmodifiableList(knots);
    }

    /**
     * Adds a knot value to the spline's knot list.
     * @param knot The knot value to add.
     */
    public void addKnot(double knot) {
        this.knots.add(knot);
    }

    /**
     * Gets an unmodifiable list of control points for the spline.
     * @return An unmodifiable list of control points.
     */
    public List<Point3D> getControlPoints() {
        return Collections.unmodifiableList(controlPoints);
    }

    /**
     * Adds a control point to the spline.
     * @param controlPoint The control point to add.
     */
    public void addControlPoint(Point3D controlPoint) {
        if (controlPoint != null) {
            this.controlPoints.add(controlPoint);
        }
    }

    /**
     * Gets an unmodifiable list of fit points for the spline.
     * Fit points are points that the curve must pass through.
     * @return An unmodifiable list of fit points.
     */
    public List<Point3D> getFitPoints() {
        return Collections.unmodifiableList(fitPoints);
    }

    /**
     * Adds a fit point to the spline.
     * @param fitPoint The fit point to add.
     */
    public void addFitPoint(Point3D fitPoint) {
        if (fitPoint != null) {
            this.fitPoints.add(fitPoint);
        }
    }

    /** Gets the knot tolerance. @return The knot tolerance. */
    public double getKnotTolerance() { return knotTolerance; }
    /** Sets the knot tolerance. @param knotTolerance The knot tolerance. */
    public void setKnotTolerance(double knotTolerance) { this.knotTolerance = knotTolerance; }

    /** Gets the control point tolerance. @return The control point tolerance. */
    public double getControlPointTolerance() { return controlPointTolerance; }
    /** Sets the control point tolerance. @param controlPointTolerance The control point tolerance. */
    public void setControlPointTolerance(double controlPointTolerance) { this.controlPointTolerance = controlPointTolerance; }

    /** Gets the fit tolerance. @return The fit tolerance. */
    public double getFitTolerance() { return fitTolerance; }
    /** Sets the fit tolerance. @param fitTolerance The fit tolerance. */
    public void setFitTolerance(double fitTolerance) { this.fitTolerance = fitTolerance; }

    /**
     * Calculates the bounding box of the spline.
     * This implementation provides an approximation based on the control points,
     * or fit points if control points are not available.
     * For a precise bounding box, the spline curve itself would need to be evaluated.
     * @return A {@link Bounds} object representing the approximate extents of the spline.
     *         Returns null or invalid bounds if no defining points are available.
     */
    @Override
    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        if (!controlPoints.isEmpty()) {
            for (Point3D cp : controlPoints) {
                bounds.addToBounds(cp);
            }
        } else if (!fitPoints.isEmpty()) {
            for (Point3D fp : fitPoints) {
                bounds.addToBounds(fp);
            }
        }
        return bounds.isValid() ? bounds : null;
    }

    @Override
    public String toString() {
        return "DxfSpline{" +
                "layerName='" + getLayerName() + '\'' +
                ", color=" + getColor() +
                ", flags=" + flags +
                ", degree=" + degree +
                ", numKnots=" + (knots != null ? knots.size() : "0") + "/" + numberOfKnots +
                ", numCtrlPts=" + (controlPoints != null ? controlPoints.size() : "0") + "/" + numberOfControlPoints +
                ", numFitPts=" + (fitPoints != null ? fitPoints.size() : "0") + "/" + numberOfFitPoints +
                '}';
    }
}
