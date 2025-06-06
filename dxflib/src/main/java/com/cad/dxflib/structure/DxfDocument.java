package com.cad.dxflib.structure;

import com.cad.dxflib.common.DxfEntity;
import com.cad.dxflib.math.Bounds;
import com.cad.dxflib.structure.DxfLinetype; // Adicionar este import
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DxfDocument {
    private Map<String, DxfLayer> layers;
    private Map<String, DxfBlock> blocks;
    private Map<String, DxfLinetype> linetypes; // NOVO CAMPO
    // private Map<String, DxfTextStyle> textStyles; // Future
    // private DxfHeader headerVariables; // Future

    // Entities from the ENTITIES section not associated with a specific block
    // Alternatively, all entities could be stored within their respective DxfLayer objects.
    // For now, a flat list for entities in model space might be simpler to start.
    private List<DxfEntity> modelSpaceEntities;


    public DxfDocument() {
        this.layers = new HashMap<>();
        this.blocks = new HashMap<>();
        this.linetypes = new HashMap<>(); // INICIALIZAR NOVO CAMPO
        this.modelSpaceEntities = new ArrayList<>();

        // Add default layer "0"
        DxfLayer defaultLayer = new DxfLayer("0");
        this.layers.put(defaultLayer.getName(), defaultLayer);

        // Adicionar tipo de linha CONTINUOUS padrão
        DxfLinetype continuousLinetype = new DxfLinetype("CONTINUOUS");
        continuousLinetype.setDescription("Solid line");
        this.linetypes.put(continuousLinetype.getName().toUpperCase(java.util.Locale.ROOT), continuousLinetype);
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
