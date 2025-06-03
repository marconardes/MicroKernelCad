package com.cad.gui;

import com.cad.core.api.ModuleInterface;
import com.cad.dxflib.common.Point2D;
import com.cad.dxflib.parser.DxfParserException;
import com.cad.gui.tool.ActiveTool;
import com.cad.gui.tool.ToolManager;
import com.cad.modules.geometry.entities.Circle2D;
import com.cad.modules.geometry.entities.Line2D;
import com.cad.modules.rendering.DxfRenderService;
import com.kitfox.svg.app.beans.SVGPanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter; // New import
import java.io.BufferedWriter; // New import
// ArrayList and List are now managed by CadPanelLogic
// import java.util.ArrayList;
// import java.util.List;
import java.net.URI; // Keep for URI creation
// URLEncoder and StandardCharsets might not be needed if using File URI
// import java.net.URLEncoder;
// import java.nio.charset.StandardCharsets;

public class MainFrame extends JFrame implements ModuleInterface {

    SVGPanel svgCanvas;
    ToolManager toolManager;

    private CadPanelLogic cadPanelLogic;

    CadPanelLogic getCadPanelLogic() {
        return this.cadPanelLogic;
    }

    void setSvgCanvasForTest(SVGPanel canvas) {
        this.svgCanvas = canvas;
    }

    public MainFrame() {
        this(true);
    }

    public MainFrame(boolean initializeUI) {
        this.toolManager = new ToolManager();
        this.cadPanelLogic = new CadPanelLogic(this.toolManager, new DxfRenderService());

        if (initializeUI) {
            initUI();
        }
    }

    private void initUI() {
        setTitle("CAD Tool");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        ensureSvgCanvasInitialized();
        cadPanelLogic.currentScale = 1.0;
        cadPanelLogic.translateX = 0.0;
        cadPanelLogic.translateY = 0.0;
        redrawSVGCanvas();

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Arquivo");
        menuBar.add(fileMenu);

        JMenuItem openDxfMenuItem = new JMenuItem("Abrir DXF...");
        openDxfMenuItem.addActionListener(e -> openDxfFile());
        fileMenu.add(openDxfMenuItem);

        JMenuItem exitMenuItem = new JMenuItem("Sair");
        exitMenuItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitMenuItem);
        setJMenuBar(menuBar);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        ButtonGroup toolModeGroup = new ButtonGroup();

        JToggleButton lineToggleButton = new JToggleButton("Linha");
        lineToggleButton.addActionListener(e -> {
            if (lineToggleButton.isSelected()) {
                toolManager.setActiveTool(ActiveTool.DRAW_LINE);
                clearDrawingStateForNewOperation();
            }
        });
        toolModeGroup.add(lineToggleButton);
        toolBar.add(lineToggleButton);

        JToggleButton circleToggleButton = new JToggleButton("Círculo");
        circleToggleButton.addActionListener(e -> {
            if (circleToggleButton.isSelected()) {
                toolManager.setActiveTool(ActiveTool.DRAW_CIRCLE);
                clearDrawingStateForNewOperation();
            }
        });
        toolModeGroup.add(circleToggleButton);
        toolBar.add(circleToggleButton);

        JToggleButton selectToggleButton = new JToggleButton("Selecionar");
        selectToggleButton.addActionListener(e -> {
             if (selectToggleButton.isSelected()) {
                toolManager.setActiveTool(ActiveTool.SELECT);
                clearDrawingStateForNewOperation();
             }
        });
        toolModeGroup.add(selectToggleButton);
        toolBar.add(selectToggleButton);
        toolBar.addSeparator();

        JToggleButton zoomInToggleButton = new JToggleButton("Zoom In");
        zoomInToggleButton.addActionListener(e -> {
            if (zoomInToggleButton.isSelected()) {
                toolManager.setActiveTool(ActiveTool.ZOOM_IN);
                clearDrawingStateForNewOperation();
            }
        });
        toolModeGroup.add(zoomInToggleButton);
        toolBar.add(zoomInToggleButton);

        JToggleButton zoomOutToggleButton = new JToggleButton("Zoom Out");
        zoomOutToggleButton.addActionListener(e -> {
            if (zoomOutToggleButton.isSelected()) {
                toolManager.setActiveTool(ActiveTool.ZOOM_OUT);
                clearDrawingStateForNewOperation();
            }
        });
        toolModeGroup.add(zoomOutToggleButton);
        toolBar.add(zoomOutToggleButton);

        JToggleButton panToggleButton = new JToggleButton("Pan");
        panToggleButton.addActionListener(e -> {
            if (panToggleButton.isSelected()) {
                toolManager.setActiveTool(ActiveTool.PAN);
                clearDrawingStateForNewOperation();
            }
        });
        toolModeGroup.add(panToggleButton);
        toolBar.add(panToggleButton);

