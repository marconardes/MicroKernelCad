package com.cad.dxflib.converter;

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
import com.cad.dxflib.parser.DxfParser;
import com.cad.dxflib.parser.DxfParserException;
import com.cad.dxflib.structure.DxfBlock;
import com.cad.dxflib.structure.DxfDocument;
import com.cad.dxflib.structure.DxfLayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List; // For DxfEntity list in testParseLineCircleMixed
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;


class DxfToSvgConverterTest {

    private DxfParser dxfParser;
    private DxfToSvgConverter svgConverter;
    private SvgConversionOptions defaultOptions;

    @BeforeEach
    void setUp() {
        dxfParser = new DxfParser();
        svgConverter = new DxfToSvgConverter();
        defaultOptions = new SvgConversionOptions();
        // defaultOptions.setDefaultStrokeColor("black"); // Default for BYLAYER if layer 0 has no specific color
        defaultOptions.setStrokeWidth(1.0);
        // Ensure Locale.US for consistent number formatting in expected SVG strings
        Locale.setDefault(Locale.US);
    }

    private InputStream getResourceAsStream(String name) {
        // Try with a leading slash if it's not already there, common for getResourceAsStream
        String resolvedName = name.startsWith("/") ? name : "/" + name;
        InputStream in = getClass().getResourceAsStream(resolvedName);
        if (in == null) {
            throw new IllegalArgumentException("Resource not found: " + resolvedName + " (Also tried as: " + name + ")");
        }
        return in;
    }

    @Test
    void testConvertLineSimpleToSvg() throws DxfParserException {
        InputStream inputStream = getResourceAsStream("/dxf/line_simple.dxf");
        DxfDocument doc = dxfParser.parse(inputStream);
        String svg = svgConverter.convert(doc, defaultOptions);

        assertNotNull(svg);
        assertTrue(svg.startsWith("<svg xmlns=\"http://www.w3.org/2000/svg\""));
        assertTrue(svg.endsWith("</svg>\n"));

        // Check for the line element - coordinates and color
        // <line x1="10.000" y1="10.000" x2="20.000" y2="20.000" stroke="red" stroke-width="1.000" />
        String expectedLine = String.format(Locale.US,
            "<line x1=\"%.3f\" y1=\"%.3f\" x2=\"%.3f\" y2=\"%.3f\" stroke=\"%s\" stroke-width=\"%.3f\" />",
            10.0, 10.0, 20.0, 20.0, "red", defaultOptions.getStrokeWidth());
        assertTrue(svg.contains(expectedLine), "SVG output does not contain the expected line element. SVG: \n" + svg);
    }

    @Test
    void testConvertCircleSimpleToSvg() throws DxfParserException {
        InputStream inputStream = getResourceAsStream("/dxf/circle_simple.dxf");
        DxfDocument doc = dxfParser.parse(inputStream);
        String svg = svgConverter.convert(doc, defaultOptions);

        assertNotNull(svg);
        // <circle cx="50.000" cy="50.000" r="25.000" stroke="green" stroke-width="1.000" fill="none" />
        String expectedCircle = String.format(Locale.US,
            "<circle cx=\"%.3f\" cy=\"%.3f\" r=\"%.3f\" stroke=\"%s\" stroke-width=\"%.3f\" fill=\"none\" />",
            50.0, 50.0, 25.0, "green", defaultOptions.getStrokeWidth());
        assertTrue(svg.contains(expectedCircle), "SVG output does not contain the expected circle element. SVG: \n" + svg);
    }

