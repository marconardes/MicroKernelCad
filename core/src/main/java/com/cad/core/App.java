package com.cad.core;

import com.cad.core.kernel.Kernel;

/**
 * Hello world!.
 *
 */
public class App {
    private App() {
        // Utility class
    }

    /**
     * Main method to start the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Hello World!");
        Kernel kernel = new Kernel();
        kernel.loadModules();
        kernel.initialize();

    }
}
