package com.cad.core;

import com.cad.core.kernel.Kernel;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println("Hello World!");
        Kernel kernel = new Kernel();
        kernel.loadModules();
        kernel.initialize();

    }
}
