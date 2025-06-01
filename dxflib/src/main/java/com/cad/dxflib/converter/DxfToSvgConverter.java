package com.cad.dxflib.converter;

import com.cad.dxflib.structure.DxfDocument;
import com.cad.dxflib.math.Bounds;
import com.cad.dxflib.common.DxfEntity;
import com.cad.dxflib.common.EntityType; // Added import
import com.cad.dxflib.common.Point2D; // For LwPolyline
import com.cad.dxflib.common.Point3D; // Added for Point3D
import com.cad.dxflib.entities.DxfArc;
import com.cad.dxflib.entities.DxfCircle;
import com.cad.dxflib.entities.DxfLine;
import com.cad.dxflib.entities.DxfLwPolyline;
import com.cad.dxflib.entities.DxfText;
import com.cad.dxflib.entities.DxfInsert;
import com.cad.dxflib.structure.DxfBlock;
import com.cad.dxflib.structure.DxfLayer;
import com.cad.dxflib.structure.DxfLinetype; // Added
import java.util.List;
import java.util.Locale;

public class DxfToSvgConverter {

    private static final int MAX_INSERT_RECURSION_DEPTH = 16; // Prevenir recursão infinita

    public String convert(DxfDocument dxfDocument, SvgConversionOptions options) {
        if (dxfDocument == null) {
            throw new IllegalArgumentException("DxfDocument cannot be null.");
        }
        if (options == null) {
            options = new SvgConversionOptions(); // Use defaults if none provided
        }

        StringBuilder svgBuilder = new StringBuilder();
        Bounds documentBounds = dxfDocument.getBounds();

        double margin = options.getMargin();
        double svgWidth, svgHeight;
        String viewBox;

        if (documentBounds.isValid()) {
            // Option 2: Standard viewBox and use transform on a group (more conventional for SVG)
            double viewBox_minX = documentBounds.getMinX() - margin;
            double viewBox_minY = documentBounds.getMinY() - margin; // Standard minY
            double viewBox_width = documentBounds.getWidth() + (2 * margin);
            double viewBox_height = documentBounds.getHeight() + (2 * margin);

            // Ensure width and height are not zero if bounds are just a point or a line
            if (viewBox_width == 0) viewBox_width = 1;
            if (viewBox_height == 0) viewBox_height = 1;

            svgWidth = viewBox_width;
            svgHeight = viewBox_height;

            viewBox = String.format(Locale.US, "%.3f %.3f %.3f %.3f",
                                    viewBox_minX,
                                    viewBox_minY, // Using standard minY from bounds
                                    viewBox_width,
                                    viewBox_height);
        } else {
            // Default for empty or invalid bounds document
            svgWidth = 100 + (2 * margin); // Default width
            svgHeight = 100 + (2 * margin); // Default height
            viewBox = String.format(Locale.US, "%.3f %.3f %.3f %.3f", -margin, -margin, svgWidth, svgHeight);
        }

        // Start SVG structure
        svgBuilder.append("<svg xmlns=\"http://www.w3.org/2000/svg\" ")
                  .append("width=\"").append(String.format(Locale.US, "%.3f", svgWidth)).append("\" ")
                  .append("height=\"").append(String.format(Locale.US, "%.3f", svgHeight)).append("\" ")
                  .append("viewBox=\"").append(viewBox).append("\">\n");

        svgBuilder.append("  <!-- DXF Content Start -->\n");

        if (options.isGroupElementsByLayer()) {
            for (DxfLayer layer : dxfDocument.getLayers().values()) {
                if (layer == null || !layer.isVisible()) {
                    continue;
                }
                String layerId = "layer_" + layer.getName().replaceAll("[^a-zA-Z0-9_\\-]", "_");
                svgBuilder.append(String.format(Locale.US, "  <g id=\"%s\" class=\"layer %s\">\n", layerId, layerId));

                if (layer.getEntities() != null) {
                    for (DxfEntity entity : layer.getEntities()) {
                        if (entity == null) continue;
                        appendEntityToSvg(entity, dxfDocument, options, svgBuilder, 0);
                    }
                }
                svgBuilder.append("  </g> <!-- end layer ").append(layerId).append(" -->\n");
            }
        } else {
            // Iterate through all entities in all visible layers if not grouping
            for (DxfLayer layer : dxfDocument.getLayers().values()) {
                if (layer == null || !layer.isVisible()) {
                    continue;
                }
                if (layer.getEntities() != null) {
                    for (DxfEntity entity : layer.getEntities()) {
                        if (entity == null) continue;
                        appendEntityToSvg(entity, dxfDocument, options, svgBuilder, 0);
                    }
                }
            }
        }

        svgBuilder.append("  <!-- DXF Content End -->\n");

        // End SVG structure
        svgBuilder.append("</svg>\n");

        return svgBuilder.toString();
    }

