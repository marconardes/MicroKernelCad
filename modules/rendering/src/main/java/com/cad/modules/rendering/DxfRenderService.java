package com.cad.modules.rendering;

import com.cad.dxflib.parser.DxfParser;
import com.cad.dxflib.parser.DxfParserException;
import com.cad.dxflib.structure.DxfDocument;
import com.cad.dxflib.converter.DxfToSvgConverter;
import com.cad.dxflib.converter.SvgConversionOptions;

import com.kitfox.svg.SVGUniverse; // Added for SVG Salamander
import java.io.StringReader; // Added for SVG Salamander
import java.io.InputStream;
import java.io.IOException;

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

    public SVGUniverse convertDxfToSvgUniverse(InputStream dxfInputStream, String diagramName) throws DxfParserException, IOException {
        if (dxfInputStream == null) {
            throw new IllegalArgumentException("InputStream não pode ser nulo.");
        }
        // Generate SVG string first (reusing existing logic)
        String svgString = convertDxfToSvg(dxfInputStream); // This method already closes the input stream

        if (svgString == null || svgString.isEmpty()) {
            // Return an empty universe or throw an exception, based on desired behavior
            return new SVGUniverse();
        }

        SVGUniverse universe = new SVGUniverse();
        try {
            // Parse the SVG string into the SVGUniverse
            // The second argument to loadSVG is a URI that can be used to reference this diagram later.
            // It's often set to the original file URI or a unique name.
            universe.loadSVG(new StringReader(svgString), diagramName);
        } catch (Exception e) {
            // Catching a general Exception as loadSVG might throw various things from SVG Salamander's parsing
            // Or consider re-throwing as a custom exception or DxfParserException if appropriate
            throw new DxfParserException("Falha ao parsear string SVG para SVGUniverse: " + e.getMessage(), e);
        }
        return universe;
    }
}
