package com.cad.dxflib.structure;

import com.cad.dxflib.common.DxfEntity;
import com.cad.dxflib.math.Bounds;
import com.cad.dxflib.structure.DxfLinetype; // Adicionar este import
import com.cad.dxflib.structure.DxfDimStyle; // NOVA ADIÇÃO
import com.cad.dxflib.structure.DxfTextStyle; // Added DxfTextStyle import
import com.cad.dxflib.objects.DxfDictionary; // Added DxfDictionary import
import com.cad.dxflib.objects.DxfScale; // Added DxfScale import
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DxfDocument {
    private Map<String, DxfLayer> layers;
    private Map<String, DxfBlock> blocks;
    private Map<String, DxfLinetype> linetypes; // NOVO CAMPO
    private Map<String, DxfDimStyle> dimensionStyles; // NOVA ADIÇÃO
    private Map<String, DxfTextStyle> textStyles; // Added textStyles
    private Map<String, DxfBlockRecord> blockRecords; // Added blockRecords
    private Map<String, DxfDictionary> dictionaries; // Added dictionaries
    private Map<String, DxfScale> scales; // Added scales
    private Map<String, Object> genericObjects; // Added genericObjects
    // private DxfHeader headerVariables; // Future

    // Entities from the ENTITIES section not associated with a specific block
    // Alternatively, all entities could be stored within their respective DxfLayer objects.
    // For now, a flat list for entities in model space might be simpler to start.
    private List<DxfEntity> modelSpaceEntities;


    public DxfDocument() {
        this.layers = new HashMap<>();
        this.blocks = new HashMap<>();
        this.linetypes = new HashMap<>(); // INICIALIZAR NOVO CAMPO
        this.dimensionStyles = new HashMap<>(); // NOVA ADIÇÃO
        this.textStyles = new HashMap<>(); // Initialize textStyles
        this.blockRecords = new HashMap<>(); // Initialize blockRecords
        this.dictionaries = new HashMap<>(); // Initialize dictionaries
        this.scales = new HashMap<>(); // Initialize scales
        this.genericObjects = new HashMap<>(); // Initialize genericObjects
        this.modelSpaceEntities = new ArrayList<>();

        // Add default layer "0"
        DxfLayer defaultLayer = new DxfLayer("0");
        this.layers.put(defaultLayer.getName(), defaultLayer);

        // Adicionar tipo de linha CONTINUOUS padrão
        DxfLinetype continuousLinetype = new DxfLinetype("CONTINUOUS");
        continuousLinetype.setDescription("Solid line");
        this.linetypes.put(continuousLinetype.getName().toUpperCase(java.util.Locale.ROOT), continuousLinetype);

        // Add default text style "STANDARD"
        DxfTextStyle standardTextStyle = new DxfTextStyle("STANDARD");
        // Set default properties for STANDARD style if necessary, e.g.
        // standardTextStyle.setPrimaryFontFileName("arial.ttf"); // Example
        this.textStyles.put(standardTextStyle.getName().toUpperCase(java.util.Locale.ROOT), standardTextStyle);

        // Add default Dimension Style "STANDARD" with AutoCAD-like defaults
        DxfDimStyle standardDimStyle = new DxfDimStyle("STANDARD");
        // Values are based on common AutoCAD defaults (units in mm/inches)
        // Geometry
        standardDimStyle.setDimensionLineColor(0); // ByBlock
        standardDimStyle.setExtensionLineColor(0); // ByBlock
        standardDimStyle.setExtensionLineExtension(0.18); // DIMEXE
        standardDimStyle.setExtensionLineOffset(0.0625); // DIMEXO
        standardDimStyle.setArrowSize(0.18); // DIMASZ
        standardDimStyle.setDimBlkName(""); // Default closed filled arrow
        // Text
        standardDimStyle.setTextStyleName("STANDARD"); // DIMTXSTY
        standardDimStyle.setTextColor(0); // ByBlock
        standardDimStyle.setTextHeight(0.18); // DIMTXT
        standardDimStyle.setTextGap(0.09); // DIMGAP (AutoCAD default is 0.09 units, not relative initially)
        standardDimStyle.setTextVerticalAlignment(1); // DIMTAD = 1 (Above)
        // Units
        standardDimStyle.setDecimalPlaces(2); // DIMDEC (Common for metric)
        // Fit
        standardDimStyle.setTextInsideHorizontal(true); // DIMTIH
        standardDimStyle.setTextOutsideHorizontal(true); // DIMTOH
        // Add other defaults as needed
        addDimensionStyle(standardDimStyle);


        // Add default block records for *Model_Space and *Paper_Space
        addBlockRecord(new DxfBlockRecord("*Model_Space"));
        addBlockRecord(new DxfBlockRecord("*Paper_Space"));
    }

    public DxfLayer getLayer(String name) {
        return layers.get(name);
    }

    public void addLayer(DxfLayer layer) {
        if (layer != null) {
            this.layers.put(layer.getName(), layer);
        }
    }

    public Map<String, DxfLayer> getLayers() {
        return layers;
    }

    public DxfBlock getBlock(String name) {
        return blocks.get(name);
    }

    public void addBlock(DxfBlock block) {
        if (block != null) {
            this.blocks.put(block.getName(), block);
        }
    }
    public Map<String, DxfBlock> getBlocks() {
        return blocks;
    }

    public void addEntity(DxfEntity entity) {
        if (entity != null) {
            // Add to the layer specified in the entity, or default layer "0"
            DxfLayer layer = layers.get(entity.getLayerName());
            if (layer == null) {
                // If layer doesn't exist, create it? Or rely on pre-defined layers?
                // For now, assume if layer is not found, add to default layer "0".
                // DXF parsing should typically create layers first from the LAYER table.
                layer = layers.get("0");
                if (layer == null) { // Should not happen if default "0" is always there
                     layer = new DxfLayer("0"); // Fallback, though problematic
                     addLayer(layer);
                }
            }
            layer.addEntity(entity);

            // Also, if we want a flat list of model space entities:
            // (Need to decide if entity.isModelSpace() is a property we manage in DxfEntity)
            // For now, assume all entities added directly to document are model space.
            // Block definition entities are stored within DxfBlock.
            // This logic might need refinement based on how block defs are handled.
            this.modelSpaceEntities.add(entity);
        }
    }

    public List<DxfEntity> getModelSpaceEntities() {
        // This could return a combined list from all layers,
        // or just entities not part of a block definition.
        // For now, it's entities added via addEntity directly.
        return modelSpaceEntities;
    }

    // TODO: Add methods for header, linetypes, textstyles later

    // NOVOS MÉTODOS para linetypes:
    public DxfLinetype getLinetype(String name) {
        if (name == null) return null;
        return linetypes.get(name.toUpperCase(java.util.Locale.ROOT));
    }

    public void addLinetype(DxfLinetype linetype) {
        if (linetype != null && linetype.getName() != null) {
            this.linetypes.put(linetype.getName().toUpperCase(java.util.Locale.ROOT), linetype);
        }
    }

    public Map<String, DxfLinetype> getLinetypes() {
        return linetypes;
    }

    // Dimension Styles
    public void addDimensionStyle(DxfDimStyle style) {
        if (style != null && style.getName() != null && !style.getName().isEmpty()) {
            this.dimensionStyles.put(style.getName().toUpperCase(), style);
        }
    }

    public DxfDimStyle getDimensionStyle(String name) {
        if (name == null) return null;
        return this.dimensionStyles.get(name.toUpperCase());
    }

    public Map<String, DxfDimStyle> getDimensionStyles() {
        return this.dimensionStyles;
    }

    // Methods for DxfTextStyle
    public void addTextStyle(DxfTextStyle style) {
        if (style != null && style.getName() != null && !style.getName().isEmpty()) {
            this.textStyles.put(style.getName().toUpperCase(java.util.Locale.ROOT), style);
        }
    }

    public DxfTextStyle getTextStyle(String name) {
        if (name == null) return null;
        return this.textStyles.get(name.toUpperCase(java.util.Locale.ROOT));
    }

    public Map<String, DxfTextStyle> getTextStyles() {
        return textStyles;
    }

    // Methods for DxfBlockRecord
    public void addBlockRecord(DxfBlockRecord record) {
        if (record != null && record.getName() != null && !record.getName().isEmpty()) {
            this.blockRecords.put(record.getName().toUpperCase(java.util.Locale.ROOT), record);
        }
    }

    public DxfBlockRecord getBlockRecord(String name) {
        if (name == null) return null;
        return this.blockRecords.get(name.toUpperCase(java.util.Locale.ROOT));
    }

    public Map<String, DxfBlockRecord> getBlockRecords() {
        return blockRecords;
    }

    // Methods for DxfDictionary
    public void addDictionary(String nameOrHandle, DxfDictionary dict) {
        if (dict != null && nameOrHandle != null && !nameOrHandle.isEmpty()) {
            this.dictionaries.put(nameOrHandle.toUpperCase(java.util.Locale.ROOT), dict);
        }
    }

    public DxfDictionary getDictionary(String nameOrHandle) {
        if (nameOrHandle == null) return null;
        return this.dictionaries.get(nameOrHandle.toUpperCase(java.util.Locale.ROOT));
    }

    public Map<String, DxfDictionary> getDictionaries() {
        return dictionaries;
    }

    // Methods for generic DxfObjects (by handle)
    public void addObject(String handle, Object obj) {
        if (obj != null && handle != null && !handle.isEmpty()) {
            this.genericObjects.put(handle.toUpperCase(java.util.Locale.ROOT), obj);
        }
    }

    public Object getObject(String handle) {
        if (handle == null) return null;
        return this.genericObjects.get(handle.toUpperCase(java.util.Locale.ROOT));
    }

    public Map<String, Object> getGenericObjects() {
        return genericObjects;
    }

    // Methods for DxfScale
    public void addScale(DxfScale scale) {
        if (scale != null && scale.getHandle() != null && !scale.getHandle().isEmpty()) {
            this.scales.put(scale.getHandle().toUpperCase(java.util.Locale.ROOT), scale);
            // Optionally, also index by name if present and guaranteed unique,
            // but handle is the safest primary key.
            // if (scale.getName() != null && !scale.getName().isEmpty()) {
            //     this.scales.put(scale.getName().toUpperCase(java.util.Locale.ROOT), scale);
            // }
        }
    }

    public DxfScale getScale(String handleOrName) {
        if (handleOrName == null) return null;
        return this.scales.get(handleOrName.toUpperCase(java.util.Locale.ROOT));
    }

    public Map<String, DxfScale> getScales() {
        return scales;
    }

    public Bounds getBounds() {
        Bounds totalBounds = new Bounds();
        // Iterate over entities in model space
        // (This assumes modelSpaceEntities contains all relevant top-level entities for bounds calculation)
        for (DxfEntity entity : getModelSpaceEntities()) { // modelSpaceEntities might need to be populated correctly first
            Bounds entityBounds = entity.getBounds();
            if (entityBounds != null && entityBounds.isValid()) {
                totalBounds.addToBounds(entityBounds);
            }
        }

        // Alternatively, iterate through layers and then entities in layers
        // for (DxfLayer layer : layers.values()) {
        //    if (layer.isVisible()) { // Consider only visible layers
        //        for (DxfEntity entity : layer.getEntities()) {
        //            Bounds entityBounds = entity.getBounds();
        //            if (entityBounds != null && entityBounds.isValid()) {
        //                totalBounds.addToBounds(entityBounds);
        //            }
        //        }
        //    }
        // }
        return totalBounds;
    }
}