    private void appendEntityToSvg(DxfEntity entity, DxfDocument document, SvgConversionOptions options,
                               StringBuilder svgBuilder, int recursionLevel) {
        switch (entity.getType()) {
            case LINE:
                appendLineSvg((DxfLine) entity, document, options, svgBuilder);
                break;
            case CIRCLE:
                appendCircleSvg((DxfCircle) entity, document, options, svgBuilder);
                break;
            case ARC:
                appendArcSvg((DxfArc) entity, document, options, svgBuilder);
                break;
            case LWPOLYLINE:
                appendLwPolylineSvg((DxfLwPolyline) entity, document, options, svgBuilder);
                break;
            case TEXT:
                appendTextSvg((DxfText) entity, document, options, svgBuilder);
                break;
            case INSERT:
                appendInsertSvg((DxfInsert) entity, document, options, svgBuilder, recursionLevel);
                break;
            default:
                break;
        }
    }

    private String getCommonSvgStyleAttributes(DxfEntity entity, DxfDocument document, SvgConversionOptions options) {
        StringBuilder styleBuilder = new StringBuilder();

        // Color
        String svgColor = getDxfColorAsSvg(entity.getColor(), document, entity.getLayerName(), options);
        styleBuilder.append(String.format(Locale.US, "stroke=\"%s\" ", svgColor));

        // Stroke Width
        double strokeWidth = options.getStrokeWidth();
        if (entity.getType() == EntityType.LWPOLYLINE) {
           DxfLwPolyline poly = (DxfLwPolyline) entity;
           if (poly.getConstantWidth() > 0) {
               strokeWidth = poly.getConstantWidth();
           }
        }
        styleBuilder.append(String.format(Locale.US, "stroke-width=\"%.3f\" ", strokeWidth));

        // Linetype -> stroke-dasharray
        String linetypeName = entity.getLinetypeName();
        if (linetypeName == null || "BYLAYER".equalsIgnoreCase(linetypeName)) {
            DxfLayer layer = document.getLayer(entity.getLayerName());
            if (layer != null) {
                linetypeName = layer.getLinetypeName();
            } else {
                linetypeName = "CONTINUOUS"; // Fallback
            }
        }

        if ("BYBLOCK".equalsIgnoreCase(linetypeName)) {
            // This part is tricky without context of the INSERT.
            // For direct entities or entities within a block being resolved by INSERT,
            // this context needs to be passed down or handled.
            // For now, if we encounter BYBLOCK at this level, assume CONTINUOUS.
            // The appendInsertSvg method will handle BYBLOCK for entities within the block.
            linetypeName = "CONTINUOUS";
        }

        if (linetypeName != null && !linetypeName.equalsIgnoreCase("CONTINUOUS")) {
            DxfLinetype ltypeDef = document.getLinetype(linetypeName);
            if (ltypeDef != null && !ltypeDef.isContinuous()) {
                String dashArray = ltypeDef.getSvgStrokeDashArray();
                if (!"none".equals(dashArray) && !dashArray.isEmpty()) {
                    styleBuilder.append(String.format(Locale.US, "stroke-dasharray=\"%s\" ", dashArray));
                }
            }
        }

        // Fill
        if (entity.getType() == EntityType.CIRCLE ||
            entity.getType() == EntityType.LWPOLYLINE || // Changed line
            entity.getType() == EntityType.ARC ) { // Arcs are open paths, fill=none is typical
             styleBuilder.append("fill=\"none\" ");
        }
        // TEXT fill is handled in appendTextSvg

        return styleBuilder.toString().trim();
    }