        add(toolBar, BorderLayout.PAGE_START);
        selectToggleButton.setSelected(true);
        toolManager.setActiveTool(ActiveTool.SELECT);
        clearDrawingStateForNewOperation();
    }

    private void clearDrawingStateForNewOperation() {
        cadPanelLogic.clearPreviewLineState();
        cadPanelLogic.clearPreviewCircleState();
        cadPanelLogic.lineStartPoint = null;
        cadPanelLogic.circleCenterPoint = null;

        if (svgCanvas != null) {
             ActiveTool currentTool = toolManager.getActiveTool();
             if (currentTool == ActiveTool.DRAW_LINE || currentTool == ActiveTool.DRAW_CIRCLE ||
                 currentTool == ActiveTool.ZOOM_IN || currentTool == ActiveTool.ZOOM_OUT) {
                 svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
             } else if (currentTool == ActiveTool.PAN) {
                 svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
             } else {
                 svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
             }
        }
        if (toolManager.getActiveTool() != ActiveTool.SELECT && cadPanelLogic.selectedEntity != null) {
            cadPanelLogic.selectedEntity = null;
            redrawSVGCanvas();
        }
    }

    @Override
    public void init() { System.out.println("ModuleInterface.init() called on MainFrame."); }

    @Override
    public void start() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (svgCanvas == null && getContentPane().getComponentCount() == 0) {
                initUI();
            } else if (svgCanvas == null && getContentPane().getComponentCount() > 0) {
                ensureSvgCanvasInitialized();
            }
            setVisible(true);
        });
    }

    @Override
    public void stop() { setVisible(false); }

    @Override
    public void destroy() { dispose(); }

    private void openDxfFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Abrir Arquivo DXF");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos DXF (*.dxf)", "dxf"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            cadPanelLogic.loadDxfFromFile(fileToOpen);
            redrawSVGCanvas();
        }
    }

    public void loadSvg(String completeSvgString) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ensureSvgCanvasInitialized();
            if (svgCanvas == null) {
                 System.err.println("SVGCanvas not initialized in loadSvg invokeLater");
                 return;
            }
            if (completeSvgString == null || completeSvgString.trim().isEmpty()) {
                try {
                    svgCanvas.setSvgURI(null); // Clear display
                } catch (Exception e) {
                    System.err.println("Error trying to clear SVG canvas: " + e.getMessage());
                    e.printStackTrace();
                }
                svgCanvas.repaint();
                return;
            }

            File tempFile = null;
            try {
                tempFile = File.createTempFile("cadviewer-temp-svg-", ".svg");
                tempFile.deleteOnExit(); // Request deletion when JVM exits

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                    writer.write(completeSvgString);
                }

                URI fileUri = tempFile.toURI();
                svgCanvas.setSvgURI(fileUri);
                svgCanvas.repaint();

            } catch (IOException e) {
                System.err.println("Error creating or writing to temporary SVG file: " + e.getMessage());
                e.printStackTrace();
                try { // Fallback: try to clear the canvas
                    svgCanvas.setSvgURI(null);
                    svgCanvas.repaint();
                } catch (Exception ex) {
                    System.err.println("Error trying to clear SVG canvas after temp file IO failure: " + ex.getMessage());
                }
            } catch (Exception e) { // Catch other potential errors from setSvgURI with file URI
                System.err.println("Error setting SVG URI with temp file: " + e.getMessage());
                e.printStackTrace();
                try {
                    svgCanvas.setSvgURI(null);
                    svgCanvas.repaint();
                } catch (Exception ex) {
                     System.err.println("Error trying to clear SVG canvas after general temp file URI failure: " + ex.getMessage());
                }
            }
            // Note: Do not explicitly delete tempFile here if SVGPanel might load it asynchronously.
            // deleteOnExit() is the general strategy.
        });
    }

    private void ensureSvgCanvasInitialized() {
        if (this.svgCanvas == null) {
            this.svgCanvas = new SVGPanel();
            this.svgCanvas.setAntiAlias(true);
            add(this.svgCanvas, BorderLayout.CENTER);

            MouseAdapter mouseListener = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    cadPanelLogic.handleMousePress(new Point2D(e.getX(), e.getY()), toolManager.getActiveTool());
                    redrawSVGCanvas();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    cadPanelLogic.handleMouseRelease(new Point2D(e.getX(), e.getY()), toolManager.getActiveTool());
                    redrawSVGCanvas();
                }
            };

            MouseMotionListener motionListener = new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    cadPanelLogic.handleMouseDrag(new Point2D(e.getX(), e.getY()), toolManager.getActiveTool());
                    redrawSVGCanvas();
                }
                @Override public void mouseMoved(MouseEvent e) { /* Not used for now */ }
            };

            this.svgCanvas.addMouseListener(mouseListener);
            this.svgCanvas.addMouseMotionListener(motionListener);
            this.svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private void removePreviewLine() {
        if (cadPanelLogic != null) {
            cadPanelLogic.clearPreviewLineState();
            redrawSVGCanvas();
        }
    }

    private void applyZoom(double mouseX, double mouseY, double scaleFactor) {
        if (cadPanelLogic != null) {
            cadPanelLogic.applyZoom(mouseX, mouseY, scaleFactor);
            applyTransform();
        }
    }

    private void applyTransform() {
        if (svgCanvas == null || cadPanelLogic == null) return;
        System.out.println("Applying transform: Scale=" + cadPanelLogic.currentScale +
                           ", TranslateX=" + cadPanelLogic.translateX +
                           ", TranslateY=" + cadPanelLogic.translateY);
        svgCanvas.repaint();
    }

    void redrawSVGCanvas() {
        if (cadPanelLogic == null) return;
        String svgContent = cadPanelLogic.generateSvgContent();
        loadSvg(svgContent);
    }

    private void removePreviewCircle() {
        if (cadPanelLogic != null) {
            cadPanelLogic.clearPreviewCircleState();
            redrawSVGCanvas();
        }
    }
}
