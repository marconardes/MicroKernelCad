package com.cad.core.kernel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList; // Added import
import java.util.List; // Added import

// import com.cad.gui.MainFrame; // New import - MainFrame usage is commented out


/**
 * Kernel is the core class of the CAD application, responsible for initializing the system,
 * managing modules, handling events, and loading plugins.
 * It serves as the main entry point for the application.
 */
public class Kernel {

    private java.util.Properties configuration = new java.util.Properties();
    private List<String> loadedPluginNames = new ArrayList<>();
    ModuleManager moduleManager = new ModuleManager();
    
        // Método para inicializar o kernel
        public void initialize() {
            System.out.println("Initializing Kernel");
            prepareModules();
            initializeModules();
            loadConfiguration();
            manageResources();
            loadPlugins();
            
            // Outras inicializações necessárias

            // Launch the GUI - Temporarily commented out to break build cycle
            // SwingUtilities.invokeLater(() -> {
            //     MainFrame frame = new MainFrame();
            //     frame.setVisible(true);
            // });
        }
        

    public void prepareModules(){
        moduleManager.prepare();
    }

    public void initializeModules() {
        System.out.println("Initializing modules");
        moduleManager.init();
    }
    
    // Gerenciamento de recursos
    public void manageResources() {
        System.out.println("Managing resources");
        // Implementação para gerenciar recursos gráficos, memória, etc.
    }
    
    // Manipulação de eventos
    public void handleEvent() {
        System.out.println("Starting handleEvent");
        // Implementação para manipular eventos de desenho, cliques, etc.
    }

    // Carregar configurações
    public void loadConfiguration() {
        System.out.println("Starting loadConfiguration");
        // Implementação para carregar configurações específicas do CAD
        this.configuration.setProperty("default.setting1", "value1");
        this.configuration.setProperty("default.setting2", "value2");
        System.out.println("Configuration loaded with default settings.");
    }

    public java.util.Properties getConfiguration() {
        return this.configuration;
    }

    // Gerenciamento de plugins
    public void loadPlugins() {
        System.out.println("Loading plugins");
        // Implementação para carregar e inicializar plugins de extensão
        // Simulate finding and loading a couple of plugins
        this.loadedPluginNames.add("MockPluginA");
        this.loadedPluginNames.add("MockPluginB");
        System.out.println("Simulated loading of MockPluginA and MockPluginB.");
    }

    public List<String> getLoadedPluginNames() {
        return this.loadedPluginNames;
    }



    
    // Carregar módulos path default
    public void loadModules() {
        System.out.println("loadModules");
        // Get the parent directory
        String parentDir = System.getProperty("user.dir");

        System.err.println(parentDir);
        File modulesDir = new File(parentDir + "/modules");
        if (modulesDir.exists() && modulesDir.isDirectory()) {
            System.out.println("Modules directory found");
            // List all directories in modules
            File[] modules = modulesDir.listFiles();
            for (File module : modules) {
                if (module.isDirectory()) {
                    if ("export".equals(module.getName())) {
                        // List the directories in export
                        File[] exportModules = module.listFiles();
                        for (File exportModule : exportModules) {
                            if (exportModule.isDirectory()) {
                                System.out.println("Export module found: " + exportModule.getName());
                                loadModules(exportModule.getName());        
                            }
                        }
                    } else {
                        System.out.println("Module found: " + module.getName());
                        loadModules(modulesDir+"/"+module.getName());
                    }
                }
            }
        } else {
            System.out.println("Modules directory not found");
        }
        // Implementação para carregar módulos do CAD
    }




     //loadModules with a specific path
    public void loadModules(String path) {
        System.out.println("loadModules from " + path);
        
        //find the jars in the target path
        File modulesDir = new File(path+"/target"); 

        //print the path
        System.err.println(modulesDir.getAbsolutePath());

        //find the jars in the target path
        File[] jars = modulesDir.listFiles((dir, name) -> name.endsWith(".jar"));

        //create a directory if not exists
        File localModules = new File("localModules");

        //create the directory if not exists
        if (!localModules.exists()) {
            localModules.mkdir();
        }

        verifyJarsAndCopy(jars, localModules);
    }


    private void verifyJarsAndCopy(File[] jars, File localModules) {
        System.err.println("=====> ");

        if (jars != null) {
            System.err.println("=====> ");
            for (File jar : jars) {
                Path sourcePath = jar.toPath();
                Path targetPath = new File(localModules, jar.getName()).toPath();

                try {
                    if (Files.exists(targetPath)) {
                        if (Files.getLastModifiedTime(sourcePath).compareTo(Files.getLastModifiedTime(targetPath)) != 0) {
                            System.out.println("JAR file " + jar.getName() + " has a different modification date.");
                            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("++++++");

                        } else {
                            System.out.println("JAR file " + jar.getName() + " is up to date.");
                        }
                    } else {
                        System.out.println("JAR file " + jar.getName() + " does not exist in the target directory.");
                        //copy the jar to the localModules directory
                        Files.copy(sourcePath, targetPath);
                        System.out.println("------");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    

}
