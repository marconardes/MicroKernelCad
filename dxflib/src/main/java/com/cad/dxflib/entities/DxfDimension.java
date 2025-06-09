package com.cad.dxflib.entities;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.math.Bounds; // Import for Bounds

public class DxfDimension extends AbstractDxfEntity {

    private Point3D definitionPoint = new Point3D(0,0,0); // Codes 10, 20, 30 (AcDbDimension - middle of text)
    private Point3D middleOfTextPoint = new Point3D(0,0,0); // Codes 11, 21, 31 (AcDbDimension)
    private String dimensionText = ""; // Code 1 (AcDbDimension - actual text, if overridden)
    private String dimensionStyleName = "STANDARD"; // Code 3 (AcDbDimension)
    private int dimensionTypeFlags = 0; // Code 70 (AcDbDimension)
    private String blockName = ""; // Code 2 (AcDbDimension - name of the block that graphically represents the dimension)

    // Specific to AcDbAlignedDimension (or other linear types)
    private Point3D definitionPoint1 = new Point3D(0,0,0); // Codes 13, 23, 33 (AcDbAlignedDimension - start of first extension line)
    private Point3D definitionPoint2 = new Point3D(0,0,0); // Codes 14, 24, 34 (AcDbAlignedDimension - start of second extension line)

    // Extrusion direction (optional, defaults to 0,0,1)
    private Point3D extrusionDirection = new Point3D(0,0,1); // Codes 210, 220, 230

    public DxfDimension() {
        super();
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
        this.definitionPoint = definitionPoint;
    }

    public Point3D getMiddleOfTextPoint() {
        return middleOfTextPoint;
    }

    public void setMiddleOfTextPoint(Point3D middleOfTextPoint) {
        this.middleOfTextPoint = middleOfTextPoint;
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

    public Point3D getDefinitionPoint1() {
        return definitionPoint1;
    }

    public void setDefinitionPoint1(Point3D definitionPoint1) {
        this.definitionPoint1 = definitionPoint1;
    }

    public Point3D getDefinitionPoint2() {
        return definitionPoint2;
    }

    public void setDefinitionPoint2(Point3D definitionPoint2) {
        this.definitionPoint2 = definitionPoint2;
    }

    public Point3D getExtrusionDirection() {
       return extrusionDirection;
    }

    public void setExtrusionDirection(Point3D extrusionDirection) {
       this.extrusionDirection = extrusionDirection;
    }

    // Helper method to check if it's an aligned dimension based on flags
    public boolean isAlignedDimension() {
        return (this.dimensionTypeFlags & 1) == 1;
    }

    // Helper method to check if the dimension geometry is in a block
    public boolean isBlockReference() {
       // Bit 5 (value 32) indicates the dimension is drawn in a separate block.
       return (this.dimensionTypeFlags & 32) == 32;
    }

    @Override
    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        // If the dimension is block-referenced, its true bounds are within that block.
        // This is a simplified approach using available points in this entity.
        // A more accurate approach would involve parsing the block entities if blockName is present.

        if (definitionPoint != null) {
            bounds.addToBounds(definitionPoint);
        }
        if (middleOfTextPoint != null) {
            bounds.addToBounds(middleOfTextPoint);
        }

        // For linear/aligned dimensions, include the definition points of the extension lines
        if (isAlignedDimension() || (dimensionTypeFlags & 0x07) == 0 || (dimensionTypeFlags & 0x07) == 1) { // 0 = Rotated, 1 = Aligned
            if (definitionPoint1 != null) {
                bounds.addToBounds(definitionPoint1);
            }
            if (definitionPoint2 != null) {
                bounds.addToBounds(definitionPoint2);
            }
        }

        // If no points were valid or it's a type without easily accessible geometry points here,
        // bounds might still be invalid.
        // For a truly minimal implementation, if no points are relevant:
        // if (!bounds.isValid()) { return new Bounds(); // or specific logic }
        return bounds;
    }

    @Override
    public String toString() {
        return "DxfDimension [layerName=" + getLayerName() +
               ", color=" + getColor() +
               ", definitionPoint=" + definitionPoint +
               ", middleOfTextPoint=" + middleOfTextPoint +
               ", dimensionText='" + dimensionText + '\'' +
               ", dimensionStyleName='" + dimensionStyleName + '\'' +
               ", dimensionTypeFlags=" + dimensionTypeFlags +
               ", blockName='" + blockName + '\'' +
               (isAlignedDimension() ? ", definitionPoint1=" + definitionPoint1 + ", definitionPoint2=" + definitionPoint2 : "") +
               "]";
    }
}
