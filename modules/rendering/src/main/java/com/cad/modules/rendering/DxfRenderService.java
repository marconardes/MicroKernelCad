package com.cad.modules.rendering;

import com.cad.dxflib.parser.DxfParser;
import com.cad.dxflib.parser.DxfParserException;
import com.cad.dxflib.structure.DxfDocument;
import com.cad.dxflib.converter.DxfToSvgConverter;
import com.cad.dxflib.converter.SvgConversionOptions;
import com.cad.modules.rendering.DxfProcessingResult; // Added import

import org.w3c.dom.svg.SVGDocument;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.slf4j.Logger; // Added import
import org.slf4j.LoggerFactory; // Added import

import java.io.StringReader;
import java.io.InputStream;
import java.io.IOException;

public class DxfRenderService {

    private static final Logger logger = LoggerFactory.getLogger(DxfRenderService.class); // Added logger

    private final DxfParser dxfParser;
    private final DxfToSvgConverter svgConverter;

    // Consider using @Autowired if this is a Spring managed bean
    public DxfRenderService() {
        this.dxfParser = new DxfParser(); // Assuming default constructor is fine
        this.svgConverter = new DxfToSvgConverter(); // Assuming default constructor is fine
    }

    public DxfProcessingResult loadDxf(InputStream dxfInputStream, String diagramName) throws DxfParserException, IOException {
        if (dxfInputStream == null) {
            throw new IllegalArgumentException("DXF input stream cannot be null");
        }

        DxfDocument dxfDoc = null;
        try {
            // Parse the DXF input stream
            dxfDoc = dxfParser.parse(dxfInputStream);

            // Generate SVG string from DxfDocument
            // Using default SvgConversionOptions, customize as needed
            SvgConversionOptions options = new SvgConversionOptions();
            String svgString = svgConverter.convert(dxfDoc, options);

            SVGDocument batikDoc = null;
            if (svgString != null && !svgString.isEmpty()) {
                try {
                    String parser = XMLResourceDescriptor.getXMLParserClassName();
                    SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
                    // Create a unique URI for the document
                    String syntheticDocumentURI = "dxf2svg://" + (diagramName != null ? diagramName : "untitled.svg");
                    batikDoc = factory.createSVGDocument(syntheticDocumentURI, new StringReader(svgString));
                } catch (IOException e) {
                    logger.error("Error parsing SVG string for diagram: {}", diagramName, e);
                    // Wrap the Batik-specific exception or rethrow as DxfParserException
                    throw new DxfParserException("Failed to parse SVG string into Batik document for diagram: " + diagramName, e);
                } catch (Exception e) {
                    // Catch any other unexpected errors during SVG parsing or document creation
                    logger.error("Unexpected error creating Batik SVG document for diagram: {}", diagramName, e);
                    throw new DxfParserException("Unexpected error creating Batik SVG document for diagram: " + diagramName, e);
                }
            } else {
                // Log if SVG string is null or empty, but still proceed with the DxfDocument
                logger.warn("SVG string is null or empty for diagram: {}. DXF Document might be valid but contain no renderable entities.", diagramName);
            }

            // Return the result containing both DxfDocument and potentially null SVGDocument
            return new DxfProcessingResult(dxfDoc, batikDoc);

        } finally {
            // Ensure the input stream is closed in all cases
            try {
                dxfInputStream.close();
            } catch (IOException e) {
                // Log warning if closing the stream fails, but don't let it hide an original exception
                logger.warn("Failed to close DXF input stream for diagram: {}", diagramName, e);
            }
        }
    }
}