    private String getDxfColorAsSvg(int dxfColorIndex, DxfDocument document, String layerName, SvgConversionOptions options) {
        int resolvedColorIndex = dxfColorIndex;

        if (resolvedColorIndex == 0) { // BYBLOCK
            resolvedColorIndex = 256;
        }

        if (resolvedColorIndex == 256) { // BYLAYER
            if (layerName != null && document != null) {
                DxfLayer layer = document.getLayer(layerName);
                if (layer != null) {
                    resolvedColorIndex = layer.getColor();
                    if (resolvedColorIndex < 0) resolvedColorIndex = Math.abs(resolvedColorIndex);
                    if (resolvedColorIndex == 0 || resolvedColorIndex == 256) {
                        return options.getDefaultStrokeColor();
                    }
                } else {
                    return options.getDefaultStrokeColor();
                }
            } else {
                return options.getDefaultStrokeColor();
            }
        }

        switch (resolvedColorIndex) {
            case 1: return "red";
            case 2: return "yellow";
            case 3: return "green";
            case 4: return "cyan";
            case 5: return "blue";
            case 6: return "magenta";
            case 7: return options.getDefaultStrokeColor();
            case 8: return "#808080"; // Dark Grey
            case 9: return "#C0C0C0"; // Light Grey (Silver)
            case 10: return "#FF0000"; // Red
            case 11: return "#FF3F3F";
            case 12: return "#FF7F7F";
            case 30: return "#00FF00"; // Green
            case 40: return "#00FFFF"; // Cyan
            case 50: return "#0000FF"; // Blue
            case 60: return "#FF00FF"; // Magenta
            case 14: return "darkcyan";
            case 250: return "#2F2F2F";
            case 251: return "#4C4C4C";
            case 252: return "#7F7F7F";
            case 253: return "#B2B2B2";
            case 254: return "#DFDFDF";
            case 255: return "#F0F0F0";
            default:
                return options.getDefaultStrokeColor();
        }
    }

    private void appendLineSvg(DxfLine line, DxfDocument document, SvgConversionOptions options, StringBuilder svgBuilder) {
        String styleAttributes = getCommonSvgStyleAttributes(line, document, options);
        svgBuilder.append(String.format(Locale.US,
                "    <line x1=\"%.3f\" y1=\"%.3f\" x2=\"%.3f\" y2=\"%.3f\" %s />\n",
                line.getStartPoint().x,
                line.getStartPoint().y,
                line.getEndPoint().x,
                line.getEndPoint().y,
                styleAttributes));
    }

    private void appendCircleSvg(DxfCircle circle, DxfDocument document, SvgConversionOptions options, StringBuilder svgBuilder) {
        String styleAttributes = getCommonSvgStyleAttributes(circle, document, options);
        svgBuilder.append(String.format(Locale.US,
                "    <circle cx=\"%.3f\" cy=\"%.3f\" r=\"%.3f\" %s />\n",
                circle.getCenter().x,
                circle.getCenter().y,
                circle.getRadius(),
                styleAttributes));
    }

