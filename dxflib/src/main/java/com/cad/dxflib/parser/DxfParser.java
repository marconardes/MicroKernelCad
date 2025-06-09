package com.cad.dxflib.parser;

import com.cad.dxflib.common.DxfEntity;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.parser.EntitiesParser;
import com.cad.dxflib.structure.DxfBlock;
import com.cad.dxflib.structure.DxfDimStyle;
import com.cad.dxflib.structure.DxfDocument;
import com.cad.dxflib.structure.DxfLayer;
import com.cad.dxflib.structure.DxfLinetype;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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
                    aktuellenGroupCode = nextGroupCode(); // Consumes 0/SECTION, gets 2/SECTION_NAME
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 2) {
                        currentSection = aktuellenGroupCode.value.toUpperCase(Locale.ROOT);
                        switch (currentSection) {
                            case "HEADER":
                                consumeSection();
                                break;
                            case "TABLES":
                                parseTablesSection();
                                break;
                            case "BLOCKS":
                                parseBlocksSection();
                                break;
                            case "ENTITIES":
                                parseEntitiesSection();
                                break;
                            case "OBJECTS":
                                // For now, consume OBJECTS section if not fully implemented or if testParseDxf2 fails
                                // parseObjectsSection(); // TODO: Implement or verify this for dictionary test
                                consumeSection();
                                break;
                            default:
                                consumeSection();
                                break;
                        }
                        // After section processing, aktuellenGroupCode is the one that followed ENDSEC
                        // or the one that caused an issue if not properly consumed.
                        // The main loop expects aktuellenGroupCode to be ready for the next 0/SECTION or 0/EOF.
                        // If consumeSection or a specific parseXXXSection correctly consumes ENDSEC and advances,
                        // this 'continue' is fine.
                        continue;
                    } else {
                        throw new DxfParserException("Malformed SECTION: expected group code 2 after 0/SECTION, got: " + aktuellenGroupCode);
                    }
                } else if (aktuellenGroupCode.code == 0 && "EOF".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    break;
                }
                // If not a section or EOF, advance to find one. This handles comments or other data between sections.
                aktuellenGroupCode = nextGroupCode();
            }
        } catch (IOException e) {
            throw new DxfParserException("Error reading DXF file", e);
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                // Log error or ignore
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
        // Assumes aktuellenGroupCode is on 2/SECTION_NAME or first code after it.
        // Consumes until 0/ENDSEC is found and read.
        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0 && "ENDSEC".equalsIgnoreCase(aktuellenGroupCode.value)) {
                aktuellenGroupCode = nextGroupCode(); // Consume ENDSEC
                currentSection = null;
                return;
            }
            aktuellenGroupCode = nextGroupCode();
        }
        throw new DxfParserException("Premature EOF while consuming section: " + currentSection);
    }

    private void parseTablesSection() throws IOException, DxfParserException {
        // Called when aktuellenGroupCode is 2/TABLES (the section name itself)
        // The first actual content of the section will be 0/TABLE
        aktuellenGroupCode = nextGroupCode();

        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDSEC".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode();
                    currentSection = null;
                    return;
                } else if ("TABLE".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode(); // Should be 2/table_name
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 2) {
                        String tableName = aktuellenGroupCode.value.toUpperCase(Locale.ROOT);
                        aktuellenGroupCode = nextGroupCode(); // Advance past 2/table_name to table-specific headers or first 0/entry

                        // Consume table-specific header codes (e.g. 70 for max number of entries)
                        // until we hit the first 0/entry_type_name or 0/ENDTAB
                        while(aktuellenGroupCode != null && aktuellenGroupCode.code != 0) {
                            // Optionally handle specific table header codes like 70 if needed by specific table parsers
                            aktuellenGroupCode = nextGroupCode();
                        }

                        if (aktuellenGroupCode == null) { // EOF before ENDTAB
                             throw new DxfParserException("Premature EOF in TABLE " + tableName);
                        }

                        // Now aktuellenGroupCode should be on the first 0/entry_type or 0/ENDTAB
                        if ("LAYER".equals(tableName)) {
                            parseLayerTable();
                        } else if ("LTYPE".equals(tableName)) {
                            parseLinetypeTable();
                        } else if ("DIMSTYLE".equals(tableName)) {
                            parseDimStyleTable();
                        } else {
                            consumeTableOrEntries();
                        }

                        if (aktuellenGroupCode == null || !(aktuellenGroupCode.code == 0 && "ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value))) {
                            throw new DxfParserException("TABLE " + tableName + " parsing did not correctly position at ENDTAB. Current: " + aktuellenGroupCode);
                        }
                        aktuellenGroupCode = nextGroupCode(); // Consume ENDTAB
                    } else {
                        throw new DxfParserException("Malformed TABLE entry: expected group code 2 for table name. Got: " + aktuellenGroupCode);
                    }
                } else {
                    throw new DxfParserException("Unexpected group code " + aktuellenGroupCode + " in TABLES section while expecting 0/TABLE or 0/ENDSEC.");
                }
            } else {
                throw new DxfParserException("Unexpected non-zero group code " + aktuellenGroupCode + " at a point where a 0-code (TABLE/ENDSEC) was expected in TABLES section.");
            }
        }
        throw new DxfParserException("Premature EOF in TABLES section (outer loop).");
    }

    private void parseLayerTable() throws IOException, DxfParserException {
        // aktuellenGroupCode is at the first 0/LAYER or 0/ENDTAB
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
        // aktuellenGroupCode is 0/LAYER
        String layerName = null;
        int color = 7;
        String linetype = "CONTINUOUS";
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 2: layerName = aktuellenGroupCode.value; break;
                case 62: color = Integer.parseInt(aktuellenGroupCode.value); break;
                case 6: linetype = aktuellenGroupCode.value; break;
                case 70: break;
                default: break;
            }
        }
        if (layerName != null && !layerName.isEmpty()) {
            DxfLayer layer = document.getLayer(layerName);
            if (layer == null) { layer = new DxfLayer(layerName); }
            layer.setColor(Math.abs(color));
            layer.setVisible(color >= 0);
            layer.setLinetypeName(linetype);
            document.addLayer(layer);
        }
        // aktuellenGroupCode is now on the next 0/LAYER or 0/ENDTAB or null
    }

    private void parseLinetypeTable() throws IOException, DxfParserException {
        // aktuellenGroupCode is at the first 0/LTYPE or 0/ENDTAB
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
        // aktuellenGroupCode is 0/LTYPE
        String linetypeName = null;
        String description = "";
        double patternLength = 0.0;
        List<Double> patternElements = new ArrayList<>();
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 2: linetypeName = aktuellenGroupCode.value; break;
                case 3: description = aktuellenGroupCode.value; break;
                case 70: break;
                case 72: break;
                case 73: break;
                case 40: patternLength = Double.parseDouble(aktuellenGroupCode.value); break;
                case 49: patternElements.add(Double.parseDouble(aktuellenGroupCode.value)); break;
                default: break;
            }
        }
        if (linetypeName != null && !linetypeName.isEmpty()) {
            DxfLinetype ltype = new DxfLinetype(linetypeName);
            ltype.setDescription(description);
            ltype.setPatternLength(patternLength);
            for (double element : patternElements) { ltype.addPatternElement(element); }
            document.addLinetype(ltype);
        }
    }

    private void consumeTableOrEntries() throws IOException, DxfParserException {
        // aktuellenGroupCode is on 0/TABLE_ENTRY_TYPE or 0/ENDTAB
        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    return;
                }
                consumeUnknownTableEntry();
            } else {
                 consumeUnknownTableEntry(); // Should not happen, but be robust
            }
        }
        throw new DxfParserException("Premature EOF while consuming entries for an unhandled table.");
    }

    private void consumeUnknownTableEntry() throws IOException, DxfParserException {
        while((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            // Keep consuming non-zero codes
        }
    }

    private void parseBlocksSection() throws IOException, DxfParserException {
        // Called when aktuellenGroupCode is 2/BLOCKS
        aktuellenGroupCode = nextGroupCode(); // Move to first 0/BLOCK or 0/ENDSEC

        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDSEC".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode();
                    currentSection = null;
                    return;
                } else if ("BLOCK".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    parseSingleBlockDefinition();
                    // parseSingleBlockDefinition updates aktuellenGroupCode to what follows ENDBLK
                } else {
                    throw new DxfParserException("Unexpected group code " + aktuellenGroupCode + " in BLOCKS section while expecting BLOCK or ENDSEC.");
                }
            } else { // Should not happen if previous section/block was parsed correctly
                 aktuellenGroupCode = nextGroupCode();
            }
        }
        if ("BLOCKS".equalsIgnoreCase(currentSection)) {
             throw new DxfParserException("Premature EOF in BLOCKS section (outer loop), ENDSEC not found.");
        }
    }

    private void parseSingleBlockDefinition() throws IOException, DxfParserException {
        // aktuellenGroupCode is 0/BLOCK
        String blockName = null;
        Point3D basePoint = new Point3D(0, 0, 0); // Default base point
        DxfBlock currentBlock = null;

        // Read block header: name, base point, flags etc.
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 2: blockName = aktuellenGroupCode.value; break; // Block name
                case 10: basePoint = new Point3D(Double.parseDouble(aktuellenGroupCode.value), basePoint.y, basePoint.z); break;
                case 20: basePoint = new Point3D(basePoint.x, Double.parseDouble(aktuellenGroupCode.value), basePoint.z); break;
                case 30: basePoint = new Point3D(basePoint.x, basePoint.y, Double.parseDouble(aktuellenGroupCode.value)); break;
                // TODO: Handle other block flags like 70 if necessary
                default: break;
            }
        }

        if (blockName == null || blockName.trim().isEmpty()) {
            throw new DxfParserException("BLOCK definition found with no name (group code 2).");
        }
        currentBlock = new DxfBlock(blockName);
        currentBlock.setBasePoint(basePoint);

        EntitiesParser entitiesParser = new EntitiesParser(this.reader, this.document);

        // aktuellenGroupCode is now on the first 0/ENTITY_TYPE within the block, or 0/ENDBLK
        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDBLK".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    document.addBlock(currentBlock);
                    aktuellenGroupCode = nextGroupCode(); // Consume ENDBLK
                    return;
                } else { // It's an entity within the block
                    DxfEntity entity = entitiesParser.parseEntity(this.aktuellenGroupCode);
                    this.aktuellenGroupCode = entitiesParser.getAktuellenGroupCode();

                    if (entity != null) {
                        currentBlock.addEntity(entity);
                    }
                }
            } else {
                throw new DxfParserException("Unexpected non-zero group code " + aktuellenGroupCode + " within BLOCK definition '" + blockName + "'. Expected 0 for entity or ENDBLK.");
            }
        }
        throw new DxfParserException("Premature EOF within BLOCK definition for block: " + blockName + ". ENDBLK not found.");
    }

    private void parseEntitiesSection() throws IOException, DxfParserException {
        // Called when aktuellenGroupCode is 2/ENTITIES
        aktuellenGroupCode = nextGroupCode(); // Move to first 0/ENTITY_TYPE or 0/ENDSEC
        EntitiesParser entitiesParser = new EntitiesParser(this.reader, this.document);

        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDSEC".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode(); // Consume ENDSEC
                    currentSection = null;
                    return;
                }
                DxfEntity entity = entitiesParser.parseEntity(this.aktuellenGroupCode);
                this.aktuellenGroupCode = entitiesParser.getAktuellenGroupCode();

                if (entity != null) {
                    this.document.addEntity(entity);
                }
            } else {
                throw new DxfParserException("Unexpected non-zero group code " + aktuellenGroupCode + " in ENTITIES section. Expected 0 for entity type or ENDSEC.");
            }
        }
         throw new DxfParserException("Premature EOF in ENTITIES section.");
    }


    private void parseDimStyleTable() throws IOException, DxfParserException {
        // aktuellenGroupCode is at the first 0/DIMSTYLE or 0/ENDTAB
        while (aktuellenGroupCode != null && aktuellenGroupCode.code != 0) { // Skip potential table header codes
             aktuellenGroupCode = nextGroupCode();
        }

        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    return;
                } else if ("DIMSTYLE".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    parseSingleDimStyleEntry();
                } else {
                    throw new DxfParserException("Unexpected group code " + aktuellenGroupCode + " in DIMSTYLE table while expecting DIMSTYLE or ENDTAB.");
                }
            } else {
                throw new DxfParserException("Unexpected non-zero group code " + aktuellenGroupCode + " where 0/DIMSTYLE or 0/ENDTAB was expected in DIMSTYLE table.");
            }
        }
        throw new DxfParserException("Premature EOF in DIMSTYLE table.");
    }

    private void parseSingleDimStyleEntry() throws IOException, DxfParserException {
        // aktuellenGroupCode is 0/DIMSTYLE
        String dimStyleName = null;
        DxfDimStyle style = null;

        // Initialize with DxfDimStyle defaults by creating a temporary new style
        DxfDimStyle defaults = new DxfDimStyle(""); // Temporary style to get defaults
        double arrowSize = defaults.getArrowSize();
        double extensionLineOffset = defaults.getExtensionLineOffset();
        double extensionLineExtension = defaults.getExtensionLineExtension();
        double textHeight = defaults.getTextHeight();
        int decimalPlaces = defaults.getDecimalPlaces();
        double textGap = defaults.getTextGap();
        int dimensionLineColor = defaults.getDimensionLineColor();
        int extensionLineColor = defaults.getExtensionLineColor();
        int textColor = defaults.getTextColor();
        boolean nameFound = false;

        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 2: // Removed duplicate "case 2:"
                    dimStyleName = aktuellenGroupCode.value;
                    if (dimStyleName == null || dimStyleName.trim().isEmpty()) {
                        nameFound = false;
                        style = null;
                        // Let the main while loop consume the rest of the fields for this invalid entry.
                        // The style won't be added if nameFound is false or style is null.
                    } else {
                        nameFound = true;
                        style = document.getDimensionStyle(dimStyleName);
                        if (style == null) {
                            style = new DxfDimStyle(dimStyleName);
                            // If style is newly created, apply defaults that might have been parsed before name (if any were)
                            // This ensures already parsed values for this entry are not lost if name came late.
                            style.setArrowSize(arrowSize); style.setExtensionLineOffset(extensionLineOffset);
                            style.setExtensionLineExtension(extensionLineExtension); style.setTextHeight(textHeight);
                            style.setDecimalPlaces(decimalPlaces); style.setTextGap(textGap);
                            style.setDimensionLineColor(dimensionLineColor); style.setExtensionLineColor(extensionLineColor);
                            style.setTextColor(textColor);
                        }
                        // If style was retrieved, it should already have its correct values.
                        // We will continue to parse and potentially override them if new codes appear.
                    }
                    break;
                case 3:
                    if (!nameFound) {
                        dimStyleName = aktuellenGroupCode.value;
                         if (dimStyleName == null || dimStyleName.trim().isEmpty()) {
                            // Similar to above, let main loop consume fields for this nameless style.
                            // Style won't be added if name is invalid.
                         } else {
                            nameFound = true;
                            style = document.getDimensionStyle(dimStyleName);
                            if (style == null) { style = new DxfDimStyle(dimStyleName); }
                            if (style != null) {
                               style.setArrowSize(arrowSize); style.setExtensionLineOffset(extensionLineOffset);
                               style.setExtensionLineExtension(extensionLineExtension); style.setTextHeight(textHeight);
                               style.setDecimalPlaces(decimalPlaces); style.setTextGap(textGap);
                               style.setDimensionLineColor(dimensionLineColor); style.setExtensionLineColor(extensionLineColor);
                               style.setTextColor(textColor);
                            }
                        }
                    }
                    break;
                case 41: arrowSize = Double.parseDouble(aktuellenGroupCode.value); if (style != null) style.setArrowSize(arrowSize); break;
                case 42: extensionLineOffset = Double.parseDouble(aktuellenGroupCode.value); if (style != null) style.setExtensionLineOffset(extensionLineOffset); break;
                case 43: extensionLineExtension = Double.parseDouble(aktuellenGroupCode.value); if (style != null) style.setExtensionLineExtension(extensionLineExtension); break;
                case 44: // DIMTXT (fallback if 140 not present)
                    double tempTextHeightFallback = Double.parseDouble(aktuellenGroupCode.value);
                    if (style != null) { if(style.getTextHeight() == defaults.getTextHeight()) style.setTextHeight(tempTextHeightFallback); }
                    else { textHeight = tempTextHeightFallback; }
                    break;
                case 140: textHeight = Double.parseDouble(aktuellenGroupCode.value); if (style != null) style.setTextHeight(textHeight); break;
                case 147: textGap = Double.parseDouble(aktuellenGroupCode.value); if (style != null) style.setTextGap(textGap); break;
                case 278: // DIMGAP (alternative)
                    try {
                        double demoGap = Double.parseDouble(aktuellenGroupCode.value);
                        if (style != null) { if(style.getTextGap() == defaults.getTextGap()) style.setTextGap(demoGap); }
                        else { textGap = demoGap; }
                    } catch (NumberFormatException e) { /* ignore */ }
                    break;
                case 176: dimensionLineColor = Integer.parseInt(aktuellenGroupCode.value); if (style != null) style.setDimensionLineColor(dimensionLineColor); break;
                case 177: extensionLineColor = Integer.parseInt(aktuellenGroupCode.value); if (style != null) style.setExtensionLineColor(extensionLineColor); break;
                case 178: textColor = Integer.parseInt(aktuellenGroupCode.value); if (style != null) style.setTextColor(textColor); break;
                case 271: decimalPlaces = Integer.parseInt(aktuellenGroupCode.value); if (style != null) style.setDecimalPlaces(decimalPlaces); break;
                // TODO: Add more DIMSTYLE variables as needed (DIMBLK, DIMTXSTY, DIMTAD, etc.)
                default: break;
            }
        }

        if (style != null) {
            // Ensure all collected values are set, especially if name (code 2) came after some value codes
            style.setArrowSize(arrowSize);
            style.setExtensionLineOffset(extensionLineOffset);
            style.setExtensionLineExtension(extensionLineExtension);
            style.setTextHeight(textHeight);
            style.setDecimalPlaces(decimalPlaces);
            style.setTextGap(textGap);
            style.setDimensionLineColor(dimensionLineColor);
            style.setExtensionLineColor(extensionLineColor);
            style.setTextColor(textColor);
            document.addDimensionStyle(style);
        } else if (nameFound) {
             throw new DxfParserException("DIMSTYLE with name '" + dimStyleName + "' found, but DxfDimStyle object was not properly initialized/retrieved.");
        }
        // aktuellenGroupCode is now on the next 0/DIMSTYLE or 0/ENDTAB or null
    }
}
