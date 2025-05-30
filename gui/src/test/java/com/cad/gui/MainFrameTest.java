package com.cad.gui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MainFrameTest {

    @Test
    public void testMainFrameCreation() {
        // Test if the MainFrame can be instantiated without errors.
        // This is a basic smoke test.
        // It's important to run Swing instantiation on the Event Dispatch Thread (EDT)
        // if the components were to be shown or interact with system resources,
        // but for a simple instantiation test, it might not be strictly necessary.
        // However, to be safe and avoid potential headless environment issues:
        try {
            javax.swing.SwingUtilities.invokeAndWait(() -> {
                MainFrame frame = new MainFrame();
                assertNotNull(frame, "MainFrame instance should not be null.");
            });
        } catch (Exception e) {
            // Fail the test if any exception occurs during instantiation on the EDT
            org.junit.jupiter.api.Assertions.fail("MainFrame instantiation failed: " + e.getMessage());
        }
    }
}
