package com.cad.dxflib.entities;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.math.Bounds;

/**
 * Represents a DIMENSION entity in a DXF file.
 * Dimensions can be of various types (linear, aligned, angular, radial, etc.)
 * and their graphical representation is often defined by an associated anonymous block
 * and styled by a DxfDimStyle.
 */
public class DxfDimension extends AbstractDxfEntity {

    private Point3D definitionPoint = new Point3D(0,0,0); // Codes 10,20,30 (interpretation depends on dimension type)
    private Point3D middleOfTextPoint = new Point3D(0,0,0); // Codes 11,21,31 (actual middle of dimension text)
    private String dimensionText = ""; // Code 1 (user-supplied dimension text, "" or "<>" for generated)
    private String dimensionStyleName = "STANDARD"; // Code 3 (name of referenced DxfDimStyle)
    private int dimensionTypeFlags = 0; // Code 70 (bit flags indicating type, properties)
    private String blockName = ""; // Code 2 (name of the anonymous block that graphically represents the dimension)
    private double rotationAngle = 0.0; // Code 50 (for rotated dimensions: angle of dimension line)

    // Points for Linear/Aligned/Angular(2-line) dimensions
    private Point3D linearPoint1 = new Point3D(0,0,0); // Codes 13,23,33 (e.g., start of first extension line)
    private Point3D linearPoint2 = new Point3D(0,0,0); // Codes 14,24,34 (e.g., start of second extension line)
    // Code 10 (definitionPoint) is often the dimension line definition point for these types.
    // Code 15,25,35 for angular 3-point dimensions (center, start, end)
    // Code 16,26,36 for ordinate dimensions (feature location, leader endpoint)


    private Point3D extrusionDirection = new Point3D(0,0,1); // Codes 210,220,230 (default Z-axis)

    /**
     * Constructs a new DxfDimension.
     * Initializes all point fields to (0,0,0), text to empty, style to "STANDARD",
     * and extrusion to (0,0,1).
     */
    public DxfDimension() {
        super();
    }

    @Override
    public EntityType getType() {
        return EntityType.DIMENSION;
    }

    /**
     * Gets the primary definition point of the dimension (group codes 10, 20, 30).
     * Its meaning varies with the dimension type (e.g., dimension line definition point for linear/aligned,
     * center for radial/diameter, text related for some).
     * @return The definition point.
     */
    public Point3D getDefinitionPoint() {
        return definitionPoint;
    }

    /**
     * Sets the primary definition point of the dimension.
     * @param definitionPoint The definition point. Defaults to (0,0,0) if null.
     */
    public void setDefinitionPoint(Point3D definitionPoint) {
        this.definitionPoint = definitionPoint != null ? definitionPoint : new Point3D(0,0,0);
    }

    /**
     * Gets the middle point of the dimension text (group codes 11, 21, 31).
     * @return The middle of text point.
     */
    public Point3D getMiddleOfTextPoint() {
        return middleOfTextPoint;
    }

    /**
     * Sets the middle point of the dimension text.
     * @param middleOfTextPoint The middle of text point. Defaults to (0,0,0) if null.
     */
    public void setMiddleOfTextPoint(Point3D middleOfTextPoint) {
        this.middleOfTextPoint = middleOfTextPoint != null ? middleOfTextPoint : new Point3D(0,0,0);
    }

    /**
     * Gets the explicit dimension text (group code 1).
     * If empty or "&lt;&gt;", the text is generated based on the measurement.
     * @return The dimension text.
     */
    public String getDimensionText() {
        return dimensionText;
    }

    /**
     * Sets the explicit dimension text.
     * @param dimensionText The dimension text.
     */
    public void setDimensionText(String dimensionText) {
        this.dimensionText = dimensionText;
    }

    /**
     * Gets the name of the DxfDimStyle referenced by this dimension.
     * @return The dimension style name.
     */
    public String getDimensionStyleName() {
        return dimensionStyleName;
    }

    /**
     * Sets the name of the DxfDimStyle for this dimension.
     * @param dimensionStyleName The dimension style name.
     */
    public void setDimensionStyleName(String dimensionStyleName) {
        this.dimensionStyleName = dimensionStyleName;
    }

    /**
     * Gets the dimension type flags (group code 70).
     * These flags indicate the dimension type (linear, aligned, angular, etc.)
     * and other properties like whether it's block-referenced or an anonymous dimension.
     * @return The dimension type flags.
     */
    public int getDimensionTypeFlags() {
        return dimensionTypeFlags;
    }

    /**
     * Sets the dimension type flags.
     * @param dimensionTypeFlags The dimension type flags.
     */
    public void setDimensionTypeFlags(int dimensionTypeFlags) {
        this.dimensionTypeFlags = dimensionTypeFlags;
    }

