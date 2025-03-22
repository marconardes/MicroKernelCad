

package com.cad.core.kernel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author marconardes
 */
public class ModuleManager {

    private URLClassLoader urlClassLoader;



    public void prepare(){

        // Find all jar files in the localModules
        File localModulesDir = new File("localModules");
        File[] jarFiles = localModulesDir.listFiles((dir, name) -> name.endsWith(".jar"));

        if (jarFiles != null) {
            List<URL> jarUrls = new ArrayList<>();
            for (File jarFile : jarFiles) {
                try {
                    jarUrls.add(jarFile.toURI().toURL());
                    System.out.println("Loaded JAR: " + jarFile.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Load JARs into memory using URLClassLoader
            this.urlClassLoader = new URLClassLoader(jarUrls.toArray(new URL[0]), ModuleManager.class.getClassLoader());
            Thread.currentThread().setContextClassLoader(urlClassLoader);
        }
    }

    public void init() {
        if (this.urlClassLoader == null) {
            System.out.println("URLClassLoader não inicializado");
            return;
        }

        try {
            // Obter os URLs do classloader
            URL[] urls = this.urlClassLoader.getURLs();
            System.out.println("Total de JARs carregados: " + urls.length);

            for (URL url : urls) {
                System.out.println("\nClasses no JAR: " + url.getPath());

                try (java.util.jar.JarFile jarFile = new java.util.jar.JarFile(new File(url.toURI()))) {
                    java.util.Enumeration<java.util.jar.JarEntry> entries = jarFile.entries();

                    while (entries.hasMoreElements()) {
                        java.util.jar.JarEntry entry = entries.nextElement();
                        String name = entry.getName();

                        if (name.endsWith(".class")) {
                            // Converter formato do arquivo class para formato de nome de classe
                            String className = name.replace('/', '.')
                                    .substring(0, name.length() - 6); // Remove ".class"
                            System.out.println("  - " + className);
                        }
                    }
                }
            }
        } catch (IOException | java.net.URISyntaxException e) {
            System.err.println("Erro ao listar classes: " + e.getMessage());
        }
    }
    public void start() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void stop() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void destroy() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
