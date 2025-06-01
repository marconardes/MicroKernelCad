package com.cad.dxflib.entities;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.math.Bounds;

public class DxfText extends AbstractDxfEntity {
    private Point3D insertionPoint; // Group codes 10, 20, 30
    private double height;          // Group code 40
    private String textValue;       // Group code 1
    private double rotationAngle;   // Group code 50 (optional, default 0)
    private String styleName;       // Group code 7 (optional, default "STANDARD")
    // Horizontal alignment (72), Vertical alignment (73) can be added later for more precision

    public DxfText() {
        this.insertionPoint = new Point3D(0, 0, 0);
        this.height = 1.0;
        this.textValue = "";
        this.rotationAngle = 0.0;
        this.styleName = "STANDARD";
    }

    public Point3D getInsertionPoint() {
        return insertionPoint;
    }

    public void setInsertionPoint(Point3D insertionPoint) {
        this.insertionPoint = insertionPoint;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        if (height <= 0) {
            throw new IllegalArgumentException("Text height must be positive.");
        }
        this.height = height;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue == null ? "" : textValue;
    }

    public double getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = (styleName != null && !styleName.trim().isEmpty()) ? styleName : "STANDARD";
    }

    @Override
    public EntityType getType() {
        return EntityType.TEXT;
    }

    @Override
    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        if (insertionPoint != null && height > 0) {
            // Simplified bounds: a box around the insertion point with text height.
            // Does not account for rotation or actual text width.
            // For SVG viewBox calculation, this might be sufficient if text is not too large.
            bounds.addToBounds(insertionPoint.x, insertionPoint.y - height, insertionPoint.z); // Approximate bottom-left
            bounds.addToBounds(insertionPoint.x + (textValue != null ? textValue.length() * height * 0.6 : height), insertionPoint.y, insertionPoint.z); // Approximate top-right (width is a rough guess)
        }
        return bounds;
    }

    @Override
    public String toString() {
        return "DxfText{" +
               "text='" + textValue + '\'' +
               ", insertionPoint=" + insertionPoint +
               ", height=" + height +
               ", layer='" + layerName + '\'' +
               '}';
    }
}