    private void appendArcSvg(DxfArc arc, DxfDocument document, SvgConversionOptions options, StringBuilder svgBuilder) {
        String styleAttributes = getCommonSvgStyleAttributes(arc, document, options);

        double radius = arc.getRadius();
        Point3D center = arc.getCenter();

        double startAngleRad = Math.toRadians(arc.getStartAngle());
        double endAngleRad = Math.toRadians(arc.getEndAngle());

        double startX = center.x + radius * Math.cos(startAngleRad);
        double startY = center.y + radius * Math.sin(startAngleRad);
        double endX = center.x + radius * Math.cos(endAngleRad);
        double endY = center.y + radius * Math.sin(endAngleRad);

        double angleSweep = arc.getEndAngle() - arc.getStartAngle();
        if (angleSweep < 0) {
            angleSweep += 360;
        }
        int largeArcFlag = (angleSweep > 180) ? 1 : 0;
        int sweepFlag = 1;

        String pathData = String.format(Locale.US,
                "M %.3f,%.3f A %.3f,%.3f 0 %d,%d %.3f,%.3f",
                startX, startY,
                radius, radius,
                largeArcFlag, sweepFlag,
                endX, endY);

        svgBuilder.append(String.format(Locale.US,
                "    <path d=\"%s\" %s />\n",
                pathData,
                styleAttributes));
    }

    private void appendLwPolylineSvg(DxfLwPolyline lwpoly, DxfDocument document, SvgConversionOptions options, StringBuilder svgBuilder) {
        List<Point2D> vertices = lwpoly.getVertices();
        List<Double> bulges = lwpoly.getBulges();

        if (vertices.isEmpty()) {
            return;
        }

        StringBuilder pathData = new StringBuilder();
        pathData.append(String.format(Locale.US, "M %.3f,%.3f", vertices.get(0).x, vertices.get(0).y));

        for (int i = 0; i < vertices.size() - 1; i++) {
            Point2D p1 = vertices.get(i);
            Point2D p2 = vertices.get(i + 1);
            double bulge = (i < bulges.size()) ? bulges.get(i) : 0.0;

            if (bulge == 0.0) { // Straight line segment
                pathData.append(String.format(Locale.US, " L %.3f,%.3f", p2.x, p2.y));
            } else { // Arc segment
                double dx = p2.x - p1.x;
                double dy = p2.y - p1.y;
                double chordLength = Math.sqrt(dx * dx + dy * dy);

                if (chordLength < 1e-9) {
                    pathData.append(String.format(Locale.US, " L %.3f,%.3f", p2.x, p2.y));
                } else {
                    double includedAngle = 4 * Math.atan(Math.abs(bulge));
                    double radius;
                    if (Math.abs(Math.sin(includedAngle / 2.0)) < 1e-9) {
                        radius = Double.POSITIVE_INFINITY;
                    } else {
                         radius = Math.abs( (chordLength / 2.0) / Math.sin(includedAngle / 2.0) );
                    }

                    if (Double.isInfinite(radius) || Double.isNaN(radius) || radius < 1e-9) {
                        pathData.append(String.format(Locale.US, " L %.3f,%.3f", p2.x, p2.y));
                    } else {
                        int largeArcFlag = (includedAngle > Math.PI) ? 1 : 0;
                        int sweepFlag = (bulge > 0) ? 1 : 0;

                        pathData.append(String.format(Locale.US,
                                " A %.3f,%.3f 0 %d,%d %.3f,%.3f",
                                radius, radius,
                                largeArcFlag,
                                sweepFlag,
                                p2.x, p2.y));
                    }
                }
            }
        }

        if (lwpoly.isClosed()) {
            // For a closed polyline, the last segment connects the last vertex to the first.
            // We need to check if there was a bulge specified for this closing segment.
            // DXF stores bulge on the *starting* vertex of an arc segment.
            // So, the bulge for the segment from last_vertex to first_vertex would be on the last_vertex.
            if (vertices.size() > 1) { // Only makes sense if there's more than one vertex
                double closingBulge = (vertices.size() -1 < bulges.size()) ? bulges.get(vertices.size()-1) : 0.0;
                if (closingBulge == 0.0) {
                    pathData.append(" Z"); // Simple close if no bulge on closing segment
                } else {
                    // Arc for the closing segment
                    Point2D p_last = vertices.get(vertices.size() - 1);
                    Point2D p_first = vertices.get(0);
                    double dx = p_first.x - p_last.x;
                    double dy = p_first.y - p_last.y;
                    double chordLength = Math.sqrt(dx*dx + dy*dy);

                    if (chordLength < 1e-9) {
                        pathData.append(" Z"); // Points are same, just close
                    } else {
                        double includedAngle = 4 * Math.atan(Math.abs(closingBulge));
                        double radius;
                        if (Math.abs(Math.sin(includedAngle / 2.0)) < 1e-9) {
                             radius = Double.POSITIVE_INFINITY;
                        } else {
                            radius = Math.abs( (chordLength / 2.0) / Math.sin(includedAngle / 2.0) );
                        }

                        if (Double.isInfinite(radius) || Double.isNaN(radius) || radius < 1e-9) {
                            pathData.append(" Z"); // Cannot form arc, just close
                        } else {
                            int largeArcFlag = (includedAngle > Math.PI) ? 1 : 0;
                            int sweepFlag = (closingBulge > 0) ? 1 : 0;
                            pathData.append(String.format(Locale.US,
                                    " A %.3f,%.3f 0 %d,%d %.3f,%.3f",
                                    radius, radius,
                                    largeArcFlag, sweepFlag,
                                    p_first.x, p_first.y));
                            // No explicit Z needed here as the arc command itself moves to the start point.
                            // However, some SVG renderers might behave better with an explicit Z if the path isn't auto-closed by fill.
                            // Since fill="none", an explicit Z might be better if the arc doesn't perfectly land.
                            // But for a path that is stroked, an arc to the start point is sufficient.
                        }
                    }
                }
            } else { // Single point polyline, or empty - just Z if closed (though M already at P0)
                 pathData.append(" Z");
            }
        }

        String fill = "none"; // Already handled by getCommonSvgStyleAttributes if logic is there
        // String color will come from styleAttributes
        // double strokeWidth will come from styleAttributes (or be overridden if constantWidth > 0)

        String styleAttributes = getCommonSvgStyleAttributes(lwpoly, document, options);
        // If lwpoly.getConstantWidth() > 0, styleAttributes already contains the correct stroke-width.
        // The getCommonSvgStyleAttributes was updated to handle this.

        svgBuilder.append(String.format(Locale.US,
                "    <path d=\"%s\" %s />\n", // fill="none" is part of styleAttributes now
                pathData.toString(),
                styleAttributes));
    }

