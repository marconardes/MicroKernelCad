package com.cad.dxflib.common;

// Forward declaration for Bounds, assume it will be created in a math sub-package or similar
// For now, to avoid compilation error if Bounds.java isn't created yet by the worker:
// import com.cad.dxflib.math.Bounds; // This should be the actual import later

public interface DxfEntity {
    EntityType getType();
    String getLayerName();
    void setLayerName(String layerName);

    int getColor(); // DXF color index
    void setColor(int color);

    String getLinetypeName(); // Optional: For future use
    void setLinetypeName(String linetypeName); // Optional

    double getThickness(); // Optional: For future use
    void setThickness(double thickness); // Optional

    // We'll need a Bounds class later. For now, can be commented out or use a placeholder.
    // Bounds getBounds();

    // Method to apply transformations, crucial for block inserts
    // We'll need Point3D and a transformation matrix/context class later.
    // void transform(Object transformContext); // Placeholder for now
}
