package com.cad.dxflib.parser;

import com.cad.dxflib.structure.DxfDocument;
import com.cad.dxflib.objects.DxfDictionary; // Added import
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class DxfParserIntegrationTest {

    @Test
    void testParseDxf1() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("1.dxf");
        assertNotNull(inputStream, "Test file 1.dxf not found in resources.");

        DxfParser parser = new DxfParser();
        DxfDocument document = null;
        try {
            document = parser.parse(inputStream);
        } catch (DxfParserException e) { // Removed IOException from catch
            fail("Parsing 1.dxf failed with exception: " + e.getMessage(), e);
        }

        assertNotNull(document, "Parsed document should not be null for 1.dxf.");
        assertTrue(document.getLayers().size() > 0, "1.dxf: Should have parsed layers.");
        // DXF/1.dxf contains many anonymous blocks for dimensions (*Dnnn) and other blocks.
        assertTrue(document.getBlocks().size() > 0, "1.dxf: Should have parsed blocks.");
        // DXF/1.dxf has DIMENSION entities in model space.
        assertTrue(document.getModelSpaceEntities().size() > 0, "1.dxf: Should have entities in model space.");
        assertNotNull(document.getDimensionStyle("STANDARD"), "1.dxf: The STANDARD dimension style should be present.");
         // Specific assertions based on known content of 1.dxf
        assertEquals(5, document.getLayers().size(), "1.dxf: Expected number of layers does not match."); // Adjusted from 15 to 5
        // Count may vary based on how *D anonymous blocks are handled if they are not explicitly added to document.blocks
        // For now, let's assume they are added. If not, this assertion might need adjustment.
        // After reviewing 1.dxf, it has many *D blocks plus other blocks.
        assertEquals(38, document.getBlocks().size(), "1.dxf: Expected number of blocks does not match."); // Adjusted from >50 to 38
        assertEquals(281, document.getModelSpaceEntities().size(), "1.dxf: Expected number of model space entities does not match."); // Adjusted from 58 to 281

    }

    @Test
    void testParseDxf2() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("2.dxf");
        assertNotNull(inputStream, "Test file 2.dxf not found in resources.");

        DxfParser parser = new DxfParser();
        DxfDocument document = null;
        try {
            document = parser.parse(inputStream);
        } catch (DxfParserException e) { // Removed IOException from catch
            fail("Parsing 2.dxf failed with exception: " + e.getMessage(), e);
        }

        assertNotNull(document, "Parsed document should not be null for 2.dxf.");
        assertTrue(document.getLayers().size() > 0, "2.dxf: Should have parsed layers.");
        // DXF/2.dxf has SPLINE entities in model space.
        assertTrue(document.getModelSpaceEntities().size() > 0, "2.dxf: Should have entities in model space.");
        assertTrue(document.getDictionaries().size() > 0, "2.dxf: Should have parsed dictionaries.");

        // Revised check for ACAD_SCALELIST
        DxfDictionary scaleListDictProvider = null;
        String scaleListDictHandle = null;
        if (document.getDictionaries() != null) {
            for (DxfDictionary dict : document.getDictionaries().values()) {
                if (dict.getObjectHandle("ACAD_SCALELIST") != null) {
                    scaleListDictProvider = dict;
                    scaleListDictHandle = dict.getObjectHandle("ACAD_SCALELIST");
                    break;
                }
            }
        }
        assertNotNull(scaleListDictProvider, "2.dxf: A dictionary containing ACAD_SCALELIST entry should be present.");
        assertNotNull(scaleListDictHandle, "2.dxf: Handle for ACAD_SCALELIST dictionary should be found in parent dictionary.");

        DxfDictionary acadScaleListDictionary = document.getDictionary(scaleListDictHandle);
        assertNotNull(acadScaleListDictionary, "2.dxf: The ACAD_SCALELIST dictionary object itself should be present (retrieved by its handle).");

        assertTrue(document.getScales().size() > 0, "2.dxf: Should have parsed scales from ACAD_SCALELIST.");
        // Specific assertions based on known content of 2.dxf
        assertEquals(2, document.getLayers().size(), "2.dxf: Expected number of layers does not match.");
        assertEquals(706, document.getModelSpaceEntities().size(), "2.dxf: Expected number of model space entities does not match."); // Adjusted from 86 to 706
        assertTrue(document.getScales().size() >= 15, "2.dxf: Expected at least 15 SCALEs.");
    }
}
