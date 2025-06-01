package com.cad.dxflib.entities;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point3D;

public class DxfInsert extends AbstractDxfEntity {
    private String blockName;       // Group code 2
    private Point3D insertionPoint; // Group codes 10, 20, 30
    private double xScale = 1.0;    // Group code 41 (optional, default 1)
    private double yScale = 1.0;    // Group code 42 (optional, default 1)
    private double rotationAngle = 0.0; // Group code 50 (optional, default 0)
    // Column/row count and spacing for MINSERT can be added later

    public DxfInsert() {
        this.insertionPoint = new Point3D(0,0,0);
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public Point3D getInsertionPoint() {
        return insertionPoint;
    }

    public void setInsertionPoint(Point3D insertionPoint) {
        this.insertionPoint = insertionPoint;
    }

    public double getXScale() {
        return xScale;
    }

    public void setXScale(double xScale) {
        this.xScale = xScale;
    }

    public double getYScale() {
        return yScale;
    }

    public void setYScale(double yScale) {
        this.yScale = yScale;
    }

    public double getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    @Override
    public EntityType getType() {
        return EntityType.INSERT;
    }

    // getBounds() and transform() will be more complex for INSERT as they depend on the Block's entities
    // and the transformation applied. To be implemented later.

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
