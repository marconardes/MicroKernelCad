package com.cad.dxflib.objects;

import java.util.Objects;

/**
 * Represents a DXF SCALE object, typically found in the ACAD_SCALELIST dictionary.
 * Defines a scale factor using paper units and drawing units (e.g., 1:100 means
 * 1 paper unit equals 100 drawing units).
 */
public class DxfScale {
    private String handle; // Handle of this SCALE object (code 5)
    private String ownerHandle; // Handle of the owner dictionary (code 330), usually ACAD_SCALELIST.
    private String name; // Name of the scale, e.g., "1:1", "1:100" (code 300). May be empty or not unique.
    private double paperUnits; // Paper units value (code 140).
    private double drawingUnits; // Drawing units value (code 141).
    private boolean isUnitScale; // Flag indicating if this is a unit scale (1:1) (code 290).
    private int flags; // Obsolete flags (code 70).

    /**
     * Constructs a new DxfScale object with default values.
     * Defaults: paperUnits=1.0, drawingUnits=1.0, isUnitScale=false.
     */
    public DxfScale() {
        this.paperUnits = 1.0;
        this.drawingUnits = 1.0;
        this.isUnitScale = false;
    }

    /**
     * Gets the handle of this scale object.
     * @return The handle string.
     */
    public String getHandle() {
        return handle;
    }

    /**
     * Sets the handle of this scale object.
     * @param handle The handle string.
     */
    public void setHandle(String handle) {
        this.handle = handle;
    }

    /**
     * Gets the handle of the owner dictionary (typically ACAD_SCALELIST).
     * @return The owner's handle string.
     */
    public String getOwnerHandle() {
        return ownerHandle;
    }

    /**
     * Sets the handle of the owner dictionary.
     * @param ownerHandle The owner's handle string.
     */
    public void setOwnerHandle(String ownerHandle) {
        this.ownerHandle = ownerHandle;
    }

    /**
     * Gets the name of the scale (e.g., "1:100").
     * This name is primarily for display and may not be unique.
     * @return The name string.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the scale.
     * @param name The name string.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the paper units value for the scale.
     * For a scale of "1:100", this would typically be 1.
     * @return The paper units value.
     */
    public double getPaperUnits() {
        return paperUnits;
    }

    /**
     * Sets the paper units value for the scale.
     * @param paperUnits The paper units value.
     */
    public void setPaperUnits(double paperUnits) {
        this.paperUnits = paperUnits;
    }

    /**
     * Gets the drawing units value for the scale.
     * For a scale of "1:100", this would typically be 100.
     * @return The drawing units value.
     */
    public double getDrawingUnits() {
        return drawingUnits;
    }

    /**
     * Sets the drawing units value for the scale.
     * @param drawingUnits The drawing units value.
     */
    public void setDrawingUnits(double drawingUnits) {
        this.drawingUnits = drawingUnits;
    }

    /**
     * Checks if this scale is a unit scale (i.e., 1:1).
     * @return true if it is a unit scale, false otherwise.
     */
    public boolean isUnitScale() {
        return isUnitScale;
    }

    /**
     * Sets whether this scale is a unit scale.
     * @param unitScale true if it is a unit scale, false otherwise.
     */
    public void setUnitScale(boolean unitScale) {
        isUnitScale = unitScale;
    }

    /**
     * Gets the obsolete flags associated with this scale (group code 70).
     * @return The flags integer.
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Sets the obsolete flags for this scale.
     * @param flags The flags integer.
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DxfScale dxfScale = (DxfScale) o;
        // Scales are typically unique by their handle within a DXF document.
        return Objects.equals(handle, dxfScale.handle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handle);
    }

    @Override
    public String toString() {
        return "DxfScale{" +
                "handle='" + handle + '\'' +
                (name != null ? ", name='" + name + '\'' : "") +
                ", paperUnits=" + paperUnits +
                ", drawingUnits=" + drawingUnits +
                ", isUnitScale=" + isUnitScale +
                (ownerHandle != null ? ", ownerHandle='" + ownerHandle + '\'' : "") +
                '}';
    }
}
