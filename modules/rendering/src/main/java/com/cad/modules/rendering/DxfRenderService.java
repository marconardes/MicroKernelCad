package com.cad.modules.rendering;

import com.cad.dxflib.parser.DxfParser;
import com.cad.dxflib.parser.DxfParserException;
import com.cad.dxflib.structure.DxfDocument;
import com.cad.dxflib.converter.DxfToSvgConverter;
import com.cad.dxflib.converter.SvgConversionOptions;

import org.w3c.dom.svg.SVGDocument;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory; // Changed to anim.dom
import org.apache.batik.util.XMLResourceDescriptor;
import java.io.StringReader;
import java.io.InputStream;
import java.io.IOException;
// Note: org.xml.sax.InputSource might not be needed, StringReader usually suffices.

public class DxfRenderService {

    private final DxfParser dxfParser;
    private final DxfToSvgConverter svgConverter;

    public DxfRenderService() {
        this.dxfParser = new DxfParser();
        this.svgConverter = new DxfToSvgConverter();
    }

    public String convertDxfToSvg(InputStream dxfInputStream) throws DxfParserException, IOException {
        if (dxfInputStream == null) {
            throw new IllegalArgumentException("InputStream não pode ser nulo.");
        }
        try {
            DxfDocument dxfDocument = dxfParser.parse(dxfInputStream);
            // Usar opções de conversão padrão por enquanto
            SvgConversionOptions options = new SvgConversionOptions();
            return svgConverter.convert(dxfDocument, options);
        } finally {
            // É importante fechar o InputStream
            try {
                dxfInputStream.close();
            } catch (IOException e) {
                // Logar ou tratar o erro ao fechar, se necessário, mas não sobrescrever a exceção original
                System.err.println("Erro ao fechar InputStream: " + e.getMessage());
            }
        }
    }

    public org.w3c.dom.svg.SVGDocument convertDxfToBatikDocument(InputStream dxfInputStream, String diagramName) throws DxfParserException, IOException {
        if (dxfInputStream == null) {
            throw new IllegalArgumentException("InputStream não pode ser nulo.");
        }
        // Generate SVG string first (reusing existing logic)
        // The convertDxfToSvg method is responsible for closing the dxfInputStream
        String svgString = convertDxfToSvg(dxfInputStream);

        if (svgString == null || svgString.isEmpty()) {
            // Return null or throw an exception, based on desired behavior.
            // For now, returning null as per instruction.
            return null;
        }

        try {
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
            // Create a synthetic URI for the document. This helps in resolving relative URIs if any,
            // though less common for generated SVGs like this.
            String syntheticDocumentURI = "dxf2svg://" + (diagramName != null ? diagramName : "untitled.svg");
            SVGDocument doc = factory.createSVGDocument(syntheticDocumentURI, new StringReader(svgString));
            return doc;
        } catch (Exception e) { // Catch general exception as various things can go wrong during parsing
            throw new DxfParserException("Falha ao parsear string SVG para SVGDocument Batik: " + e.getMessage(), e);
        }
    }
}
