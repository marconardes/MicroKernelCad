package com.cad.dxflib.entities;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.math.Bounds;

/**
 * Represents a TEXT entity in a DXF file.
 * Single-line text defined by an insertion point, height, value, and optionally rotation and style.
 */
public class DxfText extends AbstractDxfEntity {
    private Point3D insertionPoint; // Insertion point (codes 10, 20, 30 for first alignment point)
                                    // (codes 11, 21, 31 for second alignment point if alignment is used)
    private double height;          // Text height (code 40)
    private String textValue;       // Default text value (code 1)
    private double rotationAngle;   // Text rotation angle in degrees (code 50, optional, default 0)
    private String styleName;       // Text style name (code 7, optional, default "STANDARD")
    // TODO: Add fields for horizontal (72) and vertical (73) alignment if needed.

    /**
     * Constructs a new DxfText object.
     * Initializes insertion point to (0,0,0), height to 1.0, text value to empty,
     * rotation angle to 0.0, and style name to "STANDARD".
     */
    public DxfText() {
        super();
        this.insertionPoint = new Point3D(0, 0, 0);
        this.height = 1.0;
        this.textValue = "";
        this.rotationAngle = 0.0;
        this.styleName = "STANDARD";
    }

    /**
     * Gets the insertion point of the text.
     * The meaning of this point can depend on text alignment (not fully supported here).
     * @return The insertion point.
     */
    public Point3D getInsertionPoint() {
        return insertionPoint;
    }

    /**
     * Sets the insertion point of the text.
     * @param insertionPoint The new insertion point. Defaults to (0,0,0) if null.
     */
    public void setInsertionPoint(Point3D insertionPoint) {
        this.insertionPoint = insertionPoint != null ? insertionPoint : new Point3D(0,0,0);
    }

    /**
     * Gets the text height.
     * @return The text height.
     */
    public double getHeight() {
        return height;
    }

    /**
     * Sets the text height.
     * @param height The new text height. Must be positive.
     * @throws IllegalArgumentException if the height is not positive.
     */
    public void setHeight(double height) {
        if (height <= 0) {
            throw new IllegalArgumentException("Text height must be positive.");
        }
        this.height = height;
    }

    /**
     * Gets the text string value.
     * @return The text value.
     */
    public String getTextValue() {
        return textValue;
    }

    /**
     * Sets the text string value.
     * @param textValue The new text value. If null, it's set to an empty string.
     */
    public void setTextValue(String textValue) {
        this.textValue = textValue == null ? "" : textValue;
    }

    /**
     * Gets the rotation angle of the text in degrees.
     * @return The rotation angle.
     */
    public double getRotationAngle() {
        return rotationAngle;
    }

    /**
     * Sets the rotation angle of the text in degrees.
     * @param rotationAngle The new rotation angle.
     */
    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    /**
     * Gets the name of the text style used by this text entity.
     * @return The text style name.
     */
    public String getStyleName() {
        return styleName;
    }

    /**
     * Sets the name of the text style for this text entity.
     * Defaults to "STANDARD" if the provided name is null or empty.
     * @param styleName The new text style name.
     */
    public void setStyleName(String styleName) {
        this.styleName = (styleName != null && !styleName.trim().isEmpty()) ? styleName : "STANDARD";
    }

    @Override
    public EntityType getType() {
        return EntityType.TEXT;
    }

    /**
     * Calculates an approximate bounding box for the text.
     * This is a highly simplified approximation based on the insertion point,
     * text height, and an estimated width based on text length and height.
     * It does not account for text rotation, alignment, font metrics, or special characters.
     * @return A {@link Bounds} object representing the approximate extents.
     */
    @Override
    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        if (insertionPoint != null && height > 0 && textValue != null) {
            // This is a very rough approximation.
            // Assumes left-justified text starting at insertionPoint.
            // Width is approximated by number of characters * height * an average aspect ratio (e.g., 0.6).
            // Does not account for rotation, actual font metrics, or alignment.
            double approximateWidth = textValue.length() * height * 0.6; // Rough guess
            double x = insertionPoint.x;
            double y = insertionPoint.y;
            double z = insertionPoint.z;

            // Assuming insertion point is bottom-left for non-rotated, non-aligned text
            bounds.addToBounds(x, y, z);
            bounds.addToBounds(x + approximateWidth, y + height, z);
            // TODO: Improve bounds calculation by considering rotation and alignment.
        }
        return bounds.isValid() ? bounds : null;
    }

    @Override
    public String toString() {
        return "DxfText{" +
               "text='" + textValue + '\'' +
               ", insertionPoint=" + insertionPoint +
               ", height=" + height +
               ", styleName='" + styleName + '\'' +
               ", rotation=" + rotationAngle +
               ", layer='" + layerName + '\'' +
               '}';
    }
}
