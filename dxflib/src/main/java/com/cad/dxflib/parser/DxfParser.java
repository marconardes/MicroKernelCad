package com.cad.dxflib.parser;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.DxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point2D;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.entities.*; // Individual entity imports are fine, or wildcard
import com.cad.dxflib.objects.DxfDictionary;
import com.cad.dxflib.objects.DxfScale;
import com.cad.dxflib.structure.*; // Wildcard for structure classes

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Parses a DXF file from an InputStream and populates a {@link DxfDocument} object.
 * The parser handles different sections of a DXF file including TABLES, BLOCKS, ENTITIES, and OBJECTS.
 * It progressively builds the DxfDocument by parsing entities, table entries, and objects.
 * <p>
 * The main entry point is the {@link #parse(InputStream)} method.
 * </p>
 * <p>
 * Note: This parser is not exhaustive and may not support all DXF features or versions.
 * It focuses on common entities and structures. Error handling for malformed DXF data
 * is included but may not cover all possible edge cases.
 * </p>
 */
public class DxfParser {

    private BufferedReader reader;
    private DxfDocument document;
    private DxfGroupCode aktuellenGroupCode; // Current group code being processed
    private String currentSection; // Name of the current DXF section (e.g., "ENTITIES", "TABLES")

    /**
     * Parses a DXF file from the given InputStream.
     *
     * @param inputStream The InputStream providing the DXF file data. Must not be null.
     * @return A {@link DxfDocument} containing the parsed DXF data.
     * @throws DxfParserException If an error occurs during parsing (e.g., malformed DXF, I/O issues).
     * @throws IllegalArgumentException If the inputStream is null.
     */
    public DxfDocument parse(InputStream inputStream) throws DxfParserException {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null.");
        }
        this.document = new DxfDocument();
        // It's generally better to specify UTF-8 or allow charset to be passed if known,
        // as DXF files can have different encodings. Defaulting to system default here.
        this.reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            aktuellenGroupCode = nextGroupCode();
            while (aktuellenGroupCode != null) {
                if (aktuellenGroupCode.code == 0 && "SECTION".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode(); // Expect group code 2 for section name
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 2) {
                        currentSection = aktuellenGroupCode.value.toUpperCase(Locale.ROOT);
                        switch (currentSection) {
                            case "HEADER":
                                // Header section parsing can be added here if needed.
                                // For now, we consume it.
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
                                parseObjectsSection();
                                break;
                            default:
                                // Unknown section, consume it to allow parsing to continue
                                consumeSection();
                                break;
                        }
                        continue; // Skip to next iteration as section parsers advance aktuellenGroupCode
                    } else {
                        throw new DxfParserException("Malformed SECTION: expected group code 2 after 0/SECTION, got: " + aktuellenGroupCode);
                    }
                } else if (aktuellenGroupCode.code == 0 && "EOF".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    break; // End of file
                }
                // If not a SECTION or EOF, advance to the next group code to find a section start or EOF
                aktuellenGroupCode = nextGroupCode();
            }
        } catch (IOException e) {
            throw new DxfParserException("Error reading DXF file", e);
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                // Log error or simply ignore if closing fails
                // System.err.println("Error closing reader: " + e.getMessage());
            }
        }
        return this.document;
    }

    /**
     * Parses the OBJECTS section of the DXF file.
     * This section contains non-graphical objects like dictionaries and scales.
     * @throws IOException If an I/O error occurs.
     * @throws DxfParserException If a parsing error occurs.
     */
    private void parseObjectsSection() throws IOException, DxfParserException {
        aktuellenGroupCode = nextGroupCode();
        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDSEC".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode();
                    currentSection = null;
                    return;
                }

                String objectType = aktuellenGroupCode.value.toUpperCase(Locale.ROOT);
                switch (objectType) {
                    case "DICTIONARY":
                        parseDictionaryObject();
                        break;
                    case "SCALE":
                        parseScaleObject();
                        break;
                    // TODO: Add cases for other object types like ACDBPLACEHOLDER, MATERIAL, etc.
                    default:
                        consumeGenericObject(objectType);
                        break;
                }
            } else {
                throw new DxfParserException("Unexpected non-zero group code " + aktuellenGroupCode + " at start of object in OBJECTS section.");
            }
        }
        throw new DxfParserException("Premature EOF in OBJECTS section.");
    }

    /**
     * Parses a DICTIONARY object.
     * Dictionaries map names to handles of other objects.
     * @return The parsed DxfDictionary.
     * @throws IOException If an I/O error occurs.
     * @throws DxfParserException If a parsing error occurs.
     */
    private DxfDictionary parseDictionaryObject() throws IOException, DxfParserException {
        DxfDictionary dict = new DxfDictionary();
        String currentEntryName = null;

        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 5:
                    dict.setHandle(aktuellenGroupCode.value);
                    break;
                case 100: // Subclass marker (e.g., AcDbDictionary)
                    break;
                case 280:
                    dict.setCloningFlag(Integer.parseInt(aktuellenGroupCode.value));
                    break;
                case 281:
                    dict.setHardOwner("1".equals(aktuellenGroupCode.value));
                    break;
                case 330:
                    dict.setOwnerHandle(aktuellenGroupCode.value);
                    break;
                case 3:
                    currentEntryName = aktuellenGroupCode.value;
                    break;
                case 350:
                case 360:
                case 340:
                    if (currentEntryName != null) {
                        dict.addEntry(currentEntryName, aktuellenGroupCode.value);
                        currentEntryName = null;
                    } else {
                        throw new DxfParserException("Dictionary entry handle " + aktuellenGroupCode.value + " found without preceding name (code 3).");
                    }
                    break;
                default:
                    break;
            }
        }

        if (dict.getHandle() != null) {
             document.addDictionary(dict.getHandle(), dict);
             document.addObject(dict.getHandle(), dict);
        }
        return dict;
    }

    /**
     * Consumes a generic DXF object for which a specific parser is not yet implemented.
     * Reads group codes until the next 0-group code (start of a new object or ENDSEC).
     * @param objectType The type of the object being consumed.
     * @throws IOException If an I/O error occurs.
     * @throws DxfParserException If a parsing error occurs.
     */
    private void consumeGenericObject(String objectType) throws IOException, DxfParserException {
        String handle = null;
        String ownerHandle = null;

        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            if(aktuellenGroupCode.code == 5) handle = aktuellenGroupCode.value;
            if(aktuellenGroupCode.code == 330) ownerHandle = aktuellenGroupCode.value;
        }
        if (handle != null) {
            // If needed in the future, an unknown object could be stored:
            // document.addObject(handle, new DxfGenericObjectPlaceholder(objectType, handle, ownerHandle));
        }
    }

    /**
     * Parses a SCALE object.
     * SCALE objects define drawing scales and are typically found in the ACAD_SCALELIST dictionary.
     * @return The parsed DxfScale object.
     * @throws IOException If an I/O error occurs.
     * @throws DxfParserException If a parsing error occurs.
     */
    private DxfScale parseScaleObject() throws IOException, DxfParserException {
        DxfScale scale = new DxfScale();

        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 5:
                    scale.setHandle(aktuellenGroupCode.value);
                    break;
                case 100:
                    break;
                case 330:
                    scale.setOwnerHandle(aktuellenGroupCode.value);
                    break;
                case 300:
                    scale.setName(aktuellenGroupCode.value);
                    break;
                case 140:
                    scale.setPaperUnits(Double.parseDouble(aktuellenGroupCode.value));
                    break;
                case 141:
                    scale.setDrawingUnits(Double.parseDouble(aktuellenGroupCode.value));
                    break;
                case 290:
                    scale.setUnitScale("1".equals(aktuellenGroupCode.value) || "true".equalsIgnoreCase(aktuellenGroupCode.value));
                    break;
                case 70:
                    scale.setFlags(Integer.parseInt(aktuellenGroupCode.value));
                    break;
                default:
                    break;
            }
        }

        if (scale.getHandle() != null) {
            document.addScale(scale);
            document.addObject(scale.getHandle(), scale);
        } else {
            // Optionally log a warning if a SCALE object has no handle.
        }
        return scale;
    }

    /**
     * Reads the next group code and its value from the DXF file.
     * @return A DxfGroupCode object, or null if EOF is reached.
     * @throws IOException If an I/O error occurs.
     * @throws DxfParserException If the group code format is invalid or EOF is premature.
     */
    private DxfGroupCode nextGroupCode() throws IOException, DxfParserException {
        String codeStr = reader.readLine();
        String valueStr;
        if (codeStr != null) {
            valueStr = reader.readLine();
            if (valueStr == null) {
                throw new DxfParserException("Premature EOF: Expected value after group code " + codeStr.trim());
            }
        } else {
            return null; // EOF
        }
        try {
            int code = Integer.parseInt(codeStr.trim());
            return new DxfGroupCode(code, valueStr.trim());
        } catch (NumberFormatException e) {
            throw new DxfParserException("Invalid group code format: '" + codeStr.trim() + "'", e);
        }
    }

    /**
     * Consumes group codes for an entire section until an ENDSEC marker is found.
     * Used for sections that are not fully parsed yet (e.g., HEADER).
     * @throws IOException If an I/O error occurs.
     * @throws DxfParserException If EOF is reached before ENDSEC.
     */
    private void consumeSection() throws IOException, DxfParserException {
        while ((aktuellenGroupCode = nextGroupCode()) != null) {
            if (aktuellenGroupCode.code == 0 && "ENDSEC".equalsIgnoreCase(aktuellenGroupCode.value)) {
                aktuellenGroupCode = nextGroupCode(); // Consume ENDSEC
                currentSection = null;
                return;
            }
        }
        throw new DxfParserException("Premature EOF while consuming section: " + currentSection);
    }

    /**
     * Parses the TABLES section of the DXF file.
     * This section contains definitions for layers, linetypes, text styles, dimension styles, etc.
     * @throws IOException If an I/O error occurs.
     * @throws DxfParserException If a parsing error occurs.
     */
    private void parseTablesSection() throws IOException, DxfParserException {
        aktuellenGroupCode = nextGroupCode(); // Expect 0/TABLE
        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDSEC".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode(); // Consume ENDSEC
                    currentSection = null;
                    return;
                } else if ("TABLE".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode(); // Expect 2/TableName
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 2) {
                        String tableName = aktuellenGroupCode.value.toUpperCase(Locale.ROOT);
                        switch (tableName) {
                            case "LAYER":
                                parseLayerTable();
                                break;
                            case "LTYPE":
                                parseLinetypeTable();
                                break;
                            case "STYLE":
                                parseStyleTable();
                                break;
                            case "BLOCK_RECORD":
                                parseBlockRecordTable();
                                break;
                            case "DIMSTYLE":
                                parseDimStyleTable();
                                break;
                            // TODO: Add cases for other tables like APPID, UCS, VIEW, VPORT
                            default:
                                consumeTableOrEntries(); // Consume unhandled table
                                break;
                        }
                        // All table parsing methods should position aktuellenGroupCode at 0/ENDTAB
                        if (aktuellenGroupCode == null || !(aktuellenGroupCode.code == 0 && "ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value))) {
                            throw new DxfParserException("TABLE " + tableName + " parsing did not correctly position at ENDTAB. Current: " + aktuellenGroupCode);
                        }
                        aktuellenGroupCode = nextGroupCode(); // Consume ENDTAB, expect next 0/TABLE or 0/ENDSEC
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
        // Consume table header codes specific to LAYER table if any (e.g., max entries 70)
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0 && aktuellenGroupCode.code != 2 ) {
            // Code 2 can be start of layer name, so stop before it if not 0
             if (aktuellenGroupCode.code == 0 && !"LAYER".equalsIgnoreCase(aktuellenGroupCode.value)) break; // Should be 0/LAYER
        }
         if (aktuellenGroupCode != null && aktuellenGroupCode.code == 0 && "LAYER".equalsIgnoreCase(aktuellenGroupCode.value)) {
             // Already at first 0/LAYER or consumed table header and now at first 0/LAYER
         } else if (aktuellenGroupCode != null && aktuellenGroupCode.code == 2){
            // This means the table header was minimal and we are already at the first layer's name
            // We need to backtrack or adjust. For now, assume parseSingleLayerEntry handles current code.
            // This case implies the loop condition above was too simple.
            // A robust way is to peek or ensure 0/LAYER is first.
            // For now, let's assume the main loop of parseTablesSection correctly positions us.
            // The current logic in parseTablesSection calls nextGroupCode after 0/TABLE, 2/TableName
            // then this method is called. So, aktuellenGroupCode should be the first 0/LAYER or table record header.
            // If table record header (like 70 count) is present, the loop above consumes it.
            // Then it expects 0/LAYER.
         }


        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    return; // ENDTAB consumed by caller's loop
                } else if ("LAYER".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    parseSingleLayerEntry();
                } else {
                    throw new DxfParserException("Unexpected group code " + aktuellenGroupCode + " in LAYER table while expecting LAYER or ENDTAB.");
                }
            } else {
                // This might happen if table header parsing is not exhaustive or DXF is malformed.
                throw new DxfParserException("Unexpected non-zero group code " + aktuellenGroupCode + " where 0/LAYER or 0/ENDTAB was expected in LAYER table.");
            }
        }
        throw new DxfParserException("Premature EOF in LAYER table.");
    }

    private void parseSingleLayerEntry() throws IOException, DxfParserException {
        String layerName = null;
        int color = 7; // Default color
        String linetypeName = "CONTINUOUS"; // Default linetype
        // int flags = 0; // Standard flags for layer (e.g., frozen, locked) - code 70

        // aktuellenGroupCode is 0/LAYER, next codes are properties
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 2: layerName = aktuellenGroupCode.value; break; // Layer name
                case 62: color = Integer.parseInt(aktuellenGroupCode.value); break; // Color number
                case 6: linetypeName = aktuellenGroupCode.value; break; // Linetype name
                case 70: /* flags = Integer.parseInt(aktuellenGroupCode.value); */ break; // Layer flags
                // Other layer properties like plot style (390), lineweight (370), transparency (440) can be added here.
                default: break;
            }
        }
        if (layerName != null && !layerName.isEmpty()) {
            // DxfDocument's addLayer handles case-insensitivity and default "0" layer merging if needed
            DxfLayer layer = document.getLayer(layerName); // Check if it's an update to an existing (e.g. default "0")
            if (layer == null) {
                layer = new DxfLayer(layerName);
            }
            layer.setColor(Math.abs(color)); // Store absolute color
            layer.setVisible(color >= 0);   // Visibility based on sign
            layer.setLinetypeName(linetypeName);
            // layer.setFlags(flags); // If flags are stored
            document.addLayer(layer);
        } else {
            throw new DxfParserException("Layer entry found with no name (group code 2). Current group: " + aktuellenGroupCode);
        }
        // aktuellenGroupCode is now 0 for next LAYER or ENDTAB (or null if EOF)
    }

    private void parseLinetypeTable() throws IOException, DxfParserException {
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0 && aktuellenGroupCode.code != 2) {
             if (aktuellenGroupCode.code == 0 && !"LTYPE".equalsIgnoreCase(aktuellenGroupCode.value)) break;
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
                 throw new DxfParserException("Unexpected non-zero group code " + aktuellenGroupCode + " where 0/LTYPE or 0/ENDTAB was expected in LTYPE table.");
            }
        }
        throw new DxfParserException("Premature EOF in LTYPE table.");
    }

    private void parseSingleLinetypeEntry() throws IOException, DxfParserException {
        String linetypeName = null;
        String description = "";
        double patternLength = 0.0;
        List<Double> patternElements = new ArrayList<>();
        // int alignment = 65; // Code 72, default is 'A'
        // int numDashElements = 0; // Code 73

        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 2: linetypeName = aktuellenGroupCode.value; break;
                case 3: description = aktuellenGroupCode.value; break;
                case 70: /* flags = Integer.parseInt(aktuellenGroupCode.value); */ break;
                case 72: /* alignment = Integer.parseInt(aktuellenGroupCode.value); */ break;
                case 73: /* numDashElements = Integer.parseInt(aktuellenGroupCode.value); */ break;
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
        // Consumes an entire TABLE (if current code is 0/TABLE then 2/TableName)
        // or just entries within a table if already past the 2/TableName.
        // This is a generic consumer for unhandled table types.
        // It must correctly find and position after 0/ENDTAB.

        // If we are at the start of a TABLE definition (0/TABLE, 2/TableName), consume table headers first.
        // The caller (parseTablesSection) usually handles the 0/TABLE and 2/TableName itself.
        // This method is more for consuming the *content* of an unknown table.

        // Loop through table entries until ENDTAB
        while ((aktuellenGroupCode = nextGroupCode()) != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    // ENDTAB found, return. The caller (parseTablesSection) will call nextGroupCode() to move past ENDTAB.
                    return;
                } else {
                    // Found another 0/entry_type within this unknown table. Consume this entry.
                    consumeUnknownTableEntry();
                }
            } else {
                // Unexpected non-zero code where a 0/entry_type or 0/ENDTAB was expected.
                // This could be part of a table header for certain table types if not consumed before.
                // Or it's a malformed table. For robustness, try to skip to next 0-code.
                // System.err.println("Warning: Skipping unexpected non-zero code " + aktuellenGroupCode + " in unhandled table.");
            }
        }
        throw new DxfParserException("Premature EOF while consuming entries for an unhandled table. ENDTAB not found.");
    }

    /** Consumes all group codes for an unknown table entry until the next 0-code. */
    private void consumeUnknownTableEntry() throws IOException, DxfParserException {
        while((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            // just consume codes for this specific unknown entry
        }
        // aktuellenGroupCode is now at the next 0-code (next entry or ENDTAB) or null
    }


    /**
     * Parses the BLOCKS section of the DXF file.
     * This section contains block definitions, which are reusable groups of entities.
     * @throws IOException If an I/O error occurs.
     * @throws DxfParserException If a parsing error occurs.
     */
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
                 aktuellenGroupCode = nextGroupCode();
            }
        }
        if ("BLOCKS".equalsIgnoreCase(currentSection)) {
             throw new DxfParserException("Premature EOF in BLOCKS section (outer loop), ENDSEC not found.");
        }
    }

    private void parseSingleBlockDefinition() throws IOException, DxfParserException {
        String blockName = null;
        Point3D basePoint = new Point3D(0, 0, 0);
        // Other block properties like flags (code 70), xref path (code 1) can be added here.
        DxfBlock currentBlock = null;

        // aktuellenGroupCode is 0/BLOCK. Loop to read block header codes.
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 2: blockName = aktuellenGroupCode.value; break; // Block name
                case 10: basePoint = new Point3D(Double.parseDouble(aktuellenGroupCode.value), basePoint.y, basePoint.z); break; // Base point X
                case 20: basePoint = new Point3D(basePoint.x, Double.parseDouble(aktuellenGroupCode.value), basePoint.z); break; // Base point Y
                case 30: basePoint = new Point3D(basePoint.x, basePoint.y, Double.parseDouble(aktuellenGroupCode.value)); break; // Base point Z
                case 70: /* block flags */ break;
                case 1: /* xref path name */ break;
                // Other block definition codes like 3 (block description), etc.
                default: break;
            }
        }

        if (blockName == null || blockName.trim().isEmpty()) {
            throw new DxfParserException("BLOCK definition found with no name (group code 2).");
        }

        // Check if block already exists (e.g. *Model_Space, *Paper_Space might be pre-added or defined in BLOCK_RECORDS)
        currentBlock = document.getBlock(blockName);
        if (currentBlock == null) {
            currentBlock = new DxfBlock(blockName);
        }
        currentBlock.setBasePoint(basePoint);
        // currentBlock.setFlags(flags); // if flags are parsed and stored

        // aktuellenGroupCode is now 0/entityType or 0/ENDBLK
        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDBLK".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    document.addBlock(currentBlock); // Add or update the block in the document
                    aktuellenGroupCode = nextGroupCode(); // Consume ENDBLK
                    return; // Finished parsing this block definition
                } else { // Start of an entity within the block definition
                    String entityType = aktuellenGroupCode.value.toUpperCase(Locale.ROOT);
                    DxfEntity entity = null;
                    switch (entityType) {
                        case "LINE": entity = parseLineEntity(); break;
                        case "CIRCLE": entity = parseCircleEntity(); break;
                        case "ARC": entity = parseArcEntity(); break;
                        case "LWPOLYLINE": entity = parseLwPolylineEntity(); break;
                        case "TEXT": entity = parseTextEntity(); break;
                        case "INSERT": entity = parseInsertEntity(); break;
                        case "DIMENSION": entity = parseDimensionEntity(); break;
                        case "SPLINE": entity = parseSplineEntity(); break;
                        // TODO: Add other entity types as needed
                        default: entity = consumeUnknownEntity(); break;
                    }
                    if (entity != null) {
                        currentBlock.addEntity(entity);
                    }
                    // aktuellenGroupCode is already advanced by the entity parsing method or consumeUnknownEntity
                }
            } else {
                // This should not happen if entity parsers correctly consume their data.
                throw new DxfParserException("Unexpected non-zero group code " + aktuellenGroupCode + " within BLOCK definition '" + blockName +"', expected 0 for next entity or ENDBLK.");
            }
        }
        throw new DxfParserException("Premature EOF within BLOCK definition for block: " + blockName);
    }

    /**
     * Parses the ENTITIES section of the DXF file.
     * This section contains graphical entities like lines, circles, text, etc.
     * @throws IOException If an I/O error occurs.
     * @throws DxfParserException If a parsing error occurs.
     */
    private void parseEntitiesSection() throws IOException, DxfParserException {
        aktuellenGroupCode = nextGroupCode(); // Expect first 0/entityType or 0/ENDSEC
        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDSEC".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode(); // Consume ENDSEC
                    currentSection = null;
                    return;
                }

                DxfEntity entity = null;
                String entityType = aktuellenGroupCode.value.toUpperCase(Locale.ROOT);

                switch (entityType) {
                    case "LINE": entity = parseLineEntity(); break;
                    case "CIRCLE": entity = parseCircleEntity(); break;
                    case "ARC": entity = parseArcEntity(); break;
                    case "LWPOLYLINE": entity = parseLwPolylineEntity(); break;
                    case "TEXT": entity = parseTextEntity(); break;
                    case "INSERT": entity = parseInsertEntity(); break;
                    case "DIMENSION": entity = parseDimensionEntity(); break;
                    case "SPLINE": entity = parseSplineEntity(); break;
                    // TODO: Add other entity types as needed (MTEXT, POINT, HATCH, ELLIPSE, etc.)
                    default: entity = consumeUnknownEntity(); break; // Advances aktuellenGroupCode
                }

                if (entity != null) {
                    document.addEntity(entity);
                }
                // aktuellenGroupCode is already advanced by the entity parsing method or consumeUnknownEntity
            } else {
                throw new DxfParserException("Unexpected non-zero group code " + aktuellenGroupCode + " at start of entity in ENTITIES section.");
            }
        }
         throw new DxfParserException("Premature EOF in ENTITIES section.");
    }

    private DxfLine parseLineEntity() throws IOException, DxfParserException {
        DxfLine line = new DxfLine();
        // aktuellenGroupCode is 0/LINE. Loop to read entity properties.
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
                // TODO: Add other common entity properties like linetype (6), thickness (39)
                default: break;
            }
        }
        parseAndAttachXData(line);
        parseAndAttachReactors(line);
        return line;
    }

    private DxfCircle parseCircleEntity() throws IOException, DxfParserException {
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
        parseAndAttachXData(circle);
        parseAndAttachReactors(circle);
        return circle;
    }

    private DxfEntity consumeUnknownEntity() throws IOException, DxfParserException {
        while((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            // just consume codes until the next 0-group code
        }
        return null;
    }

    private DxfArc parseArcEntity() throws IOException, DxfParserException {
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
        parseAndAttachXData(arc);
        parseAndAttachReactors(arc);
        return arc;
    }

    private DxfLwPolyline parseLwPolylineEntity() throws IOException, DxfParserException {
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

                    reader.mark(512); // Mark to peek for bulge code
                    DxfGroupCode peekCode = null;
                    String tempCodeStr = reader.readLine();
                    if (tempCodeStr != null) {
                        String tempValueStr = reader.readLine();
                        if (tempValueStr != null) {
                            try {
                               peekCode = new DxfGroupCode(Integer.parseInt(tempCodeStr.trim()), tempValueStr.trim());
                            } catch (NumberFormatException e) { /* ignore, not a valid group code */ }
                        }
                    }
                    reader.reset(); // Go back to original position

                    if (peekCode != null && peekCode.code == 42) { // Bulge group code
                        aktuellenGroupCode = nextGroupCode(); // Consume the bulge code (42)
                        bulge = Double.parseDouble(aktuellenGroupCode.value);
                    }
                    lwpoly.addVertex(new Point2D(currentX, currentY), bulge);
                    xRead = false; // Reset for next vertex
                    break;
                case 62: lwpoly.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;
                default: xRead = false; break; // Reset xRead if an unexpected code is encountered
            }
        }
        if (vertexCount > 0 && lwpoly.getNumberOfVertices() != vertexCount && lwpoly.getNumberOfVertices() > 0) {
            // Potentially log a warning if vertex count from code 90 doesn't match parsed vertices.
        }
        parseAndAttachXData(lwpoly);
        parseAndAttachReactors(lwpoly);
        return lwpoly;
    }

    private DxfText parseTextEntity() throws IOException, DxfParserException {
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
        parseAndAttachXData(text);
        parseAndAttachReactors(text);
        return text;
    }

    private DxfInsert parseInsertEntity() throws IOException, DxfParserException {
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
            throw new DxfParserException("INSERT entity missing block name (group code 2). Current group: " + aktuellenGroupCode);
        }
        parseAndAttachXData(insert);
        parseAndAttachReactors(insert);
        return insert;
    }

    private DxfDimension parseDimensionEntity() throws IOException, DxfParserException {
        DxfDimension dimension = new DxfDimension();
        double defX=0, defY=0, defZ=0;
        double midX=0, midY=0, midZ=0;
        double p1X=0, p1Y=0, p1Z=0;
        double p2X=0, p2Y=0, p2Z=0;
        double extX=0, extY=0, extZ=0;

        boolean defPointRead = false;
        boolean midTextPointRead = false;
        boolean p1Read = false;
        boolean p2Read = false;
        boolean extrusionRead = false;


        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 8: dimension.setLayerName(aktuellenGroupCode.value); break;
                case 62: dimension.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 2: dimension.setBlockName(aktuellenGroupCode.value); break;
                case 3: dimension.setDimensionStyleName(aktuellenGroupCode.value); break;
                case 1: dimension.setDimensionText(aktuellenGroupCode.value); break;
                case 70: dimension.setDimensionTypeFlags(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 50: dimension.setRotationAngle(Double.parseDouble(aktuellenGroupCode.value)); break;

                case 10: defX = Double.parseDouble(aktuellenGroupCode.value); defPointRead = true; break;
                case 20: defY = Double.parseDouble(aktuellenGroupCode.value); defPointRead = true; break;
                case 30: defZ = Double.parseDouble(aktuellenGroupCode.value); defPointRead = true; break;

                case 11: midX = Double.parseDouble(aktuellenGroupCode.value); midTextPointRead = true; break;
                case 21: midY = Double.parseDouble(aktuellenGroupCode.value); midTextPointRead = true; break;
                case 31: midZ = Double.parseDouble(aktuellenGroupCode.value); midTextPointRead = true; break;

                case 13: p1X = Double.parseDouble(aktuellenGroupCode.value); p1Read = true; break;
                case 23: p1Y = Double.parseDouble(aktuellenGroupCode.value); p1Read = true; break;
                case 33: p1Z = Double.parseDouble(aktuellenGroupCode.value); p1Read = true; break;

                case 14: p2X = Double.parseDouble(aktuellenGroupCode.value); p2Read = true; break;
                case 24: p2Y = Double.parseDouble(aktuellenGroupCode.value); p2Read = true; break;
                case 34: p2Z = Double.parseDouble(aktuellenGroupCode.value); p2Read = true; break;

                case 210: extX = Double.parseDouble(aktuellenGroupCode.value); extrusionRead = true; break;
                case 220: extY = Double.parseDouble(aktuellenGroupCode.value); extrusionRead = true; break;
                case 230: extZ = Double.parseDouble(aktuellenGroupCode.value); extrusionRead = true; break;
                default:
                    break;
            }
        }

        if(defPointRead) dimension.setDefinitionPoint(new Point3D(defX, defY, defZ));
        if(midTextPointRead) dimension.setMiddleOfTextPoint(new Point3D(midX, midY, midZ));
        if(p1Read) dimension.setLinearPoint1(new Point3D(p1X, p1Y, p1Z));
        if(p2Read) dimension.setLinearPoint2(new Point3D(p2X, p2Y, p2Z));
        if(extrusionRead) dimension.setExtrusionDirection(new Point3D(extX, extY, extZ));
        // else it keeps the default (0,0,1) set in DxfDimension constructor or field initializer

        parseAndAttachXData(dimension);
        parseAndAttachReactors(dimension);
        return dimension;
    }

    private void parseDimStyleTable() throws IOException, DxfParserException {
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0 && aktuellenGroupCode.code != 2) {
             if (aktuellenGroupCode.code == 0 && !"DIMSTYLE".equalsIgnoreCase(aktuellenGroupCode.value)) break;
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
        String dimStyleName = null;
        DxfDimStyle style = null;
        boolean nameFound = false;

        // Temporary variables to hold values until the style object is confirmed/created
        String tempHandle = null;
        int tempFlags70 = 0;
        int tempDimclrd = 0;
        int tempDimclre = 0;
        double tempDimexe = 0.18; // Default from DxfDimStyle
        double tempDimexo = 0.0625; // Default from DxfDimStyle
        String tempDimblkName = ""; // Default from DxfDimStyle
        double tempDimasz = 0.18; // Default from DxfDimStyle
        String tempDimtxstyName = "STANDARD"; // Default from DxfDimStyle
        int tempDimclrt = 0;
        double tempDimtxt = 0.18; // Default from DxfDimStyle
        double tempDimgap = 0.09; // Default from DxfDimStyle
        int tempDimdec = 2; // Default from DxfDimStyle
        int tempDimtad = 0;
        boolean tempDimtih = true;
        boolean tempDimtoh = true;
        boolean tempDimtofl = false;
        boolean tempDimse1 = false;
        boolean tempDimse2 = false;


        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 2:
                case 3:
                    dimStyleName = aktuellenGroupCode.value;
                    nameFound = true;
                    style = document.getDimensionStyle(dimStyleName);
                    if (style == null) {
                        style = new DxfDimStyle(dimStyleName);
                    }
                    if (tempHandle != null) style.setHandle(tempHandle);
                    style.setFlags70(tempFlags70);
                    style.setDimensionLineColor(tempDimclrd);
                    style.setExtensionLineColor(tempDimclre);
                    style.setExtensionLineExtension(tempDimexe);
                    style.setExtensionLineOffset(tempDimexo);
                    style.setDimBlkName(tempDimblkName);
                    style.setArrowSize(tempDimasz);
                    style.setTextStyleName(tempDimtxstyName);
                    style.setTextColor(tempDimclrt);
                    style.setTextHeight(tempDimtxt);
                    style.setTextGap(tempDimgap);
                    style.setDecimalPlaces(tempDimdec);
                    style.setTextVerticalAlignment(tempDimtad);
                    style.setTextInsideHorizontal(tempDimtih);
                    style.setTextOutsideHorizontal(tempDimtoh);
                    style.setTextOutsideExtensions(tempDimtofl);
                    style.setSuppressFirstExtensionLine(tempDimse1);
                    style.setSuppressSecondExtensionLine(tempDimse2);
                    break;
                case 105:
                    tempHandle = aktuellenGroupCode.value;
                    if (style != null) style.setHandle(tempHandle);
                    break;
                case 70:
                    tempFlags70 = Integer.parseInt(aktuellenGroupCode.value);
                    if (style != null) style.setFlags70(tempFlags70);
                    break;
                case 41:
                    tempDimasz = Double.parseDouble(aktuellenGroupCode.value);
                    if (style != null) style.setArrowSize(tempDimasz);
                    break;
                case 42:
                    tempDimexo = Double.parseDouble(aktuellenGroupCode.value);
                    if (style != null) style.setExtensionLineOffset(tempDimexo);
                    break;
                case 43:
                case 44:
                    tempDimexe = Double.parseDouble(aktuellenGroupCode.value);
                    if (style != null) style.setExtensionLineExtension(tempDimexe);
                    break;
                case 40:
                    if (style != null) {
                        if (style.getTextHeight() == new DxfDimStyle("").getTextHeight()) {
                             style.setTextHeight(Double.parseDouble(aktuellenGroupCode.value));
                        }
                    } else {
                        tempDimtxt = Double.parseDouble(aktuellenGroupCode.value);
                    }
                    break;
                case 140:
                    tempDimtxt = Double.parseDouble(aktuellenGroupCode.value);
                    if (style != null) style.setTextHeight(tempDimtxt);
                    break;
                case 147:
                    tempDimgap = Double.parseDouble(aktuellenGroupCode.value);
                    if (style != null) style.setTextGap(tempDimgap);
                    break;
                case 48:
                case 278:
                    try {
                        double gapFallback = Double.parseDouble(aktuellenGroupCode.value);
                        if (style != null) {
                            if (style.getTextGap() == new DxfDimStyle("").getTextGap()) {
                                style.setTextGap(gapFallback);
                            }
                        } else {
                            tempDimgap = gapFallback;
                        }
                    } catch (NumberFormatException e) { /* ignore */ }
                    break;
                case 176:
                    tempDimclrd = Integer.parseInt(aktuellenGroupCode.value);
                    if (style != null) style.setDimensionLineColor(tempDimclrd);
                    break;
                case 177:
                    tempDimclre = Integer.parseInt(aktuellenGroupCode.value);
                    if (style != null) style.setExtensionLineColor(tempDimclre);
                    break;
                case 178:
                    tempDimclrt = Integer.parseInt(aktuellenGroupCode.value);
                    if (style != null) style.setTextColor(tempDimclrt);
                    break;
                case 271:
                    tempDimdec = Integer.parseInt(aktuellenGroupCode.value);
                    if (style != null) style.setDecimalPlaces(tempDimdec);
                    break;
                case 1:
                case 342:
                    tempDimblkName = aktuellenGroupCode.value;
                    if (style != null) style.setDimBlkName(tempDimblkName);
                    break;
                case 340:
                    tempDimtxstyName = aktuellenGroupCode.value;
                    if (style != null) style.setTextStyleName(tempDimtxstyName);
                    break;
                case 77:
                    tempDimtad = Integer.parseInt(aktuellenGroupCode.value);
                    if (style != null) style.setTextVerticalAlignment(tempDimtad);
                    break;
                case 73:
                    tempDimtih = !"0".equals(aktuellenGroupCode.value);
                    if (style != null) style.setTextInsideHorizontal(tempDimtih);
                    break;
                case 74:
                    tempDimtoh = !"0".equals(aktuellenGroupCode.value);
                    if (style != null) style.setTextOutsideHorizontal(tempDimtoh);
                    break;
                case 172:
                    tempDimtofl = !"0".equals(aktuellenGroupCode.value);
                    if (style != null) style.setTextOutsideExtensions(tempDimtofl);
                    break;
                case 75:
                    tempDimse1 = !"0".equals(aktuellenGroupCode.value);
                    if (style != null) style.setSuppressFirstExtensionLine(tempDimse1);
                    break;
                case 76:
                    tempDimse2 = !"0".equals(aktuellenGroupCode.value);
                    if (style != null) style.setSuppressSecondExtensionLine(tempDimse2);
                    break;
                default:
                    break;
            }
        }

        if (style != null) {
            document.addDimensionStyle(style);
        } else if (nameFound) {
            throw new DxfParserException("DIMSTYLE with name '" + dimStyleName + "' found, but DxfDimStyle object was not properly initialized/retrieved.");
        }
        if (!nameFound && aktuellenGroupCode != null && !"ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value)) {
             throw new DxfParserException("DIMSTYLE entry is missing a name (group code 2 or 3). Current code: " + aktuellenGroupCode);
        }
        if (aktuellenGroupCode == null && nameFound == false && !"DIMSTYLE".equalsIgnoreCase(currentSection)) {
             // Premature EOF
        }
    }

    private void parseStyleTable() throws IOException, DxfParserException {
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0 && aktuellenGroupCode.code != 2) {
             if (aktuellenGroupCode.code == 0 && !"STYLE".equalsIgnoreCase(aktuellenGroupCode.value)) break;
        }

        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    return;
                } else if ("STYLE".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    parseSingleTextStyleEntry();
                } else {
                    throw new DxfParserException("Unexpected group code " + aktuellenGroupCode + " in STYLE table while expecting STYLE or ENDTAB.");
                }
            } else {
                throw new DxfParserException("Unexpected non-zero group code " + aktuellenGroupCode + " where 0/STYLE or 0/ENDTAB was expected in STYLE table.");
            }
        }
        throw new DxfParserException("Premature EOF in STYLE table.");
    }

    private void parseSingleTextStyleEntry() throws IOException, DxfParserException {
        String styleName = null;
        DxfTextStyle textStyle = null;

        double fixedTextHeight = 0.0;
        double widthFactor = 1.0;
        double obliqueAngle = 0.0;
        int textGenerationFlags = 0;
        double lastHeightUsed = 0.0;
        String primaryFontFileName = "";
        String bigFontFileName = null;
        int flags70 = 0;

        boolean nameFound = false;

        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 2:
                    styleName = aktuellenGroupCode.value;
                    nameFound = true;
                    // Check if style already exists (e.g. "STANDARD" added by DxfDocument constructor)
                    textStyle = document.getTextStyle(styleName);
                    if (textStyle == null) {
                        textStyle = new DxfTextStyle(styleName);
                    }
                    // Apply pre-read values if name came later (unlikely here)
                    textStyle.setFixedTextHeight(fixedTextHeight);
                    textStyle.setWidthFactor(widthFactor);
                    textStyle.setObliqueAngle(obliqueAngle);
                    textStyle.setTextGenerationFlags(textGenerationFlags);
                    textStyle.setLastHeightUsed(lastHeightUsed);
                    textStyle.setPrimaryFontFileName(primaryFontFileName);
                    if(bigFontFileName != null) textStyle.setBigFontFileName(bigFontFileName);
                    textStyle.setFlags(flags70);
                    break;
                case 3:
                    primaryFontFileName = aktuellenGroupCode.value;
                    if (textStyle != null) textStyle.setPrimaryFontFileName(primaryFontFileName);
                    break;
                case 4:
                    bigFontFileName = aktuellenGroupCode.value;
                    if (textStyle != null) textStyle.setBigFontFileName(bigFontFileName);
                    break;
                case 40:
                    fixedTextHeight = Double.parseDouble(aktuellenGroupCode.value);
                    if (textStyle != null) textStyle.setFixedTextHeight(fixedTextHeight);
                    break;
                case 41:
                    widthFactor = Double.parseDouble(aktuellenGroupCode.value);
                    if (textStyle != null) textStyle.setWidthFactor(widthFactor);
                    break;
                case 42:
                    lastHeightUsed = Double.parseDouble(aktuellenGroupCode.value);
                    if (textStyle != null) textStyle.setLastHeightUsed(lastHeightUsed);
                    break;
                case 50:
                    obliqueAngle = Double.parseDouble(aktuellenGroupCode.value);
                    if (textStyle != null) textStyle.setObliqueAngle(obliqueAngle);
                    break;
                case 70:
                    flags70 = Integer.parseInt(aktuellenGroupCode.value);
                    if (textStyle != null) textStyle.setFlags(flags70);
                    break;
                case 71:
                    textGenerationFlags = Integer.parseInt(aktuellenGroupCode.value);
                    if (textStyle != null) textStyle.setTextGenerationFlags(textGenerationFlags);
                    break;
                default:
                    break;
            }
        }

        if (textStyle != null) {
            document.addTextStyle(textStyle);
        } else if (nameFound) {
            throw new DxfParserException("STYLE with name '" + styleName + "' found, but DxfTextStyle object was not properly initialized/retrieved.");
        }
        if (!nameFound && aktuellenGroupCode != null && !"ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value)) {
             throw new DxfParserException("STYLE entry is missing a name (group code 2). Current code: " + aktuellenGroupCode);
        }
    }

    private void parseBlockRecordTable() throws IOException, DxfParserException {
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0 && aktuellenGroupCode.code != 2 ) {
             if (aktuellenGroupCode.code == 0 && !"BLOCK_RECORD".equalsIgnoreCase(aktuellenGroupCode.value)) break;
        }

        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    return;
                } else if ("BLOCK_RECORD".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    parseSingleBlockRecordEntry();
                } else {
                    throw new DxfParserException("Unexpected group code " + aktuellenGroupCode + " in BLOCK_RECORD table while expecting BLOCK_RECORD or ENDTAB.");
                }
            } else {
                throw new DxfParserException("Unexpected non-zero group code " + aktuellenGroupCode + " where 0/BLOCK_RECORD or 0/ENDTAB was expected in BLOCK_RECORD table.");
            }
        }
        throw new DxfParserException("Premature EOF in BLOCK_RECORD table.");
    }

    private void parseSingleBlockRecordEntry() throws IOException, DxfParserException {
        String blockRecordName = null;
        DxfBlockRecord blockRecord = null;
        String handle = null;
        String ownerHandle = null;
        String layoutHandle = null;
        String xrefPath = null;
        boolean nameFound = false;

        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 2:
                    blockRecordName = aktuellenGroupCode.value;
                    nameFound = true;
                    blockRecord = document.getBlockRecord(blockRecordName);
                    if (blockRecord == null) {
                        blockRecord = new DxfBlockRecord(blockRecordName);
                    }
                    if(handle != null) blockRecord.setHandle(handle);
                    if(ownerHandle != null) blockRecord.setOwnerDictionaryHandle(ownerHandle);
                    if(layoutHandle != null) blockRecord.setLayoutHandle(layoutHandle);
                    if(xrefPath != null) blockRecord.setXrefPathName(xrefPath);
                    break;
                case 5:
                    handle = aktuellenGroupCode.value;
                    if (blockRecord != null) blockRecord.setHandle(handle);
                    break;
                case 330:
                    ownerHandle = aktuellenGroupCode.value;
                    if (blockRecord != null) blockRecord.setOwnerDictionaryHandle(ownerHandle);
                    break;
                case 340:
                    layoutHandle = aktuellenGroupCode.value;
                    if (blockRecord != null) blockRecord.setLayoutHandle(layoutHandle);
                    break;
                case 1:
                    xrefPath = aktuellenGroupCode.value;
                    if (blockRecord != null) blockRecord.setXrefPathName(xrefPath);
                    break;
                default:
                    break;
            }
        }

        if (blockRecord != null) {
            document.addBlockRecord(blockRecord);
        } else if (nameFound) {
            throw new DxfParserException("BLOCK_RECORD with name '" + blockRecordName + "' found, but DxfBlockRecord object was not properly initialized/retrieved.");
        }
        if (!nameFound && aktuellenGroupCode != null && !"ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value)) {
             throw new DxfParserException("BLOCK_RECORD entry is missing a name (group code 2). Current code: " + aktuellenGroupCode);
        }
    }

    private void parseAndAttachReactors(DxfEntity entity) throws IOException, DxfParserException {
        if (!(entity instanceof com.cad.dxflib.common.AbstractDxfEntity)) {
            if (aktuellenGroupCode != null && aktuellenGroupCode.code == 102 && "{ACAD_REACTORS".equals(aktuellenGroupCode.value)) {
                 aktuellenGroupCode = nextGroupCode();
                 while(aktuellenGroupCode != null && !(aktuellenGroupCode.code == 102 && "}".equals(aktuellenGroupCode.value))) {
                    aktuellenGroupCode = nextGroupCode();
                 }
                 if (aktuellenGroupCode != null && aktuellenGroupCode.code == 102 && "}".equals(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode();
                 }
            }
            return;
        }
        com.cad.dxflib.common.AbstractDxfEntity abstractEntity = (com.cad.dxflib.common.AbstractDxfEntity) entity;

        if (aktuellenGroupCode != null && aktuellenGroupCode.code == 102 && "{ACAD_REACTORS".equals(aktuellenGroupCode.value)) {
            aktuellenGroupCode = nextGroupCode();

            while (aktuellenGroupCode != null && aktuellenGroupCode.code != 0) {
                if (aktuellenGroupCode.code == 102 && "}".equals(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode();
                    break;
                }
                if (aktuellenGroupCode.code == 330 || aktuellenGroupCode.code == 360) {
                    abstractEntity.addReactorHandle(aktuellenGroupCode.value);
                }
                aktuellenGroupCode = nextGroupCode();
            }
        }
    }

    private void parseAndAttachXData(DxfEntity entity) throws IOException, DxfParserException {
        if (!(entity instanceof com.cad.dxflib.common.AbstractDxfEntity)) {
            if (aktuellenGroupCode != null && aktuellenGroupCode.code == 1001) {
                while (aktuellenGroupCode != null && aktuellenGroupCode.code != 0) {
                     if (aktuellenGroupCode.code == 1001) {
                        aktuellenGroupCode = nextGroupCode();
                     }
                     while (aktuellenGroupCode != null && aktuellenGroupCode.code != 0 && aktuellenGroupCode.code != 1001) {
                        aktuellenGroupCode = nextGroupCode();
                     }
                     if (aktuellenGroupCode == null || aktuellenGroupCode.code == 0) break;
                }
            }
            return;
        }

        com.cad.dxflib.common.AbstractDxfEntity abstractEntity = (com.cad.dxflib.common.AbstractDxfEntity) entity;
        // Loop to capture multiple XDATA application names if present
        while (aktuellenGroupCode != null && aktuellenGroupCode.code == 1001) {
            String appName = aktuellenGroupCode.value;
            List<DxfGroupCode> xdataList = new ArrayList<>();

            // Read the first actual XDATA group code
            aktuellenGroupCode = nextGroupCode();

            while (aktuellenGroupCode != null && aktuellenGroupCode.code != 0 && aktuellenGroupCode.code != 1001) {
                xdataList.add(new DxfGroupCode(aktuellenGroupCode.code, aktuellenGroupCode.value)); // Store a copy
                aktuellenGroupCode = nextGroupCode();
            }
            abstractEntity.addXData(appName, xdataList);
            // If aktuellenGroupCode is 1001, the outer while loop in this method will handle the next appName.
            // If it's 0 or null, this method (and the calling entity parsing method) will exit.
        }
    }

    private DxfEntity parseSplineEntity() throws IOException, DxfParserException {
        DxfSpline spline = new DxfSpline();
        double normalX=0, normalY=0, normalZ=0; // Defaulting to 0,0,1 will be handled if no codes are read
        double cpX=0, cpY=0, cpZ=0;
        double fpX=0, fpY=0, fpZ=0;
        boolean normalRead = false;
        boolean cpXRead = false, cpYRead = false; // Z completes the point
        boolean fpXRead = false, fpYRead = false; // Z completes the point


        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 8: spline.setLayerName(aktuellenGroupCode.value); break;
                case 62: spline.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;

                case 210: normalX = Double.parseDouble(aktuellenGroupCode.value); normalRead = true; break;
                case 220: normalY = Double.parseDouble(aktuellenGroupCode.value); normalRead = true; break;
                case 230: normalZ = Double.parseDouble(aktuellenGroupCode.value); normalRead = true; break;

                case 70: spline.setFlags(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 71: spline.setDegree(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 72: spline.setNumberOfKnots(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 73: spline.setNumberOfControlPoints(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 74: spline.setNumberOfFitPoints(Integer.parseInt(aktuellenGroupCode.value)); break;

                case 40: spline.addKnot(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 42: spline.setKnotTolerance(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 43: spline.setControlPointTolerance(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 44: spline.setFitTolerance(Double.parseDouble(aktuellenGroupCode.value)); break;

                case 10: cpX = Double.parseDouble(aktuellenGroupCode.value); cpXRead = true; break;
                case 20: cpY = Double.parseDouble(aktuellenGroupCode.value); cpYRead = true; break;
                case 30:
                    cpZ = Double.parseDouble(aktuellenGroupCode.value);
                    spline.addControlPoint(new Point3D(cpX, cpY, cpZ));
                    cpXRead = false; cpYRead = false; // Reset for next point
                    break;

                case 11: fpX = Double.parseDouble(aktuellenGroupCode.value); fpXRead = true; break;
                case 21: fpY = Double.parseDouble(aktuellenGroupCode.value); fpYRead = true; break;
                case 31:
                    fpZ = Double.parseDouble(aktuellenGroupCode.value);
                    spline.addFitPoint(new Point3D(fpX, fpY, fpZ));
                    fpXRead = false; fpYRead = false; // Reset for next point
                    break;
                default:
                    break;
            }
        }
        if(normalRead) spline.setNormalVector(new Point3D(normalX, normalY, normalZ));
        // else it keeps the default (0,0,1)
        // Basic validation after parsing all codes for the spline
        if (spline.getNumberOfKnots() > 0 && spline.getKnots().size() != spline.getNumberOfKnots()) {
            // System.out.println("Warning: SPLINE knot count mismatch. Expected " + spline.getNumberOfKnots() + ", found " + spline.getKnots().size());
        }
        if (spline.getNumberOfControlPoints() > 0 && spline.getControlPoints().size() != spline.getNumberOfControlPoints()) {
            // System.out.println("Warning: SPLINE control point count mismatch. Expected " + spline.getNumberOfControlPoints() + ", found " + spline.getControlPoints().size());
        }
        if (spline.getNumberOfFitPoints() > 0 && spline.getFitPoints().size() != spline.getNumberOfFitPoints()) {
            // System.out.println("Warning: SPLINE fit point count mismatch. Expected " + spline.getNumberOfFitPoints() + ", found " + spline.getFitPoints().size());
        }
        parseAndAttachXData(spline);
        parseAndAttachReactors(spline);
        return spline;
    }
}

[end of dxflib/src/main/java/com/cad/dxflib/parser/DxfParser.java]
