package com.cad.gui;

import com.cad.core.api.ModuleInterface;
import com.cad.dxflib.common.Point2D;
import com.cad.dxflib.parser.DxfParserException;
import com.cad.gui.tool.ActiveTool;
import com.cad.gui.tool.ToolManager;
import com.cad.modules.rendering.DxfRenderService;
// SVGPanel is removed
// import com.kitfox.svg.app.beans.SVGPanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent; // Added for zoom
import java.awt.event.MouseWheelListener; // Added for zoom
import java.io.File;
// FileInputStream, FileNotFoundException, IOException, FileWriter, BufferedWriter, URI are likely not needed directly here anymore
// import java.io.FileInputStream;
// import java.io.FileNotFoundException;
// import java.io.IOException;
// import java.io.FileWriter;
// import java.io.BufferedWriter;
// import java.net.URI;


public class MainFrame extends JFrame implements ModuleInterface {

    // SVGPanel svgCanvas; // Removed
    CustomCadPanel customCadPanel; // Added
    ToolManager toolManager;

    // private CadPanelLogic cadPanelLogic; // Removed

    // CadPanelLogic getCadPanelLogic() { // Removed
    // return this.cadPanelLogic;
    // }

    // void setSvgCanvasForTest(SVGPanel canvas) { // Removed
    // this.svgCanvas = canvas;
    // }

    public MainFrame() {
        this(true);
    }

    public MainFrame(boolean initializeUI) {
        this.toolManager = new ToolManager();
        // this.cadPanelLogic = new CadPanelLogic(this.toolManager, new DxfRenderService()); // Removed
        this.customCadPanel = new CustomCadPanel(this.toolManager, new DxfRenderService()); // Added

        if (initializeUI) {
            initUI();
        }
    }

    private void initUI() {
        setTitle("CAD Tool");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ensureSvgCanvasInitialized(); // Removed, customCadPanel is initialized in constructor
        add(customCadPanel, BorderLayout.CENTER); // Add customCadPanel
        // customCadPanel.currentScale = 1.0; // Access via customCadPanel directly if needed or use methods
        // customCadPanel.translateX = 0.0;
        // customCadPanel.translateY = 0.0;
        customCadPanel.repaint(); // Use repaint

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

        // Zoom In/Out buttons might be re-purposed or removed if wheel zoom is primary
        // For now, they can set the tool, and mouse click on panel can trigger zoom.
        JToggleButton zoomInToggleButton = new JToggleButton("Zoom In");
        zoomInToggleButton.addActionListener(e -> {
            if (zoomInToggleButton.isSelected()) {
                toolManager.setActiveTool(ActiveTool.ZOOM_IN);
                clearDrawingStateForNewOperation();
                // customCadPanel.applyZoom(1.2, new Point2D(customCadPanel.getWidth() / 2.0, customCadPanel.getHeight() / 2.0));
            }
        });
        toolModeGroup.add(zoomInToggleButton);
        toolBar.add(zoomInToggleButton);

        JToggleButton zoomOutToggleButton = new JToggleButton("Zoom Out");
        zoomOutToggleButton.addActionListener(e -> {
            if (zoomOutToggleButton.isSelected()) {
                toolManager.setActiveTool(ActiveTool.ZOOM_OUT);
                clearDrawingStateForNewOperation();
                // customCadPanel.applyZoom(1/1.2, new Point2D(customCadPanel.getWidth() / 2.0, customCadPanel.getHeight() / 2.0));
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
        selectToggleButton.setSelected(true); // Default tool
        toolManager.setActiveTool(ActiveTool.SELECT);
        clearDrawingStateForNewOperation(); // Set initial cursor

        // Add mouse listeners to customCadPanel
        customCadPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // For Zoom In/Out tools, a click on the panel will perform the zoom
                if (toolManager.getActiveTool() == ActiveTool.ZOOM_IN) {
                    customCadPanel.applyZoom(1.2, new Point2D(e.getX(), e.getY()));
                } else if (toolManager.getActiveTool() == ActiveTool.ZOOM_OUT) {
                    customCadPanel.applyZoom(1/1.2, new Point2D(e.getX(), e.getY()));
                } else {
                    customCadPanel.handleMousePress(new Point2D(e.getX(), e.getY()));
                }
                // repaint is called within handleMousePress or applyZoom
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                customCadPanel.handleMouseRelease(new Point2D(e.getX(), e.getY()));
                // repaint is called within handleMouseRelease
            }
        });

        customCadPanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                customCadPanel.handleMouseDrag(new Point2D(e.getX(), e.getY()), toolManager.getActiveTool());
                // repaint is called within handleMouseDrag
            }
            @Override public void mouseMoved(MouseEvent e) { /* Not used for now */ }
        });

        customCadPanel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double zoomFactor = (e.getWheelRotation() < 0) ? 1.1 : 1 / 1.1;
                customCadPanel.applyZoom(zoomFactor, new Point2D(e.getX(), e.getY()));
            }
        });
    }

    private void clearDrawingStateForNewOperation() {
        if (customCadPanel == null) return; // Guard against null panel

        customCadPanel.clearPreviewLineState();
        customCadPanel.clearPreviewCircleState();
        // customCadPanel.lineStartPoint = null; // These are handled by clearPreview methods
        // customCadPanel.circleCenterPoint = null;

        ActiveTool currentTool = toolManager.getActiveTool();
        if (currentTool == ActiveTool.DRAW_LINE || currentTool == ActiveTool.DRAW_CIRCLE ||
            currentTool == ActiveTool.ZOOM_IN || currentTool == ActiveTool.ZOOM_OUT) {
            customCadPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else if (currentTool == ActiveTool.PAN) {
            customCadPanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        } else {
            customCadPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        // if (toolManager.getActiveTool() != ActiveTool.SELECT && customCadPanel.selectedEntity != null) {
        // customCadPanel.selectedEntity = null; // selectedEntity is private, handle selection clearing in CustomCadPanel if needed
        // customCadPanel.repaint();
        // }
        // For now, let selection persist until another selection is made or tool changes.
        // If explicit deselection is needed when changing tool, add a method in CustomCadPanel.
    }

    @Override
    public void init() { System.out.println("ModuleInterface.init() called on MainFrame."); }

    @Override
    public void start() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            // if (svgCanvas == null && getContentPane().getComponentCount() == 0) { // Old logic
            if (getContentPane().getComponentCount() == 0) { // Simplified: if no components, initUI
                initUI();
            // } else if (svgCanvas == null && getContentPane().getComponentCount() > 0) { // Old logic
            // ensureSvgCanvasInitialized(); // Removed
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
            // cadPanelLogic.loadDxfFromFile(fileToOpen); // Removed
            customCadPanel.loadDxfFromFile(fileToOpen); // Added
            // redrawSVGCanvas(); // Removed
            customCadPanel.repaint(); // Added
        }
    }

    // public void loadSvg(String completeSvgString) { // Removed
    // }

    // private void ensureSvgCanvasInitialized() { // Removed
    // }

    // private void removePreviewLine() { // Removed
    // }

    // private void applyZoom(double mouseX, double mouseY, double scaleFactor) { // Removed (MainFrame version)
    // }

    // private void applyTransform() { // Removed
    // }

    // void redrawSVGCanvas() { // Removed
    // }

    // private void removePreviewCircle() { // Removed
    // }
}
