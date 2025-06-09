package com.cad.modules.rendering;

import com.cad.dxflib.structure.DxfDocument;
import org.w3c.dom.svg.SVGDocument;

public class DxfProcessingResult {
    public final DxfDocument dxfDocument;
    public final org.w3c.dom.svg.SVGDocument batikDocument;

    public DxfProcessingResult(DxfDocument dxfDocument, org.w3c.dom.svg.SVGDocument batikDocument) {
        this.dxfDocument = dxfDocument;
        this.batikDocument = batikDocument;
    }
}
