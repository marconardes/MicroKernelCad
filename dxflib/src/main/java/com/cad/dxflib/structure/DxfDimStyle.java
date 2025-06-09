package com.cad.dxflib.structure;

public class DxfDimStyle {
    private String name; // Code 2

    // Atributos de DIMSTYLE priorizados (com códigos DXF comuns)
    private double arrowSize = 2.5;           // DIMASZ (41)
    private double extensionLineOffset = 0.625; // DIMEXO (42)
    private double extensionLineExtension = 1.25; // DIMEXE (43)
    private double textHeight = 2.5;          // DIMTXT (140, fallback 44)
    private int decimalPlaces = 4;            // DIMDEC (271)
    private double textGap = 0.09;            // DIMGAP (147, fallback 278, 48) - Padrão AutoCAD é geralmente 0.09 * textHeight

    private int dimensionLineColor = 0;       // DIMCLRD (176) - 0 = ByBlock, 256 = ByLayer
    private int extensionLineColor = 0;       // DIMCLRE (177) - 0 = ByBlock, 256 = ByLayer
    private int textColor = 0;                // DIMCLRT (178) - 0 = ByBlock, 256 = ByLayer

    // Outros campos do DIMSTYLE podem ser adicionados aqui no futuro

    public DxfDimStyle(String name) {
        this.name = name;
        // Inicializar textGap com base na altura do texto padrão, se desejado
        // this.textGap = 0.09 * this.textHeight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getArrowSize() {
        return arrowSize;
    }

    public void setArrowSize(double arrowSize) {
        this.arrowSize = arrowSize;
    }

    public double getExtensionLineOffset() {
        return extensionLineOffset;
    }

    public void setExtensionLineOffset(double extensionLineOffset) {
        this.extensionLineOffset = extensionLineOffset;
    }

    public double getExtensionLineExtension() {
        return extensionLineExtension;
    }

    public void setExtensionLineExtension(double extensionLineExtension) {
        this.extensionLineExtension = extensionLineExtension;
    }

    public double getTextHeight() {
        return textHeight;
    }

    public void setTextHeight(double textHeight) {
        this.textHeight = textHeight;
        // Se textGap depende da altura do texto, atualize-o aqui também.
        // this.textGap = 0.09 * this.textHeight;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    public double getTextGap() {
        return textGap;
    }

    public void setTextGap(double textGap) {
        this.textGap = textGap;
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

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    @Override
    public String toString() {
        return "DxfDimStyle{" +
               "name='" + name + '\'' +
               ", arrowSize=" + arrowSize +
               ", extensionLineOffset=" + extensionLineOffset +
               ", extensionLineExtension=" + extensionLineExtension +
               ", textHeight=" + textHeight +
               ", decimalPlaces=" + decimalPlaces +
               ", textGap=" + textGap +
               ", dimensionLineColor=" + dimensionLineColor +
               ", extensionLineColor=" + extensionLineColor +
               ", textColor=" + textColor +
               '}';
    }
}
