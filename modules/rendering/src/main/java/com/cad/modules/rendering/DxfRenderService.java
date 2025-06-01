package com.cad.modules.rendering;

import com.cad.dxflib.parser.DxfParser;
import com.cad.dxflib.parser.DxfParserException;
import com.cad.dxflib.structure.DxfDocument;
import com.cad.dxflib.converter.DxfToSvgConverter;
import com.cad.dxflib.converter.SvgConversionOptions;

import java.io.InputStream;
import java.io.IOException; // Para fechar o InputStream

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
}
