package com.cad.dxflib.entities;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.math.Bounds;
// import com.cad.dxflib.structure.DxfBlock; // For future, more complex getBounds
// import com.cad.dxflib.structure.DxfDocument; // For future, more complex getBounds


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

    @Override
    public Bounds getBounds() {
        // TODO: This is a complex calculation.
        // It requires resolving the block definition, then transforming the bounds
        // of each entity within the block by the insert's scale, rotation, and position.
        // For now, return a simple bounds around the insertion point or an invalid one.
        Bounds bounds = new Bounds();
        if (this.insertionPoint != null) {
             bounds.addToBounds(this.insertionPoint);
        }
        // A more accurate (but still simplified) version might consider the block's own bounds
        // and transform that. But block bounds themselves need to be calculated.
        // Example:
        // if (this.document != null && this.blockName != null) { // document field would need to be added
        //     DxfBlock block = this.document.getBlock(this.blockName);
        //     if (block != null) {
        //         Bounds blockBounds = block.getBounds(); // Requires DxfBlock to have getBounds()
        //         if (blockBounds.isValid()) {
        //             // This is still simplified as it doesn't account for rotation of the block bounds
        //             Point3D min = new Point3D(
        //                 insertionPoint.x + blockBounds.getMinX() * xScale,
        //                 insertionPoint.y + blockBounds.getMinY() * yScale,
        //                 insertionPoint.z + blockBounds.getMinZ() // Assuming no zScale for now
        //             );
        //             Point3D max = new Point3D(
        //                 insertionPoint.x + blockBounds.getMaxX() * xScale,
        //                 insertionPoint.y + blockBounds.getMaxY() * yScale,
        //                 insertionPoint.z + blockBounds.getMaxZ()
        //             );
        //             bounds.addToBounds(min);
        //             bounds.addToBounds(max);
        //         }
        //     }
        // }
        return bounds;
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
