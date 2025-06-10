/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.cad.core.services;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

/**
 *
 * @author marconardes
 */
public class Utils {

    //seleciona uma lista de jars e coloca na memoria
    public List<JarFile> loadJars(String path) {

        List<JarFile> jarFile= new ArrayList<JarFile>();

        System.out.println("Loading jars from " + path);
        if( Path.of(path).toFile().isDirectory() ) {
            System.out.println("Path is a directory");
            //lista os jars
            File[] jars = Path.of(path).toFile().listFiles();

            for (File file : jars) {
                System.err.println("Loading jar: " + file.getName());
                try {
                    jarFile.add(new JarFile(file));
                } catch (Exception e) {
                    System.err.println("Error loading jar: " + file.getName());
                    e.printStackTrace();
                }
                
            }
        }
                return jarFile;
    }

}