    private void appendTextSvg(DxfText text, DxfDocument document, SvgConversionOptions options, StringBuilder svgBuilder) {
        String svgColor = getDxfColorAsSvg(text.getColor(), document, text.getLayerName(), options);
        Point3D insertionPoint = text.getInsertionPoint();
        double height = text.getHeight();
        String textValue = text.getTextValue();

        // Escape XML special characters in textValue
        textValue = textValue.replace("&", "&amp;")
                             .replace("<", "&lt;")
                             .replace(">", "&gt;")
                             .replace("\"", "&quot;")
                             .replace("'", "&apos;");

        String fontFamily = "Arial"; // Default
        if (text.getStyleName() != null && !text.getStyleName().equalsIgnoreCase("STANDARD")) {
            fontFamily = text.getStyleName();
        }

        svgBuilder.append(String.format(Locale.US,
                "    <text x=\"%.3f\" y=\"%.3f\" font-size=\"%.3f\" fill=\"%s\" font-family=\"%s\"",
                insertionPoint.x,
                insertionPoint.y,
                height,
                svgColor,
                fontFamily));

        if (text.getRotationAngle() != 0.0) {
            svgBuilder.append(String.format(Locale.US,
                    " transform=\"rotate(%.3f %.3f,%.3f)\"",
                    -text.getRotationAngle(),
                    insertionPoint.x,
                    insertionPoint.y));
        }

        svgBuilder.append(">")
                  .append(textValue)
                  .append("</text>\n");
    }

