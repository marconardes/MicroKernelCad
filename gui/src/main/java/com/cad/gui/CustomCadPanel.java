package com.cad.gui;

import com.cad.dxflib.common.Point2D;
import com.cad.gui.tool.ActiveTool;
import com.cad.gui.tool.ToolManager;
import com.cad.modules.geometry.entities.Circle2D;
import com.cad.modules.geometry.entities.Line2D;
import com.cad.modules.rendering.DxfRenderService;
import com.cad.dxflib.parser.DxfParserException; // Added for loadDxfFromFile
import com.kitfox.svg.SVGDiagram; // Added for SVG Salamander rendering
import com.kitfox.svg.SVGUniverse; // Added for SVG Salamander rendering


import javax.swing.*;
import java.awt.*;
// MouseEvent not directly used in this class after refactoring MainFrame, but keep for context if any internal methods might use it
// import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.File;
import java.io.FileInputStream; // Added for loadDxfFromFile
import java.io.IOException; // Added for loadDxfFromFile
import java.util.ArrayList;
import java.util.List;
// java.net.URI will be needed for diagram lookup from SVGUniverse
import java.net.URI;
import java.util.Iterator;


public class CustomCadPanel extends JPanel {

    private DxfRenderService dxfRenderService;
    private ToolManager toolManager;
    private List<Line2D> drawnLines;
    private List<Circle2D> drawnCircles;
    private Object selectedEntity;
    private Point2D lineStartPoint;
    private Point2D previewEndPoint;
    private Point2D circleCenterPoint;
    private double previewRadius;
    // private String baseSvgContent; // Changed to SVGUniverse
    private SVGUniverse svgUniverseFromDxf; // Changed field type
    private double currentScale;
    private double translateX;
    private double translateY;
    private Point2D panLastMousePosition;
    private static final double HIT_TOLERANCE = 5.0;

    public CustomCadPanel(ToolManager toolManager, DxfRenderService dxfRenderService) {
        this.toolManager = toolManager;
        this.dxfRenderService = dxfRenderService;
        this.drawnLines = new ArrayList<>();
        this.drawnCircles = new ArrayList<>();
        this.currentScale = 1.0;
        this.translateX = 0.0;
        this.translateY = 0.0;
        // this.baseSvgContent = "<svg width=\"800\" height=\"600\" xmlns=\"http://www.w3.org/2000/svg\"></svg>"; // Default empty SVG
        this.svgUniverseFromDxf = null; // Initialize new field

        // Mouse listeners are added in MainFrame
    }

    public void clearPreviewLineState() {
        lineStartPoint = null;
        previewEndPoint = null;
        repaint();
    }

    public void clearPreviewCircleState() {
        circleCenterPoint = null;
        previewRadius = 0;
        repaint();
    }

    public void loadDxfFromFile(File file) {
        // For now, just clear lists and store base content
        // In the future, this will parse the DXF and prepare it for rendering
        drawnLines.clear();
        drawnCircles.clear();
        selectedEntity = null;
        clearPreviewLineState();
        clearPreviewCircleState();
        this.svgUniverseFromDxf = null; // Clear previous DXF content

        if (file == null || !file.exists()) {
            // Optionally, load an empty universe or a universe with an error message
            this.svgUniverseFromDxf = new SVGUniverse();
            // You could add a text element to the universe indicating the error
            System.err.println("File not found or null: " + (file != null ? file.getAbsolutePath() : "null"));
            repaint();
            return;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            // Use a unique name for the diagram, e.g., based on the file name
            String diagramName = file.getName();
            this.svgUniverseFromDxf = dxfRenderService.convertDxfToSvgUniverse(fis, diagramName);
        } catch (IOException | DxfParserException e) {
            e.printStackTrace();
            // Optionally, display an error message in the panel
            // For now, just reset the universe
            this.svgUniverseFromDxf = new SVGUniverse();
            // Example: create a text element showing error
            // com.kitfox.svg.elements.Text errorText = new com.kitfox.svg.elements.Text();
            // errorText.appendText("Error loading DXF: " + e.getMessage());
            // ... add to a diagram in svgUniverseFromDxf ...
        }
        repaint();
    }

