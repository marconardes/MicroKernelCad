package com.cad.dxflib.structure;

import com.cad.dxflib.common.DxfEntity;
import com.cad.dxflib.math.Bounds;
import com.cad.dxflib.objects.DxfDictionary;
import com.cad.dxflib.objects.DxfScale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 * Represents the entire DXF document.
 * This class holds all parsed data from a DXF file, including layers, blocks, entities,
 * table entries (linetypes, text styles, dimension styles, block records),
 * and objects (dictionaries, scales).
 */
public class DxfDocument {
    private final Map<String, DxfLayer> layers;
    private final Map<String, DxfBlock> blocks;
    private final Map<String, DxfLinetype> linetypes;
    private final Map<String, DxfDimStyle> dimensionStyles;
    private final Map<String, DxfTextStyle> textStyles;
    private final Map<String, DxfBlockRecord> blockRecords;
    private final Map<String, DxfDictionary> dictionaries;
    private final Map<String, DxfScale> scales;
    private final Map<String, Object> genericObjects; // For objects not yet strongly typed

    // Entities from the ENTITIES section not associated with a specific block (typically Model Space)
    private final List<DxfEntity> modelSpaceEntities;

    /**
     * Constructs a new DxfDocument.
     * Initializes all internal collections and adds default elements like layer "0",
     * "CONTINUOUS" linetype, "STANDARD" text style, "STANDARD" dimension style,
     * and default block records for "*Model_Space" and "*Paper_Space".
     */
    public DxfDocument() {
        this.layers = new HashMap<>();
        this.blocks = new HashMap<>();
        this.linetypes = new HashMap<>();
        this.dimensionStyles = new HashMap<>();
        this.textStyles = new HashMap<>();
        this.blockRecords = new HashMap<>();
        this.dictionaries = new HashMap<>();
        this.scales = new HashMap<>();
        this.genericObjects = new HashMap<>();
        this.modelSpaceEntities = new ArrayList<>();

        // Add default layer "0"
        DxfLayer defaultLayer = new DxfLayer("0");
        addLayer(defaultLayer); // Use addLayer to ensure consistent casing

        // Add default linetype "CONTINUOUS"
        DxfLinetype continuousLinetype = new DxfLinetype("CONTINUOUS");
        continuousLinetype.setDescription("Solid line");
        addLinetype(continuousLinetype); // Use addLinetype

        // Add default text style "STANDARD"
        DxfTextStyle standardTextStyle = new DxfTextStyle("STANDARD");
        addTextStyle(standardTextStyle); // Use addTextStyle

        // Add default Dimension Style "STANDARD"
        DxfDimStyle standardDimStyle = new DxfDimStyle("STANDARD");
        standardDimStyle.setDimensionLineColor(0);
        standardDimStyle.setExtensionLineColor(0);
        standardDimStyle.setExtensionLineExtension(0.18);
        standardDimStyle.setExtensionLineOffset(0.0625);
        standardDimStyle.setArrowSize(0.18);
        standardDimStyle.setTextStyleName("STANDARD");
        standardDimStyle.setTextColor(0);
        standardDimStyle.setTextHeight(0.18);
        standardDimStyle.setTextGap(0.09);
        standardDimStyle.setTextVerticalAlignment(1);
        standardDimStyle.setDecimalPlaces(2);
        standardDimStyle.setTextInsideHorizontal(true);
        standardDimStyle.setTextOutsideHorizontal(true);
        addDimensionStyle(standardDimStyle); // Use addDimensionStyle

        // Add default block records for *Model_Space and *Paper_Space
        addBlockRecord(new DxfBlockRecord("*Model_Space"));
        addBlockRecord(new DxfBlockRecord("*Paper_Space"));
    }

    /**
     * Retrieves a layer by its name (case-insensitive).
     * @param name The name of the layer.
     * @return The DxfLayer object, or null if not found.
     */
    public DxfLayer getLayer(String name) {
        if (name == null) {
            return null;
        }
        return layers.get(name.toUpperCase(java.util.Locale.ROOT));
    }

    /**
     * Adds a layer to the document. The layer name is stored in uppercase.
     * @param layer The DxfLayer object to add.
     */
    public void addLayer(DxfLayer layer) {
        if (layer != null && layer.getName() != null) {
            this.layers.put(layer.getName().toUpperCase(java.util.Locale.ROOT), layer);
        }
    }

    /**
     * Gets an unmodifiable map of all layers in the document.
     * Keys are layer names (uppercase).
     * @return An unmodifiable map of layers.
     */
    public Map<String, DxfLayer> getLayers() {
        return Collections.unmodifiableMap(layers);
    }

    /**
     * Retrieves a block by its name (case-insensitive).
     * @param name The name of the block.
     * @return The DxfBlock object, or null if not found.
     */
    public DxfBlock getBlock(String name) {
        if (name == null) {
            return null;
        }
        return blocks.get(name.toUpperCase(java.util.Locale.ROOT));
    }

