package com.cad.dxflib.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a DXF LTYPE (Linetype) table entry.
 * Linetypes define the dash-dot pattern of lines.
 */
public class DxfLinetype {
    private final String name; // Linetype name (code 2)
    private String description; // Descriptive text (code 3)
    private double patternLength; // Total length of one repetition of the pattern (code 40)
    private final List<Double> patternElements; // Dash/dot/space lengths (code 49, repeated)
                                          // Positive for dash, negative for space, zero for dot.

    /**
     * Constructs a new DxfLinetype with the given name.
     * Description is initialized to empty, pattern length to 0, and pattern elements to an empty list.
     * @param name The name of the linetype. Must not be null or empty.
     * @throws IllegalArgumentException if the name is null or empty.
     */
    public DxfLinetype(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Linetype name cannot be null or empty.");
        }
        this.name = name;
        this.description = "";
        this.patternElements = new ArrayList<>();
        this.patternLength = 0.0;
    }

    /**
     * Gets the name of the linetype.
     * @return The linetype name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the descriptive text for the linetype.
     * @return The description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the descriptive text for the linetype.
     * @param description The description. If null, it's set to an empty string.
     */
    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    /**
     * Gets the total length of one pattern repetition.
     * @return The pattern length.
     */
    public double getPatternLength() {
        return patternLength;
    }

    /**
     * Sets the total length of one pattern repetition.
     * @param patternLength The pattern length.
     */
    public void setPatternLength(double patternLength) {
        this.patternLength = patternLength;
    }

    /**
     * Gets an unmodifiable list of pattern elements (dash, space, dot lengths).
     * Positive values are dashes, negative values are spaces, and zero is a dot.
     * @return An unmodifiable list of pattern element lengths.
     */
    public List<Double> getPatternElements() {
        return Collections.unmodifiableList(patternElements);
    }

    /**
     * Adds a pattern element (dash, space, or dot length) to the linetype definition.
     * @param elementLength The length of the element. Positive for dash, negative for space, zero for dot.
     */
    public void addPatternElement(double elementLength) {
        this.patternElements.add(elementLength);
    }

    /**
     * Checks if this linetype represents a continuous (solid) line.
     * @return true if the linetype is continuous, false otherwise.
     */
    public boolean isContinuous() {
        return patternElements.isEmpty() ||
               (patternElements.size() == 1 && patternElements.get(0) >= 0 && patternLength > 0) ||
               name.equalsIgnoreCase("CONTINUOUS");
    }

    /**
     * Generates an SVG stroke-dasharray string representation of this linetype.
     * Note: DXF dots (length 0) are approximated by a short dash and gap.
     * @return A string suitable for SVG's `stroke-dasharray` attribute, or "none" for continuous lines.
     */
    public String getSvgStrokeDashArray() {
        if (isContinuous() || patternElements.isEmpty()) {
            return "none";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < patternElements.size(); i++) {
            double val = patternElements.get(i);
            if (val == 0) { // DOT
                sb.append("1.0");
                if (i + 1 < patternElements.size() && patternElements.get(i+1) < 0) {
                    // Gap is specified next, it will be handled by the next iteration
                } else if (i + 1 < patternElements.size() && patternElements.get(i+1) == 0) { // Dot followed by Dot
                     sb.append(" 1.0"); // Default gap after a dot if followed by another dot
                } else if (i == patternElements.size() -1 ) { // Last element is a dot
                    // No explicit gap after last dot, SVG might handle this by repeating or stopping.
                    // To ensure a visible dot, one might add a tiny gap if it were allowed by SVG spec to end on gap.
                } else { // Dot followed by dash or end of pattern elements that are not a gap
                    sb.append(" 1.0"); // Default gap if not followed by an explicit space
                }
            } else {
                sb.append(String.format(java.util.Locale.US, "%.3f", Math.abs(val)));
            }
            if (i < patternElements.size() - 1) {
                sb.append(" ");
            }
        }
        // Ensure the string is not empty if only dots were present and handled weirdly
        if (sb.length() == 0 && !patternElements.isEmpty()) {
            // This case might occur if all elements are dots and logic above doesn't produce output
            // For a pattern of only dots, e.g. ". . .", it's "1.0 1.0 1.0 1.0 ..."
            for(int i=0; i < patternElements.size(); ++i) {
                sb.append("1.0");
                if (i < patternElements.size() - 1) sb.append(" 1.0 ");
            }
        }
        return sb.length() > 0 ? sb.toString() : "none";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DxfLinetype that = (DxfLinetype) o;
        // Linetype names are case-insensitive in DXF and should be the primary identifier.
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
        return "DxfLinetype{" +
               "name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", patternLength=" + patternLength +
               ", patternElements=" + patternElements +
               '}';
    }
}
