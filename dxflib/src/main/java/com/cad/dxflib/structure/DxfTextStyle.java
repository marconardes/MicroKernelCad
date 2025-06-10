package com.cad.dxflib.structure;

import java.util.Objects;

/**
 * Represents a DXF STYLE table entry, which defines a text style.
 */
public class DxfTextStyle {

    private String name; // Text style name (code 2)
    private int flags; // Standard flags (code 70, bit-coded)
    private double fixedTextHeight; // Fixed text height (code 40); 0 if not fixed.
    private double widthFactor; // Width factor (code 41)
    private double obliqueAngle; // Oblique angle in degrees (code 50)
    private int textGenerationFlags; // Text generation flags (code 71; 2=backward, 4=upside down)
    private double lastHeightUsed; // Last height used (code 42)
    private String primaryFontFileName; // Primary font file name (code 3)
    private String bigFontFileName; // BigFont file name (code 4, optional, for Asian language support)

    /**
     * Constructs a new DxfTextStyle with the given name.
     * Initializes with default values: widthFactor=1.0, obliqueAngle=0.0,
     * fixedTextHeight=0.0 (variable), empty primaryFontFileName.
     * @param name The name of the text style. Must not be null or empty.
     * @throws IllegalArgumentException if the name is null or empty.
     */
    public DxfTextStyle(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Text style name cannot be null or empty.");
        }
        this.name = name;
        this.widthFactor = 1.0; // Default width factor
        this.obliqueAngle = 0.0; // Default oblique angle
        this.fixedTextHeight = 0.0; // Default, means height is variable (TEXT entity specifies height)
        this.primaryFontFileName = ""; // Default font file (e.g. "txt.shx" or "arial.ttf")
        // Other fields like flags, textGenerationFlags, lastHeightUsed, bigFontFileName
        // are typically set by the parser if present in the DXF.
    }

    /**
     * Gets the name of the text style.
     * @return The text style name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the text style.
     * @param name The new name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the standard flags for this text style.
     * @return The flags (bit-coded).
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Sets the standard flags for this text style.
     * @param flags The new flags.
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

    /**
     * Gets the fixed text height. If 0, the text height is variable and
     * typically set by the TEXT or MTEXT entity.
     * @return The fixed text height.
     */
    public double getFixedTextHeight() {
        return fixedTextHeight;
    }

    /**
     * Sets the fixed text height.
     * @param fixedTextHeight The new fixed text height.
     */
    public void setFixedTextHeight(double fixedTextHeight) {
        this.fixedTextHeight = fixedTextHeight;
    }

    /**
     * Gets the width factor.
     * @return The width factor.
     */
    public double getWidthFactor() {
        return widthFactor;
    }

    /**
     * Sets the width factor.
     * @param widthFactor The new width factor.
     */
    public void setWidthFactor(double widthFactor) {
        this.widthFactor = widthFactor;
    }

    /**
     * Gets the oblique angle in degrees.
     * @return The oblique angle.
     */
    public double getObliqueAngle() {
        return obliqueAngle;
    }

    /**
     * Sets the oblique angle in degrees.
     * @param obliqueAngle The new oblique angle.
     */
    public void setObliqueAngle(double obliqueAngle) {
        this.obliqueAngle = obliqueAngle;
    }

    /**
     * Gets the text generation flags.
     * (e.g., 2 = Text is backward (mirrored in X), 4 = Text is upside down (mirrored in Y)).
     * @return The text generation flags.
     */
    public int getTextGenerationFlags() {
        return textGenerationFlags;
    }

    /**
     * Sets the text generation flags.
     * @param textGenerationFlags The new text generation flags.
     */
    public void setTextGenerationFlags(int textGenerationFlags) {
        this.textGenerationFlags = textGenerationFlags;
    }

    /**
     * Gets the last text height used with this style.
     * @return The last height used.
     */
    public double getLastHeightUsed() {
        return lastHeightUsed;
    }

    /**
     * Sets the last text height used with this style.
     * @param lastHeightUsed The last height used.
     */
    public void setLastHeightUsed(double lastHeightUsed) {
        this.lastHeightUsed = lastHeightUsed;
    }

    /**
     * Gets the primary font file name (e.g., "arial.ttf", "simplex.shx").
     * @return The primary font file name.
     */
    public String getPrimaryFontFileName() {
        return primaryFontFileName;
    }

    /**
     * Sets the primary font file name.
     * @param primaryFontFileName The new primary font file name.
     */
    public void setPrimaryFontFileName(String primaryFontFileName) {
        this.primaryFontFileName = primaryFontFileName == null ? "" : primaryFontFileName;
    }

    /**
     * Gets the BigFont file name, used for Asian language support.
     * @return The BigFont file name, or null if not set.
     */
    public String getBigFontFileName() {
        return bigFontFileName;
    }

    /**
     * Sets the BigFont file name.
     * @param bigFontFileName The new BigFont file name.
     */
    public void setBigFontFileName(String bigFontFileName) {
        this.bigFontFileName = bigFontFileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DxfTextStyle that = (DxfTextStyle) o;
        // Text style names are case-insensitive in DXF and should be the primary identifier.
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
        return "DxfTextStyle{" +
                "name='" + name + '\'' +
                ", primaryFontFileName='" + primaryFontFileName + '\'' +
                (bigFontFileName != null ? ", bigFontFileName='" + bigFontFileName + '\'' : "") +
                ", fixedTextHeight=" + fixedTextHeight +
                ", widthFactor=" + widthFactor +
                ", obliqueAngle=" + obliqueAngle +
                '}';
    }
}
