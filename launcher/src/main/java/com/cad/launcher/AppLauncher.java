package com.cad.launcher;

// Required import from the gui module
import com.cad.gui.MainFrame;

public class AppLauncher {

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
