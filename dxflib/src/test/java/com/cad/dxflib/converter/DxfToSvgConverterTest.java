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

class DxfToSvgConverterTest {

    private DxfParser dxfParser;
    private DxfToSvgConverter svgConverter;
    private SvgConversionOptions defaultOptions;

    @BeforeEach
    void setUp() {
        dxfParser = new DxfParser();
        svgConverter = new DxfToSvgConverter();
        defaultOptions = new SvgConversionOptions();
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

        String expectedInsertG_Square = "transform=\"translate(50.000, 50.000) \"";
        assertTrue(svg.contains(expectedInsertG_Square), "SVG should contain transform group for SimpleSquareBlock insert. SVG: " + svg);

        String expectedLineInSquareBlock = String.format(Locale.US,
            "<line x1=\"%.3f\" y1=\"%.3f\" x2=\"%.3f\" y2=\"%.3f\" stroke=\"%s\"",
            0.0, 0.0, 10.0, 0.0, "white");
        assertTrue(svg.contains(expectedLineInSquareBlock), "SVG should contain first line of inserted SimpleSquareBlock. SVG: " + svg);

        String expectedInsertG_Circle = "transform=\"translate(100.000, 100.000) rotate(-15.000) scale(2.000, 2.000) translate(-5.000, -5.000) \"";
        assertTrue(svg.contains(expectedInsertG_Circle), "SVG should contain transform group for CircleInBlock insert. SVG: " + svg);

        String expectedCircleInBlock = String.format(Locale.US,
            "<circle cx=\"%.3f\" cy=\"%.3f\" r=\"%.3f\" stroke=\"%s\"",
            5.0, 5.0, 5.0, "white");
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
        assertTrue(svg.contains("stroke=\"#DFDFDF\""), "Line with explicit color 253 should be #DFDFDF. SVG: \n" + svg);
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
    void testConvertEntitiesWithLinetypesToSvg() throws DxfParserException {
        InputStream inputStream = getResourceAsStream("/dxf/entities_with_linetypes.dxf");
        DxfDocument doc = dxfParser.parse(inputStream);
        String svg = svgConverter.convert(doc, defaultOptions);

        assertNotNull(svg);
        // System.out.println(svg);

        String expectedDashDotPattern = "stroke-dasharray=\"0.500 0.250 1.0 0.250\"";
        assertTrue(svg.contains(expectedDashDotPattern),
            "SVG should contain DASHDOT pattern. Expected: " + expectedDashDotPattern + "\nSVG: " + svg);

        String expectedDottedPattern = "stroke-dasharray=\"1.0 0.200\"";
        assertTrue(svg.contains(expectedDottedPattern),
            "SVG should contain DOTTED pattern for circle on LayerDotted. Expected: " + expectedDottedPattern + "\nSVG: " + svg);

        String arcPathStart = "<path d=\"M 60.000,50.000 A 10.000,10.000 0 0,1 50.000,60.000\"";
        int arcIndex = svg.indexOf(arcPathStart);
        assertTrue(arcIndex != -1, "Arc path not found in SVG. SVG: " + svg);
        int endOfArcTag = svg.indexOf("/>", arcIndex);
        String arcTag = svg.substring(arcIndex, endOfArcTag);
        assertFalse(arcTag.contains("stroke-dasharray="),
            "Arc with CONTINUOUS linetype should not have stroke-dasharray. Found: " + arcTag);

        String polyPathStart = "<path d=\"M 70.000,70.000 L 100.000,70.000\"";
        int polyIndex = svg.indexOf(polyPathStart); // This polyline has 0 bulge, so it's a line
        assertTrue(polyIndex != -1, "LWPolyline path not found in SVG. SVG: " + svg);
        int endOfPolyTag = svg.indexOf("/>", polyIndex);
        String polyTag = svg.substring(polyIndex, endOfPolyTag);
        assertTrue(polyTag.contains(expectedDashDotPattern),
            "LWPolyline on LayerDashed (BYLAYER->DASHDOT) should have DASHDOT pattern. Found: " + polyTag);
    }
}
