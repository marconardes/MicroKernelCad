package com.cad.dxflib.structure;

import com.cad.dxflib.common.DxfEntity;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.math.Bounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DxfBlock {
    private String name;
    private Point3D basePoint;
    private List<DxfEntity> entities;
    // Potentially add other block properties like flags, xref path etc. later

    public DxfBlock(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Block name cannot be null or empty.");
        }
        this.name = name;
        this.entities = new ArrayList<>();
        this.basePoint = new Point3D(0, 0, 0); // Default base point
    }

    public String getName() {
        return name;
    }

    public Point3D getBasePoint() {
        return basePoint;
    }

    public void setBasePoint(Point3D basePoint) {
        this.basePoint = Objects.requireNonNull(basePoint, "Base point cannot be null");
    }

    public List<DxfEntity> getEntities() {
        return entities;
    }

    public void addEntity(DxfEntity entity) {
        if (entity != null) {
            this.entities.add(entity);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DxfBlock dxfBlock = (DxfBlock) o;
        return Objects.equals(name, dxfBlock.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public Bounds getBounds() {
        Bounds blockBounds = new Bounds();
        if (entities != null) {
            for (DxfEntity entity : entities) {
                Bounds entityBounds = entity.getBounds();
                if (entityBounds != null && entityBounds.isValid()) {
                    blockBounds.addToBounds(entityBounds);
                }
            }
        }
        return blockBounds;
    }
}
