package com.cad.launcher;

// Required import from the gui module
import com.cad.gui.MainFrame;

public class AppLauncher {

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {

            try {
                MainFrame frame = new MainFrame();
                frame.start();
            } catch (java.nio.file.InvalidPathException e) {
                System.err.println("Error: Invalid file path detected. Please check the file path encoding.");
                e.printStackTrace();
            }
        });
    }
}
