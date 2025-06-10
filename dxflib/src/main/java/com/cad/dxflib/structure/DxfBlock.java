package com.cad.dxflib.structure;

import com.cad.dxflib.common.DxfEntity;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.math.Bounds;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a DXF BLOCK definition.
 * A block is a collection of entities that can be inserted multiple times
 * (as INSERT entities) into a drawing.
 */
public class DxfBlock {
    private final String name; // Block name (code 2)
    private Point3D basePoint; // Base insertion point (code 10, 20, 30)
    private final List<DxfEntity> entities; // Entities that make up this block definition

    /**
     * Constructs a new DxfBlock with the given name.
     * The base point is initialized to (0,0,0).
     * @param name The name of the block. Must not be null or empty.
     * @throws IllegalArgumentException if the name is null or empty.
     */
    public DxfBlock(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Block name cannot be null or empty.");
        }
        this.name = name;
        this.entities = new ArrayList<>();
        this.basePoint = new Point3D(0, 0, 0); // Default base point
    }

    /**
     * Gets the name of the block.
     * @return The block name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the base point of the block. This is the reference point used when inserting the block.
     * @return The base point.
     */
    public Point3D getBasePoint() {
        return basePoint;
    }

    /**
     * Sets the base point of the block.
     * @param basePoint The new base point. Must not be null.
     */
    public void setBasePoint(Point3D basePoint) {
        this.basePoint = Objects.requireNonNull(basePoint, "Base point cannot be null");
    }

    /**
     * Gets an unmodifiable list of entities contained within this block definition.
     * @return An unmodifiable list of entities.
     */
    public List<DxfEntity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    /**
     * Adds an entity to this block definition.
     * @param entity The DxfEntity to add. If null, the entity is not added.
     */
    public void addEntity(DxfEntity entity) {
        if (entity != null) {
            this.entities.add(entity);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DxfBlock dxfBlock = (DxfBlock) o;
        return Objects.equals(name, dxfBlock.name); // Blocks are typically unique by name
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * Calculates the bounding box for all entities within this block definition,
     * relative to the block's base point (0,0,0).
     * @return A Bounds object. If the block has no entities or entities with no bounds,
     *         the bounds might be invalid or represent a point at origin.
     */
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
