package com.cad.dxflib.structure;

import com.cad.dxflib.common.DxfEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DxfLayer {
    private String name;
    private int color = 7; // Default DXF color for layer (white/black)
    private String linetypeName = "CONTINUOUS";
    private boolean visible = true;
    private List<DxfEntity> entities;

    public DxfLayer(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Layer name cannot be null or empty.");
        }
        this.name = name;
        this.entities = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getLinetypeName() {
        return linetypeName;
    }

    public void setLinetypeName(String linetypeName) {
        this.linetypeName = linetypeName;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<DxfEntity> getEntities() {
        return entities;
    }

    public void addEntity(DxfEntity entity) {
        if (entity != null) {
            this.entities.add(entity);
            // Optionally set the entity's layer name if not already set,
            // or ensure consistency. For now, direct add.
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DxfLayer dxfLayer = (DxfLayer) o;
        return Objects.equals(name, dxfLayer.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "DxfLayer{'name='" + name + "'}";
    }
}