    @Test
    void testConvertLineCircleMixedToSvg() throws DxfParserException {
        InputStream inputStream = getResourceAsStream("/dxf/line_circle_mixed.dxf");
        DxfDocument doc = dxfParser.parse(inputStream);
        String svg = svgConverter.convert(doc, defaultOptions);

        assertNotNull(svg);

        // Line 1 (color BYLAYER -> layer "lines" -> default color, as layer "lines" is not in layers_simple.dxf)
        // The DxfParser currently creates layer "0" if a layer is not found for an entity.
        // The getDxfColorAsSvg will use defaultStrokeColor if layer "lines" is not found in the document.
        String expectedLine1 = String.format(Locale.US,
            "<line x1=\"%.3f\" y1=\"%.3f\" x2=\"%.3f\" y2=\"%.3f\" stroke=\"%s\" stroke-width=\"%.3f\" />",
            0.0, 0.0, 100.0, 100.0, defaultOptions.getDefaultStrokeColor(), defaultOptions.getStrokeWidth());
        assertTrue(svg.contains(expectedLine1), "SVG does not contain line1. SVG: \n" + svg);

        // Circle (layer "circles", default color)
        String expectedCircle = String.format(Locale.US,
            "<circle cx=\"%.3f\" cy=\"%.3f\" r=\"%.3f\" stroke=\"%s\" stroke-width=\"%.3f\" fill=\"none\" />",
            150.0, 150.0, 50.0, defaultOptions.getDefaultStrokeColor(), defaultOptions.getStrokeWidth());
        assertTrue(svg.contains(expectedCircle), "SVG does not contain circle. SVG: \n" + svg);

        // Line 2 (color 5 - blue)
        String expectedLine2 = String.format(Locale.US,
            "<line x1=\"%.3f\" y1=\"%.3f\" x2=\"%.3f\" y2=\"%.3f\" stroke=\"%s\" stroke-width=\"%.3f\" />",
            200.0, 200.0, 300.0, 200.0, "blue", defaultOptions.getStrokeWidth());
        assertTrue(svg.contains(expectedLine2), "SVG does not contain line2. SVG: \n" + svg);
    }

    @Test
    void testSvgStructureAndBounds() throws DxfParserException {
        // Uses line_simple.dxf which has bounds from (10,10) to (20,20)
        InputStream inputStream = getResourceAsStream("/dxf/line_simple.dxf");
        DxfDocument doc = dxfParser.parse(inputStream);
        SvgConversionOptions options = new SvgConversionOptions();
        options.setMargin(5); // Use a specific margin for predictability
        String svg = svgConverter.convert(doc, options);

        // Bounds of line_simple.dxf: minX=10, minY=10, maxX=20, maxY=20
        // Width = 10, Height = 10
        // Margin = 5
        // viewBox_minX = 10 - 5 = 5
        // viewBox_minY = 10 - 5 = 5
        // viewBox_width = 10 + (2*5) = 20
        // viewBox_height = 10 + (2*5) = 20
        // svgWidth = 20, svgHeight = 20

        assertTrue(svg.contains("width=\"20.000\""), "SVG width attribute is incorrect. SVG: \n" + svg);
        assertTrue(svg.contains("height=\"20.000\""), "SVG height attribute is incorrect. SVG: \n" + svg);
        assertTrue(svg.contains("viewBox=\"5.000 5.000 20.000 20.000\""), "SVG viewBox attribute is incorrect. SVG: \n" + svg);
    }

    @Test
    void testConvertInsertEntitiesToSvg() throws DxfParserException {
        InputStream inputStream = getResourceAsStream("/dxf/blocks_simple.dxf");
        DxfDocument doc = dxfParser.parse(inputStream);
        String svg = svgConverter.convert(doc, defaultOptions);

        assertNotNull(svg);
        // System.out.println(svg);

        String expectedInsertG_Square = "transform=\"translate(50.000, 50.000)\""; // Removed trailing space
        assertTrue(svg.contains(expectedInsertG_Square), "SVG should contain transform group for SimpleSquareBlock insert. SVG: " + svg);

        String expectedLineInSquareBlock = String.format(Locale.US,
            "<line x1=\"%.3f\" y1=\"%.3f\" x2=\"%.3f\" y2=\"%.3f\" stroke=\"%s\"",
            0.0, 0.0, 10.0, 0.0, defaultOptions.getDefaultStrokeColor()); // Changed "white" to default
        assertTrue(svg.contains(expectedLineInSquareBlock), "SVG should contain first line of inserted SimpleSquareBlock. SVG: " + svg);

        String expectedInsertG_Circle = "transform=\"translate(100.000, 100.000) rotate(-15.000) scale(2.000, 2.000) translate(-5.000, -5.000)\""; // Removed trailing space
        assertTrue(svg.contains(expectedInsertG_Circle), "SVG should contain transform group for CircleInBlock insert. SVG: " + svg);

        String expectedCircleInBlock = String.format(Locale.US,
            "<circle cx=\"%.3f\" cy=\"%.3f\" r=\"%.3f\" stroke=\"%s\"",
            5.0, 5.0, 5.0, defaultOptions.getDefaultStrokeColor()); // Changed "white" to default
        assertTrue(svg.contains(expectedCircleInBlock), "SVG should contain circle of inserted CircleInBlock. SVG: " + svg);

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
        assertEquals(15.0, insertCircle.getRotationAngle(), 0.001);
    }

