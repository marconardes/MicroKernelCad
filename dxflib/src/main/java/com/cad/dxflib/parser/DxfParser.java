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
import com.cad.dxflib.entities.DxfSpline; // Added DxfSpline import
import com.cad.dxflib.entities.DxfText;
import com.cad.dxflib.structure.DxfBlock;
import com.cad.dxflib.structure.DxfDimStyle; // NOVA ADIÇÃO
import com.cad.dxflib.structure.DxfDocument;
import com.cad.dxflib.structure.DxfLayer;
import com.cad.dxflib.structure.DxfLinetype; // Added
import com.cad.dxflib.structure.DxfTextStyle; // Added DxfTextStyle import
import com.cad.dxflib.structure.DxfBlockRecord; // Added DxfBlockRecord import
import com.cad.dxflib.objects.DxfDictionary; // Added DxfDictionary import
import com.cad.dxflib.objects.DxfScale; // Added DxfScale import
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
                        } else if ("OBJECTS".equals(currentSection)) { // Added OBJECTS section
                            parseObjectsSection();
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

    private void parseObjectsSection() throws IOException, DxfParserException {
        aktuellenGroupCode = nextGroupCode(); // Should be the first 0/ObjectType or 0/ENDSEC
        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDSEC".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode(); // Consume ENDSEC
                    currentSection = null;
                    return;
                }

                String objectType = aktuellenGroupCode.value.toUpperCase(Locale.ROOT);
                if ("DICTIONARY".equalsIgnoreCase(objectType)) {
                    parseDictionaryObject();
                    // parseDictionaryObject consumes its codes and positions
                    // aktuellenGroupCode to the next 0/ObjectType or 0/ENDSEC
                } else if ("SCALE".equalsIgnoreCase(objectType)) {
                    parseScaleObject();
                }
                // Add other specific object parsers here
                else {
                    // System.out.println("Found unknown object type: " + objectType + ", consuming generically.");
                    consumeGenericObject(objectType);
                }
            } else {
                // This state should not be reached if objects are parsed correctly.
                throw new DxfParserException("Unexpected non-zero group code " + aktuellenGroupCode + " at start of object in OBJECTS section.");
            }
        }
        throw new DxfParserException("Premature EOF in OBJECTS section.");
    }

    private DxfDictionary parseDictionaryObject() throws IOException, DxfParserException {
        DxfDictionary dict = new DxfDictionary();
        String currentEntryName = null;

        // The first code after 0/DICTIONARY is usually its handle (5) or subclass marker (100)
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 5: // Handle of the dictionary itself
                    dict.setHandle(aktuellenGroupCode.value);
                    break;
                case 100: // Subclass marker (e.g., AcDbDictionary)
                    // Can be used for validation if needed: if (!"AcDbDictionary".equals(aktuellenGroupCode.value)) { throw... }
                    break;
                case 280: // Cloning flag
                    dict.setCloningFlag(Integer.parseInt(aktuellenGroupCode.value));
                    break;
                case 281: // Hard owner flag
                    dict.setHardOwner("1".equals(aktuellenGroupCode.value));
                    break;
                case 330: // Owner handle
                    dict.setOwnerHandle(aktuellenGroupCode.value);
                    break;
                case 3: // Entry name
                    currentEntryName = aktuellenGroupCode.value;
                    break;
                case 350: // Entry object handle (common)
                case 360: // Entry object handle (for some specific objects like MLSTYLE)
                case 340: // Entry object handle (less common for dictionary entries, but possible)
                    if (currentEntryName != null) {
                        dict.addEntry(currentEntryName, aktuellenGroupCode.value);
                        currentEntryName = null; // Reset for the next entry
                    } else {
                        throw new DxfParserException("Dictionary entry handle " + aktuellenGroupCode.value + " found without preceding name (code 3).");
                    }
                    break;
                default:
                    // System.out.println("Unhandled group code for DICTIONARY: " + aktuellenGroupCode.code + " = " + aktuellenGroupCode.value);
                    break;
            }
        }

        if (dict.getHandle() == null) {
            // While DXF spec allows anonymous dictionaries in some contexts (not typical for named objects dict),
            // we generally expect a handle for dictionaries managed in the OBJECTS section.
            // For now, we'll log or throw if a dictionary in OBJECTS section has no handle.
            // A handle might be assigned by AutoCAD if not present, but we need it for storage.
             System.err.println("Warning: Parsed a DICTIONARY object without a handle (code 5).");
            // Potentially, we could generate a temporary handle if strictly needed for internal map storage,
            // but it's better if the DXF provides it.
        }

        // Add to document's generic objects map by its handle.
        // Specific named dictionaries (like ACAD_SCALELIST) might be put into document.dictionaries by name
        // if their name is known from a higher-level reference (e.g. header variable).
        // For now, all dictionaries parsed here are added by their handle.
        if (dict.getHandle() != null) {
             document.addDictionary(dict.getHandle(), dict); // Store by handle
             document.addObject(dict.getHandle(), dict); // Also add to generic objects
        }


        // aktuellenGroupCode is now the 0 code for the next object or ENDSEC
        return dict;
    }

    private void consumeGenericObject(String objectType) throws IOException, DxfParserException {
        // System.out.println("Consuming generic object of type: " + objectType);
        // Read and discard codes until the next 0-group code, which marks the start of a new object or ENDSEC.
        // The initial 0/objectType code has already been read by the caller.
        String handle = null;
        String ownerHandle = null;

        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            if(aktuellenGroupCode.code == 5) handle = aktuellenGroupCode.value;
            if(aktuellenGroupCode.code == 330) ownerHandle = aktuellenGroupCode.value;
            // Just consume other codes for now
        }
        // If a handle was found, we could potentially store this "unknown" object by its handle
        // in DxfDocument.genericObjects for later inspection or if it's referenced elsewhere.
        if (handle != null) {
            // System.out.println("Generic object " + objectType + " with handle " + handle + " consumed.");
            // For now, we don't create a specific object for it, but DxfDocument.addObject could store it.
            // Example: document.addObject(handle, new DxfGenericObjectPlaceholder(objectType, handle, ownerHandle));
        }
        // aktuellenGroupCode is now the 0 code for the next object or ENDSEC
    }

    private DxfScale parseScaleObject() throws IOException, DxfParserException {
        DxfScale scale = new DxfScale();

        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 5: // Handle
                    scale.setHandle(aktuellenGroupCode.value);
                    break;
                case 100: // Subclass marker (e.g., AcDbScale)
                    // Validate if necessary: if (!"AcDbScale".equals(aktuellenGroupCode.value)) { throw ... }
                    break;
                case 330: // Owner handle (typically a DxfDictionary like ACAD_SCALELIST)
                    scale.setOwnerHandle(aktuellenGroupCode.value);
                    break;
                case 300: // Name of the scale (e.g., "1:1", "1:100")
                    scale.setName(aktuellenGroupCode.value);
                    break;
                case 140: // Paper units
                    scale.setPaperUnits(Double.parseDouble(aktuellenGroupCode.value));
                    break;
                case 141: // Drawing units
                    scale.setDrawingUnits(Double.parseDouble(aktuellenGroupCode.value));
                    break;
                case 290: // Is unit scale flag
                    scale.setUnitScale("1".equals(aktuellenGroupCode.value) || "true".equalsIgnoreCase(aktuellenGroupCode.value));
                    break;
                case 70: // Flags (obsolete)
                    scale.setFlags(Integer.parseInt(aktuellenGroupCode.value));
                    break;
                default:
                    // System.out.println("Unhandled group code for SCALE object: " + aktuellenGroupCode.code + " = " + aktuellenGroupCode.value);
                    break;
            }
        }

        if (scale.getHandle() == null) {
            System.err.println("Warning: Parsed a SCALE object without a handle (code 5).");
            // Consider how to handle this; for now, it might not be added to maps if handle is primary key.
        } else {
            document.addScale(scale); // Add to the specific scales map in DxfDocument
            document.addObject(scale.getHandle(), scale); // Also add to generic objects map
        }

        // aktuellenGroupCode is now the 0 code for the next object or ENDSEC
        return scale;
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
                        } else if ("STYLE".equals(tableName)) { // ADDED FOR TEXTSTYLE
                            parseStyleTable();
                        } else if ("BLOCK_RECORD".equalsIgnoreCase(tableName)) { // ADDED FOR BLOCK_RECORD
                            parseBlockRecordTable();
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
                    DxfEntity entity = null;
                    if ("LINE".equals(entityType)) entity = parseLineEntity();
                    else if ("CIRCLE".equals(entityType)) entity = parseCircleEntity();
                    else if ("ARC".equals(entityType)) entity = parseArcEntity();
                    else if ("LWPOLYLINE".equals(entityType)) entity = parseLwPolylineEntity();
                    else if ("TEXT".equals(entityType)) entity = parseTextEntity();
                    else if ("INSERT".equals(entityType)) entity = parseInsertEntity();
                    else if ("DIMENSION".equalsIgnoreCase(entityType)) entity = parseDimensionEntity();
                    else if ("SPLINE".equalsIgnoreCase(entityType)) entity = parseSplineEntity(); // Added SPLINE
                    else consumeUnknownEntity(); // consumeUnknownEntity will advance aktuellenGroupCode

                    if (entity != null) {
                        currentBlock.addEntity(entity);
                    }
                }
            } else {
                // This case should ideally not be reached if entity parsing methods correctly consume all their codes
                // and position aktuellenGroupCode to the next 0-code.
                // If it's reached, it means there's unexpected non-0 code after an entity within a block.
                throw new DxfParserException("Unexpected non-zero group code " + aktuellenGroupCode + " within BLOCK definition '" + blockName +"', expected 0 for next entity or ENDBLK.");
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
                }

                DxfEntity entity = null;
                String entityType = aktuellenGroupCode.value.toUpperCase(Locale.ROOT);

                if ("LINE".equalsIgnoreCase(entityType)) entity = parseLineEntity();
                else if ("CIRCLE".equalsIgnoreCase(entityType)) entity = parseCircleEntity();
                else if ("ARC".equalsIgnoreCase(entityType)) entity = parseArcEntity();
                else if ("LWPOLYLINE".equalsIgnoreCase(entityType)) entity = parseLwPolylineEntity();
                else if ("TEXT".equalsIgnoreCase(entityType)) entity = parseTextEntity();
                else if ("INSERT".equalsIgnoreCase(entityType)) entity = parseInsertEntity();
                else if ("DIMENSION".equalsIgnoreCase(entityType)) entity = parseDimensionEntity();
                else if ("SPLINE".equalsIgnoreCase(entityType)) entity = parseSplineEntity(); // Added SPLINE
                else {
                    consumeUnknownEntity(); // Advances aktuellenGroupCode
                    // No entity to add, loop continues
                }

                if (entity != null) {
                    document.addEntity(entity);
                }
                // If consumeUnknownEntity was called, aktuellenGroupCode is already advanced.
                // If a parse<Entity>Entity method was called, it's responsible for advancing aktuellenGroupCode
                // to the code that follows the entity it parsed (which should be the next 0/entityType or 0/ENDSEC).
            } else {
                 // This should not be reached if entities are correctly parsed and consume their group codes.
                throw new DxfParserException("Unexpected non-zero group code " + aktuellenGroupCode + " at start of entity in ENTITIES section.");
            }
        }
         throw new DxfParserException("Premature EOF in ENTITIES section.");
    }

    private DxfLine parseLineEntity() throws IOException, DxfParserException {
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
                default: break; // Ignore other codes
            }
        }
        // aktuellenGroupCode is now the 0 code for the next entity or ENDSEC/ENDBLK
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
                default: break; // Ignore other codes
            }
        }
        return circle;
    }

    private DxfEntity consumeUnknownEntity() throws IOException, DxfParserException {
        // System.out.println("Consuming unknown entity type: " + aktuellenGroupCode.value + " in section " + currentSection);
        while((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            // just consume codes until the next 0-group code
        }
        return null; // Return null as no known entity was parsed
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
                default: break; // Ignore other codes
            }
        }
        return arc;
    }

    private DxfLwPolyline parseLwPolylineEntity() throws IOException, DxfParserException {
        DxfLwPolyline lwpoly = new DxfLwPolyline();
        int vertexCount = 0; // Expected vertex count from code 90
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
            // Potentially log a warning if vertex count from code 90 doesn't match parsed vertices.
            // System.out.println("LWPOLYLINE: Expected " + vertexCount + " vertices, found " + lwpoly.getNumberOfVertices());
        }
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
                default: break; // Ignore other codes
            }
        }
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
                default: break; // Ignore other codes
            }
        }
        if (insert.getBlockName() == null || insert.getBlockName().trim().isEmpty()) {
            throw new DxfParserException("INSERT entity missing block name (group code 2). Current group: " + aktuellenGroupCode);
        }
        return insert;
    }

    private DxfDimension parseDimensionEntity() throws IOException, DxfParserException {
        DxfDimension dimension = new DxfDimension();
        // Initialize points to avoid null checks when setting individual coordinates
        Point3D defPoint = new Point3D(0,0,0); // For codes 10,20,30
        Point3D midTextPoint = new Point3D(0,0,0); // For codes 11,21,31
        Point3D p1 = new Point3D(0,0,0); // For codes 13,23,33
        Point3D p2 = new Point3D(0,0,0); // For codes 14,24,34
        Point3D extrusion = new Point3D(0,0,1); // Default extrusion

        boolean defPointXRead = false, defPointYRead = false, defPointZRead = false;
        boolean midTextXRead = false, midTextYRead = false, midTextZRead = false;
        boolean p1XRead = false, p1YRead = false, p1ZRead = false;
        boolean p2XRead = false, p2YRead = false, p2ZRead = false;
        boolean extrusionXRead = false, extrusionYRead = false, extrusionZRead = false;


        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 8: dimension.setLayerName(aktuellenGroupCode.value); break;
                case 62: dimension.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 2: dimension.setBlockName(aktuellenGroupCode.value); break; // Geometry block name
                case 3: dimension.setDimensionStyleName(aktuellenGroupCode.value); break;
                case 1: dimension.setDimensionText(aktuellenGroupCode.value); break;
                case 70: dimension.setDimensionTypeFlags(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 50: dimension.setRotationAngle(Double.parseDouble(aktuellenGroupCode.value)); break; // For rotated dimensions

                // Definition Point (Code 10, 20, 30) - Also used as Dimension Line Definition Point
                case 10: defPoint.x = Double.parseDouble(aktuellenGroupCode.value); defPointXRead = true; break;
                case 20: defPoint.y = Double.parseDouble(aktuellenGroupCode.value); defPointYRead = true; break;
                case 30: defPoint.z = Double.parseDouble(aktuellenGroupCode.value); defPointZRead = true; break;

                // Middle of Text Point (Code 11, 21, 31)
                case 11: midTextPoint.x = Double.parseDouble(aktuellenGroupCode.value); midTextXRead = true; break;
                case 21: midTextPoint.y = Double.parseDouble(aktuellenGroupCode.value); midTextYRead = true; break;
                case 31: midTextPoint.z = Double.parseDouble(aktuellenGroupCode.value); midTextZRead = true; break;

                // Linear/Aligned Point 1 (Codes 13, 23, 33)
                case 13: p1.x = Double.parseDouble(aktuellenGroupCode.value); p1XRead = true; break;
                case 23: p1.y = Double.parseDouble(aktuellenGroupCode.value); p1YRead = true; break;
                case 33: p1.z = Double.parseDouble(aktuellenGroupCode.value); p1ZRead = true; break;

                // Linear/Aligned Point 2 (Codes 14, 24, 34)
                case 14: p2.x = Double.parseDouble(aktuellenGroupCode.value); p2XRead = true; break;
                case 24: p2.y = Double.parseDouble(aktuellenGroupCode.value); p2YRead = true; break;
                case 34: p2.z = Double.parseDouble(aktuellenGroupCode.value); p2ZRead = true; break;

                // Extrusion Direction (Codes 210, 220, 230)
                case 210: extrusion.x = Double.parseDouble(aktuellenGroupCode.value); extrusionXRead = true; break;
                case 220: extrusion.y = Double.parseDouble(aktuellenGroupCode.value); extrusionYRead = true; break;
                case 230: extrusion.z = Double.parseDouble(aktuellenGroupCode.value); extrusionZRead = true; break;

                // Other codes like 5 (handle), 100 (subclass markers) are ignored for now
                // More specific codes for different dimension types (angular, diameter, radius, ordinate)
                // (e.g., 15,25,35 for angular; 16,26,36 for angular 3pt) would be added here.
                default:
                    // System.out.println("Unhandled DIMENSION group code: " + aktuellenGroupCode.code + " = " + aktuellenGroupCode.value);
                    break;
            }
        }

        if(defPointXRead || defPointYRead || defPointZRead) dimension.setDefinitionPoint(defPoint);
        if(midTextXRead || midTextYRead || midTextZRead) dimension.setMiddleOfTextPoint(midTextPoint);
        if(p1XRead || p1YRead || p1ZRead) dimension.setLinearPoint1(p1);
        if(p2XRead || p2YRead || p2ZRead) dimension.setLinearPoint2(p2);
        if(extrusionXRead || extrusionYRead || extrusionZRead) dimension.setExtrusionDirection(extrusion);
        else dimension.setExtrusionDirection(new Point3D(0,0,1)); // Ensure default if no codes read

        return dimension;
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
        int tempDimtad = 0; // Default from DxfDimStyle (text vertical alignment)
        boolean tempDimtih = true; // Default from DxfDimStyle
        boolean tempDimtoh = true; // Default from DxfDimStyle
        boolean tempDimtofl = false; // Default from DxfDimStyle
        boolean tempDimse1 = false; // Default from DxfDimStyle
        boolean tempDimse2 = false; // Default from DxfDimStyle


        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 2: // DIMSTYLE name
                case 3: // Alternative DIMSTYLE name (less common)
                    dimStyleName = aktuellenGroupCode.value;
                    nameFound = true;
                    style = document.getDimensionStyle(dimStyleName);
                    if (style == null) {
                        style = new DxfDimStyle(dimStyleName);
                    }
                    // Apply any pre-read values if name came later (though less likely with this loop structure)
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
                case 105: // Handle
                    tempHandle = aktuellenGroupCode.value;
                    if (style != null) style.setHandle(tempHandle);
                    break;
                case 70: // Standard flags
                    tempFlags70 = Integer.parseInt(aktuellenGroupCode.value);
                    if (style != null) style.setFlags70(tempFlags70);
                    // DIMSE1, DIMSE2 can be bits in 70 for newer DXF versions
                    // Example: if ( (tempFlags70 & 1) != 0 ) style.setSuppressFirstExtensionLine(true);
                    // Example: if ( (tempFlags70 & 2) != 0 ) style.setSuppressSecondExtensionLine(true);
                    break;
                case 41: // DIMASZ - Arrow size
                    tempDimasz = Double.parseDouble(aktuellenGroupCode.value);
                    if (style != null) style.setArrowSize(tempDimasz);
                    break;
                case 42: // DIMEXO - Extension line offset
                    tempDimexo = Double.parseDouble(aktuellenGroupCode.value);
                    if (style != null) style.setExtensionLineOffset(tempDimexo);
                    break;
                case 43: // DIMEXE (R12) - Extension line extension. AutoCAD seems to use 44 more often in modern DXF.
                case 44: // DIMEXE (Modern) - Extension line extension
                    tempDimexe = Double.parseDouble(aktuellenGroupCode.value);
                    if (style != null) style.setExtensionLineExtension(tempDimexe);
                    break;
                // DIMTXT can be 40 (older R12), 44 (intermediate), or 140 (modern preferred)
                // We will prioritize 140 if seen, then 44, then 40.
                case 40: // DIMTXT - Text height (fallback 2)
                    if (style != null) { // Only if 140 or 44 not yet processed
                        if (style.getTextHeight() == new DxfDimStyle("").getTextHeight()) { // Check if it's still default
                             style.setTextHeight(Double.parseDouble(aktuellenGroupCode.value));
                        }
                    } else {
                        tempDimtxt = Double.parseDouble(aktuellenGroupCode.value);
                    }
                    break;
                // case 44: // DIMTXT - Text height (fallback 1) - Covered by current DxfDimStyle default logic, but we can be more explicit
                //     double val44 = Double.parseDouble(aktuellenGroupCode.value);
                //     if (style != null) {
                //         // If 140 hasn't set it, or if current value is default, use 44
                //         if (style.getTextHeight() == new DxfDimStyle("").getTextHeight() || /* logic to check if 140 was source */) {
                //             style.setTextHeight(val44);
                //         }
                //     } else {
                //         tempDimtxt = val44;
                //     }
                //    break;
                case 140: // DIMTXT - Text height (preferred)
                    tempDimtxt = Double.parseDouble(aktuellenGroupCode.value);
                    if (style != null) style.setTextHeight(tempDimtxt);
                    break;
                case 147: // DIMGAP - Text gap (preferred)
                    tempDimgap = Double.parseDouble(aktuellenGroupCode.value);
                    if (style != null) style.setTextGap(tempDimgap);
                    break;
                case 48: // DIMGAP (fallback 1)
                case 278: // DIMGAP (fallback 2) - Sometimes value is color index, handle with care
                    try {
                        double gapFallback = Double.parseDouble(aktuellenGroupCode.value);
                        if (style != null) {
                            if (style.getTextGap() == new DxfDimStyle("").getTextGap()) { // Only if 147 not processed
                                style.setTextGap(gapFallback);
                            }
                        } else {
                             // tempDimgap should be updated only if 147 was not seen.
                             // This requires more state or setting it and letting 147 overwrite. For now, simple overwrite:
                            tempDimgap = gapFallback;
                        }
                    } catch (NumberFormatException e) { /* ignore if not a double, e.g. for 278 as color */ }
                    break;
                case 176: // DIMCLRD - Dimension line color
                    tempDimclrd = Integer.parseInt(aktuellenGroupCode.value);
                    if (style != null) style.setDimensionLineColor(tempDimclrd);
                    break;
                case 177: // DIMCLRE - Extension line color
                    tempDimclre = Integer.parseInt(aktuellenGroupCode.value);
                    if (style != null) style.setExtensionLineColor(tempDimclre);
                    break;
                case 178: // DIMCLRT - Text color
                    tempDimclrt = Integer.parseInt(aktuellenGroupCode.value);
                    if (style != null) style.setTextColor(tempDimclrt);
                    break;
                case 271: // DIMDEC - Decimal places (for linear dimensions)
                    tempDimdec = Integer.parseInt(aktuellenGroupCode.value);
                    if (style != null) style.setDecimalPlaces(tempDimdec);
                    break;
                case 1:   // DIMBLK - Arrow block name (obsolete but might appear)
                case 342: // DIMBLK - Arrow block name (preferred)
                    tempDimblkName = aktuellenGroupCode.value;
                    if (style != null) style.setDimBlkName(tempDimblkName);
                    break;
                case 340: // DIMTXSTY - Text style name (handle to STYLE object)
                    tempDimtxstyName = aktuellenGroupCode.value;
                    if (style != null) style.setTextStyleName(tempDimtxstyName);
                    break;
                case 77: // DIMTAD - Text vertical alignment (0=center, 1=above, 2=os, 3=jis, 4=below)
                    tempDimtad = Integer.parseInt(aktuellenGroupCode.value);
                    if (style != null) style.setTextVerticalAlignment(tempDimtad);
                    break;
                case 73: // DIMTIH - Text inside horizontal (1=true, 0=false)
                    tempDimtih = !"0".equals(aktuellenGroupCode.value);
                    if (style != null) style.setTextInsideHorizontal(tempDimtih);
                    break;
                case 74: // DIMTOH - Text outside horizontal (1=true, 0=false)
                    tempDimtoh = !"0".equals(aktuellenGroupCode.value);
                    if (style != null) style.setTextOutsideHorizontal(tempDimtoh);
                    break;
                case 172: // DIMTOFL - Text outside extensions if it doesn't fit (1=true, 0=false)
                    tempDimtofl = !"0".equals(aktuellenGroupCode.value);
                    if (style != null) style.setTextOutsideExtensions(tempDimtofl);
                    break;
                case 75: // DIMSE1 - Suppress first extension line (R12) (1=true, 0=false)
                    tempDimse1 = !"0".equals(aktuellenGroupCode.value);
                    if (style != null) style.setSuppressFirstExtensionLine(tempDimse1);
                    break;
                case 76: // DIMSE2 - Suppress second extension line (R12) (1=true, 0=false)
                    tempDimse2 = !"0".equals(aktuellenGroupCode.value);
                    if (style != null) style.setSuppressSecondExtensionLine(tempDimse2);
                    break;
                // Add more cases for other DIMSTYLE variables as needed:
                // DIMLWD (7), DIMLWE (370), DIMSCALE (40, but usually a header var), DIMLFAC (143)
                // DIMTVP (145), DIMTIX (179), DIMSOXD (171), etc.
                default:
                    // System.out.println("Unhandled/Ignored DIMSTYLE group code: " + aktuellenGroupCode.code + " = " + aktuellenGroupCode.value);
                    break;
            }
        }

        if (style != null) {
            document.addDimensionStyle(style); // addDimensionStyle handles overwriting if name exists
        } else if (nameFound) {
            // This means a name was found, but the style object somehow wasn't created or retrieved.
            // This should ideally be caught by the logic within the loop when name is parsed.
            throw new DxfParserException("DIMSTYLE with name '" + dimStyleName + "' found, but DxfDimStyle object was not properly initialized/retrieved.");
        }
        // If nameFound is false, it implies a DIMSTYLE entry without a name (code 2 or 3).
        // This is generally invalid for named table entries.
        if (!nameFound && aktuellenGroupCode != null && !"ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value)) {
             throw new DxfParserException("DIMSTYLE entry is missing a name (group code 2 or 3). Current code: " + aktuellenGroupCode);
        }
        if (aktuellenGroupCode == null && nameFound == false && !"DIMSTYLE".equalsIgnoreCase(currentSection)) {
             // Premature EOF
        }
        // aktuellenGroupCode is already positioned at the next 0 code or is null by the loop condition.
    }

    private void parseStyleTable() throws IOException, DxfParserException {
        // Consume table header codes until the first "0" "STYLE" or "0" "ENDTAB"
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            // Ignoring table-specific header codes like 70 (max number of entries)
        }

        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    return; // End of STYLE table
                } else if ("STYLE".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    parseSingleTextStyleEntry();
                    // parseSingleTextStyleEntry advances aktuellenGroupCode to the next 0 code
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

        // Default values according to DXF specification or common practice
        double fixedTextHeight = 0.0; // Code 40
        double widthFactor = 1.0;     // Code 41
        double obliqueAngle = 0.0;    // Code 50
        int textGenerationFlags = 0;  // Code 71
        double lastHeightUsed = 0.0;  // Code 42 (often not critical for parsing definition)
        String primaryFontFileName = ""; // Code 3
        String bigFontFileName = null;   // Code 4 (optional)
        int flags70 = 0;                 // Code 70 (standard flags)

        boolean nameFound = false;

        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 2: // Style name
                    styleName = aktuellenGroupCode.value;
                    nameFound = true;
                    textStyle = new DxfTextStyle(styleName);
                    break;
                case 3: // Primary font file name
                    primaryFontFileName = aktuellenGroupCode.value;
                    if (textStyle != null) textStyle.setPrimaryFontFileName(primaryFontFileName);
                    break;
                case 4: // Big font file name (optional)
                    bigFontFileName = aktuellenGroupCode.value;
                    if (textStyle != null) textStyle.setBigFontFileName(bigFontFileName);
                    break;
                case 40: // Fixed text height
                    fixedTextHeight = Double.parseDouble(aktuellenGroupCode.value);
                    if (textStyle != null) textStyle.setFixedTextHeight(fixedTextHeight);
                    break;
                case 41: // Width factor
                    widthFactor = Double.parseDouble(aktuellenGroupCode.value);
                    if (textStyle != null) textStyle.setWidthFactor(widthFactor);
                    break;
                case 42: // Last height used
                    lastHeightUsed = Double.parseDouble(aktuellenGroupCode.value);
                    if (textStyle != null) textStyle.setLastHeightUsed(lastHeightUsed);
                    break;
                case 50: // Oblique angle
                    obliqueAngle = Double.parseDouble(aktuellenGroupCode.value);
                    if (textStyle != null) textStyle.setObliqueAngle(obliqueAngle);
                    break;
                case 70: // Standard flags
                    flags70 = Integer.parseInt(aktuellenGroupCode.value);
                    if (textStyle != null) textStyle.setFlags(flags70);
                    break;
                case 71: // Text generation flags
                    textGenerationFlags = Integer.parseInt(aktuellenGroupCode.value);
                    if (textStyle != null) textStyle.setTextGenerationFlags(textGenerationFlags);
                    break;
                // Codes to ignore for now:
                // case 100: // Subclass marker AcDbTextStyleTableRecord
                // case 1071: // Extended font data (less common)
                // case 5: // Handle (usually not needed for basic style definition)
                // case 330: // Soft-pointer ID to owner dictionary
                default:
                    // Optionally log unhandled group codes for STYLE if debugging
                    // System.out.println("Unhandled group code for STYLE: " + aktuellenGroupCode.code + " = " + aktuellenGroupCode.value);
                    break;
            }
        }

        if (textStyle != null) { // If the style was instantiated (name was found)
            // Ensure all parsed values are set, especially if they were read before the name
            textStyle.setPrimaryFontFileName(primaryFontFileName);
            if (bigFontFileName != null) textStyle.setBigFontFileName(bigFontFileName);
            textStyle.setFixedTextHeight(fixedTextHeight);
            textStyle.setWidthFactor(widthFactor);
            textStyle.setLastHeightUsed(lastHeightUsed);
            textStyle.setObliqueAngle(obliqueAngle);
            textStyle.setFlags(flags70);
            textStyle.setTextGenerationFlags(textGenerationFlags);

            document.addTextStyle(textStyle);
        } else if (nameFound) {
            // This state should ideally not be reached if nameFound implies textStyle is not null.
            // However, as a safeguard:
            throw new DxfParserException("STYLE with name '" + styleName + "' found, but DxfTextStyle object was not properly initialized.");
        }
        // If nameFound is false, it means a STYLE entry was encountered without a name (code 2).
        // This is invalid according to DXF standards for named styles.
        // aktuellenGroupCode is now positioned at the next 0 code (e.g., 0/STYLE or 0/ENDTAB) or is null.
        if (!nameFound && aktuellenGroupCode != null && !"ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value)) {
             // Only throw if it's not the end of the table and we genuinely expected a name for a style entry.
             // If aktuellenGroupCode is null here, it means premature EOF.
             // If aktuellenGroupCode is 0/ENDTAB, it's just the end of the table.
             // This check is a bit lenient; ideally, every 0/STYLE should be followed by a name.
        }
         if (aktuellenGroupCode == null && nameFound == false && !"STYLE".equalsIgnoreCase(currentSection)) {
            // If we are in the middle of parsing and hit EOF without a name, it's an error.
            // The check for currentSection ensures we are not at a global EOF.
            // This condition might be too complex or might need refinement based on strictness.
            // For now, the primary check is that if a textStyle object exists, it's added.
        }
    }

    private void parseBlockRecordTable() throws IOException, DxfParserException {
        // Consume table header codes until the first "0" "BLOCK_RECORD" or "0" "ENDTAB"
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            // Ignoring table-specific header codes like 70 (max number of entries), 100 (AcDbSymbolTable)
        }

        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    return; // End of BLOCK_RECORD table
                } else if ("BLOCK_RECORD".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    parseSingleBlockRecordEntry();
                    // parseSingleBlockRecordEntry advances aktuellenGroupCode to the next 0 code
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
                case 2: // Block record name
                    blockRecordName = aktuellenGroupCode.value;
                    nameFound = true;
                    // Check if a record with this name already exists (e.g. default *Model_Space)
                    blockRecord = document.getBlockRecord(blockRecordName);
                    if (blockRecord == null) {
                        blockRecord = new DxfBlockRecord(blockRecordName);
                    } else {
                        // If it exists, we will just populate its other fields if available
                    }
                    break;
                case 5: // Handle
                    handle = aktuellenGroupCode.value;
                    if (blockRecord != null) blockRecord.setHandle(handle);
                    break;
                case 330: // Soft pointer to owner dictionary (often parent dictionary of table)
                    ownerHandle = aktuellenGroupCode.value;
                    if (blockRecord != null) blockRecord.setOwnerDictionaryHandle(ownerHandle);
                    break;
                case 340: // Hard pointer to layout object
                    layoutHandle = aktuellenGroupCode.value;
                    if (blockRecord != null) blockRecord.setLayoutHandle(layoutHandle);
                    break;
                case 1: // Xref path name (only for XREF block records)
                    xrefPath = aktuellenGroupCode.value;
                    if (blockRecord != null) blockRecord.setXrefPathName(xrefPath);
                    break;
                // Codes to ignore for now:
                // case 100: // Subclass marker (AcDbSymbolTableRecord, AcDbBlockTableRecord)
                // case 70: // Block explodability and scalability flags (not directly used for structure)
                // case 280: // Block insertion units (not used for basic definition)
                // case 281: // Block explodability (0 = not explodable, 1 = explodable)
                // case 310: // Binary data for preview icon (optional)
                // case 1001, 1000, 1002, etc. (XDATA)
                default:
                    // Optionally log unhandled group codes for BLOCK_RECORD
                    // System.out.println("Unhandled group code for BLOCK_RECORD: " + aktuellenGroupCode.code + " = " + aktuellenGroupCode.value);
                    break;
            }
        }

        if (blockRecord != null) {
            // Ensure all fields are set if they were read before the name was (though unlikely with current structure)
            if (handle != null) blockRecord.setHandle(handle);
            if (ownerHandle != null) blockRecord.setOwnerDictionaryHandle(ownerHandle);
            if (layoutHandle != null) blockRecord.setLayoutHandle(layoutHandle);
            if (xrefPath != null) blockRecord.setXrefPathName(xrefPath);

            document.addBlockRecord(blockRecord);
        } else if (nameFound) {
            // This implies a name was found but the object wasn't created, which shouldn't happen.
            throw new DxfParserException("BLOCK_RECORD with name '" + blockRecordName + "' found, but DxfBlockRecord object was not properly initialized.");
        }
        // If nameFound is false, it means a BLOCK_RECORD entry was encountered without a name (code 2).
        // This is invalid.
        if (!nameFound && aktuellenGroupCode != null && !"ENDTAB".equalsIgnoreCase(aktuellenGroupCode.value)) {
            // This condition is to catch entries that are not ENDTAB but also lack a name.
            // A more robust check might be needed if some BLOCK_RECORD entries can be anonymous (unlikely for named table records).
             throw new DxfParserException("BLOCK_RECORD entry is missing a name (group code 2). Current code: " + aktuellenGroupCode);
        }
         if (aktuellenGroupCode == null && nameFound == false && !"BLOCK_RECORD".equalsIgnoreCase(currentSection)) {
             // Premature EOF in the middle of a block record definition
             // The currentSection check might be redundant if we are inside parseSingleBlockRecordEntry
         }
        // aktuellenGroupCode is now positioned at the next 0 code or is null.
    }

    private DxfEntity parseSplineEntity() throws IOException, DxfParserException {
        DxfSpline spline = new DxfSpline();
        Point3D currentControlPoint = new Point3D(0,0,0);
        Point3D currentFitPoint = new Point3D(0,0,0);
        boolean normalXread = false, normalYread = false;

        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                // Common entity codes
                case 8: spline.setLayerName(aktuellenGroupCode.value); break;
                case 62: spline.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;
                // Spline specific codes
                case 210:
                    spline.getNormalVector().x = Double.parseDouble(aktuellenGroupCode.value);
                    normalXread = true;
                    break;
                case 220:
                    spline.getNormalVector().y = Double.parseDouble(aktuellenGroupCode.value);
                    normalYread = true;
                    break;
                case 230:
                    spline.getNormalVector().z = Double.parseDouble(aktuellenGroupCode.value);
                    // If only Z is provided, X and Y are assumed 0, which is default.
                    // If X or Y were read, Z completes it.
                    break;
                case 70: spline.setFlags(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 71: spline.setDegree(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 72: spline.setNumberOfKnots(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 73: spline.setNumberOfControlPoints(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 74: spline.setNumberOfFitPoints(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 40: // Knot value
                    spline.addKnot(Double.parseDouble(aktuellenGroupCode.value));
                    break;
                case 42: spline.setKnotTolerance(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 43: spline.setControlPointTolerance(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 44: spline.setFitTolerance(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 10: // Control point X
                    currentControlPoint = new Point3D(Double.parseDouble(aktuellenGroupCode.value), 0, 0);
                    break;
                case 20: // Control point Y
                    currentControlPoint.y = Double.parseDouble(aktuellenGroupCode.value);
                    break;
                case 30: // Control point Z
                    currentControlPoint.z = Double.parseDouble(aktuellenGroupCode.value);
                    spline.addControlPoint(new Point3D(currentControlPoint.x, currentControlPoint.y, currentControlPoint.z));
                    break;
                case 11: // Fit point X
                    currentFitPoint = new Point3D(Double.parseDouble(aktuellenGroupCode.value),0,0);
                    break;
                case 21: // Fit point Y
                    currentFitPoint.y = Double.parseDouble(aktuellenGroupCode.value);
                    break;
                case 31: // Fit point Z
                    currentFitPoint.z = Double.parseDouble(aktuellenGroupCode.value);
                    spline.addFitPoint(new Point3D(currentFitPoint.x, currentFitPoint.y, currentFitPoint.z));
                    break;
                default:
                    // Optionally log unhandled SPLINE group codes
                    // System.out.println("Unhandled SPLINE group code: " + aktuellenGroupCode.code + " = " + aktuellenGroupCode.value);
                    break;
            }
        }
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

        return spline;
    }
}
