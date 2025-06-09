package com.cad.gui;

import com.cad.dxflib.common.Point2D;
import com.cad.dxflib.parser.DxfParserException;
import com.cad.gui.tool.ActiveTool;
import com.cad.gui.tool.ToolManager;
import com.cad.modules.geometry.entities.Circle2D;
import com.cad.modules.geometry.entities.Line2D;
import com.cad.modules.rendering.DxfRenderService;
import com.cad.modules.rendering.DxfProcessingResult; // Added import

import javax.swing.*; // Required for JOptionPane in loadDxfFromFile
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CadPanelLogic {

    // Services and Managers
    DxfRenderService dxfRenderService;
    ToolManager toolManager;

    // Drawing State
    List<Line2D> drawnLines;
    List<Circle2D> drawnCircles;
    Object selectedEntity;

    // Current operation state
    Point2D lineStartPoint;
    Point2D previewEndPoint;
    Point2D circleCenterPoint;
    double previewRadius;

    // Viewport/Transform State
    String baseSvgContent;
    double currentScale;
    double translateX;
    double translateY;
    Point2D panLastMousePosition;

    static final double HIT_TOLERANCE = 5.0;

    public CadPanelLogic(ToolManager toolManager, DxfRenderService dxfRenderService) {
        this.toolManager = toolManager;
        this.dxfRenderService = dxfRenderService;

        this.drawnLines = new ArrayList<>();
        this.drawnCircles = new ArrayList<>();
        this.selectedEntity = null;
        this.lineStartPoint = null;
        this.previewEndPoint = null;
        this.circleCenterPoint = null;
        this.previewRadius = 0;
        this.baseSvgContent = null;
        this.currentScale = 1.0;
        this.translateX = 0.0;
        this.translateY = 0.0;
        this.panLastMousePosition = null;
    }

    public void clearPreviewLineState() {
        this.previewEndPoint = null;
    }

    public void clearPreviewCircleState() {
        this.previewRadius = 0;
    }

    public void loadDxfFromFile(File fileToOpen) {
        drawnLines.clear();
        drawnCircles.clear();
        currentScale = 1.0;
        translateX = 0.0;
        translateY = 0.0;
        baseSvgContent = null;
        selectedEntity = null;
        previewEndPoint = null;
        previewRadius = 0;
        panLastMousePosition = null;

        try (FileInputStream fis = new FileInputStream(fileToOpen)) {
            DxfProcessingResult result = dxfRenderService.loadDxf(fis, fileToOpen.getName());
            if (result != null) {
                baseSvgContent = result.svgString;
                // Potentially, if CadPanelLogic needs to be aware of the DxfDocument for other reasons:
                // DxfDocument dxfDocument = result.dxfDocument;
                // For now, we only need baseSvgContent based on the error.
            } else {
                // Handle case where result might be null, though loadDxf should throw exceptions
                baseSvgContent = null;
                // Consider logging an error or showing a message
                System.err.println("Falha ao processar DXF: resultado nulo de dxfRenderService.loadDxf");
                JOptionPane.showMessageDialog(null, "Falha ao processar o arquivo DXF: resultado nulo.", "Erro de DXF", JOptionPane.ERROR_MESSAGE);
            }
        } catch (FileNotFoundException ex) {
            System.err.println("Arquivo não encontrado: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Arquivo não encontrado: " + fileToOpen.getAbsolutePath(), "Erro ao Abrir Arquivo", JOptionPane.ERROR_MESSAGE);
            baseSvgContent = null;
        } catch (DxfParserException ex) {
            System.err.println("Erro ao parsear DXF: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Erro ao processar o arquivo DXF: " + ex.getMessage(), "Erro de DXF", JOptionPane.ERROR_MESSAGE);
            baseSvgContent = null;
        } catch (IOException ex) { // Covers DxfParserException as well if it's an IOException subclass, but specific catch is good.
            System.err.println("Erro de I/O ou ao parsear DXF: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Erro de I/O ou ao processar o arquivo DXF: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            baseSvgContent = null;
        }
    }

    public void handleMousePress(Point2D currentPoint, ActiveTool activeTool) {
        if (activeTool == ActiveTool.DRAW_LINE) {
            if (lineStartPoint == null) {
                lineStartPoint = currentPoint;
                previewEndPoint = currentPoint; // Initialize preview
            } else {
                drawnLines.add(new Line2D(lineStartPoint, currentPoint));
                lineStartPoint = null;
                previewEndPoint = null; // Clear preview
            }
        } else if (activeTool == ActiveTool.DRAW_CIRCLE) {
            if (circleCenterPoint == null) {
                circleCenterPoint = currentPoint;
                previewRadius = 0; // Initialize preview
            } else {
                double radius = Math.sqrt(Math.pow(currentPoint.x - circleCenterPoint.x, 2) + Math.pow(currentPoint.y - circleCenterPoint.y, 2));
                if (radius > 0) {
                    drawnCircles.add(new Circle2D(circleCenterPoint, radius));
                }
                circleCenterPoint = null;
                previewRadius = 0; // Clear preview
            }
        } else if (activeTool == ActiveTool.SELECT) {
            selectedEntity = null; // Clear previous selection
            // Check circles first (typically fewer, or specific selection order)
            for (int i = drawnCircles.size() - 1; i >= 0; i--) {
                Circle2D circle = drawnCircles.get(i);
                if (isPointNearCircle(currentPoint, circle, HIT_TOLERANCE)) {
                    selectedEntity = circle;
                    break;
                }
            }
            // If no circle selected, check lines
            if (selectedEntity == null) {
                for (int i = drawnLines.size() - 1; i >= 0; i--) {
                    Line2D line = drawnLines.get(i);
                    if (isPointNearLine(currentPoint, line, HIT_TOLERANCE)) {
                        selectedEntity = line;
                        break;
                    }
                }
            }
        } else if (activeTool == ActiveTool.ZOOM_IN) {
            applyZoom(currentPoint.x, currentPoint.y, 1.25);
        } else if (activeTool == ActiveTool.ZOOM_OUT) {
            applyZoom(currentPoint.x, currentPoint.y, 0.8);
        } else if (activeTool == ActiveTool.PAN) {
            panLastMousePosition = currentPoint;
        }
    }

    public void handleMouseRelease(Point2D currentPoint, ActiveTool activeTool) {
        if (activeTool == ActiveTool.PAN) {
            panLastMousePosition = null;
        }
    }

    public void handleMouseDrag(Point2D currentPoint, ActiveTool activeTool) {
        if (activeTool == ActiveTool.DRAW_LINE && lineStartPoint != null) {
            previewEndPoint = currentPoint;
        } else if (activeTool == ActiveTool.DRAW_CIRCLE && circleCenterPoint != null) {
            previewRadius = Math.sqrt(Math.pow(currentPoint.x - circleCenterPoint.x, 2) + Math.pow(currentPoint.y - circleCenterPoint.y, 2));
            if (previewRadius < 0) previewRadius = 0;
        } else if (activeTool == ActiveTool.PAN && panLastMousePosition != null) {
            double dx = currentPoint.x - panLastMousePosition.x;
            double dy = currentPoint.y - panLastMousePosition.y;
            translateX += dx;
            translateY += dy;
            panLastMousePosition = currentPoint;
        } else {
            // If dragging with no specific tool action, clear previews if any were active
            if (previewEndPoint != null) previewEndPoint = null;
            if (previewRadius > 0) previewRadius = 0;
        }
    }

    public void applyZoom(double mouseX, double mouseY, double scaleFactor) {
        double preZoomWorldX = (mouseX - translateX) / currentScale;
        double preZoomWorldY = (mouseY - translateY) / currentScale;
        currentScale *= scaleFactor;
        translateX = mouseX - preZoomWorldX * currentScale;
        translateY = mouseY - preZoomWorldY * currentScale;
    }

    public String generateSvgContent() {
        StringBuilder svgBuilder = new StringBuilder();
        svgBuilder.append("<svg width=\"100%\" height=\"100%\" xmlns=\"http://www.w3.org/2000/svg\">");
        svgBuilder.append("<g transform=\"translate(").append(translateX)
                  .append(",").append(translateY)
                  .append(") scale(").append(currentScale).append(")\">");

        if (baseSvgContent != null) {
            int firstSvgTagEnd = baseSvgContent.indexOf('>');
            int lastSvgTagStart = baseSvgContent.lastIndexOf("</svg>");
            if (firstSvgTagEnd != -1 && lastSvgTagStart != -1 && firstSvgTagEnd < lastSvgTagStart) {
                svgBuilder.append(baseSvgContent.substring(firstSvgTagEnd + 1, lastSvgTagStart));
            } else {
                System.err.println("Could not strip <svg> tags from baseSvgContent.");
            }
        }

        for (Line2D line : drawnLines) {
            String lineStrokeColor = (line == selectedEntity) ? "red" : "black";
            String lineStrokeWidth = (line == selectedEntity) ? "3" : "2";
            svgBuilder.append("<line x1=\"").append(line.getStartPoint().x)
                      .append("\" y1=\"").append(line.getStartPoint().y)
                      .append("\" x2=\"").append(line.getEndPoint().x)
                      .append("\" y2=\"").append(line.getEndPoint().y)
                      .append("\" stroke=\"").append(lineStrokeColor)
                      .append("\" stroke-width=\"").append(lineStrokeWidth)
                      .append("\"/>");
        }

        for (Circle2D circle : drawnCircles) {
            String circleStrokeColor = (circle == selectedEntity) ? "red" : "blue";
            String circleStrokeWidth = (circle == selectedEntity) ? "3" : "2";
            svgBuilder.append("<circle cx=\"").append(circle.getCenter().x)
                      .append("\" cy=\"").append(circle.getCenter().y)
                      .append("\" r=\"").append(circle.getRadius())
                      .append("\" stroke=\"").append(circleStrokeColor)
                      .append("\" stroke-width=\"").append(circleStrokeWidth)
                      .append("\" fill=\"none\"/>");
        }

        if (toolManager.getActiveTool() == ActiveTool.DRAW_LINE && lineStartPoint != null && previewEndPoint != null) {
            svgBuilder.append("<line x1=\"").append(lineStartPoint.x)
                      .append("\" y1=\"").append(lineStartPoint.y)
                      .append("\" x2=\"").append(previewEndPoint.x)
                      .append("\" y2=\"").append(previewEndPoint.y)
                      .append("\" stroke=\"gray\" stroke-width=\"1\" stroke-dasharray=\"5,5\"/>");
        }

        if (toolManager.getActiveTool() == ActiveTool.DRAW_CIRCLE && circleCenterPoint != null && previewRadius > 0) {
            svgBuilder.append("<circle cx=\"").append(circleCenterPoint.x)
                      .append("\" cy=\"").append(circleCenterPoint.y)
                      .append("\" r=\"").append(previewRadius)
                      .append("\" stroke=\"gray\" stroke-width=\"1\" stroke-dasharray=\"5,5\" fill=\"none\"/>");
        }

        svgBuilder.append("</g>");
        svgBuilder.append("</svg>");
        return svgBuilder.toString();
    }

    // Utility methods moved from MainFrame
    boolean isPointNearLine(Point2D p, Line2D line, double tolerance) {
        Point2D p1 = line.getStartPoint();
        Point2D p2 = line.getEndPoint();
        double dxL = p2.x - p1.x;
        double dyL = p2.y - p1.y;
        if (dxL == 0 && dyL == 0) {
            return Math.sqrt(Math.pow(p.x - p1.x, 2) + Math.pow(p.y - p1.y, 2)) <= tolerance;
        }
        double t = ((p.x - p1.x) * dxL + (p.y - p1.y) * dyL) / (dxL * dxL + dyL * dyL);
        Point2D closestPoint;
        if (t < 0) closestPoint = p1;
        else if (t > 1) closestPoint = p2;
        else closestPoint = new Point2D(p1.x + t * dxL, p1.y + t * dyL);
        double dist = Math.sqrt(Math.pow(p.x - closestPoint.x, 2) + Math.pow(p.y - closestPoint.y, 2));
        return dist <= tolerance;
    }

    boolean isPointNearCircle(Point2D point, Circle2D circle, double tolerance) {
        Point2D center = circle.getCenter();
        double radius = circle.getRadius();
        double distToCenter = Math.sqrt(Math.pow(point.x - center.x, 2) + Math.pow(point.y - center.y, 2));
        return Math.abs(distToCenter - radius) <= tolerance;
    }
}
