package com.cad.launcher;

// Required import from the gui module

import com.cad.gui.MainFrame;

/**
 * AppLauncher is the entry point for the CAD application.
 * It initializes the GUI and starts the main application frame.
 * This class is responsible for launching the application in a thread-safe manner.
 */
public class AppLauncher {

  /**
   * Main method to start the application.
   * It uses SwingUtilities to ensure that the GUI is created on the Event Dispatch Thread (EDT).
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    javax.swing.SwingUtilities.invokeLater(() -> {

      try {
        MainFrame frame = new MainFrame();
        frame.start();
      } catch (java.nio.file.InvalidPathException e) {
        System.err.println("Error: Invalid file path detected. " +
                "Please check the file path encoding.");
        e.printStackTrace();
      }
    });
  }
}