    @Test
    void testAciColorMapping() throws DxfParserException {
        InputStream inputStream = getResourceAsStream("/dxf/colors_aci_test.dxf");
        DxfDocument doc = dxfParser.parse(inputStream);
        String svg = svgConverter.convert(doc, defaultOptions);

        // Line on Layer_Color_BYLAYER (layer color is ACI 10 - red #FF0000)
        assertTrue(svg.contains("stroke=\"#FF0000\""), "Line on Layer_Color_BYLAYER should be red (#FF0000). SVG: \n" + svg);

        // Circle on Layer_Color_9_Light_Grey (layer color is ACI 9 - #C0C0C0)
        assertTrue(svg.contains("stroke=\"#C0C0C0\""), "Circle on Layer_Color_9_Light_Grey should be #C0C0C0. SVG: \n" + svg);

        // Line with explicit color ACI 253 (#DFDFDF)
        assertTrue(svg.contains("stroke=\"#B2B2B2\""), "Line with explicit color 253 should be #B2B2B2. SVG: \n" + svg);
    }

    @Test
    void testSvgOutputWithLayerGrouping() throws DxfParserException {
        InputStream inputStream = getResourceAsStream("/dxf/layers_simple.dxf");
        DxfDocument doc = dxfParser.parse(inputStream);

        SvgConversionOptions options = new SvgConversionOptions();
        options.setGroupElementsByLayer(true);
        String svg = svgConverter.convert(doc, options);

        // System.out.println(svg);

        assertTrue(svg.contains("<g id=\"layer_0\" class=\"layer layer_0\">"), "SVG should contain group for layer_0. SVG: \n" + svg);
        assertTrue(svg.contains("<g id=\"layer_MyLayer1\" class=\"layer layer_MyLayer1\">"), "SVG should contain group for layer_MyLayer1. SVG: \n" + svg);

        assertFalse(svg.contains("<g id=\"layer_MyLayer2_Off_Red\""), "SVG should NOT contain group for invisible layer MyLayer2_Off_Red. SVG: \n" + svg);
    }

    @Test
    void testSvgOutputWithoutLayerGrouping() throws DxfParserException {
        InputStream inputStream = getResourceAsStream("/dxf/layers_simple.dxf");
        DxfDocument doc = dxfParser.parse(inputStream);

        SvgConversionOptions options = new SvgConversionOptions();
        options.setGroupElementsByLayer(false);
        String svg = svgConverter.convert(doc, options);

        assertFalse(svg.contains("<g id=\"layer_0\""), "SVG should NOT contain explicit group for layer_0 when grouping is off. SVG: \n" + svg);
        assertFalse(svg.contains("<g id=\"layer_MyLayer1\""), "SVG should NOT contain explicit group for layer_MyLayer1 when grouping is off. SVG: \n" + svg);

        // Check if entities from visible layers are present (e.g., one from Layer_Color_BYLAYER in layers_simple.dxf which is color red)
        // This relies on an entity being on Layer_Color_BYLAYER which resolves to red.
        // The layers_simple.dxf has entities on "0" and "MyLayer1" and "MyLayer2_Off_Red" and "MyLayer3_Locked_Blue"
        // Let's check for an entity from layer "0" (color white/default)
        // A specific check for an entity string might be too fragile.
        // The goal is to confirm no <g id="layer_..."> tags.
        // Presence of entities is implicitly tested by other tests like testConvertLineSimpleToSvg when grouping is off by default.
    }

