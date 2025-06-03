package com.cad.gui;

import com.cad.dxflib.common.Point2D;
import com.cad.gui.tool.ActiveTool;
import com.cad.gui.tool.ToolManager;
import com.cad.modules.geometry.entities.Line2D;
import com.cad.modules.geometry.entities.Circle2D;

import com.kitfox.svg.app.beans.SVGPanel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito; // Explicit import for Mockito.reset
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList; // Import for List initialization

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cad.modules.rendering.DxfRenderService; // Added for direct instantiation

@ExtendWith(MockitoExtension.class)
class CadPanelLogicTest { // Renamed class
    // private MainFrame mainFrame; // Removed MainFrame instance
    private CadPanelLogic panelLogic;

    // @Mock // No longer mocking SVGPanel for these logic tests
    // private SVGPanel mockSvgCanvas;

    // Fields from MainFrame that need to be accessed/set for tests
    // These would be package-private in MainFrame for this to work.
    // For simplicity in this step, direct assignment is used in tests.
    // In a real scenario, consider if MainFrame needs more testable APIs or internal refactoring.


    // Helper to decode data URI
    private String uriToContent(URI uri) {
        if (uri == null) return "";
        try {
            String raw = uri.getRawSchemeSpecificPart();
            if (raw == null) return uri.toString(); // Not a data URI or malformed

            // Check if it's the expected data URI format
            String prefix = "image/svg+xml;charset=UTF-8,";
            if (raw.startsWith(prefix)) {
                String encoded = raw.substring(prefix.length());
                return URLDecoder.decode(encoded, "UTF-8");
            }
            // Fallback for other URIs (e.g. if it were a file URI, though not expected for setSvgURI here)
            if (uri.getPath() != null) return uri.getPath();
            return uri.toString();
        } catch (UnsupportedEncodingException e) {
            // This should not happen with UTF-8
            e.printStackTrace();
            return "";
        }  catch (Exception e) {
            e.printStackTrace();
            System.err.println("Problematic URI: " + uri.toString());
            return "";
        }
    }

    @BeforeEach
    void setUp() {
        // mainFrame = new MainFrame(false); // No longer instantiating MainFrame
        // panelLogic = mainFrame.getCadPanelLogic();
        ToolManager toolManager = new ToolManager();
        DxfRenderService dxfRenderService = new DxfRenderService();
        panelLogic = new CadPanelLogic(toolManager, dxfRenderService);

        // mainFrame.setSvgCanvasForTest(mockSvgCanvas); // No longer needed

        // Initialize state in panelLogic for clean tests
        panelLogic.drawnLines = new ArrayList<>();
        panelLogic.drawnCircles = new ArrayList<>();
        panelLogic.baseSvgContent = null;
        panelLogic.currentScale = 1.0;
        panelLogic.translateX = 0.0;
        panelLogic.translateY = 0.0;
        panelLogic.lineStartPoint = null;
        panelLogic.previewEndPoint = null;
        panelLogic.circleCenterPoint = null;
        panelLogic.previewRadius = 0;
        panelLogic.selectedEntity = null;
        panelLogic.panLastMousePosition = null;

        // Mockito.reset(mockSvgCanvas); // No longer needed
    }

    @Test
    void testRedrawSVGCanvas_initialState() {
        String svgString = panelLogic.generateSvgContent();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "<g transform=\"translate\\s*\\(\\s*0(\\.0)?\\s*,\\s*0(\\.0)?\\s*\\) scale\\s*\\(\\s*1(\\.0)?\\s*\\)\">\\s*</g>"
        );
        assertTrue(svgString.startsWith("<svg width=\"100%\" height=\"100%\""), "SVG string should start with <svg...>");
        assertTrue(pattern.matcher(svgString).find(), "Should contain default empty transform group. Actual: " + svgString);
        assertTrue(svgString.endsWith("</g></svg>"), "SVG string should end with </g></svg>");
    }

    @Test
    void testRedrawSVGCanvas_withBaseContent() {
        panelLogic.baseSvgContent = "<svg version=\"1.1\"><rect x='10' y='10' width='100' height='100'/></svg>";
        String svgString = panelLogic.generateSvgContent();

        java.util.regex.Pattern transformPattern = java.util.regex.Pattern.compile(
            "<g transform=\"translate\\s*\\(\\s*0(\\.0)?\\s*,\\s*0(\\.0)?\\s*\\) scale\\s*\\(\\s*1(\\.0)?\\s*\\)\">"
        );
        assertTrue(svgString.contains("<rect x='10' y='10' width='100' height='100'/>"), "Should contain base rect element");
        assertTrue(transformPattern.matcher(svgString).find(), "Base content should be within default transform group. Actual: " + svgString);
    }

    @Test
    void testRedrawSVGCanvas_withDrawnLine() {
        panelLogic.drawnLines.add(new Line2D(new Point2D(10, 20), new Point2D(30, 40)));
        String svgString = panelLogic.generateSvgContent();

        assertTrue(svgString.contains("<line x1=\"10.0\" y1=\"20.0\" x2=\"30.0\" y2=\"40.0\" stroke=\"black\" stroke-width=\"2\"/>"), "Should contain drawn line");
    }

    @Test
    void testRedrawSVGCanvas_withPreviewLine() {
        panelLogic.toolManager.setActiveTool(ActiveTool.DRAW_LINE);
        panelLogic.lineStartPoint = new Point2D(5, 5);
        panelLogic.previewEndPoint = new Point2D(15, 15);
        String svgString = panelLogic.generateSvgContent();

        assertTrue(svgString.contains("<line x1=\"5.0\" y1=\"5.0\" x2=\"15.0\" y2=\"15.0\" stroke=\"gray\" stroke-width=\"1\" stroke-dasharray=\"5,5\"/>"), "Should contain preview line");
    }

    @Test
    void testRedrawSVGCanvas_withPreviewCircle() {
        panelLogic.toolManager.setActiveTool(ActiveTool.DRAW_CIRCLE);
        panelLogic.circleCenterPoint = new Point2D(50, 50);
        panelLogic.previewRadius = 25.0;
        String svgString = panelLogic.generateSvgContent();

        assertTrue(svgString.contains("<circle cx=\"50.0\" cy=\"50.0\" r=\"25.0\" stroke=\"gray\" stroke-width=\"1\" stroke-dasharray=\"5,5\" fill=\"none\"/>"), "Should contain preview circle");
    }

    @Test
    void testRedrawSVGCanvas_withTransform() {
        panelLogic.currentScale = 2.0;
        panelLogic.translateX = 100;
        panelLogic.translateY = 50;
        String svgString = panelLogic.generateSvgContent();

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "<g transform=\"translate\\s*\\(\\s*100(\\.0)?\\s*,\\s*50(\\.0)?\\s*\\) scale\\s*\\(\\s*2(\\.0)?\\s*\\)\">"
        );
        assertTrue(pattern.matcher(svgString).find(), "Should contain specified transform group. Actual: " + svgString);
    }
}
