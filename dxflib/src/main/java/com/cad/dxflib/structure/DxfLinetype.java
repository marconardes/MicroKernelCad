package com.cad.dxflib.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DxfLinetype {
    private String name;
    private String description;
    private double patternLength;
    private List<Double> patternElements; // Positive for dash, negative for space, zero for dot

    public DxfLinetype(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Linetype name cannot be null or empty.");
        }
        this.name = name;
        this.description = "";
        this.patternElements = new ArrayList<>();
        this.patternLength = 0.0;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    public double getPatternLength() {
        return patternLength;
    }

    public void setPatternLength(double patternLength) {
        this.patternLength = patternLength;
    }

    public List<Double> getPatternElements() {
        return patternElements;
    }

    public void addPatternElement(double elementLength) {
        this.patternElements.add(elementLength);
    }

    public boolean isContinuous() {
        return patternElements.isEmpty() ||
               (patternElements.size() == 1 && patternElements.get(0) >= 0 && patternLength > 0) || // Solid line if one positive element
               name.equalsIgnoreCase("CONTINUOUS"); // Common name
    }

    // Helper to generate an SVG stroke-dasharray string
    public String getSvgStrokeDashArray() {
        if (isContinuous() || patternElements.isEmpty()) {
            return "none"; // Or null to not apply the attribute
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < patternElements.size(); i++) {
            // DXF positive=dash, negative=space, zero=dot.
            // SVG dasharray is all positive lengths: dash, gap, dash, gap...
            // A DXF dot (0) can be represented by a very short dash and a gap.
            double val = patternElements.get(i);
            if (val == 0) { // DOT
                sb.append("1.0"); // Short dash for dot (adjust as needed)
                // Peek next for gap, or assume a small gap if it's the last element or followed by another dot/dash
                if (i + 1 < patternElements.size() && patternElements.get(i+1) < 0) {
                    // Gap is specified next
                } else {
                     sb.append(" 1.0"); // Default gap after a dot if no explicit gap follows
                }
            } else {
                sb.append(String.format(java.util.Locale.US, "%.3f", Math.abs(val)));
            }
            if (i < patternElements.size() - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DxfLinetype that = (DxfLinetype) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "DxfLinetype{" +
               "name='" + name + '\'' +
               ", pattern=" + patternElements +
               '}';
    }
}
