package com.cad.gui;

import com.cad.dxflib.common.Point2D;
import com.cad.dxflib.common.DxfEntity; // Added import
import com.cad.gui.tool.ActiveTool;
import com.cad.gui.tool.ToolManager;
import com.cad.modules.geometry.entities.Circle2D;
import com.cad.modules.geometry.entities.Line2D;
import com.cad.modules.rendering.DxfRenderService;
import com.cad.modules.rendering.DxfProcessingResult; // Added import
import com.cad.dxflib.parser.DxfParserException; // Added for loadDxfFromFile
import com.cad.dxflib.entities.DxfLine; // Added import for DXF entity types
import com.cad.dxflib.entities.DxfCircle; // Added import for DXF entity types

// Apache Batik Imports
import org.w3c.dom.svg.SVGDocument;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.gvt.GraphicsNode;

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
// import java.net.URI; // No longer directly used in this class
// import java.util.Iterator; // No longer directly used in this class


public class CustomCadPanel extends JPanel {

    private DxfRenderService dxfRenderService;
    private ToolManager toolManager;
    private List<Line2D> drawnLines;
    private List<Circle2D> drawnCircles;
    private List<DxfEntity> importedDxfEntities = new ArrayList<>(); // Added field
    private Object selectedEntity;
    private Point2D lineStartPoint;
    private Point2D previewEndPoint;
    private Point2D circleCenterPoint;
    private double previewRadius;
    private org.w3c.dom.svg.SVGDocument svgDocument; // Batik document
    private double currentScale;
    private double translateX;
    private double translateY;
    private Point2D panLastMousePosition;
    private static final double HIT_TOLERANCE = 5.0;

    // Batik bridge components
    private UserAgentAdapter userAgentAdapter;
    private DocumentLoader documentLoader;
    private GVTBuilder gvtBuilder;
    private BridgeContext bridgeContext;
    private GraphicsNode gvtRoot; // To store the built GVT tree

    public CustomCadPanel(ToolManager toolManager, DxfRenderService dxfRenderService) {
        this.toolManager = toolManager;
        this.dxfRenderService = dxfRenderService;
        this.drawnLines = new ArrayList<>();
        this.drawnCircles = new ArrayList<>();
        this.currentScale = 1.0;
        this.translateX = 0.0;
        this.translateY = 0.0;

        // Initialize Batik components
        this.userAgentAdapter = new UserAgentAdapter();
        this.documentLoader = new DocumentLoader(userAgentAdapter);
        this.gvtBuilder = new GVTBuilder();
        this.bridgeContext = new BridgeContext(userAgentAdapter, documentLoader);
        this.bridgeContext.setDynamicState(BridgeContext.DYNAMIC); // For dynamic updates

        this.svgDocument = null;
        this.gvtRoot = null;

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
        drawnLines.clear(); // Removed duplicate clear
        drawnCircles.clear(); // Keep this
        this.importedDxfEntities.clear(); // Clear imported entities
        selectedEntity = null;
        clearPreviewLineState();
        clearPreviewCircleState();
        this.svgDocument = null; // Clear previous document
        this.gvtRoot = null;     // Clear previous GVT tree

        if (file == null || !file.exists()) {
            System.err.println("File not found or null: " + (file != null ? file.getAbsolutePath() : "null"));
            repaint();
            return;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            String diagramName = file.getName();
            // Call the new loadDxf method
            DxfProcessingResult result = dxfRenderService.loadDxf(fis, diagramName);

            if (result != null) {
                this.svgDocument = result.batikDocument; // Set SVG document from result
                if (result.dxfDocument != null && result.dxfDocument.getModelSpaceEntities() != null) {
                    this.importedDxfEntities.addAll(result.dxfDocument.getModelSpaceEntities());
                }

                // Build GVT tree if batikDocument is available
                if (this.svgDocument != null) {
                    this.gvtRoot = gvtBuilder.build(bridgeContext, this.svgDocument);
                } else {
                    this.gvtRoot = null; // No Batik document to build GVT from
                    // Potentially log that DXF was loaded but SVG part is missing/empty
                    System.out.println("DXF loaded, but no Batik SVGDocument was generated for: " + diagramName);
                }
            } else {
                // This case should ideally not happen if loadDxf throws exceptions on failure
                // or returns a result with null fields.
                this.svgDocument = null;
                this.gvtRoot = null;
                System.err.println("DXF processing result was null for: " + diagramName);
            }
        } catch (IOException | DxfParserException e) { // DxfRenderService.loadDxf can throw these
            e.printStackTrace();
            this.svgDocument = null; // Ensure clean state on error
            this.gvtRoot = null;
            this.importedDxfEntities.clear(); // Also clear entities on error
        } catch (Exception e) { // Catch other Batik-specific exceptions during GVT build or other unexpected issues
            e.printStackTrace();
            this.svgDocument = null; // Ensure clean state on error
            this.gvtRoot = null;
            this.importedDxfEntities.clear(); // Also clear entities on error
        }
        repaint();
    }

