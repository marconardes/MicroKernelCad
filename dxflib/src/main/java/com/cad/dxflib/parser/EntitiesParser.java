package com.cad.dxflib.parser;

import com.cad.dxflib.common.AbstractDxfEntity;
import com.cad.dxflib.common.DxfEntity;
import com.cad.dxflib.common.Point2D;
import com.cad.dxflib.common.Point3D;
import com.cad.dxflib.structure.*;
import com.cad.dxflib.entities.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class EntitiesParser {

    private BufferedReader reader;
    private DxfGroupCode aktuellenGroupCode;

    public EntitiesParser(BufferedReader reader, DxfDocument document) {
        this.reader = reader;
        // this.document = document; // Field removed
    }

    public DxfGroupCode getAktuellenGroupCode() {
        return aktuellenGroupCode;
    }

    private DxfGroupCode nextGroupCode() throws IOException, DxfParserException {
        String codeStr = reader.readLine();
        if (codeStr == null) {
            return null;
        }
        String valueStr = reader.readLine();
        if (valueStr == null) {
            throw new DxfParserException("Unexpected end of file: missing value for code " + codeStr);
        }
        try {
            int code = Integer.parseInt(codeStr.trim());
            return new DxfGroupCode(code, valueStr.trim());
        } catch (NumberFormatException e) {
            throw new DxfParserException("Invalid group code format: " + codeStr, e);
        }
    }

    private void parseAndAttachXData(AbstractDxfEntity entity) throws IOException, DxfParserException {
        if (aktuellenGroupCode == null || aktuellenGroupCode.code != 1001) {
            return;
        }
        String appName = aktuellenGroupCode.value;
        List<DxfGroupCode> xdataList = new ArrayList<>();
        xdataList.add(aktuellenGroupCode);

        while ((aktuellenGroupCode = nextGroupCode()) != null) {
            if (aktuellenGroupCode.code == 1001) {
                entity.addXData(appName, xdataList);
                return;
            }
            if (aktuellenGroupCode.code < 1000) {
                 entity.addXData(appName, xdataList);
                 return;
            }
            xdataList.add(aktuellenGroupCode);
        }
        if (!xdataList.isEmpty()) {
             entity.addXData(appName, xdataList);
        }
    }

    private void parseAndAttachReactors(AbstractDxfEntity entity) throws IOException, DxfParserException {
        if (aktuellenGroupCode == null || aktuellenGroupCode.code != 102 || !"{ACAD_REACTORS".equals(aktuellenGroupCode.value)) {
            return;
        }
        aktuellenGroupCode = nextGroupCode();

        while (aktuellenGroupCode != null) {
            if (aktuellenGroupCode.code == 102 && "}".equals(aktuellenGroupCode.value)) {
                aktuellenGroupCode = nextGroupCode();
                return;
            }
            if (aktuellenGroupCode.code == 330 || aktuellenGroupCode.code == 360) {
                entity.addReactorHandle(aktuellenGroupCode.value);
            }
            aktuellenGroupCode = nextGroupCode();
        }
    }

    private void updateDimensionPoints(DxfDimension dimension,
                                       boolean defRead, double defX, double defY, double defZ,
                                       boolean midRead, double midX, double midY, double midZ,
                                       boolean p1Read, double p1X, double p1Y, double p1Z,
                                       boolean p2Read, double p2X, double p2Y, double p2Z,
                                       boolean extrusionRead, double extX, double extY, double extZ) {
        if(defRead) {dimension.setDefinitionPoint(new Point3D(defX, defY, defZ));}
        if(midRead) {dimension.setMiddleOfTextPoint(new Point3D(midX, midY, midZ));}
        if(p1Read) {dimension.setLinearPoint1(new Point3D(p1X, p1Y, p1Z));}
        if(p2Read) {dimension.setLinearPoint2(new Point3D(p2X, p2Y, p2Z));}
        if(extrusionRead) {dimension.setExtrusionDirection(new Point3D(extX,extY,extZ));}
    }

    public DxfLine parseLineEntity() throws IOException, DxfParserException {
        DxfLine line = new DxfLine();
        double x1=0;
        double y1=0;
        double z1=0;
        boolean x1Read=false;
        boolean y1Read=false;
        boolean z1Read=false;
        double x2=0;
        double y2=0;
        double z2=0;
        boolean x2Read=false;
        boolean y2Read=false;
        boolean z2Read=false;

        while ((this.aktuellenGroupCode = nextGroupCode()) != null) {
            if (this.aktuellenGroupCode.code == 0) {
                break;
            }
            switch (aktuellenGroupCode.code) {
                case 8: line.setLayerName(aktuellenGroupCode.value); break;
                case 6: line.setLinetypeName(aktuellenGroupCode.value); break;
                case 62: line.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 10: x1 = Double.parseDouble(aktuellenGroupCode.value); x1Read=true; break;
                case 20: y1 = Double.parseDouble(aktuellenGroupCode.value); y1Read=true; break;
                case 30: z1 = Double.parseDouble(aktuellenGroupCode.value); z1Read=true; break;
                case 11: x2 = Double.parseDouble(aktuellenGroupCode.value); x2Read=true; break;
                case 21: y2 = Double.parseDouble(aktuellenGroupCode.value); y2Read=true; break;
                case 31: z2 = Double.parseDouble(aktuellenGroupCode.value); z2Read=true; break;
                case 1001:
                    if(x1Read || y1Read || z1Read) { line.setStartPoint(new Point3D(x1,y1,z1)); x1Read=y1Read=z1Read=false;}
                    if(x2Read || y2Read || z2Read) { line.setEndPoint(new Point3D(x2,y2,z2)); x2Read=y2Read=z2Read=false;}
                    parseAndAttachXData(line);
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 0) {
                        return line;
                    }
                    break;
                case 102:
                    if(x1Read || y1Read || z1Read) { line.setStartPoint(new Point3D(x1,y1,z1)); x1Read=y1Read=z1Read=false;}
                    if(x2Read || y2Read || z2Read) { line.setEndPoint(new Point3D(x2,y2,z2)); x2Read=y2Read=z2Read=false;}
                    parseAndAttachReactors(line);
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 0) {
                        return line;
                    }
                    break;
                default: break;
            }
            if (aktuellenGroupCode != null && (aktuellenGroupCode.code == 1001 || aktuellenGroupCode.code == 102)) {
                 if (aktuellenGroupCode.code == 0) {
                     break;
                 }
            }
        }
        if(x1Read || y1Read || z1Read) {
            line.setStartPoint(new Point3D(x1,y1,z1));
        }
        if(x2Read || y2Read || z2Read) {
            line.setEndPoint(new Point3D(x2,y2,z2));
        }
        return line;
    }

    public DxfCircle parseCircleEntity() throws IOException, DxfParserException {
        DxfCircle circle = new DxfCircle();
        double cx=0;
        double cy=0;
        double cz=0;
        boolean centerRead = false;
        while ((this.aktuellenGroupCode = nextGroupCode()) != null) {
            if (this.aktuellenGroupCode.code == 0) {
                break;
            }
            switch (aktuellenGroupCode.code) {
                case 8: circle.setLayerName(aktuellenGroupCode.value); break;
                case 6: circle.setLinetypeName(aktuellenGroupCode.value); break;
                case 62: circle.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 10: cx = Double.parseDouble(aktuellenGroupCode.value); centerRead=true; break;
                case 20: cy = Double.parseDouble(aktuellenGroupCode.value); centerRead=true; break;
                case 30: cz = Double.parseDouble(aktuellenGroupCode.value); centerRead=true; break;
                case 40: circle.setRadius(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 1001:
                    if(centerRead) { circle.setCenter(new Point3D(cx,cy,cz)); centerRead=false; }
                    parseAndAttachXData(circle);
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 0) {
                        return circle;
                    }
                    break;
                case 102:
                    if(centerRead) { circle.setCenter(new Point3D(cx,cy,cz)); centerRead=false; }
                    parseAndAttachReactors(circle);
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 0) {
                        return circle;
                    }
                    break;
                default: break;
            }
             if (aktuellenGroupCode != null && (aktuellenGroupCode.code == 1001 || aktuellenGroupCode.code == 102)) {
                if (aktuellenGroupCode.code == 0) {
                    break;
                }
            }
        }
        if(centerRead) {
            circle.setCenter(new Point3D(cx,cy,cz));
        }
        return circle;
    }

    public DxfArc parseArcEntity() throws IOException, DxfParserException {
        DxfArc arc = new DxfArc();
        double cx=0;
        double cy=0;
        double cz=0;
        boolean centerRead = false;
        while ((this.aktuellenGroupCode = nextGroupCode()) != null) {
            if (this.aktuellenGroupCode.code == 0) {
                break;
            }
            switch (aktuellenGroupCode.code) {
                case 8: arc.setLayerName(aktuellenGroupCode.value); break;
                case 6: arc.setLinetypeName(aktuellenGroupCode.value); break;
                case 62: arc.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 10: cx = Double.parseDouble(aktuellenGroupCode.value); centerRead=true; break;
                case 20: cy = Double.parseDouble(aktuellenGroupCode.value); centerRead=true; break;
                case 30: cz = Double.parseDouble(aktuellenGroupCode.value); centerRead=true; break;
                case 40: arc.setRadius(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 50: arc.setStartAngle(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 51: arc.setEndAngle(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 1001:
                    if(centerRead) { arc.setCenter(new Point3D(cx,cy,cz)); centerRead=false; }
                    parseAndAttachXData(arc);
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 0) {
                        return arc;
                    }
                    break;
                case 102:
                    if(centerRead) { arc.setCenter(new Point3D(cx,cy,cz)); centerRead=false; }
                    parseAndAttachReactors(arc);
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 0) {
                        return arc;
                    }
                    break;
                default: break;
            }
            if (aktuellenGroupCode != null && (aktuellenGroupCode.code == 1001 || aktuellenGroupCode.code == 102)) {
                if (aktuellenGroupCode.code == 0) {
                    break;
                }
            }
        }
        if(centerRead) {
            arc.setCenter(new Point3D(cx,cy,cz));
        }
        return arc;
    }

    public DxfLwPolyline parseLwPolylineEntity() throws IOException, DxfParserException {
        DxfLwPolyline lwpolyline = new DxfLwPolyline();
        double tempX = 0;
        double tempY = 0;
        double tempBulge = 0;
        boolean xRead = false;
        boolean yRead = false;

        while ((this.aktuellenGroupCode = nextGroupCode()) != null) {
            if (this.aktuellenGroupCode.code == 0) {
                break;
            }
            switch (aktuellenGroupCode.code) {
                case 8: lwpolyline.setLayerName(aktuellenGroupCode.value); break;
                case 6: lwpolyline.setLinetypeName(aktuellenGroupCode.value); break;
                case 62: lwpolyline.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 90: /* numVertices = Integer.parseInt(aktuellenGroupCode.value); DxfLwPolyline manages its own count */ break;
                case 70:
                    int flags = Integer.parseInt(aktuellenGroupCode.value);
                    if ((flags & 1) == 1) { lwpolyline.setClosed(true); }
                    break;
                case 43: lwpolyline.setConstantWidth(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 38: lwpolyline.setElevation(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 39: lwpolyline.setThickness(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 10:
                    if (xRead && yRead) { // Finalize previous vertex before starting new one
                        lwpolyline.addVertex(new Point2D(tempX, tempY), tempBulge);
                        tempBulge = 0; // Reset for new vertex
                    }
                    tempX = Double.parseDouble(aktuellenGroupCode.value);
                    xRead = true; yRead = false; // Reset yRead for new vertex
                    break;
                case 20:
                    if (!xRead) { throw new DxfParserException("LWPOLYLINE: Y coordinate (20) read for a vertex before X coordinate (10)."); }
                    tempY = Double.parseDouble(aktuellenGroupCode.value);
                    yRead = true;
                    // Don't add vertex yet; wait for optional bulge (42) or next 10/0 code
                    break;
                case 42:
                    if (!xRead || !yRead) { /* Or log warning: Bulge received without complete X,Y pair */ }
                    tempBulge = Double.parseDouble(aktuellenGroupCode.value);
                    break;
                default: // This case handles adding a vertex if a non-vertex code is encountered
                    if (xRead && yRead) {
                        lwpolyline.addVertex(new Point2D(tempX, tempY), tempBulge);
                        xRead = false; yRead = false; tempBulge = 0;
                    }
                    if (aktuellenGroupCode.code == 1001) {
                        parseAndAttachXData(lwpolyline);
                        if (aktuellenGroupCode != null && aktuellenGroupCode.code == 0) {
                            return lwpolyline;
                        }
                    } else if (aktuellenGroupCode.code == 102) {
                        parseAndAttachReactors(lwpolyline);
                        if (aktuellenGroupCode != null && aktuellenGroupCode.code == 0) {
                            return lwpolyline;
                        }
                    }
                    break;
            }
             if (aktuellenGroupCode != null && (aktuellenGroupCode.code == 1001 || aktuellenGroupCode.code == 102)) {
                 if (xRead && yRead) { // Add pending vertex before processing XDATA/Reactors
                    lwpolyline.addVertex(new Point2D(tempX, tempY), tempBulge);
                    xRead = false; yRead = false; tempBulge = 0;
                 }
                 if (aktuellenGroupCode.code == 1001) { // Re-check after parseAndAttachXData/Reactors
                    parseAndAttachXData(lwpolyline);
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 0) {
                        return lwpolyline;
                    }
                 } else if (aktuellenGroupCode.code == 102) {
                    parseAndAttachReactors(lwpolyline);
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 0) {
                        return lwpolyline;
                    }
                 }
            }
        }
        if (xRead && yRead) { // Add any final pending vertex
            lwpolyline.addVertex(new Point2D(tempX, tempY), tempBulge);
        }
        return lwpolyline;
    }

    public DxfText parseTextEntity() throws IOException, DxfParserException {
        DxfText text = new DxfText();
        double insX=0;
        double insY=0;
        double insZ=0;
        boolean insRead = false;
        while ((this.aktuellenGroupCode = nextGroupCode()) != null) {
            if (this.aktuellenGroupCode.code == 0) {
                break;
            }
            switch (aktuellenGroupCode.code) {
                case 1: text.setTextValue(aktuellenGroupCode.value); break;
                case 8: text.setLayerName(aktuellenGroupCode.value); break;
                case 6: text.setLinetypeName(aktuellenGroupCode.value); break;
                case 62: text.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 10: insX = Double.parseDouble(aktuellenGroupCode.value); insRead=true; break;
                case 20: insY = Double.parseDouble(aktuellenGroupCode.value); insRead=true; break;
                case 30: insZ = Double.parseDouble(aktuellenGroupCode.value); insRead=true; break;
                case 11: /* alignX = Double.parseDouble(aktuellenGroupCode.value); alignRead=true; DxfText has no setAlignmentPoint */ break;
                case 21: /* alignY = Double.parseDouble(aktuellenGroupCode.value); alignRead=true; */ break;
                case 31: /* alignZ = Double.parseDouble(aktuellenGroupCode.value); alignRead=true; */ break;
                case 40: text.setHeight(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 50: text.setRotationAngle(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 7: text.setStyleName(aktuellenGroupCode.value); break;
                case 1001:
                    if(insRead) { text.setInsertionPoint(new Point3D(insX, insY, insZ)); insRead = false; }
                    parseAndAttachXData(text);
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 0) {
                        return text;
                    }
                    break;
                case 102:
                    if(insRead) { text.setInsertionPoint(new Point3D(insX, insY, insZ)); insRead = false; }
                    parseAndAttachReactors(text);
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 0) {
                        return text;
                    }
                    break;
                default: break;
            }
            if (aktuellenGroupCode != null && (aktuellenGroupCode.code == 1001 || aktuellenGroupCode.code == 102)) {
                if (aktuellenGroupCode.code == 0) {
                    break;
                }
            }
        }
        if(insRead) {
            text.setInsertionPoint(new Point3D(insX, insY, insZ));
        }
        return text;
    }

    public DxfInsert parseInsertEntity() throws IOException, DxfParserException {
        DxfInsert insert = new DxfInsert();
        double insX=0;
        double insY=0;
        double insZ=0;
        boolean insRead = false;
        while ((this.aktuellenGroupCode = nextGroupCode()) != null) {
            if (this.aktuellenGroupCode.code == 0) {
                break;
            }
            switch (aktuellenGroupCode.code) {
                case 2: insert.setBlockName(aktuellenGroupCode.value.toUpperCase(Locale.ROOT)); break;
                case 8: insert.setLayerName(aktuellenGroupCode.value); break;
                case 6: insert.setLinetypeName(aktuellenGroupCode.value); break;
                case 62: insert.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 10: insX = Double.parseDouble(aktuellenGroupCode.value); insRead=true; break;
                case 20: insY = Double.parseDouble(aktuellenGroupCode.value); insRead=true; break;
                case 30: insZ = Double.parseDouble(aktuellenGroupCode.value); insRead=true; break;
                case 41: insert.setXScale(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 42: insert.setYScale(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 50: insert.setRotationAngle(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 66: break;
                case 1001:
                    if(insRead) { insert.setInsertionPoint(new Point3D(insX,insY,insZ)); insRead = false; }
                    parseAndAttachXData(insert);
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 0) {
                        return insert;
                    }
                    break;
                case 102:
                    if(insRead) { insert.setInsertionPoint(new Point3D(insX,insY,insZ)); insRead = false; }
                    parseAndAttachReactors(insert);
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 0) {
                        return insert;
                    }
                    break;
                default: break;
            }
            if (aktuellenGroupCode != null && (aktuellenGroupCode.code == 1001 || aktuellenGroupCode.code == 102)) {
                if (aktuellenGroupCode.code == 0) {
                    break;
                }
            }
        }
        if(insRead) {
            insert.setInsertionPoint(new Point3D(insX,insY,insZ));
        }
        return insert;
    }

    public DxfDimension parseDimensionEntity() throws IOException, DxfParserException {
        DxfDimension dimension = new DxfDimension();
        double defX=0;
        double defY=0;
        double defZ=0;
        boolean defRead = false;
        double midX=0;
        double midY=0;
        double midZ=0;
        boolean midRead = false;
        double p1X=0;
        double p1Y=0;
        double p1Z=0;
        boolean p1Read = false;
        double p2X=0;
        double p2Y=0;
        double p2Z=0;
        boolean p2Read = false;
        double extX=0;
        double extY=0;
        double extZ=0;
        boolean extrusionRead = false;

        while ((this.aktuellenGroupCode = nextGroupCode()) != null) {
            if (this.aktuellenGroupCode.code == 0) {
                break;
            }
            switch (aktuellenGroupCode.code) {
                case 2: dimension.setBlockName(aktuellenGroupCode.value); break;
                case 3: dimension.setDimensionStyleName(aktuellenGroupCode.value.toUpperCase(Locale.ROOT)); break;
                case 8: dimension.setLayerName(aktuellenGroupCode.value); break;
                case 6: dimension.setLinetypeName(aktuellenGroupCode.value); break;
                case 62: dimension.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 10: defX = Double.parseDouble(aktuellenGroupCode.value); defRead=true; break;
                case 20: defY = Double.parseDouble(aktuellenGroupCode.value); defRead=true; break;
                case 30: defZ = Double.parseDouble(aktuellenGroupCode.value); defRead=true; break;
                case 11: midX = Double.parseDouble(aktuellenGroupCode.value); midRead=true; break;
                case 21: midY = Double.parseDouble(aktuellenGroupCode.value); midRead=true; break;
                case 31: midZ = Double.parseDouble(aktuellenGroupCode.value); midRead=true; break;
                case 13: p1X = Double.parseDouble(aktuellenGroupCode.value); p1Read=true; break;
                case 23: p1Y = Double.parseDouble(aktuellenGroupCode.value); p1Read=true; break;
                case 33: p1Z = Double.parseDouble(aktuellenGroupCode.value); p1Read=true; break;
                case 14: p2X = Double.parseDouble(aktuellenGroupCode.value); p2Read=true; break;
                case 24: p2Y = Double.parseDouble(aktuellenGroupCode.value); p2Read=true; break;
                case 34: p2Z = Double.parseDouble(aktuellenGroupCode.value); p2Read=true; break;
                case 1: dimension.setDimensionText(aktuellenGroupCode.value); break;
                case 50: dimension.setRotationAngle(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 70: dimension.setDimensionTypeFlags(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 210: extX = Double.parseDouble(aktuellenGroupCode.value); extrusionRead=true; break;
                case 220: extY = Double.parseDouble(aktuellenGroupCode.value); extrusionRead=true; break;
                case 230: extZ = Double.parseDouble(aktuellenGroupCode.value); extrusionRead=true; break;
                case 1001:
                    updateDimensionPoints(dimension, defRead, defX, defY, defZ, midRead, midX, midY, midZ, p1Read, p1X, p1Y, p1Z, p2Read, p2X, p2Y, p2Z, extrusionRead, extX, extY, extZ);
                    // Reset flags after updating, as this is specific to the XDATA/Reactor handling path
                    if(defRead) { defRead=false; }
                    if(midRead) { midRead=false; }
                    if(p1Read) { p1Read=false; }
                    if(p2Read) { p2Read=false; }
                    if(extrusionRead) { extrusionRead=false; }
                    parseAndAttachXData(dimension);
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 0) {
                        return dimension;
                    }
                    break;
                case 102:
                    updateDimensionPoints(dimension, defRead, defX, defY, defZ, midRead, midX, midY, midZ, p1Read, p1X, p1Y, p1Z, p2Read, p2X, p2Y, p2Z, extrusionRead, extX, extY, extZ);
                    // Reset flags after updating
                    if(defRead) { defRead=false; }
                    if(midRead) { midRead=false; }
                    if(p1Read) { p1Read=false; }
                    if(p2Read) { p2Read=false; }
                    if(extrusionRead) { extrusionRead=false; }
                    parseAndAttachReactors(dimension);
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 0) {
                        return dimension;
                    }
                    break;
                default: break;
            }
             if (aktuellenGroupCode != null && (aktuellenGroupCode.code == 1001 || aktuellenGroupCode.code == 102)) {
                if (aktuellenGroupCode.code == 0) {
                    break;
                }
            }
        }
        if(defRead) { dimension.setDefinitionPoint(new Point3D(defX, defY, defZ)); }
        if(midRead) { dimension.setMiddleOfTextPoint(new Point3D(midX, midY, midZ)); }
        if(p1Read) { dimension.setLinearPoint1(new Point3D(p1X, p1Y, p1Z)); }
        if(p2Read) { dimension.setLinearPoint2(new Point3D(p2X, p2Y, p2Z)); }
        if(extrusionRead) { dimension.setExtrusionDirection(new Point3D(extX,extY,extZ)); }
        return dimension;
    }

    public DxfSpline parseSplineEntity() throws IOException, DxfParserException {
        DxfSpline spline = new DxfSpline();
        double normalX=0;
        double normalY=0;
        double normalZ=0;
        boolean normalRead = false;
        double currentCpX=0;
        double currentCpY=0;
        boolean cpXRead = false;
        double currentFpX=0;
        double currentFpY=0;
        boolean fpXRead = false;

        while ((this.aktuellenGroupCode = nextGroupCode()) != null) {
            if (this.aktuellenGroupCode.code == 0) {
                break;
            }
            switch (aktuellenGroupCode.code) {
                case 8: spline.setLayerName(aktuellenGroupCode.value); break;
                case 6: spline.setLinetypeName(aktuellenGroupCode.value); break;
                case 62: spline.setColor(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 210: normalX = Double.parseDouble(aktuellenGroupCode.value); normalRead=true; break;
                case 220: normalY = Double.parseDouble(aktuellenGroupCode.value); normalRead=true; break;
                case 230: normalZ = Double.parseDouble(aktuellenGroupCode.value); normalRead=true; break;
                case 70: spline.setFlags(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 71: spline.setDegree(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 72: spline.setNumberOfKnots(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 73: spline.setNumberOfControlPoints(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 74: spline.setNumberOfFitPoints(Integer.parseInt(aktuellenGroupCode.value)); break;
                case 40: spline.addKnot(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 10: currentCpX = Double.parseDouble(aktuellenGroupCode.value); cpXRead = true; break;
                case 20: currentCpY = Double.parseDouble(aktuellenGroupCode.value); break;
                case 30:
                    if(cpXRead) {
                        spline.addControlPoint(new Point3D(currentCpX, currentCpY, Double.parseDouble(aktuellenGroupCode.value)));
                        cpXRead = false; currentCpY = 0; currentCpX = 0;
                    }
                    break;
                case 11: currentFpX = Double.parseDouble(aktuellenGroupCode.value); fpXRead = true; break;
                case 21: currentFpY = Double.parseDouble(aktuellenGroupCode.value); break;
                case 31:
                    if(fpXRead) {
                        spline.addFitPoint(new Point3D(currentFpX, currentFpY, Double.parseDouble(aktuellenGroupCode.value)));
                        fpXRead = false; currentFpY = 0; currentFpX = 0;
                    }
                    break;
                case 42: spline.setKnotTolerance(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 43: spline.setControlPointTolerance(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 44: spline.setFitTolerance(Double.parseDouble(aktuellenGroupCode.value)); break;
                case 1001:
                    if(normalRead) { spline.setNormalVector(new Point3D(normalX, normalY, normalZ)); normalRead = false; } // This if already has braces in a sense due to {} in Point3D potentially, but PMD might want outer braces. Let's assume the main action is one line.
                    parseAndAttachXData(spline);
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 0) {
                        return spline;
                    }
                    break;
                case 102:
                    if(normalRead) { spline.setNormalVector(new Point3D(normalX, normalY, normalZ)); normalRead = false; }
                    parseAndAttachReactors(spline);
                    if (aktuellenGroupCode != null && aktuellenGroupCode.code == 0) {
                        return spline;
                    }
                    break;
                default: break;
            }
            if (aktuellenGroupCode != null && (aktuellenGroupCode.code == 1001 || aktuellenGroupCode.code == 102)) {
                if (aktuellenGroupCode.code == 0) {
                    break;
                }
            }
        }
        if(normalRead) {
            spline.setNormalVector(new Point3D(normalX, normalY, normalZ));
        }
        return spline;
    }

    public DxfEntity consumeUnknownEntity() throws IOException, DxfParserException {
        while ((this.aktuellenGroupCode = nextGroupCode()) != null) {
            if (this.aktuellenGroupCode.code == 0) {
                return null;
            }
        }
        return null;
    }

    public DxfEntity parseEntity(DxfGroupCode entityStartCode) throws IOException, DxfParserException {
        if (entityStartCode == null || entityStartCode.code != 0) {
            throw new DxfParserException("Entity start code must be a 0 group code. Got: " + entityStartCode);
        }
        this.aktuellenGroupCode = entityStartCode;
        String entityType = entityStartCode.value.toUpperCase(Locale.ROOT);

        switch (entityType) {
            case "LINE": return parseLineEntity();
            case "CIRCLE": return parseCircleEntity();
            case "ARC": return parseArcEntity();
            case "LWPOLYLINE": return parseLwPolylineEntity();
            case "TEXT": return parseTextEntity();
            case "INSERT": return parseInsertEntity();
            case "DIMENSION": return parseDimensionEntity();
            case "SPLINE": return parseSplineEntity();
            default: return consumeUnknownEntity();
        }
    }
}
