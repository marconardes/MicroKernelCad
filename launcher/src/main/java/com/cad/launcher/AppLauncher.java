package com.cad.launcher;

// Required import from the gui module
import com.cad.gui.MainFrame;

public class AppLauncher {

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            // The MainFrame constructor now calls init() internally.
            // So, we just need to call start() to make it visible.
            frame.start();
        });
    }
}
