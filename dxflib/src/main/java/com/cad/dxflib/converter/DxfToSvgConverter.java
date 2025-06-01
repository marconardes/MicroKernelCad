package com.cad.dxflib.converter;

import com.cad.dxflib.structure.DxfDocument;
import com.cad.dxflib.math.Bounds;
import com.cad.dxflib.common.DxfEntity;
import com.cad.dxflib.common.Point2D; // For LwPolyline
import com.cad.dxflib.entities.DxfArc;
import com.cad.dxflib.entities.DxfCircle;
import com.cad.dxflib.entities.DxfLine;
import com.cad.dxflib.entities.DxfLwPolyline;
import com.cad.dxflib.entities.DxfText;
import com.cad.dxflib.entities.DxfInsert; // Added import
import com.cad.dxflib.structure.DxfBlock; // Added import
import com.cad.dxflib.structure.DxfLayer;
import java.util.List; // For LwPolyline vertices
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

        // Iterate through model space entities
        if (dxfDocument.getModelSpaceEntities() != null) {
            for (DxfEntity entity : dxfDocument.getModelSpaceEntities()) {
                if (entity == null) continue;

                switch (entity.getType()) {
                    case LINE:
                        appendLineSvg((DxfLine) entity, dxfDocument, options, svgBuilder);
                        break;
                    case CIRCLE:
                        appendCircleSvg((DxfCircle) entity, dxfDocument, options, svgBuilder);
                        break;
                    case ARC:
                        appendArcSvg((DxfArc) entity, dxfDocument, options, svgBuilder);
                        break;
                    case LWPOLYLINE:
                        appendLwPolylineSvg((DxfLwPolyline) entity, dxfDocument, options, svgBuilder);
                        break;
                    case TEXT:
                        appendTextSvg((DxfText) entity, dxfDocument, options, svgBuilder);
                        break;
                    case INSERT:
                        appendInsertSvg((DxfInsert) entity, dxfDocument, options, svgBuilder, 0); // Nível inicial de recursão
                        break;
                    default:
                        // Skip unknown or unsupported entities for now
                        break;
                }
            }
        }

        svgBuilder.append("  <!-- DXF Content End -->\n");

        // End SVG structure
        svgBuilder.append("</svg>\n");

        return svgBuilder.toString();
    }

    private String getDxfColorAsSvg(int dxfColorIndex, DxfDocument document, String layerName, SvgConversionOptions options) {
        if (dxfColorIndex == 0) { // BYBLOCK
            dxfColorIndex = 256;
        }

        if (dxfColorIndex == 256) { // BYLAYER
            if (layerName != null && document != null) {
                DxfLayer layer = document.getLayer(layerName);
                if (layer != null) {
                    dxfColorIndex = layer.getColor();
                    if (dxfColorIndex < 0) dxfColorIndex = Math.abs(dxfColorIndex);
                } else {
                    return options.getDefaultStrokeColor();
                }
            } else {
                return options.getDefaultStrokeColor();
            }
        }

        switch (dxfColorIndex) {
            case 1: return "red";
            case 2: return "yellow";
            case 3: return "green";
            case 4: return "cyan";
            case 5: return "blue";
            case 6: return "magenta";
            case 7: return "white";
            case 8: return "gray";
            case 9: return "lightgray";
            default:
                return options.getDefaultStrokeColor();
        }
    }

    private void appendLineSvg(DxfLine line, DxfDocument document, SvgConversionOptions options, StringBuilder svgBuilder) {
        String color = getDxfColorAsSvg(line.getColor(), document, line.getLayerName(), options);
        svgBuilder.append(String.format(Locale.US,
                "    <line x1=\"%.3f\" y1=\"%.3f\" x2=\"%.3f\" y2=\"%.3f\" stroke=\"%s\" stroke-width=\"%.3f\" />\n",
                line.getStartPoint().x,
                line.getStartPoint().y,
                line.getEndPoint().x,
                line.getEndPoint().y,
                color,
                options.getStrokeWidth()));
    }

    private void appendCircleSvg(DxfCircle circle, DxfDocument document, SvgConversionOptions options, StringBuilder svgBuilder) {
        String color = getDxfColorAsSvg(circle.getColor(), document, circle.getLayerName(), options);
        svgBuilder.append(String.format(Locale.US,
                "    <circle cx=\"%.3f\" cy=\"%.3f\" r=\"%.3f\" stroke=\"%s\" stroke-width=\"%.3f\" fill=\"none\" />\n",
                circle.getCenter().x,
                circle.getCenter().y,
                circle.getRadius(),
                color,
                options.getStrokeWidth()));
    }

    private void appendArcSvg(DxfArc arc, DxfDocument document, SvgConversionOptions options, StringBuilder svgBuilder) {
        String color = getDxfColorAsSvg(arc.getColor(), document, arc.getLayerName(), options);

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
                "    <path d=\"%s\" stroke=\"%s\" stroke-width=\"%.3f\" fill=\"none\" />\n",
                pathData,
                color,
                options.getStrokeWidth()));
    }

    private void appendLwPolylineSvg(DxfLwPolyline lwpoly, DxfDocument document, SvgConversionOptions options, StringBuilder svgBuilder) {
        String color = getDxfColorAsSvg(lwpoly.getColor(), document, lwpoly.getLayerName(), options);
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

            if (bulge == 0.0) {
                pathData.append(String.format(Locale.US, " L %.3f,%.3f", p2.x, p2.y));
            } else {
                // TODO: Implement full bulge to SVG arc conversion.
                pathData.append(String.format(Locale.US, " L %.3f,%.3f", p2.x, p2.y));
            }
        }

        if (lwpoly.isClosed()) {
            if ((vertices.size() -1) >= bulges.size() || bulges.get(vertices.size()-1) == 0.0 ) {
                 pathData.append(" Z");
            } else {
                 pathData.append(" Z");
            }
        }

        String fill = "none";
        double strokeWidth = (lwpoly.getConstantWidth() > 0) ? lwpoly.getConstantWidth() : options.getStrokeWidth();

        svgBuilder.append(String.format(Locale.US,
                "    <path d=\"%s\" stroke=\"%s\" stroke-width=\"%.3f\" fill=\"%s\" />\n",
                pathData.toString(),
                color,
                strokeWidth,
                fill));
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

        // Store current length of svgBuilder to clear it if we only write the group tag
        int openingGTagStart = svgBuilder.length();
        svgBuilder.append("  <g transform=\"");
        boolean transformApplied = false;

        // Apply transformations in order: translate to insertion point, rotate, scale, translate by negative block base
        svgBuilder.append(String.format(Locale.US, "translate(%.3f, %.3f)", insertPt.x, insertPt.y));
        transformApplied = true;

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
            boolean changedLayer = false;
            boolean changedColor = false;

            if ("0".equals(originalEntityLayer)) {
                entityInBlock.setLayerName(insert.getLayerName());
                changedLayer = true;
            }
            if (entityInBlock.getColor() == 0) { // BYBLOCK
                changedColor = true;
                if (insert.getColor() == 256) {
                    DxfLayer insertLayer = document.getLayer(insert.getLayerName());
                    entityInBlock.setColor(insertLayer != null ? insertLayer.getColor() : 7);
                } else if (insert.getColor() != 0) {
                     entityInBlock.setColor(insert.getColor());
                } else {
                    entityInBlock.setColor(7);
                }
            }

            switch (entityInBlock.getType()) {
                case LINE: appendLineSvg((DxfLine) entityInBlock, document, options, svgBuilder); break;
                case CIRCLE: appendCircleSvg((DxfCircle) entityInBlock, document, options, svgBuilder); break;
                case ARC: appendArcSvg((DxfArc) entityInBlock, document, options, svgBuilder); break;
                case LWPOLYLINE: appendLwPolylineSvg((DxfLwPolyline) entityInBlock, document, options, svgBuilder); break;
                case TEXT: appendTextSvg((DxfText) entityInBlock, document, options, svgBuilder); break;
                case INSERT: appendInsertSvg((DxfInsert) entityInBlock, document, options, svgBuilder, recursionLevel + 1); break;
                default: break;
            }

            if (changedLayer) entityInBlock.setLayerName(originalEntityLayer);
            if (changedColor) entityInBlock.setColor(originalEntityColor);
        }
        svgBuilder.append("  </g>\n");
    }
}
