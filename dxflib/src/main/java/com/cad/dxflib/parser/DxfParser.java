package com.cad.dxflib.parser;

import com.cad.dxflib.common.DxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point2D; // Para DxfLwPolyline
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.entities.DxfArc;
import com.cad.dxflib.entities.DxfCircle;
import com.cad.dxflib.entities.DxfInsert;
import com.cad.dxflib.entities.DxfLine;
import com.cad.dxflib.entities.DxfLwPolyline;
import com.cad.dxflib.entities.DxfText;
import com.cad.dxflib.structure.DxfDocument;
// import com.cad.dxflib.structure.DxfLayer; // Not explicitly used here, DxfDocument handles it.

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale; // Para toUpperCase(Locale.ROOT)

public class DxfParser {

    private BufferedReader reader;
    private DxfDocument document;
    private DxfGroupCode aktuellenGroupCode;
    private String currentSection;
    // private boolean inSection = false; // Pode ser inferido por currentSection != null

    public DxfDocument parse(InputStream inputStream) throws DxfParserException {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null.");
        }
        this.document = new DxfDocument();
        this.reader = new BufferedReader(new InputStreamReader(inputStream)); // Considerar encoding depois

        try {
            aktuellenGroupCode = nextGroupCode(); // Read first group code
            while (aktuellenGroupCode != null) {
                if (aktuellenGroupCode.code == 0 && "SECTION".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode();
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 2) {
                        currentSection = aktuellenGroupCode.value.toUpperCase(Locale.ROOT);
                        // System.out.println("Entering section: " + currentSection); // Debug
                        if ("ENTITIES".equals(currentSection)) {
                            parseEntitiesSection(); // This will consume ENDSEC and set aktuellenGroupCode to the one AFTER ENDSEC
                        } else if ("HEADER".equals(currentSection)) {
                            consumeSection(); // Consumes until ENDSEC and sets aktuellenGroupCode
                        } else if ("TABLES".equals(currentSection)) {
                            consumeSection();
                        } else if ("BLOCKS".equals(currentSection)) {
                            consumeSection();
                        } else {
                            // Unknown section, consume until ENDSEC
                            consumeSection();
                        }
                        // After a section is parsed/consumed, aktuellenGroupCode is the one AFTER that section's ENDSEC
                        // The loop condition will then re-evaluate.
                        continue; // Ensure we process the group code that followed ENDSEC
                    } else {
                         throw new DxfParserException("Malformed SECTION: expected group code 2 after 0/SECTION, got: " + aktuellenGroupCode);
                    }
                } else if (aktuellenGroupCode.code == 0 && "EOF".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    // System.out.println("End of File reached."); // Debug
                    break;
                } else {
                    // Data outside a section or before first section, or after last ENDSEC before EOF
                    // For robust parsing, might need to decide how to handle this.
                    // For now, just read next.
                    // System.out.println("Orphan group code: " + aktuellenGroupCode); // Debug
                    aktuellenGroupCode = nextGroupCode();
                }
            }
        } catch (IOException e) {
            throw new DxfParserException("Error reading DXF file", e);
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                // Log or handle closing error
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
            return null; // End of stream
        }

        try {
            int code = Integer.parseInt(codeStr.trim());
            return new DxfGroupCode(code, valueStr.trim());
        } catch (NumberFormatException e) {
            throw new DxfParserException("Invalid group code format: '" + codeStr.trim() + "'", e);
        }
    }

    // Helper to consume group codes until the end of a section when not parsing it fully yet
    private void consumeSection() throws IOException, DxfParserException {
        while((aktuellenGroupCode = nextGroupCode()) != null) {
            if(aktuellenGroupCode.code == 0 && "ENDSEC".equalsIgnoreCase(aktuellenGroupCode.value)) {
                aktuellenGroupCode = nextGroupCode(); // Consume the group code after ENDSEC, to position for next section/EOF
                currentSection = null; // We are out of the section
                return;
            }
        }
        throw new DxfParserException("Premature EOF while consuming section: " + currentSection);
    }


    private void parseEntitiesSection() throws IOException, DxfParserException {
        aktuellenGroupCode = nextGroupCode(); // Read the first 0/EntityType or 0/ENDSEC
        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 0) {
                if ("ENDSEC".equalsIgnoreCase(aktuellenGroupCode.value)) {
                    aktuellenGroupCode = nextGroupCode(); // Consume the group code after ENDSEC
                    currentSection = null; // Mark that we are out of a section
                    return; // Return to main loop
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
                    // System.out.println("Skipping entity: " + aktuellenGroupCode.value); // Debug
                    consumeUnknownEntity();
                }
                // After parsing an entity (or consuming an unknown one),
                // aktuellenGroupCode is already set to the next 0/EntityType or 0/ENDSEC
                // by the respective parse/consume methods. So, the loop continues.
            } else {
                throw new DxfParserException("Unexpected group code " + aktuellenGroupCode + " at start of entity in ENTITIES section.");
            }
        }
         throw new DxfParserException("Premature EOF in ENTITIES section.");
    }

    private void parseLineEntity() throws IOException, DxfParserException {
        DxfLine line = new DxfLine();
        // aktuellenGroupCode is currently 0/LINE. Read next for attributes.
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 8: // Layer name
                    line.setLayerName(aktuellenGroupCode.value);
                    break;
                case 10: // Start X
                    line.setStartPoint(new Point3D(Double.parseDouble(aktuellenGroupCode.value), line.getStartPoint().y, line.getStartPoint().z));
                    break;
                case 20: // Start Y
                    line.setStartPoint(new Point3D(line.getStartPoint().x, Double.parseDouble(aktuellenGroupCode.value), line.getStartPoint().z));
                    break;
                case 30: // Start Z
                    line.setStartPoint(new Point3D(line.getStartPoint().x, line.getStartPoint().y, Double.parseDouble(aktuellenGroupCode.value)));
                    break;
                case 11: // End X
                    line.setEndPoint(new Point3D(Double.parseDouble(aktuellenGroupCode.value), line.getEndPoint().y, line.getEndPoint().z));
                    break;
                case 21: // End Y
                    line.setEndPoint(new Point3D(line.getEndPoint().x, Double.parseDouble(aktuellenGroupCode.value), line.getEndPoint().z));
                    break;
                case 31: // End Z
                    line.setEndPoint(new Point3D(line.getEndPoint().x, line.getEndPoint().y, Double.parseDouble(aktuellenGroupCode.value)));
                    break;
                case 62: // Color number
                    line.setColor(Integer.parseInt(aktuellenGroupCode.value));
                    break;
                default:
                    break;
            }
        }
        document.addEntity(line);
        // aktuellenGroupCode is now 0/NextEntityType or 0/ENDSEC or null
    }

    private void parseCircleEntity() throws IOException, DxfParserException {
        DxfCircle circle = new DxfCircle();
         while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 8: // Layer name
                    circle.setLayerName(aktuellenGroupCode.value);
                    break;
                case 10: // Center X
                    circle.setCenter(new Point3D(Double.parseDouble(aktuellenGroupCode.value), circle.getCenter().y, circle.getCenter().z));
                    break;
                case 20: // Center Y
                    circle.setCenter(new Point3D(circle.getCenter().x, Double.parseDouble(aktuellenGroupCode.value), circle.getCenter().z));
                    break;
                case 30: // Center Z
                    circle.setCenter(new Point3D(circle.getCenter().x, circle.getCenter().y, Double.parseDouble(aktuellenGroupCode.value)));
                    break;
                case 40: // Radius
                    circle.setRadius(Double.parseDouble(aktuellenGroupCode.value));
                    break;
                case 62: // Color number
                    circle.setColor(Integer.parseInt(aktuellenGroupCode.value));
                    break;
                default:
                    break;
            }
        }
        document.addEntity(circle);
        // aktuellenGroupCode is now 0/NextEntityType or 0/ENDSEC or null
    }

    private void consumeUnknownEntity() throws IOException, DxfParserException {
        // aktuellenGroupCode is 0/UnknownEntityType. Read until next 0 code or EOF.
        while((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            // just consume
        }
        // aktuellenGroupCode is now 0/NextEntityType or 0/ENDSEC or null
    }

    private void parseArcEntity() throws IOException, DxfParserException {
        DxfArc arc = new DxfArc();
        // aktuellenGroupCode is 0/ARC. Read attributes.
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 8: // Layer name
                    arc.setLayerName(aktuellenGroupCode.value);
                    break;
                case 10: // Center X
                    arc.setCenter(new Point3D(Double.parseDouble(aktuellenGroupCode.value), arc.getCenter().y, arc.getCenter().z));
                    break;
                case 20: // Center Y
                    arc.setCenter(new Point3D(arc.getCenter().x, Double.parseDouble(aktuellenGroupCode.value), arc.getCenter().z));
                    break;
                case 30: // Center Z
                    arc.setCenter(new Point3D(arc.getCenter().x, arc.getCenter().y, Double.parseDouble(aktuellenGroupCode.value)));
                    break;
                case 40: // Radius
                    arc.setRadius(Double.parseDouble(aktuellenGroupCode.value));
                    break;
                case 50: // Start Angle
                    arc.setStartAngle(Double.parseDouble(aktuellenGroupCode.value));
                    break;
                case 51: // End Angle
                    arc.setEndAngle(Double.parseDouble(aktuellenGroupCode.value));
                    break;
                case 62: // Color number
                    arc.setColor(Integer.parseInt(aktuellenGroupCode.value));
                    break;
                default:
                    break;
            }
        }
        document.addEntity(arc);
    }

    private void parseLwPolylineEntity() throws IOException, DxfParserException {
        DxfLwPolyline lwpoly = new DxfLwPolyline();
        int vertexCount = 0; // Expected number of vertices if code 90 is present
        double currentX = 0, currentY = 0; // Temporary holders for vertex coords
        boolean xRead = false;

        // aktuellenGroupCode is 0/LWPOLYLINE. Read attributes.
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 8: // Layer name
                    lwpoly.setLayerName(aktuellenGroupCode.value);
                    break;
                case 90: // Number of vertices
                    vertexCount = Integer.parseInt(aktuellenGroupCode.value);
                    break;
                case 70: // Polyline flag (1 = closed)
                    lwpoly.setClosed((Integer.parseInt(aktuellenGroupCode.value) & 1) == 1);
                    break;
                case 43: // Constant width
                    lwpoly.setConstantWidth(Double.parseDouble(aktuellenGroupCode.value));
                    break;
                case 38: // Elevation (single value for all vertices)
                    lwpoly.setElevation(Double.parseDouble(aktuellenGroupCode.value));
                    break;
                case 10: // Vertex X (repeats for each vertex)
                    currentX = Double.parseDouble(aktuellenGroupCode.value);
                    xRead = true;
                    break;
                case 20: // Vertex Y
                    if (!xRead) throw new DxfParserException("LWPOLYLINE: Y coordinate (20) found without preceding X (10).");
                    currentY = Double.parseDouble(aktuellenGroupCode.value);
                    // Try to read bulge (42)
                    reader.mark(256); // Mark to allow reset
                    DxfGroupCode next = nextGroupCode(); // Peek ahead
                    double bulge = 0.0;
                    if (next != null && next.code == 42) {
                        bulge = Double.parseDouble(next.value);
                        // Bulge consumed, next iteration will read code after bulge
                    } else {
                        // Not a bulge, put 'next' back by making it the current for next loop iteration
                        // This requires the main loop of this method to use 'next' if it's not null from here
                        reader.reset();
                        // aktuellenGroupCode for the next outer loop iteration will be 'next'
                        // This is still tricky; the outer loop's nextGroupCode() will re-read.
                        // The current `aktuellenGroupCode` will be the one for Y.
                        // The `nextGroupCode()` at the top of the while will get the one after Y.
                        // To handle this without a true pushback/peek, we must consume 'next' if it's not a bulge
                        // OR ensure 'aktuellenGroupCode' is set to 'next' before loop continues.
                        // For now, this simplification means if 'next' is not bulge, it becomes current for next outer loop.
                        // This is not ideal. A better way:
                        // After reading Y:
                        // DxfGroupCode potentialBulge = nextGroupCode();
                        // if (potentialBulge != null && potentialBulge.code == 42) { bulge = ...; }
                        // else { aktuellenGroupCode = potentialBulge; /* "push back" by making it current for next iteration */ goto next_entity_property_loop; }
                        // This needs labels or restructuring.
                        // For now, let's use a simpler approach for this step, but acknowledge it's fragile:
                        // Assume if bulge (42) is not next, it's 0.0 and the already-peeked 'next' is the next group code.
                        // This means we need to assign 'next' to 'aktuellenGroupCode' if it wasn't a bulge
                        // and then break this inner switch to let the outer while loop handle it.
                        // This is too complex for a simple switch. Let's simplify the peek:
                        // Read X, then Y. Then attempt to read bulge.
                        // The main while loop will then read the next code.
                        // This means bulge must directly follow Y.
                        // Let's refine:
                        // After reading Y for currentX, currentY:
                        DxfGroupCode peekCode = null;
                        reader.mark(512); // Max 2 lines for code + value, plus some buffer
                        String tempCodeStr = reader.readLine();
                        if (tempCodeStr != null) {
                            String tempValueStr = reader.readLine();
                            if (tempValueStr != null) {
                                try {
                                   peekCode = new DxfGroupCode(Integer.parseInt(tempCodeStr.trim()), tempValueStr.trim());
                                } catch (NumberFormatException e) { /* ignore, not a valid pair */ }
                            }
                        }
                        reader.reset(); // Always reset after peeking

                        if (peekCode != null && peekCode.code == 42) {
                            aktuellenGroupCode = nextGroupCode(); // Consume the bulge code (42)
                            bulge = Double.parseDouble(aktuellenGroupCode.value);
                        }
                        // else bulge remains 0.0
                    }
                    lwpoly.addVertex(new Point2D(currentX, currentY), bulge);
                    xRead = false; // Reset for next vertex
                    break;
                case 62: // Color number
                    lwpoly.setColor(Integer.parseInt(aktuellenGroupCode.value));
                    break;
                default:
                    xRead = false; // If an unexpected code appears, reset xRead
                    break;
            }
        }
        if (vertexCount > 0 && lwpoly.getNumberOfVertices() != vertexCount && lwpoly.getNumberOfVertices() > 0) {
             //System.out.println("LWPOLYLINE: Vertex count mismatch. Expected " + vertexCount + ", got " + lwpoly.getNumberOfVertices());
             // This can happen if group 90 is not reliable or parsing logic for vertices is not complete
        }
        document.addEntity(lwpoly);
    }

    private void parseTextEntity() throws IOException, DxfParserException {
        DxfText text = new DxfText();
        // aktuellenGroupCode is 0/TEXT. Read attributes.
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 1: // Text value
                    text.setTextValue(aktuellenGroupCode.value);
                    break;
                case 7: // Style name
                    text.setStyleName(aktuellenGroupCode.value);
                    break;
                case 8: // Layer name
                    text.setLayerName(aktuellenGroupCode.value);
                    break;
                case 10: // Insertion point X
                    text.setInsertionPoint(new Point3D(Double.parseDouble(aktuellenGroupCode.value), text.getInsertionPoint().y, text.getInsertionPoint().z));
                    break;
                case 20: // Insertion point Y
                    text.setInsertionPoint(new Point3D(text.getInsertionPoint().x, Double.parseDouble(aktuellenGroupCode.value), text.getInsertionPoint().z));
                    break;
                case 30: // Insertion point Z
                    text.setInsertionPoint(new Point3D(text.getInsertionPoint().x, text.getInsertionPoint().y, Double.parseDouble(aktuellenGroupCode.value)));
                    break;
                case 40: // Text height
                    text.setHeight(Double.parseDouble(aktuellenGroupCode.value));
                    break;
                case 50: // Rotation angle
                    text.setRotationAngle(Double.parseDouble(aktuellenGroupCode.value));
                    break;
                case 62: // Color number
                    text.setColor(Integer.parseInt(aktuellenGroupCode.value));
                    break;
                default:
                    break;
            }
        }
        document.addEntity(text);
    }

    private void parseInsertEntity() throws IOException, DxfParserException {
        DxfInsert insert = new DxfInsert();
        // aktuellenGroupCode is 0/INSERT. Read attributes.
        while ((aktuellenGroupCode = nextGroupCode()) != null && aktuellenGroupCode.code != 0) {
            switch (aktuellenGroupCode.code) {
                case 2: // Block name
                    insert.setBlockName(aktuellenGroupCode.value);
                    break;
                case 8: // Layer name
                    insert.setLayerName(aktuellenGroupCode.value);
                    break;
                case 10: // Insertion point X
                    insert.setInsertionPoint(new Point3D(Double.parseDouble(aktuellenGroupCode.value), insert.getInsertionPoint().y, insert.getInsertionPoint().z));
                    break;
                case 20: // Insertion point Y
                    insert.setInsertionPoint(new Point3D(insert.getInsertionPoint().x, Double.parseDouble(aktuellenGroupCode.value), insert.getInsertionPoint().z));
                    break;
                case 30: // Insertion point Z
                    insert.setInsertionPoint(new Point3D(insert.getInsertionPoint().x, insert.getInsertionPoint().y, Double.parseDouble(aktuellenGroupCode.value)));
                    break;
                case 41: // X scale factor
                    insert.setXScale(Double.parseDouble(aktuellenGroupCode.value));
                    break;
                case 42: // Y scale factor
                    insert.setYScale(Double.parseDouble(aktuellenGroupCode.value));
                    break;
                case 50: // Rotation angle
                    insert.setRotationAngle(Double.parseDouble(aktuellenGroupCode.value));
                    break;
                case 62: // Color number
                    insert.setColor(Integer.parseInt(aktuellenGroupCode.value));
                    break;
                default:
                    break;
            }
        }
        if (insert.getBlockName() == null || insert.getBlockName().trim().isEmpty()) {
            throw new DxfParserException("INSERT entity missing block name (group code 2).");
        }
        document.addEntity(insert);
    }
}
