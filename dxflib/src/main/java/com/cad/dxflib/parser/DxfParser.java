package com.cad.dxflib.parser;

import com.cad.dxflib.common.DxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point2D;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.entities.DxfArc;
import com.cad.dxflib.entities.DxfCircle;
import com.cad.dxflib.entities.DxfDimension;
import com.cad.dxflib.entities.DxfInsert;
import com.cad.dxflib.entities.DxfLine;
import com.cad.dxflib.entities.DxfLwPolyline;
import com.cad.dxflib.entities.DxfText;
import com.cad.dxflib.structure.DxfBlock;
import com.cad.dxflib.structure.DxfDimStyle; // NOVA ADIÇÃO
import com.cad.dxflib.structure.DxfDocument;
import com.cad.dxflib.structure.DxfLayer;
import com.cad.dxflib.structure.DxfLinetype; // Added
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList; // Added
import java.util.List;      // Added
import java.util.Locale;

public class DxfParser {

    private BufferedReader reader;
    private DxfDocument document;
    private DxfGroupCode aktuellenGroupCode;
    private String currentSection;

    public DxfDocument parse(InputStream inputStream) throws DxfParserException {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null.");
        }
        this.document = new DxfDocument();
        this.reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            aktuellenGroupCode = nextGroupCode();
            while (aktuellenGroupCode != null) {
                if (aktuellenGroupCode.code == 0 && "SECTION".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode();
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 2) {
                        currentSection = aktuellenGroupCode.value.toUpperCase(Locale.ROOT);
                        if ("ENTITIES".equals(currentSection)) {
                            parseEntitiesSection();
                        } else if ("HEADER".equals(currentSection)) {
                            consumeSection();
                        } else if ("TABLES".equals(currentSection)) {
                            parseTablesSection(); // Updated call
                        } else if ("BLOCKS".equals(currentSection)) {
                            parseBlocksSection();
                        } else {
                            consumeSection();
                        }
                        continue;
                    } else {
                        throw new DxfParserException("Malformed SECTION: expected group code 2 after 0/SECTION, got: " + aktuellenGroupCode);
                    }
                } else if (aktuellenGroupCode.code == 0 && "EOF".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    break;
                } else {
                    aktuellenGroupCode = nextGroupCode();
                }
            }
        } catch (IOException e) {
            throw new DxfParserException("Error reading DXF file", e);
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                // Log error
            }
        }
        return this.document;
    }

    private DxfGroupCode nextGroupCode() throws IOException, DxfParserException {
        String codeStr = reader.readLine();
        String valueStr;
        if (codeStr != null) {
            valueStr = reader.readLine();
            if (valueStr == null) {
                throw new DxfParserException("Premature EOF: Expected value after group code " + codeStr.trim());
            }
        } else {
            return null;
        }
        try {
            int code = Integer.parseInt(codeStr.trim());
            return new DxfGroupCode(code, valueStr.trim());
        } catch (NumberFormatException e) {
            throw new DxfParserException("Invalid group code format: '" + codeStr.trim() + "'", e);
        }
    }

    private void consumeSection() throws IOException, DxfParserException {
        while ((aktuellenGroupCode = nextGroupCode()) != null) {
            if (aktuellenGroupCode.code == 0 && "ENDSEC".equalsIgnoreCase(aktuellenGroupCode.value)) {
                aktuellenGroupCode = nextGroupCode();
                currentSection = null;
                return;
            }
        }
        throw new DxfParserException("Premature EOF while consuming section: " + currentSection);
    }

    private void parseTablesSection() throws IOException, DxfParserException {
        aktuellenGroupCode = nextGroupCode();
        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDSEC".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode();
                    currentSection = null;
                    return;
                } else if ("TABLE".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode();
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 2) {
                        String tableName = aktuellenGroupCode.value.toUpperCase(Locale.ROOT);
                        if ("LAYER".equals(tableName)) {
                            parseLayerTable();
                        } else if ("LTYPE".equals(tableName)) { // NEW CASE
                            parseLinetypeTable();
                        } else if ("DIMSTYLE".equals(tableName)) { // NOVA CONDIÇÃO
                            parseDimStyleTable();                 // NOVO MÉTODO
                        } else {
                            consumeTableOrEntries();
                        }
                        if (aktuellenGroupCode == null || !(aktuellenGroupCode.code == 0 && "ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value))) {
                            throw new DxfParserException("TABLE " + tableName + " parsing did not correctly position at ENDTAB. Current: " + aktuellenGroupCode);
                        }
                        aktuellenGroupCode = nextGroupCode();
                    } else {
                        throw new DxfParserException("Malformed TABLE entry: expected group code 2 for table name. Got: " + aktuellenGroupCode);
                    }
                } else {
                    throw new DxfParserException("Unexpected group code " + aktuellenGroupCode + " in TABLES section while expecting TABLE or ENDSEC.");
                }
            } else {
                throw new DxfParserException("Unexpected non-zero group code " + aktuellenGroupCode + " at a point where a 0-code (TABLE/ENDSEC) was expected in TABLES section.");
            }
        }
        throw new DxfParserException("Premature EOF in TABLES section (outer loop).");
    }

    private void parseLayerTable() throws IOException, DxfParserException {
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            // Ignoring group 70 and other table-specific header codes
        }
        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    return;
                } else if ("LAYER".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    parseSingleLayerEntry();
                } else {
                    throw new DxfParserException("Unexpected group code " + aktuellenGroupCode + " in LAYER table while expecting LAYER or ENDTAB.");
                }
            } else {
                throw new DxfParserException("Unexpected non-zero group code " + aktuellenGroupCode + " where 0/LAYER or 0/ENDTAB was expected in LAYER table.");
            }
        }
        throw new DxfParserException("Premature EOF in LAYER table.");
    }

    private void parseSingleLayerEntry() throws IOException, DxfParserException {
        String layerName = null;
        int color = 7;
        String linetype = "CONTINUOUS";
        int flags = 0;
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 2: layerName = aktuellenGroupCode.value; break;
                case 62: color = Integer.parseInt(aktuellenGroupCode.value); break;
                case 6: linetype = aktuellenGroupCode.value; break;
                case 70: flags = Integer.parseInt(aktuellenGroupCode.value); break;
                default: break;
            }
        }
        if (layerName != null && !layerName.isEmpty()) {
            DxfLayer layer = document.getLayer(layerName);
            if (layer == null) {
                 if ("0".equals(layerName) && document.getLayers().containsKey("0")) {
                    layer = document.getLayer("0");
                 } else {
                    layer = new DxfLayer(layerName);
                 }
            }
            layer.setColor(Math.abs(color));
            layer.setVisible(color >= 0);
            layer.setLinetypeName(linetype);
            document.addLayer(layer);
        } else {
            throw new DxfParserException("Layer entry found with no name (group code 2). Current group: " + aktuellenGroupCode);
        }
    }

    private void parseLinetypeTable() throws IOException, DxfParserException {
        while((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            // Ex: group 70 (max number of entries in table)
        }
        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    return;
                } else if ("LTYPE".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    parseSingleLinetypeEntry();
                } else {
                    throw new DxfParserException("Unexpected group code " + aktuellenGroupCode + " in LTYPE table while expecting LTYPE or ENDTAB.");
                }
            } else {
                 throw new DxfParserException("Unexpected group code " + aktuellenGroupCode + " at start of linetype entry.");
            }
        }
        throw new DxfParserException("Premature EOF in LTYPE table.");
    }

    private void parseSingleLinetypeEntry() throws IOException, DxfParserException {
        String linetypeName = null;
        String description = "";
        double patternLength = 0.0;
        List<Double> patternElements = new ArrayList<>();

        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 2: linetypeName = aktuellenGroupCode.value; break;
                case 3: description = aktuellenGroupCode.value; break;
                case 70: break; // Standard flags
                case 72: break; // Alignment code (always 65)
                case 73: break; // Number of dash length items
                case 40: patternLength = Double.parseDouble(aktuellenGroupCode.value); break;
                case 49: patternElements.add(Double.parseDouble(aktuellenGroupCode.value)); break;
                default: break;
            }
        }
        if (linetypeName != null && !linetypeName.isEmpty()) {
            DxfLinetype ltype = new DxfLinetype(linetypeName);
            ltype.setDescription(description);
            ltype.setPatternLength(patternLength);
            for (double element : patternElements) {
                ltype.addPatternElement(element);
            }
            document.addLinetype(ltype);
        } else {
            throw new DxfParserException("Linetype entry found with no name (group code 2).");
        }
    }

    private void consumeTableOrEntries() throws IOException, DxfParserException {
        while ((aktuellenGroupCode = nextGroupCode()) != null) {
            if (aktuellenGroupCode.code == 0 && "ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value)) {
                return;
            }
        }
        throw new DxfParserException("Premature EOF while consuming entries for an unhandled table.");
    }

    private void parseBlocksSection() throws IOException, DxfParserException {
        aktuellenGroupCode = nextGroupCode(); // Initial read, should be the first "0" "BLOCK" or "0" "ENDSEC" if empty
        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDSEC".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode(); // Consume ENDSEC, prepare for next section or EOF
                    currentSection = null;
                    return;
                } else if ("BLOCK".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    parseSingleBlockDefinition();
                    // parseSingleBlockDefinition consumes its corresponding "0" "ENDBLK"
                    // and calls nextGroupCode(), so aktuellenGroupCode is now
                    // pointing to the next "0" "BLOCK" or "0" "ENDSEC".
                    // The loop will correctly evaluate this in the next iteration.
                    // No explicit nextGroupCode() call is needed here.
                } else {
                    throw new DxfParserException("Unexpected group code " + aktuellenGroupCode + " in BLOCKS section while expecting BLOCK or ENDSEC.");
                }
            } else {
                 // If aktuellenGroupCode.code is not 0, we are expecting a 0 BLOCK or 0 ENDSEC.
                 // This means previous entity/block parsing finished, and the current code is not a new 0-marker.
                 // We should skip such codes until we find a 0 or hit EOF.
                 // System.err.println("INFO: Skipping unexpected non-zero group code " + aktuellenGroupCode + " in BLOCKS section, expecting 0/BLOCK or 0/ENDSEC.");
                 aktuellenGroupCode = nextGroupCode(); // Consume the unexpected non-zero code and try again
                 // The loop will continue and re-evaluate the new aktuellenGroupCode
            }
        }
        // If currentSection is still "BLOCKS", it means EOF was reached before ENDSEC
        if ("BLOCKS".equalsIgnoreCase(currentSection)) {
             throw new DxfParserException("Premature EOF in BLOCKS section (outer loop), ENDSEC not found.");
        }
        // If aktuellenGroupCode is null and currentSection is not BLOCKS, it's a normal EOF after all sections.
    }

    private void parseSingleBlockDefinition() throws IOException, DxfParserException {
        String blockName = null;
        Point3D basePoint = new Point3D(0, 0, 0);
        DxfBlock currentBlock = null;

        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 2: blockName = aktuellenGroupCode.value; break;
                case 10: basePoint = new Point3D(Double.parseDouble(aktuellenGroupCode.value), basePoint.y, basePoint.z); break;
                case 20: basePoint = new Point3D(basePoint.x, Double.parseDouble(aktuellenGroupCode.value), basePoint.z); break;
                case 30: basePoint = new Point3D(basePoint.x, basePoint.y, Double.parseDouble(aktuellenGroupCode.value)); break;
                default: break;
            }
        }

        if (blockName == null || blockName.trim().isEmpty()) {
            throw new DxfParserException("BLOCK definition found with no name (group code 2).");
        }
        currentBlock = new DxfBlock(blockName);
        currentBlock.setBasePoint(basePoint);

        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDBLK".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    document.addBlock(currentBlock);
                    aktuellenGroupCode = nextGroupCode();
                    return;
                } else {
                    String entityType = aktuellenGroupCode.value.toUpperCase(Locale.ROOT);
                    int entitiesBefore = document.getModelSpaceEntities().size();
                    if ("LINE".equals(entityType)) parseLineEntity();
                    else if ("CIRCLE".equals(entityType)) parseCircleEntity();
                    else if ("ARC".equals(entityType)) parseArcEntity();
                    else if ("LWPOLYLINE".equals(entityType)) parseLwPolylineEntity();
                    else if ("TEXT".equals(entityType)) parseTextEntity();
                    else if ("INSERT".equals(entityType)) parseInsertEntity();
                    else if ("DIMENSION".equalsIgnoreCase(entityType)) parseDimensionEntity(); // NOVA CONDIÇÃO
                    else consumeUnknownEntity();

                    if (document.getModelSpaceEntities().size() > entitiesBefore) {
                        DxfEntity lastEntity = document.getModelSpaceEntities().remove(document.getModelSpaceEntities().size() - 1);
                        currentBlock.addEntity(lastEntity);
                    }
                }
            } else {
                throw new DxfParserException("Unexpected group code " + aktuellenGroupCode + " within BLOCK definition, expected 0.");
            }
        }
        throw new DxfParserException("Premature EOF within BLOCK definition for block: " + blockName);
    }

    private void parseEntitiesSection() throws IOException, DxfParserException {
        aktuellenGroupCode = nextGroupCode();
        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDSEC".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode();
                    currentSection = null;
                    return;
                } else if ("LINE".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    parseLineEntity();
                } else if ("CIRCLE".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    parseCircleEntity();
                } else if ("ARC".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    parseArcEntity();
                } else if ("LWPOLYLINE".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    parseLwPolylineEntity();
                } else if ("TEXT".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    parseTextEntity();
                } else if ("INSERT".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    parseInsertEntity();
                } else if ("DIMENSION".equalsIgnoreCase(aktuellenGroupCode.value)) { // NOVA CONDIÇÃO
                    parseDimensionEntity();                                     // NOVO MÉTODO
                } else {
                    consumeUnknownEntity();
                }
            } else {
                throw new DxfParserException("Unexpected group code " + aktuellenGroupCode + " at start of entity in ENTITIES section.");
            }
        }
         throw new DxfParserException("Premature EOF in ENTITIES section.");
    }

    private void parseLineEntity() throws IOException, DxfParserException {
        DxfLine line = new DxfLine();
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 8: line.setLayerName(aktuellenGroupCode.value); break;
                case 10: line.setStartPoint(new Point3D(Double.parseDouble(aktuellenGroupCode.value), line.getStartPoint().y, line.getStartPoint().z)); break;
                case 20: line.setStartPoint(new Point3D(line.getStartPoint().x, Double.parseDouble(aktuellenGroupCode.value), line.getStartPoint().z)); break;
                case 30: line.setStartPoint(new Point3D(line.getStartPoint().x, line.getStartPoint().y, Double.parseDouble(aktuellenGroupCode.value))); break;
                case 11: line.setEndPoint(new Point3D(Double.parseDouble(aktuellenGroupCode.value), line.getEndPoint().y, line.getEndPoint().z)); break;
                case 21: line.setEndPoint(new Point3D(line.getEndPoint().x, Double.parseDouble(aktuellenGroupCode.value), line.getEndPoint().z)); break;
                case 31: line.setEndPoint(new Point3D(line.getEndPoint().x, line.getEndPoint().y, Double.parseDouble(aktuellenGroupCode.value))); break;
                case 62: line.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;
                default: break;
            }
        }
        document.addEntity(line);
    }

    private void parseCircleEntity() throws IOException, DxfParserException {
        DxfCircle circle = new DxfCircle();
         while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 8: circle.setLayerName(aktuellenGroupCode.value); break;
                case 10: circle.setCenter(new Point3D(Double.parseDouble(aktuellenGroupCode.value), circle.getCenter().y, circle.getCenter().z)); break;
                case 20: circle.setCenter(new Point3D(circle.getCenter().x, Double.parseDouble(aktuellenGroupCode.value), circle.getCenter().z)); break;
                case 30: circle.setCenter(new Point3D(circle.getCenter().x, circle.getCenter().y, Double.parseDouble(aktuellenGroupCode.value))); break;
                case 40: circle.setRadius(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 62: circle.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;
                default: break;
            }
        }
        document.addEntity(circle);
    }

    private void consumeUnknownEntity() throws IOException, DxfParserException {
        while((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            // just consume
        }
    }

    private void parseArcEntity() throws IOException, DxfParserException {
        DxfArc arc = new DxfArc();
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 8: arc.setLayerName(aktuellenGroupCode.value); break;
                case 10: arc.setCenter(new Point3D(Double.parseDouble(aktuellenGroupCode.value), arc.getCenter().y, arc.getCenter().z)); break;
                case 20: arc.setCenter(new Point3D(arc.getCenter().x, Double.parseDouble(aktuellenGroupCode.value), arc.getCenter().z)); break;
                case 30: arc.setCenter(new Point3D(arc.getCenter().x, arc.getCenter().y, Double.parseDouble(aktuellenGroupCode.value))); break;
                case 40: arc.setRadius(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 50: arc.setStartAngle(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 51: arc.setEndAngle(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 62: arc.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;
                default: break;
            }
        }
        document.addEntity(arc);
    }

    private void parseLwPolylineEntity() throws IOException, DxfParserException {
        DxfLwPolyline lwpoly = new DxfLwPolyline();
        int vertexCount = 0;
        double currentX = 0, currentY = 0;
        boolean xRead = false;

        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 8: lwpoly.setLayerName(aktuellenGroupCode.value); break;
                case 90: vertexCount = Integer.parseInt(aktuellenGroupCode.value); break;
                case 70: lwpoly.setClosed((Integer.parseInt(aktuellenGroupCode.value) & 1) == 1); break;
                case 43: lwpoly.setConstantWidth(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 38: lwpoly.setElevation(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 10:
                    currentX = Double.parseDouble(aktuellenGroupCode.value);
                    xRead = true;
                    break;
                case 20:
                    if (!xRead) throw new DxfParserException("LWPOLYLINE: Y coordinate (20) found without preceding X (10).");
                    currentY = Double.parseDouble(aktuellenGroupCode.value);
                    double bulge = 0.0;

                    reader.mark(512);
                    DxfGroupCode peekCode = null;
                    String tempCodeStr = reader.readLine();
                    if (tempCodeStr != null) {
                        String tempValueStr = reader.readLine();
                        if (tempValueStr != null) {
                            try {
                               peekCode = new DxfGroupCode(Integer.parseInt(tempCodeStr.trim()), tempValueStr.trim());
                            } catch (NumberFormatException e) { /* ignore */ }
                        }
                    }
                    reader.reset();

                    if (peekCode != null && peekCode.code == 42) {
                        aktuellenGroupCode = nextGroupCode(); // Consume the bulge code (42)
                        bulge = Double.parseDouble(aktuellenGroupCode.value);
                    }
                    lwpoly.addVertex(new Point2D(currentX, currentY), bulge);
                    xRead = false;
                    break;
                case 62: lwpoly.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;
                default: xRead = false; break;
            }
        }
        if (vertexCount > 0 && lwpoly.getNumberOfVertices() != vertexCount && lwpoly.getNumberOfVertices() > 0) {
            // Warning or error for vertex count mismatch could be added here.
        }
        document.addEntity(lwpoly);
    }

    private void parseTextEntity() throws IOException, DxfParserException {
        DxfText text = new DxfText();
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 1: text.setTextValue(aktuellenGroupCode.value); break;
                case 7: text.setStyleName(aktuellenGroupCode.value); break;
                case 8: text.setLayerName(aktuellenGroupCode.value); break;
                case 10: text.setInsertionPoint(new Point3D(Double.parseDouble(aktuellenGroupCode.value), text.getInsertionPoint().y, text.getInsertionPoint().z)); break;
                case 20: text.setInsertionPoint(new Point3D(text.getInsertionPoint().x, Double.parseDouble(aktuellenGroupCode.value), text.getInsertionPoint().z)); break;
                case 30: text.setInsertionPoint(new Point3D(text.getInsertionPoint().x, text.getInsertionPoint().y, Double.parseDouble(aktuellenGroupCode.value))); break;
                case 40: text.setHeight(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 50: text.setRotationAngle(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 62: text.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;
                default: break;
            }
        }
        document.addEntity(text);
    }

    private void parseInsertEntity() throws IOException, DxfParserException {
        DxfInsert insert = new DxfInsert();
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 2: insert.setBlockName(aktuellenGroupCode.value); break;
                case 8: insert.setLayerName(aktuellenGroupCode.value); break;
                case 10: insert.setInsertionPoint(new Point3D(Double.parseDouble(aktuellenGroupCode.value), insert.getInsertionPoint().y, insert.getInsertionPoint().z)); break;
                case 20: insert.setInsertionPoint(new Point3D(insert.getInsertionPoint().x, Double.parseDouble(aktuellenGroupCode.value), insert.getInsertionPoint().z)); break;
                case 30: insert.setInsertionPoint(new Point3D(insert.getInsertionPoint().x, insert.getInsertionPoint().y, Double.parseDouble(aktuellenGroupCode.value))); break;
                case 41: insert.setXScale(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 42: insert.setYScale(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 50: insert.setRotationAngle(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 62: insert.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;
                default: break;
            }
        }
        if (insert.getBlockName() == null || insert.getBlockName().trim().isEmpty()) {
            throw new DxfParserException("INSERT entity missing block name (group code 2).");
        }
        document.addEntity(insert);
    }

    private void parseDimensionEntity() throws IOException, DxfParserException {
        DxfDimension dimension = new DxfDimension();
        // Common entity properties like layer, color are typically handled by AbstractDxfEntity or a common parser method if one exists.
        // For now, we'll set them directly if they are common, or let DxfDimension handle defaults.

        // Loop through group codes for the DIMENSION entity
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                // Common AcDbEntity codes
                case 8: dimension.setLayerName(aktuellenGroupCode.value); break;
                case 62: dimension.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;
                // TODO: Add other common entity properties if needed (linetype, lineweight, visibility etc.)
                // For AcDbDimension subclass
                case 2: dimension.setBlockName(aktuellenGroupCode.value); break;
                case 3: dimension.setDimensionStyleName(aktuellenGroupCode.value); break;
                case 10: dimension.setDefinitionPoint(new Point3D(Double.parseDouble(aktuellenGroupCode.value), dimension.getDefinitionPoint().y, dimension.getDefinitionPoint().z)); break;
                case 20: dimension.setDefinitionPoint(new Point3D(dimension.getDefinitionPoint().x, Double.parseDouble(aktuellenGroupCode.value), dimension.getDefinitionPoint().z)); break;
                case 30: dimension.setDefinitionPoint(new Point3D(dimension.getDefinitionPoint().x, dimension.getDefinitionPoint().y, Double.parseDouble(aktuellenGroupCode.value))); break;
                case 11: dimension.setMiddleOfTextPoint(new Point3D(Double.parseDouble(aktuellenGroupCode.value), dimension.getMiddleOfTextPoint().y, dimension.getMiddleOfTextPoint().z)); break;
                case 21: dimension.setMiddleOfTextPoint(new Point3D(dimension.getMiddleOfTextPoint().x, Double.parseDouble(aktuellenGroupCode.value), dimension.getMiddleOfTextPoint().z)); break;
                case 31: dimension.setMiddleOfTextPoint(new Point3D(dimension.getMiddleOfTextPoint().x, dimension.getMiddleOfTextPoint().y, Double.parseDouble(aktuellenGroupCode.value))); break;
                case 1: dimension.setDimensionText(aktuellenGroupCode.value); break; // Actual dimension text (if overridden)
                case 70: dimension.setDimensionTypeFlags(Integer.parseInt(aktuellenGroupCode.value)); break;

                // Codes for AcDbAlignedDimension (or other linear types)
                // These are typically after a 100/AcDbAlignedDimension group, but we're simplifying for now
                case 13: dimension.setDefinitionPoint1(new Point3D(Double.parseDouble(aktuellenGroupCode.value), dimension.getDefinitionPoint1().y, dimension.getDefinitionPoint1().z)); break;
                case 23: dimension.setDefinitionPoint1(new Point3D(dimension.getDefinitionPoint1().x, Double.parseDouble(aktuellenGroupCode.value), dimension.getDefinitionPoint1().z)); break;
                case 33: dimension.setDefinitionPoint1(new Point3D(dimension.getDefinitionPoint1().x, dimension.getDefinitionPoint1().y, Double.parseDouble(aktuellenGroupCode.value))); break;
                case 14: dimension.setDefinitionPoint2(new Point3D(Double.parseDouble(aktuellenGroupCode.value), dimension.getDefinitionPoint2().y, dimension.getDefinitionPoint2().z)); break;
                case 24: dimension.setDefinitionPoint2(new Point3D(dimension.getDefinitionPoint2().x, Double.parseDouble(aktuellenGroupCode.value), dimension.getDefinitionPoint2().z)); break;
                case 34: dimension.setDefinitionPoint2(new Point3D(dimension.getDefinitionPoint2().x, dimension.getDefinitionPoint2().y, Double.parseDouble(aktuellenGroupCode.value))); break;

                // Extrusion direction
                case 210: dimension.setExtrusionDirection(new Point3D(Double.parseDouble(aktuellenGroupCode.value), dimension.getExtrusionDirection().y, dimension.getExtrusionDirection().z)); break;
                case 220: dimension.setExtrusionDirection(new Point3D(dimension.getExtrusionDirection().x, Double.parseDouble(aktuellenGroupCode.value), dimension.getExtrusionDirection().z)); break;
                case 230: dimension.setExtrusionDirection(new Point3D(dimension.getExtrusionDirection().x, dimension.getExtrusionDirection().y, Double.parseDouble(aktuellenGroupCode.value))); break;

                // Codes to skip for now (related to AcDbDimension specific subclasses or less critical data)
                case 5: // Handle - already handled by AbstractDxfEntity or not stored explicitly at this level
                case 100: // Subclass marker (e.g., AcDbDimension, AcDbAlignedDimension) - we infer type from 70 or handle specific codes
                case 71: // Attachment point
                case 72: // Text line spacing style
                case 41: // Text line spacing factor
                case 42: // Actual measurement (read-only)
                case 50: // Angle of rotated dimension - For AcDbRotatedDimension
                case 51: // OBSOLETE - Horizontal direction angle
                case 52: // OBSOLETE - Rotation angle of dimension text away from dimension line
                case 53: // Rotation angle of dimension text
                // ... any other codes that are not immediately needed for basic representation
                    break;
                default:
                    // Optionally log unhandled group codes for DIMENSION if debugging:
                    // System.out.println("Unhandled group code for DIMENSION: " + aktuellenGroupCode.code + " = " + aktuellenGroupCode.value);
                    break;
            }
        }
        document.addEntity(dimension);
    }

    private void parseDimStyleTable() throws IOException, DxfParserException {
        // Consumir códigos de cabeçalho da tabela DIMSTYLE até o primeiro "0" "DIMSTYLE"
        // Ex: código 70 (max number of entries), 100 (AcDbDimStyleTable), 71 (count)
        // O loop abaixo é uma forma genérica de consumir até o primeiro 0/DIMSTYLE ou 0/ENDTAB
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            // Ignoring table-specific header codes like 70, 100, 71
        }

        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    return; // Fim da tabela DIMSTYLE
                } else if ("DIMSTYLE".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    parseSingleDimStyleEntry();
                    // parseSingleDimStyleEntry já avança aktuellenGroupCode para o próximo código 0
                } else {
                    throw new DxfParserException("Unexpected group code " + aktuellenGroupCode + " in DIMSTYLE table while expecting DIMSTYLE or ENDTAB.");
                }
            } else {
                // Isso pode acontecer se houver dados inesperados antes de um 0/DIMSTYLE ou 0/ENDTAB
                throw new DxfParserException("Unexpected non-zero group code " + aktuellenGroupCode + " where 0/DIMSTYLE or 0/ENDTAB was expected in DIMSTYLE table.");
            }
        }
        throw new DxfParserException("Premature EOF in DIMSTYLE table.");
    }

    private void parseSingleDimStyleEntry() throws IOException, DxfParserException {
        String dimStyleName = null;
        // Outros atributos do DIMSTYLE podem ser lidos aqui no futuro
        // Ex: int dimTxt = 0; // Code 140: text height

        // O primeiro código após 0/DIMSTYLE geralmente é 105 (handle da dimensão), mas pode ser 2 (nome) ou outros.
        // Precisamos de um loop que leia até o próximo código 0.
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 2: // Nome do estilo de cota
                    dimStyleName = aktuellenGroupCode.value;
                    break;
                // case 105: // Handle (geralmente ignoramos handles de tabela por enquanto)
                //    break;
                // case 140: // DIMTXT - altura do texto
                //    // dimTxt = Integer.parseInt(aktuellenGroupCode.value);
                //    break;
                // Adicionar outros códigos de grupo para DIMSTYLE aqui se necessário no futuro
                default:
                    // Ignorar outros códigos por enquanto
                    break;
            }
        }

        if (dimStyleName != null && !dimStyleName.isEmpty()) {
            DxfDimStyle style = new DxfDimStyle(dimStyleName);
            // Definir outros atributos aqui: style.setDimTxt(dimTxt);
            document.addDimensionStyle(style);
        } else {
            // Não lançar exceção se o nome não for encontrado imediatamente, pois pode vir depois do handle 105.
            // Uma verificação mais robusta seria necessária se quiséssemos garantir que todo DIMSTYLE tenha um nome.
            // Por agora, se não houver nome, o estilo não será adicionado.
            // Se aktuellenGroupCode for null aqui, significa EOF prematuro dentro de uma entrada DIMSTYLE.
            if (aktuellenGroupCode == null) {
                throw new DxfParserException("Premature EOF within a DIMSTYLE entry.");
            }
        }
        // aktuellenGroupCode já está posicionado no próximo código 0 (seja outro DIMSTYLE ou ENDTAB)
        // ou é null se EOF.
    }
}
