package com.cad.dxflib.parser;

import com.cad.dxflib.common.DxfEntity;
import com.cad.dxflib.common.EntityType;
import com.cad.dxflib.common.Point2D; // For LwPolyline test
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.entities.DxfArc;
import com.cad.dxflib.entities.DxfCircle;
import com.cad.dxflib.entities.DxfInsert;
import com.cad.dxflib.entities.DxfLine;
import com.cad.dxflib.entities.DxfLwPolyline;
import com.cad.dxflib.entities.DxfText;
import com.cad.dxflib.structure.DxfBlock;
import com.cad.dxflib.structure.DxfDocument;
import com.cad.dxflib.structure.DxfLayer;
import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DxfParserTest {

    private InputStream getResourceAsStream(String name) {
        InputStream in = getClass().getResourceAsStream(name);
        if (in == null) {
            // Try with a leading slash if it's not already there, common for getResourceAsStream
            if (!name.startsWith("/")) {
                name = "/" + name;
            }
            in = getClass().getResourceAsStream(name);
            if (in == null) {
                throw new IllegalArgumentException("Resource not found: " + name);
            }
        }
        return in;
    }

    @Test
    void testParseLineSimple() throws DxfParserException {
        DxfParser parser = new DxfParser();
        InputStream inputStream = getResourceAsStream("/dxf/line_simple.dxf");
        DxfDocument doc = parser.parse(inputStream);

        assertNotNull(doc);
        assertEquals(1, doc.getModelSpaceEntities().size());
        DxfEntity entity = doc.getModelSpaceEntities().get(0);
        assertEquals(EntityType.LINE, entity.getType());

        DxfLine line = (DxfLine) entity;
        assertEquals(new Point3D(10.0, 10.0, 0.0), line.getStartPoint());
        assertEquals(new Point3D(20.0, 20.0, 0.0), line.getEndPoint());
        assertEquals("0", line.getLayerName());
        assertEquals(1, line.getColor());
    }

    @Test
    void testParseCircleSimple() throws DxfParserException {
        DxfParser parser = new DxfParser();
        InputStream inputStream = getResourceAsStream("/dxf/circle_simple.dxf");
        DxfDocument doc = parser.parse(inputStream);

        assertNotNull(doc);
        assertEquals(1, doc.getModelSpaceEntities().size());
        DxfEntity entity = doc.getModelSpaceEntities().get(0);
        assertEquals(EntityType.CIRCLE, entity.getType());

        DxfCircle circle = (DxfCircle) entity;
        assertEquals(new Point3D(50.0, 50.0, 0.0), circle.getCenter());
        assertEquals(25.0, circle.getRadius(), 0.001);
        assertEquals("circles_layer", circle.getLayerName());
        assertEquals(3, circle.getColor());
    }

    @Test
    void testParseLineCircleMixed() throws DxfParserException {
        DxfParser parser = new DxfParser();
        InputStream inputStream = getResourceAsStream("/dxf/line_circle_mixed.dxf");
        DxfDocument doc = parser.parse(inputStream);

        assertNotNull(doc);
        List<DxfEntity> entities = doc.getModelSpaceEntities();
        assertEquals(3, entities.size());

        // First Line
        assertTrue(entities.get(0) instanceof DxfLine);
        DxfLine line1 = (DxfLine) entities.get(0);
        assertEquals("lines", line1.getLayerName());
        assertEquals(new Point3D(0.0, 0.0, 0.0), line1.getStartPoint());
        assertEquals(new Point3D(100.0, 100.0, 0.0), line1.getEndPoint());
        assertEquals(256, line1.getColor());

        // Circle
        assertTrue(entities.get(1) instanceof DxfCircle);
        DxfCircle circle = (DxfCircle) entities.get(1);
        assertEquals("circles", circle.getLayerName());
        assertEquals(new Point3D(150.0, 150.0, 0.0), circle.getCenter());
        assertEquals(50.0, circle.getRadius(), 0.001);

        // Second Line
        assertTrue(entities.get(2) instanceof DxfLine);
        DxfLine line2 = (DxfLine) entities.get(2);
        assertEquals("lines", line2.getLayerName());
        assertEquals(new Point3D(200.0, 200.0, 0.0), line2.getStartPoint());
        assertEquals(new Point3D(300.0, 200.0, 0.0), line2.getEndPoint());
        assertEquals(5, line2.getColor());
    }
     @Test
    void testParseEmptyStream() {
        DxfParser parser = new DxfParser();
        InputStream inputStream = getResourceAsStream("/dxf/empty.dxf");
        // For an empty or minimal DXF (just 0/EOF), the loop in parse() might not even start
        // or nextGroupCode() returns null. This should not throw an exception but return an empty document.
        // If it should throw, the test needs to be adjusted.
        // For now, let's expect a non-null document, possibly empty of entities.
        // If the design is that 0/EOF without sections is an error, then assertThrows.
        // The current DxfParser structure with `aktuellenGroupCode = nextGroupCode();` before loop
        // and then checking for EOF inside loop will result in DxfParserException for "0/EOF" if no sections.
        // Let's assume this is the case (throws exception for malformed minimal file).
        assertThrows(DxfParserException.class, () -> {
            parser.parse(inputStream);
        }, "Parsing an empty or malformed DXF (just EOF) should ideally throw DxfParserException or be handled gracefully.");
    }

    @Test
    void testParseMalformedSection() {
         DxfParser parser = new DxfParser();
         InputStream inputStream = getResourceAsStream("/dxf/malformed_section.dxf");
         assertThrows(DxfParserException.class, () -> {
             parser.parse(inputStream);
         });
    }

    @Test
    void testParseArcSimple() throws DxfParserException {
        DxfParser parser = new DxfParser();
        InputStream inputStream = getResourceAsStream("/dxf/arc_simple.dxf");
        DxfDocument doc = parser.parse(inputStream);

        assertNotNull(doc);
        assertEquals(1, doc.getModelSpaceEntities().size());
        DxfEntity entity = doc.getModelSpaceEntities().get(0);
        assertEquals(EntityType.ARC, entity.getType());

        DxfArc arc = (DxfArc) entity;
        assertEquals(new Point3D(10.0, 10.0, 0.0), arc.getCenter());
        assertEquals(5.0, arc.getRadius(), 0.001);
        assertEquals(0.0, arc.getStartAngle(), 0.001);
        assertEquals(180.0, arc.getEndAngle(), 0.001);
        assertEquals("arcs_layer", arc.getLayerName());
        assertEquals(4, arc.getColor());
    }

    @Test
    void testParseLwPolylineSimple() throws DxfParserException {
        DxfParser parser = new DxfParser();
        InputStream inputStream = getResourceAsStream("/dxf/lwpolyline_simple.dxf");
        DxfDocument doc = parser.parse(inputStream);

        assertNotNull(doc);
        assertEquals(1, doc.getModelSpaceEntities().size());
        DxfEntity entity = doc.getModelSpaceEntities().get(0);
        assertEquals(EntityType.LWPOLYLINE, entity.getType());

        DxfLwPolyline poly = (DxfLwPolyline) entity;
        assertEquals("polylines", poly.getLayerName());
        assertTrue(poly.isClosed());
        assertEquals(3, poly.getNumberOfVertices());
        assertEquals(new Point2D(0.0, 0.0), poly.getVertices().get(0));
        assertEquals(new Point2D(10.0, 10.0), poly.getVertices().get(1));
        assertEquals(new Point2D(20.0, 0.0), poly.getVertices().get(2));
        // Assuming parser/entity defaults bulge to 0 if not present in file
        // The DxfLwPolyline's addVertex(Point2D) method adds a default 0.0 bulge.
        // The parser's current logic for LWPOLYLINE for X (10) and Y (20) coordinates
        // will call addVertex(Point2D, bulge), where bulge might be 0.0 if no 42 code was found.
        assertEquals(0.0, poly.getBulges().get(0), 0.001);
        assertEquals(0.0, poly.getBulges().get(1), 0.001);
        assertEquals(0.0, poly.getBulges().get(2), 0.001);
    }

    @Test
    void testParseLwPolylineWithBulge() throws DxfParserException {
        DxfParser parser = new DxfParser();
        InputStream inputStream = getResourceAsStream("/dxf/lwpolyline_with_bulge.dxf");
        DxfDocument doc = parser.parse(inputStream);

        assertNotNull(doc);
        assertEquals(1, doc.getModelSpaceEntities().size());
        DxfLwPolyline poly = (DxfLwPolyline) doc.getModelSpaceEntities().get(0);
        assertEquals("poly_bulge", poly.getLayerName());
        assertFalse(poly.isClosed()); // Flag 70 is 0
        assertEquals(4, poly.getNumberOfVertices());

        assertEquals(new Point2D(0.0, 0.0), poly.getVertices().get(0));
        assertEquals(0.5, poly.getBulges().get(0), 0.001);

        assertEquals(new Point2D(10.0, 10.0), poly.getVertices().get(1));
        assertEquals(0.0, poly.getBulges().get(1), 0.001);

        assertEquals(new Point2D(20.0, 0.0), poly.getVertices().get(2));
        assertEquals(-0.5, poly.getBulges().get(2), 0.001);

        assertEquals(new Point2D(30.0, -10.0), poly.getVertices().get(3));
        // The parser's simplified bulge handling means the last vertex won't have an explicit bulge read after it,
        // so the DxfLwPolyline.addVertex(point, bulge) for the last vertex would have received bulge=0.0.
        assertEquals(0.0, poly.getBulges().get(3), 0.001);
    }

    @Test
    void testParseTextSimple() throws DxfParserException {
        DxfParser parser = new DxfParser();
        InputStream inputStream = getResourceAsStream("/dxf/text_simple.dxf");
        DxfDocument doc = parser.parse(inputStream);

        assertNotNull(doc);
        assertEquals(1, doc.getModelSpaceEntities().size());
        DxfEntity entity = doc.getModelSpaceEntities().get(0);
        assertEquals(EntityType.TEXT, entity.getType());

        DxfText text = (DxfText) entity;
        assertEquals("text_layer", text.getLayerName());
        assertEquals(2, text.getColor());
        assertEquals(new Point3D(5.0, 5.0, 0.0), text.getInsertionPoint());
        assertEquals(2.5, text.getHeight(), 0.001);
        assertEquals("Hello, DXF!", text.getTextValue());
        assertEquals(45.0, text.getRotationAngle(), 0.001);
        assertEquals("ARIAL", text.getStyleName());
    }

    @Test
    void testParseInsertSimple() throws DxfParserException {
        DxfParser parser = new DxfParser();
        InputStream inputStream = getResourceAsStream("/dxf/insert_simple.dxf");
        DxfDocument doc = parser.parse(inputStream);

        assertNotNull(doc);
        assertEquals(1, doc.getModelSpaceEntities().size());
        DxfEntity entity = doc.getModelSpaceEntities().get(0);
        assertEquals(EntityType.INSERT, entity.getType());

        DxfInsert insert = (DxfInsert) entity;
        assertEquals("inserts_layer", insert.getLayerName());
        assertEquals("MY_BLOCK", insert.getBlockName());
        assertEquals(new Point3D(100.0, 100.0, 0.0), insert.getInsertionPoint());
        assertEquals(2.0, insert.getXScale(), 0.001);
        assertEquals(2.0, insert.getYScale(), 0.001);
        assertEquals(15.0, insert.getRotationAngle(), 0.001);
        assertEquals(256, insert.getColor()); // Default BYLAYER
    }

    @Test
    void testParseLayersSimple() throws DxfParserException {
        DxfParser parser = new DxfParser();
        InputStream inputStream = getResourceAsStream("/dxf/layers_simple.dxf");
        DxfDocument doc = parser.parse(inputStream);

        assertNotNull(doc);
        assertNotNull(doc.getLayers());
        // Expects "0", "MyLayer1", "MyLayer2_Off_Red", "MyLayer3_Locked_Blue"
        assertEquals(4, doc.getLayers().size());

        DxfLayer layer0 = doc.getLayer("0");
        assertNotNull(layer0);
        assertEquals("0", layer0.getName());
        assertEquals(7, layer0.getColor());
        assertEquals("CONTINUOUS", layer0.getLinetypeName());
        assertTrue(layer0.isVisible());

        DxfLayer layer1 = doc.getLayer("MyLayer1");
        assertNotNull(layer1);
        assertEquals("MyLayer1", layer1.getName());
        assertEquals(1, layer1.getColor());
        assertEquals("DASHDOT", layer1.getLinetypeName());
        assertTrue(layer1.isVisible());

        DxfLayer layer2 = doc.getLayer("MyLayer2_Off_Red");
        assertNotNull(layer2);
        assertEquals("MyLayer2_Off_Red", layer2.getName());
        assertEquals(1, layer2.getColor()); // Color is stored as positive
        assertEquals("CONTINUOUS", layer2.getLinetypeName());
        assertFalse(layer2.isVisible()); // Visibility is false due to negative color -1

        DxfLayer layer3 = doc.getLayer("MyLayer3_Locked_Blue");
        assertNotNull(layer3);
        assertEquals("MyLayer3_Locked_Blue", layer3.getName());
        assertEquals(5, layer3.getColor());
        assertEquals("HIDDEN", layer3.getLinetypeName());
        assertTrue(layer3.isVisible());
        // TODO: Add getFlags() to DxfLayer and assert ((flags & 4) == 4) for locked state if implemented
    }

    @Test
    void testParseBlocksSimple() throws DxfParserException {
        DxfParser parser = new DxfParser();
        InputStream inputStream = getResourceAsStream("/dxf/blocks_simple.dxf");
        DxfDocument doc = parser.parse(inputStream);

        assertNotNull(doc);
        assertNotNull(doc.getBlocks());
        assertEquals(2, doc.getBlocks().size(), "Should parse two blocks.");

        // Test SimpleSquareBlock
        DxfBlock squareBlock = doc.getBlock("SimpleSquareBlock");
        assertNotNull(squareBlock, "SimpleSquareBlock not found.");
        assertEquals("SimpleSquareBlock", squareBlock.getName());
        assertEquals(new Point3D(0.0, 0.0, 0.0), squareBlock.getBasePoint());
        assertEquals(4, squareBlock.getEntities().size(), "SimpleSquareBlock should have 4 entities.");
        assertTrue(squareBlock.getEntities().get(0) instanceof DxfLine, "First entity in SimpleSquareBlock should be a Line.");
        // Add more detailed checks for entities within the block if necessary

        // Test CircleInBlock
        DxfBlock circleBlock = doc.getBlock("CircleInBlock");
        assertNotNull(circleBlock, "CircleInBlock not found.");
        assertEquals("CircleInBlock", circleBlock.getName());
        assertEquals(new Point3D(5.0, 5.0, 0.0), circleBlock.getBasePoint());
        assertEquals(1, circleBlock.getEntities().size(), "CircleInBlock should have 1 entity.");
        assertTrue(circleBlock.getEntities().get(0) instanceof DxfCircle, "Entity in CircleInBlock should be a Circle.");
        DxfCircle circleInBlock = (DxfCircle) circleBlock.getEntities().get(0);
        assertEquals(new Point3D(5.0,5.0,0.0), circleInBlock.getCenter());
        assertEquals(5.0, circleInBlock.getRadius(), 0.001);

        // Test INSERT entities in ENTITIES section
        assertNotNull(doc.getModelSpaceEntities());
        long insertCount = doc.getModelSpaceEntities().stream().filter(e -> e.getType() == EntityType.INSERT).count();
        assertEquals(2, insertCount, "Should be two INSERT entities in model space.");

        DxfInsert insertSquare = (DxfInsert) doc.getModelSpaceEntities().stream()
            .filter(e -> e.getType() == EntityType.INSERT && "SimpleSquareBlock".equals(((DxfInsert)e).getBlockName()))
            .findFirst().orElse(null);
        assertNotNull(insertSquare);
        assertEquals(new Point3D(50.0, 50.0, 0.0), insertSquare.getInsertionPoint());

        DxfInsert insertCircle = (DxfInsert) doc.getModelSpaceEntities().stream()
            .filter(e -> e.getType() == EntityType.INSERT && "CircleInBlock".equals(((DxfInsert)e).getBlockName()))
            .findFirst().orElse(null);
        assertNotNull(insertCircle);
        assertEquals(new Point3D(100.0, 100.0, 0.0), insertCircle.getInsertionPoint());
        assertEquals(2.0, insertCircle.getXScale(), 0.001);
        assertEquals(2.0, insertCircle.getYScale(), 0.001);
    }
}
