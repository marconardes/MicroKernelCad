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

@ExtendWith(MockitoExtension.class)
class MainFrameTest {
    private MainFrame mainFrame;

    @Mock
    private SVGPanel mockSvgCanvas;

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
        mainFrame = new MainFrame(false); // Use new constructor to avoid full UI init
        // Manually inject the mock SVGPanel.
        // This requires MainFrame.svgCanvas to be at least package-private.
        mainFrame.svgCanvas = mockSvgCanvas;

        // Also, ensure internal lists are initialized if MainFrame's init doesn't do it early enough
        // or if we need fresh lists for each test. MainFrame's constructor calls init(),
        // which should initialize these.
        mainFrame.drawnLines = new ArrayList<>();
        mainFrame.drawnCircles = new ArrayList<>();
        mainFrame.baseSvgContent = null;
        mainFrame.currentScale = 1.0;
        mainFrame.translateX = 0.0;
        mainFrame.translateY = 0.0;
        mainFrame.lineStartPoint = null;
        mainFrame.previewEndPoint = null;
        mainFrame.circleCenterPoint = null;
        mainFrame.previewRadius = 0;

        // Reset interactions on the mock from MainFrame's constructor and init() sequence
        Mockito.reset(mockSvgCanvas);
    }

    @Test
    void testRedrawSVGCanvas_initialState() {
        mainFrame.redrawSVGCanvas();

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        // loadSvg is async, so verify might need to wait or be structured differently
        // For now, assume direct call or test the synchronous part if possible.
        // The current MainFrame.loadSvg uses SwingUtilities.invokeLater.
        // For pure unit tests, it's better if the method generating SVG is separate
        // from the one dispatching to Swing.
        // Let's assume for now that redrawSVGCanvas -> loadSvg will eventually call setSvgURI.
        // This test might be flaky or fail if setSvgURI is not called on the same thread
        // immediately. For robust testing, refactor MainFrame or use a test framework
        // that handles Swing's EDT.
        // For this iteration, we'll verify, acknowledging this limitation.

        // To test what's passed to loadSvg, we can spy on mainFrame or make loadSvg testable.
        // Alternative: make redrawSVGCanvas return the string.
        // For now, let's assume loadSvg is called by redrawSVGCanvas.

        // Given that loadSvg is async, direct verification on mockSvgCanvas might be tricky.
        // A pragmatic approach for now:
        // 1. Make redrawSVGCanvas return the string it *would* send to loadSvg.
        // 2. Or, add a test hook (e.g., a Consumer<String>) in MainFrame that tests can set.
        // For now, I will proceed with the ArgumentCaptor, assuming the invokeLater
        // might execute quickly enough in a test environment or that setSvgURI is called
        // before invokeLater if the string is null/empty.

        verify(mockSvgCanvas, timeout(100)).setSvgURI(uriCaptor.capture()); // Added timeout for invokeLater
        String svgString = uriToContent(uriCaptor.getValue());

        assertTrue(svgString.startsWith("<svg width=\"100%\" height=\"100%\""), "SVG string should start with <svg...>");
        assertTrue(svgString.contains("<g transform=\"translate(0.0, 0.0) scale(1.0)\">"), "Should contain default transform group");
        String groupContent = svgString.substring(svgString.indexOf("<g transform"), svgString.lastIndexOf("</g>")).trim();
        // Regex to match the group tag and its content, allowing for self-closing or empty content.
        assertTrue(groupContent.matches("<g transform=\"translate\\(0\\.0, 0\\.0\\) scale\\(1\\.0\\)\">\\s*</g>"), "Group should be empty or self-closed. Actual: " + groupContent);
        assertTrue(svgString.endsWith("</g></svg>"), "SVG string should end with </g></svg>");
    }

    @Test
    void testRedrawSVGCanvas_withBaseContent() {
        mainFrame.baseSvgContent = "<svg version=\"1.1\"><rect x='10' y='10' width='100' height='100'/></svg>";
        mainFrame.redrawSVGCanvas();

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(mockSvgCanvas, timeout(100)).setSvgURI(uriCaptor.capture());
        String svgString = uriToContent(uriCaptor.getValue());

        assertTrue(svgString.contains("<rect x='10' y='10' width='100' height='100'/>"), "Should contain base rect element");
        assertTrue(svgString.contains("<g transform=\"translate(0.0, 0.0) scale(1.0)\">"), "Base content should be within transform group");
    }

    @Test
    void testRedrawSVGCanvas_withDrawnLine() {
        mainFrame.drawnLines.add(new Line2D(new Point2D(10, 20), new Point2D(30, 40)));
        mainFrame.redrawSVGCanvas();

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(mockSvgCanvas, timeout(100)).setSvgURI(uriCaptor.capture());
        String svgString = uriToContent(uriCaptor.getValue());

        assertTrue(svgString.contains("<line x1=\"10.0\" y1=\"20.0\" x2=\"30.0\" y2=\"40.0\" stroke=\"black\" stroke-width=\"2\"/>"), "Should contain drawn line");
    }

    @Test
    void testRedrawSVGCanvas_withPreviewLine() {
        mainFrame.toolManager.setActiveTool(ActiveTool.DRAW_LINE);
        mainFrame.lineStartPoint = new Point2D(5, 5);
        mainFrame.previewEndPoint = new Point2D(15, 15);
        mainFrame.redrawSVGCanvas();

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(mockSvgCanvas, timeout(100)).setSvgURI(uriCaptor.capture());
        String svgString = uriToContent(uriCaptor.getValue());

        assertTrue(svgString.contains("<line x1=\"5.0\" y1=\"5.0\" x2=\"15.0\" y2=\"15.0\" stroke=\"gray\" stroke-width=\"1\" stroke-dasharray=\"5,5\"/>"), "Should contain preview line");
    }

    @Test
    void testRedrawSVGCanvas_withPreviewCircle() {
        mainFrame.toolManager.setActiveTool(ActiveTool.DRAW_CIRCLE);
        mainFrame.circleCenterPoint = new Point2D(50, 50);
        mainFrame.previewRadius = 25.0;
        mainFrame.redrawSVGCanvas();

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(mockSvgCanvas, timeout(100)).setSvgURI(uriCaptor.capture());
        String svgString = uriToContent(uriCaptor.getValue());

        assertTrue(svgString.contains("<circle cx=\"50.0\" cy=\"50.0\" r=\"25.0\" stroke=\"gray\" stroke-width=\"1\" stroke-dasharray=\"5,5\" fill=\"none\"/>"), "Should contain preview circle");
    }

    @Test
    void testRedrawSVGCanvas_withTransform() {
        mainFrame.currentScale = 2.0;
        mainFrame.translateX = 100;
        mainFrame.translateY = 50;
        mainFrame.redrawSVGCanvas();

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(mockSvgCanvas, timeout(100)).setSvgURI(uriCaptor.capture());
        String svgString = uriToContent(uriCaptor.getValue());

        assertTrue(svgString.contains("<g transform=\"translate(100.0, 50.0) scale(2.0)\">"), "Should contain specified transform group");
    }
}
