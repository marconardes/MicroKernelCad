package com.cad.dxflib.common;

import com.cad.dxflib.math.Bounds;
import com.cad.dxflib.parser.DxfGroupCode;
//Comons
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for most DXF entities.
 * Implements common properties such as layer name, color, linetype, thickness,
 * Extended Entity Data (XDATA), and reactor handles.
 */
public abstract class AbstractDxfEntity implements DxfEntity {
    protected String layerName = "0"; // Default layer (code 8)
    protected int color = 256; // DXF color code 256 = BYLAYER (code 62)
    protected String linetypeName = "BYLAYER"; // Default linetype (code 6)
    protected double thickness = 0.0; // Entity thickness (code 39)
    protected Map<String, List<DxfGroupCode>> xdata;
    protected List<String> reactorHandles;

    /**
     * Constructs an AbstractDxfEntity, initializing common fields to default values.
     * Layer is "0", color is 256 (BYLAYER), linetype is "BYLAYER".
     * XDATA map and reactor handles list are initialized as empty.
     */
    public AbstractDxfEntity() {
        this.xdata = new HashMap<> ();
        this.reactorHandles = new ArrayList<> ();
    }

    /**
     * Gets the layer name of the entity.
     *
     * @return The layer name.
     */
    @Override
    public String getLayerName () {
        return layerName;
    }

    /**
     * Sets the layer name for the entity.
     * If the provided name is null or empty, it defaults to "0".
     *
     * @param layerName The new layer name.
     */
    @Override
    public void setLayerName (String layerName) {
        this.layerName = (layerName != null && !layerName.trim ().isEmpty ()) ? layerName : "0";
    }

    /**
     * Gets the color index of the entity.
     * 256 means BYLAYER, 0 means BYBLOCK.
     *
     * @return The color index.
     */
    @Override
    public int getColor () {
        return color;
    }

    /**
     * Sets the color index for the entity.
     *
     * @param color The new color index.
     */
    @Override
    public void setColor (int color) {
        this.color = color;
    }

    /**
     * Gets the linetype name of the entity.
     * "BYLAYER" and "BYBLOCK" are common special values.
     *
     * @return The linetype name.
     */
    @Override
    public String getLinetypeName () {
        return linetypeName;
    }

    /**
     * Sets the linetype name for the entity.
     * If the provided name is null or empty, it defaults to "BYLAYER".
     *
     * @param linetypeName The new linetype name.
     */
    @Override
    public void setLinetypeName (String linetypeName) {
        this.linetypeName = (linetypeName != null
                && !linetypeName.trim ().isEmpty ()) ? linetypeName : "BYLAYER";
    }

    /**
     * Gets the thickness of the entity (e.g., for lines or polylines).
     *
     * @return The thickness value.
     */
    @Override
    public double getThickness () {
        return thickness;
    }

    /**
     * Sets the thickness for the entity.
     *
     * @param thickness The new thickness value.
     */
    @Override
    public void setThickness (double thickness) {
        this.thickness = thickness;
    }

    /**
     * Adds Extended Entity Data (XDATA) associated with a specific application name.
     * XDATA allows applications to attach custom data to DXF entities.
     *
     * @param appName The application name (registered application ID, code 1001).
     * @param data    A list of {@link DxfGroupCode} objects representing the XDATA.
     */
    public void addXData (String appName, List<DxfGroupCode> data) {
        if (appName != null && !appName.isEmpty () && data != null) {
            this.xdata.computeIfAbsent (appName, k -> new ArrayList<> ()).addAll (data);
        }
    }

    /**
     * Gets all XDATA associated with this entity.
     *
     * @return An unmodifiable map where keys are application names and values are lists of {@link DxfGroupCode}s.
     */
    public Map<String, List<DxfGroupCode>> getXData () {
        return Collections.unmodifiableMap (xdata);
    }

    /**
     * Gets the XDATA for a specific application name.
     *
     * @param appName The application name.
     * @return An unmodifiable list of {@link DxfGroupCode}s for the given application, or null if not found.
     */
    public List<DxfGroupCode> getXDataForApplication (String appName) {
        List<DxfGroupCode> dataList = xdata.get (appName);
        return dataList != null ? Collections.unmodifiableList (dataList) : null;
    }

    /**
     * Adds a reactor handle to this entity.
     * Reactors are a mechanism for objects to be notified of changes in other objects.
     *
     * @param handle The handle (string) of the reactor object.
     */
    public void addReactorHandle (String handle) {
        if (handle != null && !handle.isEmpty ()) {
            this.reactorHandles.add (handle);
        }
    }

    /**
     * Gets an unmodifiable list of reactor handles associated with this entity.
     *
     * @return An unmodifiable list of reactor handle strings.
     */
    public List<String> getReactorHandles () {
        return Collections.unmodifiableList (reactorHandles);
    }

    /**
     * Gets the specific DXF entity type.
     * This method must be implemented by all concrete entity subclasses.
     *
     * @return The {@link EntityType} enum value.
     */
    @Override
    public abstract EntityType getType ();

    /**
     * Calculates and returns the geometric bounds of this entity.
     * This method must be implemented by all concrete entity subclasses.
     *
     * @return A {@link Bounds} object representing the entity's extents.
     */
    @Override
    public abstract Bounds getBounds ();

}
