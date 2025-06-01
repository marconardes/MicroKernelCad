package com.cad.dxflib.converter;

public class SvgConversionOptions {
    private double strokeWidth = 1.0; // Default stroke width for entities
    private String defaultStrokeColor = "black"; // Default if color is not specified or resolved
    private double margin = 10.0; // Margin around the drawing in SVG units
    private boolean groupElementsByLayer = false; // Whether to group SVG elements by layer

    // Add constructors, getters, and setters as needed.
    // For now, a simple class with public fields or default constructor is fine.

    public double getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth > 0 ? strokeWidth : 0.1; // Ensure positive
    }

    public String getDefaultStrokeColor() {
        return defaultStrokeColor;
    }

    public void setDefaultStrokeColor(String defaultStrokeColor) {
        this.defaultStrokeColor = defaultStrokeColor;
    }

    public double getMargin() {
        return margin;
    }

    public void setMargin(double margin) {
        this.margin = margin;
    }

    public boolean isGroupElementsByLayer() {
        return groupElementsByLayer;
    }

    public void setGroupElementsByLayer(boolean groupElementsByLayer) {
        this.groupElementsByLayer = groupElementsByLayer;
    }
}
