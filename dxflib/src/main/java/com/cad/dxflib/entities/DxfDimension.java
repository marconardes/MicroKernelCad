package com.cad.dxflib.entities;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.math.Bounds;

public class DxfDimension extends AbstractDxfEntity {

    private Point3D definitionPoint = new Point3D(0,0,0); // Codes 10, 20, 30 (AcDbDimension general definition point, often used for text placement or dim line)
    private Point3D middleOfTextPoint = new Point3D(0,0,0); // Codes 11, 21, 31 (AcDbDimension specific middle of text)
    private String dimensionText = ""; // Code 1 (AcDbDimension - actual text, if overridden)
    private String dimensionStyleName = "STANDARD"; // Code 3 (AcDbDimension)
    private int dimensionTypeFlags = 0; // Code 70 (AcDbDimension)
    private String blockName = ""; // Code 2 (AcDbDimension - name of the block that graphically represents the dimension)
    private double rotationAngle = 0.0; // Code 50 (AcDbRotatedDimension - rotation angle of the dimension line)

    // For Linear/Aligned dimensions
    private Point3D linearPoint1 = new Point3D(0,0,0); // Codes 13, 23, 33 (AcDbAlignedDimension/AcDbRotatedDimension - start of first extension line)
    private Point3D linearPoint2 = new Point3D(0,0,0); // Codes 14, 24, 34 (AcDbAlignedDimension/AcDbRotatedDimension - start of second extension line)
    // Note: Code 10 for linear/aligned dimensions is often the "Dimension line definition point"
    // It's stored in 'definitionPoint' for now, but its specific meaning depends on dimensionTypeFlags.
    // We can rename 'definitionPoint' to 'dimensionLineDefPoint' or add a separate field if clarity is needed later.

    private Point3D extrusionDirection = new Point3D(0,0,1); // Codes 210, 220, 230

    public DxfDimension() {
        super(); // Sets default EntityType to UNKNOWN
        setEntityType(EntityType.DIMENSION); // Explicitly set type
    }

    @Override
    public EntityType getType() {
        return EntityType.DIMENSION;
    }

    // Getters and Setters
    public Point3D getDefinitionPoint() {
        return definitionPoint;
    }

    public void setDefinitionPoint(Point3D definitionPoint) {
        this.definitionPoint = definitionPoint != null ? definitionPoint : new Point3D(0,0,0);
    }

    public Point3D getMiddleOfTextPoint() {
        return middleOfTextPoint;
    }

    public void setMiddleOfTextPoint(Point3D middleOfTextPoint) {
        this.middleOfTextPoint = middleOfTextPoint != null ? middleOfTextPoint : new Point3D(0,0,0);
    }

    public String getDimensionText() {
        return dimensionText;
    }

    public void setDimensionText(String dimensionText) {
        this.dimensionText = dimensionText;
    }

    public String getDimensionStyleName() {
        return dimensionStyleName;
    }

    public void setDimensionStyleName(String dimensionStyleName) {
        this.dimensionStyleName = dimensionStyleName;
    }

    public int getDimensionTypeFlags() {
        return dimensionTypeFlags;
    }

    public void setDimensionTypeFlags(int dimensionTypeFlags) {
        this.dimensionTypeFlags = dimensionTypeFlags;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public double getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public Point3D getLinearPoint1() {
        return linearPoint1;
    }

    public void setLinearPoint1(Point3D linearPoint1) {
        this.linearPoint1 = linearPoint1 != null ? linearPoint1 : new Point3D(0,0,0);
    }

    public Point3D getLinearPoint2() {
        return linearPoint2;
    }

    public void setLinearPoint2(Point3D linearPoint2) {
        this.linearPoint2 = linearPoint2 != null ? linearPoint2 : new Point3D(0,0,0);
    }

    // 'dimensionLinePoint' is typically what 'definitionPoint' (code 10) is for linear/aligned types.
    // For clarity, one might add:
    // public Point3D getDimensionLineDefPoint() { return definitionPoint; }
    // public void setDimensionLineDefPoint(Point3D point) { this.definitionPoint = point; }


    public Point3D getExtrusionDirection() {
       return extrusionDirection;
    }

    public void setExtrusionDirection(Point3D extrusionDirection) {
       this.extrusionDirection = extrusionDirection != null ? extrusionDirection : new Point3D(0,0,1);
    }

    public boolean isBlockReferenced() {
       // Bit 5 (value 32) of code 70 means the dimension is block-referenced
       // Bit 0 (value 1) for aligned, linear, angular, diameter, radius
       // Bit 6 (value 64) for ordinate type
       // Bit 7 (value 128) means this is an anonymous block if combined with bit 5
       return (this.dimensionTypeFlags & 32) == 32;
    }

    public int getDimensionType() {
        // The type of dimension is stored in bits 0-4 of group code 70.
        // 0 = Rotated, horizontal, or vertical
        // 1 = Aligned
        // 2 = Angular
        // 3 = Diameter
        // 4 = Radius
        // 5 = Angular 3-point
        // 6 = Ordinate
        // (add 32 for block reference, add 128 for anonymous block)
        return this.dimensionTypeFlags & 0x1F; // Mask to get lower 5 bits
    }


    @Override
    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        // This is a simplified approximation. True bounds depend on the dimension's block geometry,
        // text size, arrow size, etc., derived from DxfDimStyle and the referenced DxfBlock.

        // Use definitionPoint (often dim line def point or text related)
        if (this.definitionPoint != null) bounds.addPoint(this.definitionPoint);
        // Use middleOfTextPoint
        if (this.middleOfTextPoint != null) bounds.addPoint(this.middleOfTextPoint);

        int type = getDimensionType();
        // For linear, aligned, and potentially rotated dimensions (type 0 or 1)
        if (type == 0 || type == 1) {
            if (this.linearPoint1 != null) bounds.addPoint(this.linearPoint1);
            if (this.linearPoint2 != null) bounds.addPoint(this.linearPoint2);
        }
        // For other types (angular, diameter, radius, ordinate), specific points would be needed.
        // E.g., for radius/diameter, it might be center + radius.
        // For now, this is a basic approximation.

        return bounds.isValid() ? bounds : null; // Return null if no points were added
    }

    @Override
    public String toString() {
        return "DxfDimension [layerName=" + getLayerName() +
               ", color=" + getColor() +
               ", definitionPoint=" + definitionPoint +
               ", middleOfTextPoint=" + middleOfTextPoint +
               ", dimensionText='" + dimensionText + '\'' +
               ", dimensionStyleName='" + dimensionStyleName + '\'' +
               ", dimensionTypeFlags=" + dimensionTypeFlags + " (type: " + getDimensionType() + ")" +
               ", blockName='" + blockName + '\'' +
               ", rotationAngle=" + rotationAngle +
               ", linearPoint1=" + linearPoint1 +
               ", linearPoint2=" + linearPoint2 +
               ", extrusionDirection=" + extrusionDirection +
               "]";
    }
}