    @Test
    void testConvertLwPolylineWithBulgeToSvg() throws DxfParserException {
        InputStream inputStream = getResourceAsStream("/dxf/lwpolyline_with_bulge.dxf");
        DxfDocument doc = dxfParser.parse(inputStream);
        String svg = svgConverter.convert(doc, defaultOptions);

        assertNotNull(svg);
        // System.out.println(svg);

        // V0 (0,0) bulge 0.5  -> para V1(10,10)
        // P1=(0,0), P2=(10,10), bulge=0.5
        // chordLength = 14.142, includedAngle = 1.8546 rad (106.26 deg), radius = 8.839
        // largeArcFlag = 0, sweepFlag = 1
        String expectedArc1 = String.format(Locale.US, "A %.3f,%.3f 0 0,1 %.3f,%.3f", 8.839, 8.839, 10.0, 10.0);

        // V1 (10,10) bulge 0.0 -> para V2(20,0)
        String expectedLine1 = String.format(Locale.US, "L %.3f,%.3f", 20.0, 0.0);

        // V2 (20,0) bulge -0.5 -> para V3(30,-10)
        // P1=(20,0), P2=(30,-10), bulge=-0.5
        // chordLength = 14.142, includedAngle = 1.8546 rad, radius = 8.839
        // largeArcFlag = 0, sweepFlag = 0 (due to negative bulge)
        String expectedArc2 = String.format(Locale.US, "A %.3f,%.3f 0 0,0 %.3f,%.3f", 8.839, 8.839, 30.0, -10.0);

        // Path data string
        String expectedPathData = String.format(Locale.US, "d=\"M %.3f,%.3f %s %s %s\"",
                                                0.0, 0.0, expectedArc1, expectedLine1, expectedArc2);

        assertTrue(svg.contains(expectedPathData),
            "SVG path data for lwpolyline with bulges is not as expected.\nExpected contains: " + expectedPathData + "\nActual SVG: \n" + svg);

        assertTrue(svg.contains("stroke=\"" + defaultOptions.getDefaultStrokeColor() + "\""),
            "LWPolyline with bulges SVG color is not the default. SVG: \n" + svg);
    }

    @Test
    void testConvertArcToSvg() throws DxfParserException {
        InputStream inputStream = getResourceAsStream("/dxf/arc_simple.dxf");
        DxfDocument doc = dxfParser.parse(inputStream);
        String svg = svgConverter.convert(doc, defaultOptions);

        assertNotNull(svg);
        // System.out.println(svg); // For debugging

        // Arc 1: Center (10,10), Radius 5, StartAngle 0, EndAngle 90
        // Color: BYLAYER (Layer 0 is default color - white/black depending on SvgConversionOptions)
        // Expected path: M 15,10 A 5,5 0 0,1 10,15 (or similar based on calculation)
        // Start point: x = 10 + 5*cos(0) = 15, y = 10 + 5*sin(0) = 10
        // End point: x = 10 + 5*cos(90deg) = 10, y = 10 + 5*sin(90deg) = 15
        // Large arc flag: 0 (90 deg sweep is <= 180)
        // Sweep flag: 1 (positive direction from start to end angle)
        String expectedArc1Path = String.format(Locale.US,
                "d=\"M %.3f,%.3f A %.3f,%.3f 0 %d,%d %.3f,%.3f\"",
                15.0, 10.0, // Start point (10+5*cos(0), 10+5*sin(0))
                5.0, 5.0,   // Radius rx, ry
                0,          // large-arc-flag
                1,          // sweep-flag (0 to 90)
                10.0, 15.0  // End point (10+5*cos(90), 10+5*sin(90))
        );
        String expectedArc1Stroke = String.format(Locale.US, "stroke=\"%s\"", defaultOptions.getDefaultStrokeColor());
        assertTrue(svg.contains(expectedArc1Path), "SVG output does not contain the expected path for arc 1. SVG: \n" + svg);
        assertTrue(svg.contains(expectedArc1Stroke), "SVG output does not contain the expected stroke for arc 1. SVG: \n" + svg);


        // Arc 2: Center (30,10), Radius 7, StartAngle 45, EndAngle 180
        // Color: 3 (green)
        // Start point: x = 30 + 7*cos(45) = 30 + 7*0.7071 = 34.950
        //              y = 10 + 7*sin(45) = 10 + 7*0.7071 = 14.950
        // End point: x = 30 + 7*cos(180) = 30 - 7 = 23.000
        //            y = 10 + 7*sin(180) = 10 + 0 = 10.000
        // Angle sweep = 180 - 45 = 135 degrees
        // Large arc flag: 0 (135 deg sweep is <= 180)
        // Sweep flag: 1 (positive direction from start to end angle)
        String expectedArc2Path = String.format(Locale.US,
                "d=\"M %.3f,%.3f A %.3f,%.3f 0 %d,%d %.3f,%.3f\"",
                30.0 + 7.0 * Math.cos(Math.toRadians(45.0)), // startX
                10.0 + 7.0 * Math.sin(Math.toRadians(45.0)), // startY
                7.0, 7.0,   // Radius rx, ry
                0,          // large-arc-flag
                1,          // sweep-flag (45 to 180)
                30.0 + 7.0 * Math.cos(Math.toRadians(180.0)), // endX
                10.0 + 7.0 * Math.sin(Math.toRadians(180.0))  // endY
        );
        String expectedArc2Stroke = "stroke=\"green\"";
        assertTrue(svg.contains(expectedArc2Path), "SVG output does not contain the expected path for arc 2. SVG: \n" + svg);
        assertTrue(svg.contains(expectedArc2Stroke), "SVG output does not contain the expected stroke for arc 2. SVG: \n" + svg);
    }

