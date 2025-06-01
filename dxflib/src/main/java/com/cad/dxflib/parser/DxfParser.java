package com.cad.dxflib.parser;

import com.cad.dxflib.common.DxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point2D;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.entities.DxfArc;
import com.cad.dxflib.entities.DxfCircle;
import com.cad.dxflib.entities.DxfInsert;
import com.cad.dxflib.entities.DxfLine;
import com.cad.dxflib.entities.DxfLwPolyline;
import com.cad.dxflib.entities.DxfText;
import com.cad.dxflib.structure.DxfBlock;
import com.cad.dxflib.structure.DxfDocument;
import com.cad.dxflib.structure.DxfLayer; // Required for parseSingleLayerEntry

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
                            parseTablesSection();
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
                        } else {
                            consumeTableOrEntries();
                        }
                        if (aktuellenGroupCode == null || !(aktuellenGroupCode.code == 0 && "ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value))) {
                            throw new DxfParserException("TABLE " + tableName + " parsing did not correctly position at ENDTAB. Current: " + aktuellenGroupCode);
                        }
                        aktuellenGroupCode = nextGroupCode(); // Consume 0/ENDTAB
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
            // layer.setFlags(flags); // If DxfLayer is updated to handle flags
            document.addLayer(layer);
        } else {
            throw new DxfParserException("Layer entry found with no name (group code 2). Current group: " + aktuellenGroupCode);
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
        aktuellenGroupCode = nextGroupCode();
        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDSEC".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode();
                    currentSection = null;
                    return;
                } else if ("BLOCK".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    parseSingleBlockDefinition();
                } else {
                    throw new DxfParserException("Unexpected group code " + aktuellenGroupCode + " in BLOCKS section while expecting BLOCK or ENDSEC.");
                }
            } else {
                 throw new DxfParserException("Premature EOF in BLOCKS section expecting 0 code.");
            }
        }
        throw new DxfParserException("Premature EOF in BLOCKS section (outer loop).");
    }

    private void parseSingleBlockDefinition() throws IOException, DxfParserException {
        String blockName = null;
        Point3D basePoint = new Point3D(0, 0, 0);
        DxfBlock currentBlock = null;
        // String layerForBlockDefinition = "0"; // Layer for the BLOCK definition itself

        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 2: blockName = aktuellenGroupCode.value; break;
                // case 8: layerForBlockDefinition = aktuellenGroupCode.value; break; // Layer for BLOCK record
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
        // currentBlock.setLayerName(layerForBlockDefinition); // If DxfBlock needs a layer

        while (aktuellenGroupCode != null) { // aktuellenGroupCode is 0/EntityType or 0/ENDBLK
            if (aktuellenGroupCode.code == 0) {
                if ("ENDBLK".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    document.addBlock(currentBlock);
                    aktuellenGroupCode = nextGroupCode();
                    return;
                } else {
                    String entityType = aktuellenGroupCode.value.toUpperCase(Locale.ROOT);
                    // Temporarily add to document, then move to block
                    int entitiesBefore = document.getModelSpaceEntities().size();
                    if ("LINE".equals(entityType)) parseLineEntity();
                    else if ("CIRCLE".equals(entityType)) parseCircleEntity();
                    else if ("ARC".equals(entityType)) parseArcEntity();
                    else if ("LWPOLYLINE".equals(entityType)) parseLwPolylineEntity();
                    else if ("TEXT".equals(entityType)) parseTextEntity();
                    else if ("INSERT".equals(entityType)) parseInsertEntity();
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
}
