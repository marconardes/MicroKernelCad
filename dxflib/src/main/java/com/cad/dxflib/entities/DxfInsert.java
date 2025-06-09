package com.cad.dxflib.entities;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.math.Bounds;

/**
 * Represents an INSERT entity in a DXF file.
 * An INSERT entity is an instance of a BLOCK definition, placed at a specific
 * insertion point, with optional scaling and rotation.
 */
public class DxfInsert extends AbstractDxfEntity {
    private String blockName;       // Name of the block to insert (code 2)
    private Point3D insertionPoint; // Insertion point (codes 10, 20, 30)
    private double xScale = 1.0;    // X scale factor (code 41, optional, default 1.0)
    private double yScale = 1.0;    // Y scale factor (code 42, optional, default 1.0)
    // Note: Z scale factor (code 43) is also possible but less common for 2D.
    private double rotationAngle = 0.0; // Rotation angle in degrees (code 50, optional, default 0)
    // TODO: Add support for MINSERT (multiple inserts in a grid) if needed:
    // columnCount, rowCount, columnSpacing, rowSpacing.

    /**
     * Constructs a new DxfInsert.
     * Initializes insertion point to (0,0,0), scales to 1.0, and rotation angle to 0.0.
     * The block name must be set separately.
     */
    public DxfInsert() {
        super();
        this.insertionPoint = new Point3D(0,0,0);
    }

    /**
     * Gets the name of the block being inserted.
     * @return The block name.
     */
    public String getBlockName() {
        return blockName;
    }

    /**
     * Sets the name of the block to be inserted.
     * @param blockName The block name.
     */
    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    /**
     * Gets the insertion point of the block.
     * @return The insertion point.
     */
    public Point3D getInsertionPoint() {
        return insertionPoint;
    }

    /**
     * Sets the insertion point of the block.
     * @param insertionPoint The new insertion point. Defaults to (0,0,0) if null.
     */
    public void setInsertionPoint(Point3D insertionPoint) {
        this.insertionPoint = insertionPoint != null ? insertionPoint : new Point3D(0,0,0);
    }

    /**
     * Gets the X scale factor.
     * @return The X scale factor.
     */
    public double getXScale() {
        return xScale;
    }

    /**
     * Sets the X scale factor.
     * @param xScale The new X scale factor.
     */
    public void setXScale(double xScale) {
        this.xScale = xScale;
    }

    /**
     * Gets the Y scale factor.
     * @return The Y scale factor.
     */
    public double getYScale() {
        return yScale;
    }

    /**
     * Sets the Y scale factor.
     * @param yScale The new Y scale factor.
     */
    public void setYScale(double yScale) {
        this.yScale = yScale;
    }

    /**
     * Gets the rotation angle in degrees.
     * @return The rotation angle.
     */
    public double getRotationAngle() {
        return rotationAngle;
    }

    /**
     * Sets the rotation angle in degrees.
     * @param rotationAngle The new rotation angle.
     */
    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    @Override
    public EntityType getType() {
        return EntityType.INSERT;
    }

    /**
     * Calculates an approximate bounding box for the INSERT entity.
     * Currently, this returns a simple bounds around the insertion point or an invalid one
     * if the insertion point is null.
     * <p>
     * A more accurate calculation would require resolving the referenced {@link com.cad.dxflib.structure.DxfBlock}
     * definition, getting its bounds, and then transforming those bounds by the INSERT's
     * scale, rotation, and insertion point. This is a complex operation not yet implemented.
     * </p>
     * @return A {@link Bounds} object. Currently, it's a point or invalid.
     */
    @Override
    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        if (this.insertionPoint != null) {
             bounds.addToBounds(this.insertionPoint);
        }
        // TODO: Implement full bounds calculation by transforming the referenced block's bounds.
        // This requires access to the DxfDocument to resolve the blockName.
        return bounds.isValid() ? bounds : null;
    }

    @Override
    public String toString() {
        return "DxfInsert{" +
               "blockName='" + blockName + '\'' +
               ", insertionPoint=" + insertionPoint +
               ", xScale=" + xScale +
               ", yScale=" + yScale +
               ", rotation=" + rotationAngle +
               ", layer='" + layerName + '\'' +
               '}';
    }
}