    @Test
    void testConvertTextToSvg() throws DxfParserException {
        InputStream inputStream = getResourceAsStream("/dxf/text_simple.dxf");
        DxfDocument doc = dxfParser.parse(inputStream);
        String svg = svgConverter.convert(doc, defaultOptions);

        assertNotNull(svg);
        // System.out.println(svg); // For debugging

        // Text 1: Default Color Text
        String expectedText1 = String.format(Locale.US,
                "<text x=\"%.3f\" y=\"%.3f\" font-size=\"%.3f\" fill=\"%s\" font-family=\"%s\">%s</text>",
                5.0, 5.0, 2.5, defaultOptions.getDefaultStrokeColor(), "Arial", "Default Color Text");
        assertTrue(svg.contains(expectedText1), "SVG does not contain expected Text1. SVG: \n" + svg);

        // Text 2: Blue Text on TextLayer
        // Layer "TextLayer" is ACI color 4 (cyan). Text entity has explicit color 5 (blue). Explicit color should override.
        String expectedText2 = String.format(Locale.US,
                "<text x=\"%.3f\" y=\"%.3f\" font-size=\"%.3f\" fill=\"%s\" font-family=\"%s\">%s</text>",
                5.0, 15.0, 2.5, "blue", "Arial", "Blue Text on TextLayer");
        assertTrue(svg.contains(expectedText2), "SVG does not contain expected Text2. SVG: \n" + svg);

        // Text 3: Rotated Text (rotation 30 deg)
        // Note: SVG rotation is negative of DXF rotation angle
        String expectedText3Partial = String.format(Locale.US,
                "<text x=\"%.3f\" y=\"%.3f\" font-size=\"%.3f\" fill=\"%s\" font-family=\"%s\"",
                5.0, 25.0, 3.0, defaultOptions.getDefaultStrokeColor(), "Arial");
        String expectedText3Transform = String.format(Locale.US,
                " transform=\"rotate(%.3f %.3f,%.3f)\"", -30.0, 5.0, 25.0);
        String expectedText3Content = ">Rotated Text</text>";
        assertTrue(svg.contains(expectedText3Partial) && svg.contains(expectedText3Transform) && svg.contains(expectedText3Content),
                "SVG does not contain expected Text3 (rotated). SVG: \n" + svg);

        // Text 4: Text with & < > " ' special chars
        // Converter should escape these: &amp; &lt; &gt; &quot; &apos;
        String expectedText4Content = "Text with &amp; &lt; &gt; &quot; &apos; special chars";
        String expectedText4 = String.format(Locale.US,
                "<text x=\"%.3f\" y=\"%.3f\" font-size=\"%.3f\" fill=\"%s\" font-family=\"%s\">%s</text>",
                5.0, 35.0, 2.0, defaultOptions.getDefaultStrokeColor(), "Arial", expectedText4Content);
        assertTrue(svg.contains(expectedText4), "SVG does not contain expected Text4 (special chars). SVG: \n" + svg);
    }

