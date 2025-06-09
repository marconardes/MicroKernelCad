package com.cad.dxflib.structure;

public class DxfTextStyle {

    private String name; // code 2
    private int flags; // code 70
    private double fixedTextHeight; // code 40
    private double widthFactor; // code 41
    private double obliqueAngle; // code 50
    private int textGenerationFlags; // code 71
    private double lastHeightUsed; // code 42
    private String primaryFontFileName; // code 3
    private String bigFontFileName; // code 4 (optional)

    public DxfTextStyle(String name) {
        this.name = name;
        this.widthFactor = 1.0; // Default width factor
        this.obliqueAngle = 0.0; // Default oblique angle
        this.fixedTextHeight = 0.0; // Default, means height is variable
        this.primaryFontFileName = ""; // Default font file
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public double getFixedTextHeight() {
        return fixedTextHeight;
    }

    public void setFixedTextHeight(double fixedTextHeight) {
        this.fixedTextHeight = fixedTextHeight;
    }

    public double getWidthFactor() {
        return widthFactor;
    }

    public void setWidthFactor(double widthFactor) {
        this.widthFactor = widthFactor;
    }

    public double getObliqueAngle() {
        return obliqueAngle;
    }

    public void setObliqueAngle(double obliqueAngle) {
        this.obliqueAngle = obliqueAngle;
    }

    public int getTextGenerationFlags() {
        return textGenerationFlags;
    }

    public void setTextGenerationFlags(int textGenerationFlags) {
        this.textGenerationFlags = textGenerationFlags;
    }

    public double getLastHeightUsed() {
        return lastHeightUsed;
    }

    public void setLastHeightUsed(double lastHeightUsed) {
        this.lastHeightUsed = lastHeightUsed;
    }

    public String getPrimaryFontFileName() {
        return primaryFontFileName;
    }

    public void setPrimaryFontFileName(String primaryFontFileName) {
        this.primaryFontFileName = primaryFontFileName;
    }

    public String getBigFontFileName() {
        return bigFontFileName;
    }

    public void setBigFontFileName(String bigFontFileName) {
        this.bigFontFileName = bigFontFileName;
    }
}
