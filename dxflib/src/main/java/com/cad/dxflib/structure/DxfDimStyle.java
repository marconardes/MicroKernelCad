package com.cad.dxflib.structure;

import java.util.Objects;

/**
 * Represents a DXF DIMSTYLE table entry.
 * Dimension styles control the appearance of dimension entities.
 */
public class DxfDimStyle {
    private String name; // Dimension style name (code 2)
    private String handle; // Handle of the DIMSTYLE object (code 105)

    // Standard flags (group 70), includes settings like DIMTAD, DIMTIH, DIMTOH through bit flags.
    private int flags70;

    // Geometry - Lines and Arrows
    private int dimensionLineColor = 0;       // DIMCLRD (176) - Color of dimension line. 0 = ByBlock, 256 = ByLayer.
    private int extensionLineColor = 0;       // DIMCLRE (177) - Color of extension lines.
    private double extensionLineExtension = 0.18; // DIMEXE (44) - Extension line extension beyond dimension line.
    private double extensionLineOffset = 0.0625;  // DIMEXO (42) - Offset of extension lines from definition points.
    private String dimBlkName = "";           // DIMBLK (1, 5, or 342) - Arrow block name. Empty for default closed filled.
    private double arrowSize = 0.18;          // DIMASZ (41) - Size of arrows and arrow blocks.

    // Text
    private String textStyleName = "STANDARD"; // DIMTXSTY (340) - Handle/name of the text style for dimension text.
    private int textColor = 0;                // DIMCLRT (178) - Color of dimension text.
    private double textHeight = 0.18;         // DIMTXT (140, fallback 44) - Height of dimension text.
    private double textGap = 0.09;            // DIMGAP (147) - Gap between dimension line and dimension text.

    // Units
    private int decimalPlaces = 2;            // DIMDEC (271) - Number of decimal places for linear dimensions.

    // Fit and Placement
    // Note: Many of these are also controlled by bits in flags70 in modern DXF.
    // Explicit fields are provided for common ones.
    private int textVerticalAlignment = 1;    // DIMTAD (77) - Vertical placement of text (0=center, 1=above, etc.). Default 1 (Above).
    private boolean textInsideHorizontal = true; // DIMTIH (73) - Text inside extensions is horizontal (1=true).
    private boolean textOutsideHorizontal = true;// DIMTOH (74) - Text outside extensions is horizontal (1=true).
    private boolean textOutsideExtensions = false; // DIMTOFL (172) - Force text between extension lines if it doesn't fit.
    private boolean suppressFirstExtensionLine = false; // DIMSE1 (75 or bit in 70) - Suppress first extension line.
    private boolean suppressSecondExtensionLine = false; // DIMSE2 (76 or bit in 70) - Suppress second extension line.