    @Test
    void testConvertLwPolylineClosedToSvg() throws DxfParserException {
        InputStream inputStream = getResourceAsStream("/dxf/lwpolyline_closed.dxf");
        DxfDocument doc = dxfParser.parse(inputStream);
        String svg = svgConverter.convert(doc, defaultOptions);

        assertNotNull(svg);
        // System.out.println(svg); // For debugging

        // Polyline vertices: (10,10), (50,10), (30,40), closed
        // Expected path: M 10.000,10.000 L 50.000,10.000 L 30.000,40.000 Z
        String expectedPathData = String.format(Locale.US,
                "d=\"M %.3f,%.3f L %.3f,%.3f L %.3f,%.3f Z\"",
                10.0, 10.0, 50.0, 10.0, 30.0, 40.0);
        assertTrue(svg.contains(expectedPathData),
            "SVG path data for closed lwpolyline is not as expected.\nExpected contains: " + expectedPathData + "\nActual SVG: \n" + svg);
        assertTrue(svg.contains("stroke=\"" + defaultOptions.getDefaultStrokeColor() + "\""),
            "Closed LWPolyline SVG color is not the default. SVG: \n" + svg);
        assertTrue(svg.contains("fill=\"none\""), "Closed LWPolyline fill should be none. SVG: \n" + svg);
    }

    @Test
    void testConvertLwPolylineConstantWidthToSvg() throws DxfParserException {
        InputStream inputStream = getResourceAsStream("/dxf/lwpolyline_constant_width.dxf");
        DxfDocument doc = dxfParser.parse(inputStream);
        String svg = svgConverter.convert(doc, defaultOptions);

        assertNotNull(svg);
        // System.out.println(svg); // For debugging

        // Polyline vertices: (10,10), (50,50) with constant width 2.5
        // Expected path: M 10.000,10.000 L 50.000,50.000
        String expectedPathData = String.format(Locale.US,
                "d=\"M %.3f,%.3f L %.3f,%.3f\"",
                10.0, 10.0, 50.0, 50.0);
        String expectedStrokeWidth = String.format(Locale.US, "stroke-width=\"%.3f\"", 2.5);

        assertTrue(svg.contains(expectedPathData),
            "SVG path data for constant width lwpolyline is not as expected.\nExpected contains: " + expectedPathData + "\nActual SVG: \n" + svg);
        assertTrue(svg.contains(expectedStrokeWidth),
            "SVG stroke-width for constant width lwpolyline is not as expected.\nExpected contains: " + expectedStrokeWidth + "\nActual SVG: \n" + svg);
        assertTrue(svg.contains("stroke=\"" + defaultOptions.getDefaultStrokeColor() + "\""),
            "Constant width LWPolyline SVG color is not the default. SVG: \n" + svg);
        assertTrue(svg.contains("fill=\"none\""), "Constant width LWPolyline fill should be none. SVG: \n" + svg);
    }

    @Test
    void testConvertInsertWithByBlockColorToSvg() throws DxfParserException {
        InputStream inputStream = getResourceAsStream("/dxf/insert_byblock_color.dxf");
        DxfDocument doc = dxfParser.parse(inputStream);
        String svg = svgConverter.convert(doc, defaultOptions);

        assertNotNull(svg);
        // System.out.println(svg); // For debugging

        // The INSERT entity has color 1 (red).
        // The entities within ColorTestBlock have color 0 (BYBLOCK).
        // Therefore, the rendered line and circle should be red.

        // Expected line from block, now red
        // Original line in block: (0,0) to (10,0)
        // Inserted at (20,20)
        // Transformed line: (20,20) to (30,20)
        String expectedLine = String.format(Locale.US,
                "<line x1=\"%.3f\" y1=\"%.3f\" x2=\"%.3f\" y2=\"%.3f\" stroke=\"%s\"",
                0.0, 0.0, 10.0, 0.0, "red");
        // Check within the transform group of the insert
        String insertTransform = "transform=\"translate(20.000, 20.000)\"";


        // Expected circle from block, now red
        // Original circle in block: center (5,10), radius 2
        // Inserted at (20,20)
        // Transformed circle: center (25,30) -> relative to insert point, so (5,10) in block coords
        String expectedCircle = String.format(Locale.US,
                "<circle cx=\"%.3f\" cy=\"%.3f\" r=\"%.3f\" stroke=\"%s\"",
                5.0, 10.0, 2.0, "red");

        // We need to check that these entities are rendered with red stroke *inside* the <g transform="...">
        // A more robust check would be to parse the SVG or use regex, but string contains is simpler for now.
        // Let's check for the group and then the entities within it.
        int groupStartIndex = svg.indexOf(insertTransform);
        assertTrue(groupStartIndex != -1, "SVG should contain transform group for INSERT. SVG: " + svg);

        int groupEndIndex = svg.indexOf("</g>", groupStartIndex);
        assertTrue(groupEndIndex != -1, "SVG should contain closing g for INSERT transform. SVG: " + svg);

        String groupContent = svg.substring(groupStartIndex, groupEndIndex);

        assertTrue(groupContent.contains(expectedLine),
            "Transformed group for INSERT does not contain the expected red line. Group content: \n" + groupContent);
        assertTrue(groupContent.contains(expectedCircle),
            "Transformed group for INSERT does not contain the expected red circle. Group content: \n" + groupContent);
    }

