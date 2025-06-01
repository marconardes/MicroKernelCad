package com.cad.dxflib.common;

import com.cad.dxflib.math.Bounds;

public abstract class AbstractDxfEntity implements DxfEntity {
    protected String layerName = "0"; // Default layer
    protected int color = 256; // DXF color code 256 = BYLAYER
    protected String linetypeName = "BYLAYER"; // Default linetype
    protected double thickness = 0.0;

    @Override
    public String getLayerName() {
        return layerName;
    }

    @Override
    public void setLayerName(String layerName) {
        this.layerName = (layerName != null && !layerName.trim().isEmpty()) ? layerName : "0";
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public void setColor(int color) {
        // Basic validation, DXF colors are typically 0-256 (0=BYBLOCK, 256=BYLAYER)
        // Specific entities might have true color support later.
        this.color = color;
    }

    @Override
    public String getLinetypeName() {
        return linetypeName;
    }

    @Override
    public void setLinetypeName(String linetypeName) {
        this.linetypeName = (linetypeName != null && !linetypeName.trim().isEmpty()) ? linetypeName : "BYLAYER";
    }

    @Override
    public double getThickness() {
        return thickness;
    }

    @Override
    public void setThickness(double thickness) {
        this.thickness = thickness;
    }

    // getType() and getBounds() will be abstract and implemented by concrete entity classes.
    @Override
    public abstract EntityType getType();

    @Override
    public abstract Bounds getBounds();

    // @Override
    // public abstract void transform(Object transformContext); // Will be uncommented
}
