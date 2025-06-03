package com.cad.gui;

import com.cad.core.api.ModuleInterface;
import com.cad.dxflib.common.Point2D; // Keep if Point2D is used in event data, e.g. new Point2D(e.getX(), e.getY())
import com.cad.dxflib.parser.DxfParserException; // Keep for openDxfFile JOptionPane
import com.cad.gui.tool.ActiveTool;
import com.cad.gui.tool.ToolManager;
// Entities are now managed by CadPanelLogic
// import com.cad.modules.geometry.entities.Circle2D;
// import com.cad.modules.geometry.entities.Line2D;
import com.cad.modules.rendering.DxfRenderService; // CadPanelLogic will need this
import com.kitfox.svg.app.beans.SVGPanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File; // Keep for JFileChooser
import java.io.FileInputStream; // Keep for openDxfFile
import java.io.FileNotFoundException; // Keep for openDxfFile
import java.io.IOException; // Keep for openDxfFile
// ArrayList and List are now managed by CadPanelLogic
// import java.util.ArrayList;
// import java.util.List;

public class MainFrame extends JFrame implements ModuleInterface {

    SVGPanel svgCanvas; // UI component, stays in MainFrame
    ToolManager toolManager; // UI related state, stays in MainFrame

    private CadPanelLogic cadPanelLogic; // Handles the core logic

    // Getter for tests to access CadPanelLogic
    CadPanelLogic getCadPanelLogic() {
        return this.cadPanelLogic;
    }

    // Package-private setter for tests to inject a mock SVGPanel
    void setSvgCanvasForTest(SVGPanel canvas) {
        this.svgCanvas = canvas;
        // If mouse listeners were added to the real svgCanvas in ensureSvgCanvasInitialized,
        // tests might need to be aware or this method might need to re-attach them
        // to the mock if those listeners are part of what's being tested indirectly.
        // For now, tests mock interactions on svgCanvas directly via Mockito.
    }

    public MainFrame() {
        this(true);
    }

    public MainFrame(boolean initializeUI) {
        this.toolManager = new ToolManager();
        // DxfRenderService is now instantiated within CadPanelLogic
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
        // Initial state for logic components is handled in CadPanelLogic constructor
        redrawSVGCanvas(); // Draw initial state from CadPanelLogic

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

        // Tool buttons now primarily set the tool in ToolManager and update UI state.
        // Logic for clearing previews etc. is handled by CadPanelLogic or MainFrame delegates.
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
        // Delegate state clearing to CadPanelLogic
        cadPanelLogic.clearPreviewLineState();
        cadPanelLogic.clearPreviewCircleState();
        // Reset interaction-specific points in logic
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
        // If deselection should happen on any tool change other than SELECT:
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
            // Delegate file handling and state reset to CadPanelLogic
            cadPanelLogic.loadDxfFromFile(fileToOpen);
            redrawSVGCanvas();
        }
    }

    public void loadSvg(String completeSvgString) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ensureSvgCanvasInitialized();
            if (svgCanvas == null) return;
            if (completeSvgString == null || completeSvgString.trim().isEmpty()) {
                try { svgCanvas.setSvgURI(null); } catch (Exception e) { e.printStackTrace(); }
            } else {
                try {
                    String encodedSvg = java.net.URLEncoder.encode(completeSvgString, "UTF-8").replace("+", "%20");
                    java.net.URI svgUri = new java.net.URI("data:image/svg+xml;charset=UTF-8," + encodedSvg);
                    svgCanvas.setSvgURI(svgUri);
                } catch (Exception e) {
                     e.printStackTrace();
                     try { svgCanvas.setSvgURI(null); } catch (Exception ex) { ex.printStackTrace(); }
                }
            }
            svgCanvas.repaint();
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
                    // Delegate to CadPanelLogic to handle the event and update its state
                    cadPanelLogic.handleMousePress(new Point2D(e.getX(), e.getY()), toolManager.getActiveTool());
                    redrawSVGCanvas(); // Redraw based on updated state in CadPanelLogic
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    // Delegate if necessary, e.g., for PAN tool
                    cadPanelLogic.handleMouseRelease(new Point2D(e.getX(), e.getY()), toolManager.getActiveTool());
                    redrawSVGCanvas();
                }
            };

            MouseMotionListener motionListener = new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    // Delegate to CadPanelLogic
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
            applyTransform(); // repaint
        }
    }

    private void applyTransform() { // This method now primarily triggers repaint
        if (svgCanvas == null || cadPanelLogic == null) return;
        System.out.println("Applying transform: Scale=" + cadPanelLogic.currentScale +
                           ", TranslateX=" + cadPanelLogic.translateX +
                           ", TranslateY=" + cadPanelLogic.translateY);
        svgCanvas.repaint();
    }

    void redrawSVGCanvas() {
        if (cadPanelLogic == null) return;
        // Get the complete SVG string from CadPanelLogic
        String svgContent = cadPanelLogic.generateSvgContent();
        loadSvg(svgContent);
    }

    private void removePreviewCircle() {
        if (cadPanelLogic != null) {
            cadPanelLogic.clearPreviewCircleState();
            redrawSVGCanvas();
        }
    }
    // isPointNearLine and isPointNearCircle are now in CadPanelLogic
}