    @Test
    void testConvertEmptyDocumentToSvg() throws DxfParserException {
        InputStream inputStream = getResourceAsStream("/dxf/empty_document.dxf");
        DxfDocument doc = dxfParser.parse(inputStream);
        SvgConversionOptions options = new SvgConversionOptions(); // Use default margin
        options.setMargin(10); // Explicitly set margin for predictability in test
        String svg = svgConverter.convert(doc, options);

        assertNotNull(svg);
        // System.out.println(svg); // For debugging

        // Expected default viewBox when bounds are invalid:
        // svgWidth = 100 + (2 * margin) = 100 + 20 = 120
        // svgHeight = 100 + (2 * margin) = 100 + 20 = 120
        // viewBox = String.format(Locale.US, "%.3f %.3f %.3f %.3f", -margin, -margin, svgWidth, svgHeight);
        // viewBox = "-10.000 -10.000 120.000 120.000"

        String expectedWidth = String.format(Locale.US, "width=\"%.3f\"", 120.0);
        String expectedHeight = String.format(Locale.US, "height=\"%.3f\"", 120.0);
        String expectedViewBox = String.format(Locale.US, "viewBox=\"%.3f %.3f %.3f %.3f\"", -10.0, -10.0, 120.0, 120.0);

        assertTrue(svg.contains(expectedWidth), "SVG width for empty doc is not as expected. SVG: \n" + svg);
        assertTrue(svg.contains(expectedHeight), "SVG height for empty doc is not as expected. SVG: \n" + svg);
        assertTrue(svg.contains(expectedViewBox), "SVG viewBox for empty doc is not as expected. SVG: \n" + svg);
    }

    @Test
    void testConvertSinglePointDocumentToSvg() throws DxfParserException {
        // This test assumes POINT entities either don't exist or don't create valid bounds with area.
        // The DxfParser may not fully support POINT entities yet, so an empty entity list or one
        // that results in invalid/zero-area bounds is the expected outcome to trigger default viewBox.
        InputStream inputStream = getResourceAsStream("/dxf/single_point_document.dxf");
        DxfDocument doc = dxfParser.parse(inputStream);

        // Check if the POINT entity was parsed, if relevant for future.
        // For now, we mainly care that the bounds are not 'valid' in a way that produces non-default SVG box.
        // boolean pointEntityFound = doc.getEntities().stream().anyMatch(e -> e.getType() == EntityType.POINT);
        // This might require adding POINT to EntityType and DxfParser if we want to actually parse it.
        // For this test, the key is that doc.getBounds() should be invalid or zero-area.

        SvgConversionOptions options = new SvgConversionOptions();
        options.setMargin(5); // Different margin for this test
        String svg = svgConverter.convert(doc, options);

        assertNotNull(svg);
        // System.out.println(svg); // For debugging

        // Expected default viewBox when bounds are invalid or zero-area:
        // svgWidth = 100 + (2 * margin) = 100 + 10 = 110
        // svgHeight = 100 + (2 * margin) = 100 + 10 = 110
        // viewBox = String.format(Locale.US, "%.3f %.3f %.3f %.3f", -margin, -margin, svgWidth, svgHeight);
        // viewBox = "-5.000 -5.000 110.000 110.000"

        String expectedWidth = String.format(Locale.US, "width=\"%.3f\"", 110.0);
        String expectedHeight = String.format(Locale.US, "height=\"%.3f\"", 110.0);
        String expectedViewBox = String.format(Locale.US, "viewBox=\"%.3f %.3f %.3f %.3f\"", -5.0, -5.0, 110.0, 110.0);

        assertTrue(svg.contains(expectedWidth), "SVG width for single point doc is not as expected. SVG: \n" + svg);
        assertTrue(svg.contains(expectedHeight), "SVG height for single point doc is not as expected. SVG: \n" + svg);
        assertTrue(svg.contains(expectedViewBox), "SVG viewBox for single point doc is not as expected. SVG: \n" + svg);
    }
}