    private Point2D screenToModel(Point2D screenPoint) {
        try {
            AffineTransform viewTransform = new AffineTransform();
            viewTransform.translate(translateX, translateY);
            viewTransform.scale(currentScale, currentScale);
            AffineTransform inverseTransform = viewTransform.createInverse();
            java.awt.geom.Point2D.Double modelCoords = new java.awt.geom.Point2D.Double();
            inverseTransform.transform(new java.awt.geom.Point2D.Double(screenPoint.x, screenPoint.y), modelCoords);
            return new Point2D(modelCoords.x, modelCoords.y);
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace(); // Should not happen with typical view transforms
            // Fallback to screen coordinates, though incorrect for transformed views
            return screenPoint;
        }
    }

    public void handleMousePress(Point2D screenPoint) {
        Point2D modelPoint = screenToModel(screenPoint);
        ActiveTool activeTool = toolManager.getActiveTool();

        if (activeTool == ActiveTool.SELECT) {
            selectedEntity = null; // Clear previous selection
            // Check lines
            for (Line2D line : drawnLines) {
                if (isPointNearLine(modelPoint, line, HIT_TOLERANCE / currentScale)) {
                    selectedEntity = line;
                    break;
                }
            }
            // Check circles if no line selected
            if (selectedEntity == null) {
                for (Circle2D circle : drawnCircles) {
                    if (isPointNearCircle(modelPoint, circle, HIT_TOLERANCE / currentScale)) {
                        selectedEntity = circle;
                        break;
                    }
                }
            }
        } else if (activeTool == ActiveTool.DRAW_LINE) {
            if (lineStartPoint == null) {
                lineStartPoint = modelPoint;
            } else {
                drawnLines.add(new Line2D(lineStartPoint, modelPoint));
                clearPreviewLineState(); // Clears lineStartPoint and previewEndPoint
            }
        } else if (activeTool == ActiveTool.DRAW_CIRCLE) {
            if (circleCenterPoint == null) {
                circleCenterPoint = modelPoint;
            } else {
                double radius = circleCenterPoint.distanceTo(modelPoint);
                drawnCircles.add(new Circle2D(circleCenterPoint, radius));
                clearPreviewCircleState(); // Clears circleCenterPoint and previewRadius
            }
        } else if (activeTool == ActiveTool.PAN) {
            panLastMousePosition = screenPoint; // Use screen coordinates for panning
        }
        repaint();
    }

    public void handleMouseRelease(Point2D screenPoint) {
        ActiveTool activeTool = toolManager.getActiveTool();
        if (activeTool == ActiveTool.PAN) {
            panLastMousePosition = null;
        }
        // For drawing tools, the actual entity creation happens on the second click in handleMousePress
        repaint();
    }

    public void handleMouseDrag(Point2D screenPoint, ActiveTool activeTool) { // Pass activeTool directly
        Point2D modelPoint = screenToModel(screenPoint);

        if (activeTool == ActiveTool.DRAW_LINE && lineStartPoint != null) {
            previewEndPoint = modelPoint;
        } else if (activeTool == ActiveTool.DRAW_CIRCLE && circleCenterPoint != null) {
            previewRadius = circleCenterPoint.distanceTo(modelPoint);
        } else if (activeTool == ActiveTool.PAN && panLastMousePosition != null) {
            double dx = screenPoint.x - panLastMousePosition.x;
            double dy = screenPoint.y - panLastMousePosition.y;
            translateX += dx;
            translateY += dy;
            panLastMousePosition = screenPoint;
        }
        repaint();
    }

    public void applyZoom(double factor, Point2D centerScreenPoint) {
        Point2D centerModelPoint = screenToModel(centerScreenPoint);

        currentScale *= factor;

        // Adjust translateX and translateY to keep the model point under the mouse cursor stationary
        translateX = centerScreenPoint.x - centerModelPoint.x * currentScale;
        translateY = centerScreenPoint.y - centerModelPoint.y * currentScale;

        repaint();
    }

    public boolean isPointNearLine(Point2D point, Line2D line, double tolerance) {
        double x1 = line.getStartPoint().x;
        double y1 = line.getStartPoint().y;
        double x2 = line.getEndPoint().x;
        double y2 = line.getEndPoint().y;
        double px = point.x;
        double py = point.y;

        double lenSq = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        if (lenSq == 0) { // Line is a point
            return point.distanceTo(line.getStartPoint()) <= tolerance;
        }

        double t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / lenSq;
        t = Math.max(0, Math.min(1, t)); // Clamp t to the range [0, 1]

        double closestX = x1 + t * (x2 - x1);
        double closestY = y1 + t * (y2 - y1);
        Point2D closestPoint = new Point2D(closestX, closestY);

        return point.distanceTo(closestPoint) <= tolerance;
    }