    // Consider adding a method to get importedDxfEntities if needed externally
    // public List<DxfEntity> getImportedDxfEntities() {
    //     return importedDxfEntities;
    // }

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

    public boolean isPointNearDxfLine(Point2D point, com.cad.dxflib.entities.DxfLine dxfLine, double tolerance) {
        com.cad.dxflib.common.Point3D dxfStart = dxfLine.getStartPoint();
        com.cad.dxflib.common.Point3D dxfEnd = dxfLine.getEndPoint();

        // Using dxflib.common.Point2D for calculations to match existing logic style
        Point2D p1 = new Point2D(dxfStart.x, dxfStart.y);
        Point2D p2 = new Point2D(dxfEnd.x, dxfEnd.y);
        double px = point.x;
        double py = point.y;

        double lenSq = (p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y);
        if (lenSq == 0) { // Line is a point
            return point.distanceTo(p1) <= tolerance;
        }

        double t = ((px - p1.x) * (p2.x - p1.x) + (py - p1.y) * (p2.y - p1.y)) / lenSq;
        t = Math.max(0, Math.min(1, t)); // Clamp t to the range [0, 1]

        double closestX = p1.x + t * (p2.x - p1.x);
        double closestY = p1.y + t * (p2.y - p1.y);
        Point2D closestPoint = new Point2D(closestX, closestY);

        return point.distanceTo(closestPoint) <= tolerance;
    }

    public boolean isPointNearDxfCircle(Point2D point, com.cad.dxflib.entities.DxfCircle dxfCircle, double tolerance) {
        com.cad.dxflib.common.Point3D dxfCenter = dxfCircle.getCenter();
        Point2D center = new Point2D(dxfCenter.x, dxfCenter.y);
        double radius = dxfCircle.getRadius();

        double distToCenter = point.distanceTo(center);
        return Math.abs(distToCenter - radius) <= tolerance;
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

            if (selectedEntity == null) { // Check imported DXF entities if no drawn entity was selected
                for (DxfEntity entity : importedDxfEntities) {
                    if (entity instanceof com.cad.dxflib.entities.DxfLine) {
                        if (isPointNearDxfLine(modelPoint, (com.cad.dxflib.entities.DxfLine) entity, HIT_TOLERANCE / currentScale)) {
                            selectedEntity = entity;
                            break;
                        }
                    } else if (entity instanceof com.cad.dxflib.entities.DxfCircle) {
                        if (isPointNearDxfCircle(modelPoint, (com.cad.dxflib.entities.DxfCircle) entity, HIT_TOLERANCE / currentScale)) {
                            selectedEntity = entity;
                            break;
                        }
                    }
                    // Future: Add support for other DxfEntity types like DxfArc, DxfLwPolyline
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

            // Render DXF content using Batik
            if (this.gvtRoot != null) {
                try {
                    // Optional: Set rendering hints for quality if Batik doesn't handle them internally via SVG
                    // g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Already set earlier
                    // g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                    this.gvtRoot.paint(g2d);
                } catch (Exception e) {
                    e.printStackTrace(); // Log rendering errors
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

            // Highlight selected imported DXF entities
            if (selectedEntity instanceof com.cad.dxflib.entities.DxfLine) {
                com.cad.dxflib.entities.DxfLine dxfLine = (com.cad.dxflib.entities.DxfLine) selectedEntity;
                com.cad.dxflib.common.Point3D start = dxfLine.getStartPoint();
                com.cad.dxflib.common.Point3D end = dxfLine.getEndPoint();

                g2d.setColor(Color.MAGENTA); // Highlight color for selected DXF entities
                g2d.setStroke(new BasicStroke(3)); // Thicker stroke for highlight
                g2d.drawLine((int) start.x, (int) start.y, (int) end.x, (int) end.y);

            } else if (selectedEntity instanceof com.cad.dxflib.entities.DxfCircle) {
                com.cad.dxflib.entities.DxfCircle dxfCircle = (com.cad.dxflib.entities.DxfCircle) selectedEntity;
                com.cad.dxflib.common.Point3D center = dxfCircle.getCenter();
                double radius = dxfCircle.getRadius();

                g2d.setColor(Color.MAGENTA); // Highlight color
                g2d.setStroke(new BasicStroke(3)); // Thicker stroke
                g2d.drawArc(
                    (int) (center.x - radius),
                    (int) (center.y - radius),
                    (int) (2 * radius),
                    (int) (2 * radius),
                    0, 360
                );
            }
            // Reset stroke to default if other drawing operations were to follow
            // g2d.setStroke(new BasicStroke(1));

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