    /**
     * Adds a block definition to the document. The block name is stored in uppercase.
     * @param block The DxfBlock object to add.
     */
    public void addBlock(DxfBlock block) {
        if (block != null && block.getName() != null) {
            this.blocks.put(block.getName().toUpperCase(java.util.Locale.ROOT), block);
        }
    }

    /**
     * Gets an unmodifiable map of all block definitions in the document.
     * Keys are block names (uppercase).
     * @return An unmodifiable map of blocks.
     */
    public Map<String, DxfBlock> getBlocks() {
        return Collections.unmodifiableMap(blocks);
    }

    /**
     * Adds a top-level entity (typically to Model Space).
     * The entity is also added to its respective layer's entity list.
     * If the layer specified in the entity does not exist, it attempts to use layer "0".
     * If layer "0" is also missing (which shouldn't happen with a properly initialized document),
     * a new layer "0" is created and used.
     * @param entity The DxfEntity to add.
     */
    public void addEntity(DxfEntity entity) {
        if (entity != null) {
            DxfLayer layer = getLayer(entity.getLayerName());
            if (layer == null) {
                layer = getLayer("0");
                if (layer == null) {
                     layer = new DxfLayer("0");
                     addLayer(layer);
                }
            }
            layer.addEntity(entity);
            this.modelSpaceEntities.add(entity);
        }
    }

    /**
     * Gets an unmodifiable list of entities defined directly in the ENTITIES section (Model Space).
     * @return An unmodifiable list of model space entities.
     */
    public List<DxfEntity> getModelSpaceEntities() {
        return Collections.unmodifiableList(modelSpaceEntities);
    }

    /**
     * Retrieves a linetype by its name (case-insensitive).
     * @param name The name of the linetype.
     * @return The DxfLinetype object, or null if not found.
     */
    public DxfLinetype getLinetype(String name) {
        if (name == null) {
            return null;
        }
        return linetypes.get(name.toUpperCase(java.util.Locale.ROOT));
    }

    /**
     * Adds a linetype to the document. The linetype name is stored in uppercase.
     * @param linetype The DxfLinetype object to add.
     */
    public void addLinetype(DxfLinetype linetype) {
        if (linetype != null && linetype.getName() != null) {
            this.linetypes.put(linetype.getName().toUpperCase(java.util.Locale.ROOT), linetype);
        }
    }

    /**
     * Gets an unmodifiable map of all linetypes in the document.
     * Keys are linetype names (uppercase).
     * @return An unmodifiable map of linetypes.
     */
    public Map<String, DxfLinetype> getLinetypes() {
        return Collections.unmodifiableMap(linetypes);
    }

    /**
     * Retrieves a dimension style by its name (case-insensitive).
     * @param name The name of the dimension style.
     * @return The DxfDimStyle object, or null if not found.
     */
    public DxfDimStyle getDimensionStyle(String name) {
        if (name == null) {
            return null;
        }
        return this.dimensionStyles.get(name.toUpperCase(java.util.Locale.ROOT));
    }

    /**
     * Adds a dimension style to the document. The style name is stored in uppercase.
     * @param style The DxfDimStyle object to add.
     */
    public void addDimensionStyle(DxfDimStyle style) {
        if (style != null && style.getName() != null && !style.getName().isEmpty()) {
            this.dimensionStyles.put(style.getName().toUpperCase(java.util.Locale.ROOT), style);
        }
    }

    /**
     * Gets an unmodifiable map of all dimension styles in the document.
     * Keys are dimension style names (uppercase).
     * @return An unmodifiable map of dimension styles.
     */
    public Map<String, DxfDimStyle> getDimensionStyles() {
        return Collections.unmodifiableMap(this.dimensionStyles);
    }

    /**
     * Retrieves a text style by its name (case-insensitive).
     * @param name The name of the text style.
     * @return The DxfTextStyle object, or null if not found.
     */
    public DxfTextStyle getTextStyle(String name) {
        if (name == null) {
            return null;
        }
        return this.textStyles.get(name.toUpperCase(java.util.Locale.ROOT));
    }

    /**
     * Adds a text style to the document. The style name is stored in uppercase.
     * @param style The DxfTextStyle object to add.
     */
    public void addTextStyle(DxfTextStyle style) {
        if (style != null && style.getName() != null && !style.getName().isEmpty()) {
            this.textStyles.put(style.getName().toUpperCase(java.util.Locale.ROOT), style);
        }
    }

    /**
     * Gets an unmodifiable map of all text styles in the document.
     * Keys are text style names (uppercase).
     * @return An unmodifiable map of text styles.
     */
    public Map<String, DxfTextStyle> getTextStyles() {
        return Collections.unmodifiableMap(textStyles);
    }

    /**
     * Retrieves a block record by its name (case-insensitive).
     * @param name The name of the block record.
     * @return The DxfBlockRecord object, or null if not found.
     */
    public DxfBlockRecord getBlockRecord(String name) {
        if (name == null) {
            return null;
        }
        return this.blockRecords.get(name.toUpperCase(java.util.Locale.ROOT));
    }