    /**
     * Gets the name of the anonymous block that contains the graphical representation of the dimension.
     * This is set if the dimension is block-referenced (bit 5 of code 70 is set).
     * @return The block name, or an empty string if not set or not applicable.
     */
    public String getBlockName() {
        return blockName;
    }

    /**
     * Sets the name of the anonymous block for this dimension.
     * @param blockName The block name.
     */
    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    /**
     * Gets the rotation angle of the dimension line (group code 50).
     * Primarily used for Rotated Linear dimensions.
     * @return The rotation angle in degrees.
     */
    public double getRotationAngle() {
        return rotationAngle;
    }

    /**
     * Sets the rotation angle of the dimension line.
     * @param rotationAngle The rotation angle in degrees.
     */
    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    /**
     * Gets the first definition point for linear or aligned dimensions (e.g., start of the first extension line).
     * (Group codes 13, 23, 33)
     * @return The first linear definition point.
     */
    public Point3D getLinearPoint1() {
        return linearPoint1;
    }

    /**
     * Sets the first definition point for linear or aligned dimensions.
     * @param linearPoint1 The point. Defaults to (0,0,0) if null.
     */
    public void setLinearPoint1(Point3D linearPoint1) {
        this.linearPoint1 = linearPoint1 != null ? linearPoint1 : new Point3D(0,0,0);
    }

    /**
     * Gets the second definition point for linear or aligned dimensions (e.g., start of the second extension line).
     * (Group codes 14, 24, 34)
     * @return The second linear definition point.
     */
    public Point3D getLinearPoint2() {
        return linearPoint2;
    }

    /**
     * Sets the second definition point for linear or aligned dimensions.
     * @param linearPoint2 The point. Defaults to (0,0,0) if null.
     */
    public void setLinearPoint2(Point3D linearPoint2) {
        this.linearPoint2 = linearPoint2 != null ? linearPoint2 : new Point3D(0,0,0);
    }

    /**
     * Gets the extrusion direction vector of the dimension.
     * @return The extrusion direction as a Point3D (defaults to 0,0,1).
     */
    public Point3D getExtrusionDirection() {
       return extrusionDirection;
    }

    /**
     * Sets the extrusion direction vector of the dimension.
     * @param extrusionDirection The extrusion direction. Defaults to (0,0,1) if null.
     */
    public void setExtrusionDirection(Point3D extrusionDirection) {
       this.extrusionDirection = extrusionDirection != null ? extrusionDirection : new Point3D(0,0,1);
    }

    /**
     * Checks if the dimension's geometry is defined in a separate anonymous block.
     * This is determined by bit 5 (value 32) of the dimension type flags (group code 70).
     * @return true if the dimension is block-referenced, false otherwise.
     */
    public boolean isBlockReferenced() {
       return (this.dimensionTypeFlags & 32) == 32;
    }

    /**
     * Gets the specific type of the dimension (e.g., linear, aligned, angular).
     * This is determined by bits 0-4 of the dimension type flags (group code 70).
     * <ul>
     *   <li>0: Rotated, horizontal, or vertical linear dimension</li>
     *   <li>1: Aligned linear dimension</li>
     *   <li>2: Angular (2 lines) dimension</li>
     *   <li>3: Diameter dimension</li>
     *   <li>4: Radius dimension</li>
     *   <li>5: Angular 3-point dimension</li>
     *   <li>6: Ordinate dimension</li>
     * </ul>
     * @return The integer representing the dimension type.
     */
    public int getDimensionType() {
        return this.dimensionTypeFlags & 0x1F; // Mask to get lower 5 bits (0-4 for type, bit 5 for block ref)
    }

    /**
     * Calculates an approximate bounding box for the dimension.
     * This is a simplified approximation using the main definition points available in this entity.
     * True bounds depend on the dimension's associated block geometry, text, arrows, etc.,
     * which are defined by the DxfDimStyle and the referenced anonymous DxfBlock.
     * @return A {@link Bounds} object, or null if no relevant points are available to define valid bounds.
     */
    @Override
    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        if (this.definitionPoint != null) bounds.addToBounds(this.definitionPoint);
        if (this.middleOfTextPoint != null) bounds.addToBounds(this.middleOfTextPoint);

        int type = getDimensionType();
        if (type == 0 || type == 1) { // Rotated, Horizontal, Vertical, Aligned
            if (this.linearPoint1 != null) bounds.addToBounds(this.linearPoint1);
            if (this.linearPoint2 != null) bounds.addToBounds(this.linearPoint2);
        }
        // Add more specific points for other types if available and simple enough
        // e.g., for radial/diameter, definitionPoint (10,20,30) is center, point 15,25,35 is on circumference.
        return bounds.isValid() ? bounds : null;
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
