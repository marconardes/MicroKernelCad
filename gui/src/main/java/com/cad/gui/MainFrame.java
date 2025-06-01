package com.cad.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JMenuBar; // New import
import javax.swing.JMenu;    // New import
import javax.swing.JMenuItem; // New import

public class MainFrame extends JFrame {

    private JPanel cadViewPlaceholder; // New field

    public MainFrame() {
        setTitle("CAD Tool");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Changed from EXIT_ON_CLOSE

        // Set layout and add placeholder
        setLayout(new BorderLayout()); // New line

        cadViewPlaceholder = new JPanel();
        cadViewPlaceholder.setBackground(Color.LIGHT_GRAY);
        add(cadViewPlaceholder, BorderLayout.CENTER);

        // --- Add Menu Bar ---
        JMenuBar menuBar = new JMenuBar(); // New

        JMenu fileMenu = new JMenu("File"); // New
        menuBar.add(fileMenu); // New

        JMenuItem exitMenuItem = new JMenuItem("Exit"); // New
        exitMenuItem.addActionListener(e -> System.exit(0)); // New
        fileMenu.add(exitMenuItem); // New

        setJMenuBar(menuBar); // New
        // --- End Menu Bar ---
    }

    // It's good practice to have a main method for testing the frame independently,
    // though it won't be the primary entry point for the whole application.
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