    /**
     * Adds a block record to the document. The record name is stored in uppercase.
     * @param record The DxfBlockRecord object to add.
     */
    public void addBlockRecord(DxfBlockRecord record) {
        if (record != null && record.getName() != null && !record.getName().isEmpty()) {
            this.blockRecords.put(record.getName().toUpperCase(java.util.Locale.ROOT), record);
        }
    }

    /**
     * Gets an unmodifiable map of all block records in the document.
     * Keys are block record names (uppercase).
     * @return An unmodifiable map of block records.
     */
    public Map<String, DxfBlockRecord> getBlockRecords() {
        return Collections.unmodifiableMap(blockRecords);
    }

    /**
     * Retrieves a dictionary by its handle or a well-known name (case-insensitive).
     * @param nameOrHandle The handle or name of the dictionary.
     * @return The DxfDictionary object, or null if not found.
     */
    public DxfDictionary getDictionary(String nameOrHandle) {
        if (nameOrHandle == null) {
            return null;
        }
        return this.dictionaries.get(nameOrHandle.toUpperCase(java.util.Locale.ROOT));
    }

    /**
     * Adds a dictionary to the document, keyed by its handle or a well-known name (case-insensitive key).
     * @param nameOrHandle The handle or specific name (e.g., "ACAD_SCALELIST") for the dictionary.
     * @param dict The DxfDictionary object to add.
     */
    public void addDictionary(String nameOrHandle, DxfDictionary dict) {
        if (dict != null && nameOrHandle != null && !nameOrHandle.isEmpty()) {
            this.dictionaries.put(nameOrHandle.toUpperCase(java.util.Locale.ROOT), dict);
        }
    }

    /**
     * Gets an unmodifiable map of all dictionaries stored by their handle or well-known name.
     * Keys are handles/names (uppercase).
     * @return An unmodifiable map of dictionaries.
     */
    public Map<String, DxfDictionary> getDictionaries() {
        return Collections.unmodifiableMap(dictionaries);
    }

     /**
     * Retrieves a generic DXF object by its handle (case-insensitive).
     * This is used for objects that do not have a dedicated class representation yet.
     * @param handle The handle of the object.
     * @return The Object, or null if not found.
     */
    public Object getObject(String handle) {
        if (handle == null) {
            return null;
        }
        return this.genericObjects.get(handle.toUpperCase(java.util.Locale.ROOT));
    }

    /**
     * Adds a generic DXF object to the document, keyed by its handle (case-insensitive key).
     * @param handle The handle of the object.
     * @param obj The object to add.
     */
    public void addObject(String handle, Object obj) {
        if (obj != null && handle != null && !handle.isEmpty()) {
            this.genericObjects.put(handle.toUpperCase(java.util.Locale.ROOT), obj);
        }
    }

    /**
     * Gets an unmodifiable map of all generic objects stored by their handle.
     * Keys are handles (uppercase).
     * @return An unmodifiable map of generic objects.
     */
    public Map<String, Object> getGenericObjects() {
        return Collections.unmodifiableMap(genericObjects);
    }

    /**
     * Retrieves a scale object by its handle or name (case-insensitive).
     * Note: Scales are typically referenced by handle; name lookup might be ambiguous if names are not unique.
     * @param handleOrName The handle or name of the scale.
     * @return The DxfScale object, or null if not found.
     */
    public DxfScale getScale(String handleOrName) {
        if (handleOrName == null) {
            return null;
        }
        return this.scales.get(handleOrName.toUpperCase(java.util.Locale.ROOT));
    }

    /**
     * Adds a scale object to the document. It's primarily stored by its handle.
     * @param scale The DxfScale object to add.
     */
    public void addScale(DxfScale scale) {
        if (scale != null && scale.getHandle() != null && !scale.getHandle().isEmpty()) {
            this.scales.put(scale.getHandle().toUpperCase(java.util.Locale.ROOT), scale);
            // If name is also a reliable unique key, could add here too:
            // if (scale.getName() != null && !scale.getName().isEmpty()) {
            //     this.scales.put(scale.getName().toUpperCase(java.util.Locale.ROOT), scale);
            // }
        }
    }

    /**
     * Gets an unmodifiable map of all scale objects in the document.
     * Keys are typically handles (uppercase).
     * @return An unmodifiable map of scales.
     */
    public Map<String, DxfScale> getScales() {
        return Collections.unmodifiableMap(scales);
    }

    /**
     * Calculates the overall bounding box of all entities in the model space.
     * @return A Bounds object representing the total extent of model space entities. Returns invalid bounds if no entities.
     */
    public Bounds getBounds() {
        Bounds totalBounds = new Bounds();
        for (DxfEntity entity : getModelSpaceEntities()) {
            Bounds entityBounds = entity.getBounds();
            if (entityBounds != null && entityBounds.isValid()) {
                totalBounds.addToBounds(entityBounds);
            }
        }
        return totalBounds;
    }
}
