package com.cad.dxflib.objects;

import java.util.Objects;

public class DxfScale {
    private String handle; // code 5
    private String ownerHandle; // code 330
    private String name; // code 300
    private double paperUnits; // code 140
    private double drawingUnits; // code 141
    private boolean isUnitScale; // code 290
    private int flags; // code 70 (obsolete)

    public DxfScale() {
        // Default values
        this.paperUnits = 1.0;
        this.drawingUnits = 1.0;
        this.isUnitScale = false; // Typically true only for 1:1 scale
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getOwnerHandle() {
        return ownerHandle;
    }

    public void setOwnerHandle(String ownerHandle) {
        this.ownerHandle = ownerHandle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPaperUnits() {
        return paperUnits;
    }

    public void setPaperUnits(double paperUnits) {
        this.paperUnits = paperUnits;
    }

    public double getDrawingUnits() {
        return drawingUnits;
    }

    public void setDrawingUnits(double drawingUnits) {
        this.drawingUnits = drawingUnits;
    }

    public boolean isUnitScale() {
        return isUnitScale;
    }

    public void setUnitScale(boolean unitScale) {
        isUnitScale = unitScale;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DxfScale dxfScale = (DxfScale) o;
        return Objects.equals(handle, dxfScale.handle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handle);
    }

    @Override
    public String toString() {
        return "DxfScale{" +
                "handle='" + handle + '\'' +
                (name != null ? ", name='" + name + '\'' : "") +
                ", paperUnits=" + paperUnits +
                ", drawingUnits=" + drawingUnits +
                ", isUnitScale=" + isUnitScale +
                (ownerHandle != null ? ", ownerHandle='" + ownerHandle + '\'' : "") +
                '}';
    }
}
