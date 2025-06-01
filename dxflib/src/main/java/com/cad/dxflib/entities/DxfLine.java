package com.cad.dxflib.entities;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.math.Bounds;

public class DxfLine extends AbstractDxfEntity {
    private Point3D startPoint;
    private Point3D endPoint;

    public DxfLine() {
        this.startPoint = new Point3D(0, 0, 0);
        this.endPoint = new Point3D(0, 0, 0);
    }

    public Point3D getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Point3D startPoint) {
        this.startPoint = startPoint;
    }

    public Point3D getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(Point3D endPoint) {
        this.endPoint = endPoint;
    }

    @Override
    public EntityType getType() {
        return EntityType.LINE;
    }

    @Override
    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        if (startPoint != null) bounds.addToBounds(this.startPoint);
        if (endPoint != null) bounds.addToBounds(this.endPoint);
        // TODO: Consider entity thickness for Z bounds if relevant for 2D projection
        return bounds;
    }

    // @Override
    // public void transform(Object transformContext) {
    //     // Implementation will be added later
    //     // this.startPoint = transformContext.transform(this.startPoint);
    //     // this.endPoint = transformContext.transform(this.endPoint);
    // }

    @Override
    public String toString() {
        return "DxfLine{" +
               "start=" + startPoint +
               ", end=" + endPoint +
               ", layer='" + layerName + '\'' +
               ", color=" + color +
               '}';
    }
}