    /**
     * Constructs a new DxfDimStyle with the given name.
     * Initializes dimension style properties with common default values.
     * @param name The name of the dimension style. Must not be null or empty.
     * @throws IllegalArgumentException if the name is null or empty.
     */
    public DxfDimStyle(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Dimension style name cannot be null or empty.");
        }
        this.name = name;
        // Default values are set at field declaration, reflecting common AutoCAD-like defaults.
    }

    /**
     * Gets the name of the dimension style.
     * @return The dimension style name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the dimension style.
     * @param name The new name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the handle of this dimension style object.
     * @return The handle string.
     */
    public String getHandle() {
        return handle;
    }

    /**
     * Sets the handle of this dimension style object.
     * @param handle The handle string.
     */
    public void setHandle(String handle) {
        this.handle = handle;
    }

    /**
     * Gets the standard flags (group code 70) for this dimension style.
     * These flags control various behaviors like text alignment and placement.
     * @return The integer value of the flags.
     */
    public int getFlags70() {
        return flags70;
    }

    /**
     * Sets the standard flags (group code 70) for this dimension style.
     * @param flags70 The integer value of the flags.
     */
    public void setFlags70(int flags70) {
        this.flags70 = flags70;
    }

    // ... Getters and Setters for all other fields ...

    public int getDimensionLineColor() {
        return dimensionLineColor;
    }

    public void setDimensionLineColor(int dimensionLineColor) {
        this.dimensionLineColor = dimensionLineColor;
    }

    public int getExtensionLineColor() {
        return extensionLineColor;
    }

    public void setExtensionLineColor(int extensionLineColor) {
        this.extensionLineColor = extensionLineColor;
    }

    public double getExtensionLineExtension() {
        return extensionLineExtension;
    }

    public void setExtensionLineExtension(double extensionLineExtension) {
        this.extensionLineExtension = extensionLineExtension;
    }

    public double getExtensionLineOffset() {
        return extensionLineOffset;
    }

    public void setExtensionLineOffset(double extensionLineOffset) {
        this.extensionLineOffset = extensionLineOffset;
    }

    public String getDimBlkName() {
        return dimBlkName;
    }

    public void setDimBlkName(String dimBlkName) {
        this.dimBlkName = dimBlkName == null ? "" : dimBlkName;
    }

    public double getArrowSize() {
        return arrowSize;
    }

    public void setArrowSize(double arrowSize) {
        this.arrowSize = arrowSize;
    }

    public String getTextStyleName() {
        return textStyleName;
    }

    public void setTextStyleName(String textStyleName) {
        this.textStyleName = textStyleName == null ? "STANDARD" : textStyleName;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public double getTextHeight() {
        return textHeight;
    }

    public void setTextHeight(double textHeight) {
        this.textHeight = textHeight;
    }

    public double getTextGap() {
        return textGap;
    }

    public void setTextGap(double textGap) {
        this.textGap = textGap;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    public int getTextVerticalAlignment() {
        return textVerticalAlignment;
    }

    public void setTextVerticalAlignment(int textVerticalAlignment) {
        this.textVerticalAlignment = textVerticalAlignment;
    }

    public boolean isTextInsideHorizontal() {
        return textInsideHorizontal;
    }

    public void setTextInsideHorizontal(boolean textInsideHorizontal) {
        this.textInsideHorizontal = textInsideHorizontal;
    }

    public boolean isTextOutsideHorizontal() {
        return textOutsideHorizontal;
    }

    public void setTextOutsideHorizontal(boolean textOutsideHorizontal) {
        this.textOutsideHorizontal = textOutsideHorizontal;
    }

    public boolean isTextOutsideExtensions() {
        return textOutsideExtensions;
    }

    public void setTextOutsideExtensions(boolean textOutsideExtensions) {
        this.textOutsideExtensions = textOutsideExtensions;
    }

    public boolean isSuppressFirstExtensionLine() {
        return suppressFirstExtensionLine;
    }

    public void setSuppressFirstExtensionLine(boolean suppressFirstExtensionLine) {
        this.suppressFirstExtensionLine = suppressFirstExtensionLine;
    }

    public boolean isSuppressSecondExtensionLine() {
        return suppressSecondExtensionLine;
    }

    public void setSuppressSecondExtensionLine(boolean suppressSecondExtensionLine) {
        this.suppressSecondExtensionLine = suppressSecondExtensionLine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DxfDimStyle that = (DxfDimStyle) o;
        // Dimension Style names are case-insensitive in DXF and should be the primary identifier.
        // Assuming names are stored consistently (e.g., uppercase) by DxfDocument.
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        // Assuming names are stored consistently (e.g., uppercase).
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "DxfDimStyle{" +
                "name='" + name + '\'' +
                (handle != null ? ", handle='" + handle + '\'' : "") +
                // ", flags70=" + flags70 + // Often too verbose
                ", arrowSize=" + arrowSize +
                ", textHeight=" + textHeight +
                ", textStyleName='" + textStyleName + '\'' +
                '}';
    }
}