    private void appendInsertSvg(DxfInsert insert, DxfDocument document, SvgConversionOptions options,
                                 StringBuilder svgBuilder, int recursionLevel) {
        if (recursionLevel > MAX_INSERT_RECURSION_DEPTH) {
            // System.err.println("Max recursion depth reached for INSERT: " + insert.getBlockName());
            return;
        }

        DxfBlock block = document.getBlock(insert.getBlockName());
        if (block == null) {
            // System.err.println("Block definition not found for INSERT: " + insert.getBlockName());
            return;
        }

        Point3D insertPt = insert.getInsertionPoint();
        double xScale = insert.getXScale();
        double yScale = insert.getYScale();
        double rotation = insert.getRotationAngle();

        svgBuilder.append("  <g transform=\"");

        svgBuilder.append(String.format(Locale.US, "translate(%.3f, %.3f)", insertPt.x, insertPt.y));
        if (rotation != 0.0) {
            svgBuilder.append(String.format(Locale.US, " rotate(%.3f)", -rotation));
        }
        if (xScale != 1.0 || yScale != 1.0) {
            svgBuilder.append(String.format(Locale.US, " scale(%.3f, %.3f)", xScale, yScale));
        }
        Point3D blockBase = block.getBasePoint();
        if (blockBase.x != 0.0 || blockBase.y != 0.0) {
             svgBuilder.append(String.format(Locale.US, " translate(%.3f, %.3f)", -blockBase.x, -blockBase.y));
        }
        svgBuilder.append("\">\n");

        for (DxfEntity entityInBlock : block.getEntities()) {
            if (entityInBlock == null) continue;

            String originalEntityLayer = entityInBlock.getLayerName();
            int originalEntityColor = entityInBlock.getColor();
            String originalEntityLinetype = entityInBlock.getLinetypeName(); // Save linetype
            boolean changedLayer = false;
            boolean changedColor = false;
            boolean linetypeChanged = false;

            if ("0".equals(originalEntityLayer)) {
                entityInBlock.setLayerName(insert.getLayerName());
                changedLayer = true;
            }
            if (entityInBlock.getColor() == 0) { // BYBLOCK
                changedColor = true;
                if (insert.getColor() == 256) {
                    DxfLayer insertLayer = document.getLayer(insert.getLayerName());
                    entityInBlock.setColor(insertLayer != null ? Math.abs(insertLayer.getColor()) : 7); // Use absolute color
                } else if (insert.getColor() != 0) {
                     entityInBlock.setColor(insert.getColor());
                } else { // INSERT is also BYBLOCK
                    entityInBlock.setColor(7);
                }
            }
            if ("BYBLOCK".equalsIgnoreCase(entityInBlock.getLinetypeName())) {
                String insertLinetype = insert.getLinetypeName();
                if (insertLinetype == null || "BYLAYER".equalsIgnoreCase(insertLinetype)) {
                    DxfLayer insertLayer = document.getLayer(insert.getLayerName());
                    if (insertLayer != null) {
                        entityInBlock.setLinetypeName(insertLayer.getLinetypeName());
                        linetypeChanged = true;
                    } else {
                        entityInBlock.setLinetypeName("CONTINUOUS");
                        linetypeChanged = true;
                    }
                } else if (!"BYBLOCK".equalsIgnoreCase(insertLinetype)) {
                    entityInBlock.setLinetypeName(insertLinetype);
                    linetypeChanged = true;
                } else { // INSERT itself is BYBLOCK for linetype
                    entityInBlock.setLinetypeName("CONTINUOUS");
                    linetypeChanged = true;
                }
            }

            // Use the centralized dispatcher
            appendEntityToSvg(entityInBlock, document, options, svgBuilder, recursionLevel + 1);

            if (changedLayer) entityInBlock.setLayerName(originalEntityLayer);
            if (changedColor) entityInBlock.setColor(originalEntityColor);
            if (linetypeChanged) entityInBlock.setLinetypeName(originalEntityLinetype); // Restore linetype
        }
        svgBuilder.append("  </g>\n");
    }
}
