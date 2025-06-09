package com.cad.dxflib.structure;

public class DxfDimStyle {
    private String name; // Code 2
    private String handle; // Code 105 or 5 (DIMSTYLE handle)

    // Standard flags (group 70)
    private int flags70; // DIMTAD, DIMTIH, DIMTOH etc. are often packed here.

    // Geometry - Lines and Arrows
    private int dimensionLineColor = 0;       // DIMCLRD (176) - 0 = ByBlock, 256 = ByLayer
    private int extensionLineColor = 0;       // DIMCLRE (177) - 0 = ByBlock, 256 = ByLayer
    private double extensionLineExtension = 0.18; // DIMEXE (44 in DXF R12, but 43 seems to be used in example DxfParser?) -> Standard AutoCAD: 0.18
    private double extensionLineOffset = 0.0625;  // DIMEXO (42) -> Standard AutoCAD: 0.0625
    private String dimBlkName = "";           // DIMBLK (1 or 5 in older DXF, 342 in newer) - Arrow block name. Empty string for default closed filled.
    // private String dimBlk1Name;          // DIMBLK1 (older DXF specific)
    // private String dimBlk2Name;          // DIMBLK2 (older DXF specific)
    private double arrowSize = 0.18;          // DIMASZ (41) -> Standard AutoCAD: 0.18

    // Text
    private String textStyleName = "STANDARD"; // DIMTXSTY (340)
    private int textColor = 0;                // DIMCLRT (178) - 0 = ByBlock, 256 = ByLayer
    private double textHeight = 0.18;         // DIMTXT (140, fallback 44 or 40) -> Standard AutoCAD: 0.18
    private double textGap = 0.09;            // DIMGAP (147, fallback 278 or 48) -> Standard AutoCAD: 0.09 (or 0.625 * text_height in some contexts, relative)

    // Units
    private int decimalPlaces = 2;            // DIMDEC (271 for linear) -> Standard AutoCAD: 2 for metric, 4 for imperial often
    // private int angularDecimalPlaces = 0;   // DIMADEC (272 for angular)

    // Fit and Placement
    private int textVerticalAlignment = 0;    // DIMTAD (77) - 0=center, 1=above, 2=os, 3=jis
    private boolean textInsideHorizontal = true; // DIMTIH (73) - 1=true, 0=false
    private boolean textOutsideHorizontal = true;// DIMTOH (74) - 1=true, 0=false
    private boolean textOutsideExtensions = false; // DIMTOFL (172) - Force text between ext lines if it doesn't fit
    private boolean suppressFirstExtensionLine = false; // DIMSE1 (75) (DXF R12, or bit in 70)
    private boolean suppressSecondExtensionLine = false; // DIMSE2 (76) (DXF R12, or bit in 70)


    // TODO: Add more fields as they become necessary from DXF files.
    // Examples: DIMLWD (dim line lineweight), DIMLWE (ext line lineweight),
    // DIMSCALE (overall scale), DIMLFAC (linear unit scale factor), etc.

    public DxfDimStyle(String name) {
        this.name = name;
        // Default AutoCAD settings for a new style often depend on the template (imperial/metric)
        // The values set above are common starting points.
        // For example, textGap often defaults to 0.09 units, not relative to textHeight initially.
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public int getFlags70() {
        return flags70;
    }

    public void setFlags70(int flags70) {
        this.flags70 = flags70;
    }

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
        this.dimBlkName = dimBlkName;
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
        this.textStyleName = textStyleName;
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
    public String toString() {
        return "DxfDimStyle{" +
                "name='" + name + '\'' +
                ", handle='" + handle + '\'' +
                ", flags70=" + flags70 +
                ", dimensionLineColor=" + dimensionLineColor +
                ", extensionLineColor=" + extensionLineColor +
                ", extensionLineExtension=" + extensionLineExtension +
                ", extensionLineOffset=" + extensionLineOffset +
                ", dimBlkName='" + dimBlkName + '\'' +
                ", arrowSize=" + arrowSize +
                ", textStyleName='" + textStyleName + '\'' +
                ", textColor=" + textColor +
                ", textHeight=" + textHeight +
                ", textGap=" + textGap +
                ", decimalPlaces=" + decimalPlaces +
                ", textVerticalAlignment=" + textVerticalAlignment +
                ", textInsideHorizontal=" + textInsideHorizontal +
                ", textOutsideHorizontal=" + textOutsideHorizontal +
                ", textOutsideExtensions=" + textOutsideExtensions +
                ", suppressFirstExtensionLine=" + suppressFirstExtensionLine +
                ", suppressSecondExtensionLine=" + suppressSecondExtensionLine +
                '}';
    }
}
