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
}
