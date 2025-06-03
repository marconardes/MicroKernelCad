// test_runner/src/DXFTestRunner.java
import com.cad.dxflib.parser.DxfParser;
import com.cad.dxflib.structure.DxfDocument;
import com.cad.dxflib.parser.DxfParserException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

public class DXFTestRunner {
    public static void main(String[] args) {
        DxfParser parser = new DxfParser();
        InputStream dxfInputStream = null;
        try {
            dxfInputStream = new FileInputStream("Demo plan.dxf");
            System.out.println("Attempting to parse 'Demo plan.dxf'...");
            DxfDocument doc = parser.parse(dxfInputStream);
            System.out.println("Parsing successful!");
            System.out.println("Number of layers: " + doc.getLayers().size());
            System.out.println("Number of blocks: " + doc.getBlocks().size());
            System.out.println("Number of entities in model space: " + doc.getModelSpaceEntities().size());
            if (doc.getBlock("*Model_Space") != null) {
               System.out.println("Entities in *Model_Space block: " + doc.getBlock("*Model_Space").getEntities().size());
            } else {
               System.out.println("*Model_Space block not found.");
            }
        } catch (DxfParserException e) {
            System.err.println("Parsing failed: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (dxfInputStream != null) {
                try {
                    dxfInputStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
