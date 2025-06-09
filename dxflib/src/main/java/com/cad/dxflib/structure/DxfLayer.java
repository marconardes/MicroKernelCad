package com.cad.dxflib.structure;

import com.cad.dxflib.common.DxfEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a DXF LAYER table entry.
 * Layers are used to organize entities in a drawing and control properties like
 * color, linetype, and visibility.
 */
public class DxfLayer {
    private final String name; // Layer name (code 2)
    private int color = 7; // Default DXF color for layer (white/black for light/dark themes)
    private String linetypeName = "CONTINUOUS"; // Default linetype name (code 6)
    private boolean visible = true; // Layer visibility (derived from color code 62: negative means off)
    private final List<DxfEntity> entities; // Entities belonging to this layer

    /**
     * Constructs a new DxfLayer with the given name.
     * Entities list is initialized. Default color is 7 (white/black), default linetype is "CONTINUOUS".
     * @param name The name of the layer. Must not be null or empty.
     * @throws IllegalArgumentException if the name is null or empty.
     */
    public DxfLayer(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Layer name cannot be null or empty.");
        }
        this.name = name;
        this.entities = new ArrayList<>();
    }

    /**
     * Gets the name of the layer.
     * @return The layer name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the color index of the layer.
     * @return The color index.
     */
    public int getColor() {
        return color;
    }

    /**
     * Sets the color index of the layer.
     * Also updates visibility based on the sign of the color index.
     * @param color The color index. A negative value typically means the layer is off (not visible).
     */
    public void setColor(int color) {
        this.color = color;
        // DXF convention: if color is negative, layer is off.
        // We store the absolute color, visibility is separate.
        // The DxfParser handles this by setting visible explicitly.
    }

    /**
     * Gets the name of the linetype associated with this layer.
     * @return The linetype name.
     */
    public String getLinetypeName() {
        return linetypeName;
    }

    /**
     * Sets the name of the linetype for this layer.
     * @param linetypeName The linetype name.
     */
    public void setLinetypeName(String linetypeName) {
        this.linetypeName = (linetypeName != null && !linetypeName.trim().isEmpty()) ? linetypeName : "CONTINUOUS";
    }

    /**
     * Checks if the layer is visible.
     * @return true if visible, false otherwise.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets the visibility of the layer.
     * @param visible true to make the layer visible, false to make it hidden.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Gets an unmodifiable list of entities on this layer.
     * @return An unmodifiable list of entities.
     */
    public List<DxfEntity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    /**
     * Adds an entity to this layer.
     * Note: This method is typically called by DxfDocument when adding entities,
     * which ensures the entity is also in the main model space list if appropriate.
     * @param entity The DxfEntity to add. If null, the entity is not added.
     */
    public void addEntity(DxfEntity entity) {
        if (entity != null) {
            this.entities.add(entity);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DxfLayer dxfLayer = (DxfLayer) o;
        // Layer names are case-insensitive in DXF, comparison should reflect this.
        // Assuming names are stored consistently (e.g., uppercase) by DxfDocument.
        return Objects.equals(name, dxfLayer.name);
    }

    @Override
    public int hashCode() {
        // Assuming names are stored consistently (e.g., uppercase) for hashing.
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "DxfLayer{'name='" + name + "'}";
    }
}
