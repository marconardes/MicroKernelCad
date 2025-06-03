package com.cad.gui.tool;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ToolManagerTest {

    @Test
    void testGetActiveTool_initialState() {
        ToolManager toolManager = new ToolManager();
        // Confirmed from ToolManager.java that the default is ActiveTool.NONE
        assertEquals(ActiveTool.NONE, toolManager.getActiveTool(), "Initial active tool should be NONE.");
    }

    @Test
    void testSetAndGetActiveTool() {
        ToolManager toolManager = new ToolManager();

        toolManager.setActiveTool(ActiveTool.DRAW_LINE);
        assertEquals(ActiveTool.DRAW_LINE, toolManager.getActiveTool(), "Active tool should be DRAW_LINE after setting.");

        toolManager.setActiveTool(ActiveTool.DRAW_CIRCLE);
        assertEquals(ActiveTool.DRAW_CIRCLE, toolManager.getActiveTool(), "Active tool should be DRAW_CIRCLE after setting.");

        toolManager.setActiveTool(ActiveTool.SELECT);
        assertEquals(ActiveTool.SELECT, toolManager.getActiveTool(), "Active tool should be SELECT after setting.");

        toolManager.setActiveTool(ActiveTool.ZOOM_IN);
        assertEquals(ActiveTool.ZOOM_IN, toolManager.getActiveTool(), "Active tool should be ZOOM_IN after setting.");

        toolManager.setActiveTool(ActiveTool.ZOOM_OUT);
        assertEquals(ActiveTool.ZOOM_OUT, toolManager.getActiveTool(), "Active tool should be ZOOM_OUT after setting.");

        toolManager.setActiveTool(ActiveTool.PAN);
        assertEquals(ActiveTool.PAN, toolManager.getActiveTool(), "Active tool should be PAN after setting.");
    }

    @Test
    void testSetActiveTool_nullValue() {
        ToolManager toolManager = new ToolManager();
        // Assuming setting a null tool is allowed and results in getActiveTool() returning null.
        // If ActiveTool cannot be null (e.g., due to @NonNull annotations or internal checks),
        // this test would need to change to assert an expected exception (e.g., NullPointerException or IllegalArgumentException).
        toolManager.setActiveTool(null);
        assertNull(toolManager.getActiveTool(), "Active tool should be null if explicitly set to null.");
    }

    @Test
    void testSetActiveTool_isIdempotent() {
        ToolManager toolManager = new ToolManager();
        toolManager.setActiveTool(ActiveTool.DRAW_LINE);
        assertEquals(ActiveTool.DRAW_LINE, toolManager.getActiveTool(), "Active tool should be DRAW_LINE.");

        toolManager.setActiveTool(ActiveTool.DRAW_LINE); // Set the same tool again
        assertEquals(ActiveTool.DRAW_LINE, toolManager.getActiveTool(), "Setting the same tool again should not change it.");
    }
}