    public boolean isPointNearCircle(Point2D point, Circle2D circle, double tolerance) {
        double distToCenter = point.distanceTo(circle.getCenter());
        return Math.abs(distToCenter - circle.getRadius()) <= tolerance;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create(); // Use create() to avoid modifying the original Graphics context

        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Apply pan and zoom transformations
            g2d.translate(translateX, translateY);
            g2d.scale(currentScale, currentScale);

            // Render DXF content from SVGUniverse
            if (svgUniverseFromDxf != null) {
                // Try to get the diagram by the name used when loading, or get the first one
                URI diagramUri = null;
                Iterator<URI> uriIter = svgUniverseFromDxf.getURIs();
                if (uriIter.hasNext()) {
                    diagramUri = uriIter.next();
                }

                if (diagramUri != null) {
                    SVGDiagram diagram = svgUniverseFromDxf.getDiagram(diagramUri);
                    if (diagram != null) {
                        try {
                            // Set rendering hints for SVG if needed, though some might be ignored
                            // g2d.setRenderingHint(SVGConstants.KEY_STROKE_CONTROL, SVGConstants.VALUE_STROKE_PURE);
                            // g2d.setRenderingHint(SVGConstants.KEY_TEXT_RENDERING, SVGConstants.VALUE_TEXT_RENDERING_OPTIMIZE_LEGIBILITY);

                            diagram.render(g2d);
                        } catch (Exception e) {
                            e.printStackTrace(); // Log rendering errors
                        }
                    }
                }
            }

            // Render drawn geometric entities
            for (Line2D line : drawnLines) {
                String lineStrokeColor = (line == selectedEntity) ? "red" : "black";
                int lineStrokeWidth = (line == selectedEntity) ? 3 : 2;
                g2d.setColor(lineStrokeColor.equals("red") ? Color.RED : Color.BLACK);
                g2d.setStroke(new BasicStroke(lineStrokeWidth));
                g2d.drawLine((int) line.getStartPoint().x, (int) line.getStartPoint().y,
                             (int) line.getEndPoint().x, (int) line.getEndPoint().y);
            }

            for (Circle2D circle : drawnCircles) {
                String circleStrokeColor = (circle == selectedEntity) ? "red" : "blue";
                int circleStrokeWidth = (circle == selectedEntity) ? 3 : 2;
                g2d.setColor(circleStrokeColor.equals("red") ? Color.RED : Color.BLUE);
                g2d.setStroke(new BasicStroke(circleStrokeWidth));
                g2d.drawArc((int) (circle.getCenter().x - circle.getRadius()),
                            (int) (circle.getCenter().y - circle.getRadius()),
                            (int) (2 * circle.getRadius()),
                            (int) (2 * circle.getRadius()), 0, 360);
            }

            // Render previews
            if (toolManager.getActiveTool() == ActiveTool.DRAW_LINE && lineStartPoint != null && previewEndPoint != null) {
                g2d.setColor(Color.GRAY);
                g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 5}, 0)); // Dashed line
                g2d.drawLine((int) lineStartPoint.x, (int) lineStartPoint.y, (int) previewEndPoint.x, (int) previewEndPoint.y);
            }

            if (toolManager.getActiveTool() == ActiveTool.DRAW_CIRCLE && circleCenterPoint != null && previewRadius > 0) {
                g2d.setColor(Color.GRAY);
                g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 5}, 0)); // Dashed line
                g2d.drawArc((int) (circleCenterPoint.x - previewRadius),
                            (int) (circleCenterPoint.y - previewRadius),
                            (int) (2 * previewRadius),
                            (int) (2 * previewRadius), 0, 360);
            }
        } finally {
            g2d.dispose(); // Release resources of the copied Graphics context
        }
    }

    // Getters and Setters for fields that might be needed by MainFrame (e.g. for status bar)
    // For now, we make them public directly for simplicity, can be refactored to getters later.
    // public double getCurrentScale() { return currentScale; }
    // public double getTranslateX() { return translateX; }
    // public double getTranslateY() { return translateY; }
    // public Object getSelectedEntity() { return selectedEntity; }

    // Setter for baseSvgContent is removed as we now use SVGUniverse
    // public void setBaseSvgContent(String svgContent) {
    // this.baseSvgContent = svgContent;
    // repaint();
    // }
}
