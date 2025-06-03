package com.cad.gui;

import com.cad.core.api.ModuleInterface;
import com.cad.dxflib.common.Point2D;
import com.cad.dxflib.parser.DxfParserException;
import com.cad.gui.tool.ActiveTool;
import com.cad.gui.tool.ToolManager;
import com.cad.modules.geometry.entities.Circle2D;
import com.cad.modules.geometry.entities.Line2D;
import com.cad.modules.rendering.DxfRenderService;
// import org.apache.batik.dom.svg.SVGDOMImplementation; // Commented out for SVG Salamander
// import org.apache.batik.swing.JSVGCanvas; // Commented out for SVG Salamander
import com.kitfox.svg.app.beans.SVGPanel; // Added for SVG Salamander
// import org.w3c.dom.DOMImplementation; // Commented out, Batik/W3C DOM specific
// import org.w3c.dom.svg.SVGCircleElement; // Commented out, Batik/W3C DOM specific
// import org.w3c.dom.svg.SVGDocument; // Commented out, Batik/W3C DOM specific
// import org.w3c.dom.svg.SVGLineElement; // Commented out, Batik/W3C DOM specific

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame implements ModuleInterface {

    SVGPanel svgCanvas; // Changed from JSVGCanvas to SVGPanel, package-private for test access
    DxfRenderService dxfRenderService; // Novo campo (package-private for test access if needed, though unlikely)
    ToolManager toolManager; // Added ToolManager field, package-private for test access
    Point2D lineStartPoint = null; // Added for line drawing state, package-private for test access
    // private SVGLineElement previewLineSvgElement; // Commented out, Batik specific
    List<Line2D> drawnLines = new ArrayList<>(); // package-private for test access
    Point2D circleCenterPoint = null; // package-private for test access
    // private SVGCircleElement previewCircleSvgElement; // Commented out, Batik specific
    List<Circle2D> drawnCircles = new ArrayList<>(); // package-private for test access
    Object selectedEntity = null; // package-private for test access (if needed for selection tests)
    private final double HIT_TOLERANCE = 5.0; // Tolerância em pixels
    Point2D panLastMousePosition = null; // package-private for test access (if needed for pan tests)

    // Fields for zoom and pan state
    double currentScale = 1.0; // package-private for test access
    double translateX = 0.0; // package-private for test access
    double translateY = 0.0; // package-private for test access
    // Store the initial transform of the SVGPanel for reset or calculations if needed.
    // However, SVGPanel might not expose its transform directly.
    // For now, we'll manage scale and translation independently.
    String baseSvgContent = null; // To store the SVG from DXF conversion, package-private for test access
    Point2D previewEndPoint = null; // For line drawing preview, package-private for test access
    double previewRadius = 0; // For circle drawing preview, package-private for test access

    public MainFrame() {
        this(true); // Default constructor initializes UI
    }

    public MainFrame(boolean initializeUI) {
        // Initialize non-UI components first
        this.toolManager = new ToolManager();
        this.dxfRenderService = new DxfRenderService();

        if (initializeUI) {
            initUI(); // Initialize Swing components
        }
        // Note: ensureSvgCanvasInitialized() is called within initUI or needs to be handled
        // for the case where initializeUI is false if svgCanvas is used by non-UI logic
        // (which it isn't currently, redrawSVGCanvas calls it, but tests mock svgCanvas).
    }

    // Renamed from init() to initUI() and made private
    private void initUI() {
        setTitle("CAD Tool");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Ensure canvas is initialized for UI display
        ensureSvgCanvasInitialized();
        redrawSVGCanvas(); // Draw initial empty (but potentially transformed) canvas

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
        toolBar.setFloatable(false); // Opcional: para não permitir que a barra seja movida
        ButtonGroup toolModeGroup = new ButtonGroup();

        JToggleButton lineToggleButton = new JToggleButton("Linha");
        lineToggleButton.setToolTipText("Desenhar Linha");
        lineToggleButton.addActionListener(e -> {
            if (lineToggleButton.isSelected()) {
                removePreviewLine();
                removePreviewCircle();
                toolManager.setActiveTool(ActiveTool.DRAW_LINE);
                lineStartPoint = null;
                circleCenterPoint = null;
                if (selectedEntity != null) {
                    selectedEntity = null;
                    redrawSVGCanvas();
                }
                if (svgCanvas != null) {
                    svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                }
            }
        });
        toolModeGroup.add(lineToggleButton);
        toolBar.add(lineToggleButton);

        JToggleButton circleToggleButton = new JToggleButton("Círculo");
        circleToggleButton.setToolTipText("Desenhar Círculo");
        circleToggleButton.addActionListener(e -> {
            if (circleToggleButton.isSelected()) {
                removePreviewLine();
                removePreviewCircle();
                toolManager.setActiveTool(ActiveTool.DRAW_CIRCLE);
                lineStartPoint = null;
                circleCenterPoint = null;
                if (selectedEntity != null) {
                    selectedEntity = null;
                    redrawSVGCanvas();
                }
                if (svgCanvas != null) {
                    svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                }
            }
        });
        toolModeGroup.add(circleToggleButton);
        toolBar.add(circleToggleButton);

        toolBar.addSeparator(); // Adiciona um separador visual

        JToggleButton selectToggleButton = new JToggleButton("Selecionar");
        selectToggleButton.setToolTipText("Selecionar Entidade");
        selectToggleButton.addActionListener(e -> {
            if (selectToggleButton.isSelected()) {
                removePreviewLine();
                removePreviewCircle();
                toolManager.setActiveTool(ActiveTool.SELECT);
                lineStartPoint = null;
                circleCenterPoint = null;
                // Não limpar selectedEntity aqui
                if (svgCanvas != null) {
                    svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
        toolModeGroup.add(selectToggleButton);
        toolBar.add(selectToggleButton);

        toolBar.addSeparator();

        JToggleButton zoomInToggleButton = new JToggleButton("Zoom In");
        zoomInToggleButton.setToolTipText("Aumentar Zoom");
        zoomInToggleButton.addActionListener(e -> {
            if (zoomInToggleButton.isSelected()) {
                removePreviewLine();
                removePreviewCircle();
                toolManager.setActiveTool(ActiveTool.ZOOM_IN);
                lineStartPoint = null;
                circleCenterPoint = null;
                if (selectedEntity != null) {
                    selectedEntity = null;
                    redrawSVGCanvas();
                }
                if (svgCanvas != null) {
                    svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                }
            }
        });
        toolModeGroup.add(zoomInToggleButton);
        toolBar.add(zoomInToggleButton);

        JToggleButton zoomOutToggleButton = new JToggleButton("Zoom Out");
        zoomOutToggleButton.setToolTipText("Diminuir Zoom");
        zoomOutToggleButton.addActionListener(e -> {
            if (zoomOutToggleButton.isSelected()) {
                removePreviewLine();
                removePreviewCircle();
                toolManager.setActiveTool(ActiveTool.ZOOM_OUT);
                lineStartPoint = null;
                circleCenterPoint = null;
                if (selectedEntity != null) {
                    selectedEntity = null;
                    redrawSVGCanvas();
                }
                if (svgCanvas != null) {
                    svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                }
            }
        });
        toolModeGroup.add(zoomOutToggleButton);
        toolBar.add(zoomOutToggleButton);

        JToggleButton panToggleButton = new JToggleButton("Pan");
        panToggleButton.setToolTipText("Mover Visualização");
        panToggleButton.addActionListener(e -> {
            if (panToggleButton.isSelected()) {
                removePreviewLine();
                removePreviewCircle();
                toolManager.setActiveTool(ActiveTool.PAN);
                lineStartPoint = null;
                circleCenterPoint = null;
                if (selectedEntity != null) {
                    selectedEntity = null;
                    redrawSVGCanvas();
                }
                if (svgCanvas != null) {
                    svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }
        });
        toolModeGroup.add(panToggleButton);
        toolBar.add(panToggleButton);

        // Adiciona a toolbar ao JFrame. BorderLayout.PAGE_START é uma boa posição.
        add(toolBar, BorderLayout.PAGE_START);

        // Definir a ferramenta de Seleção como padrão inicial
        selectToggleButton.setSelected(true);
        toolManager.setActiveTool(ActiveTool.SELECT);
    }

    @Override
    public void init() {
        // This method is part of ModuleInterface.
        // If it's meant for UI setup, it should call initUI().
        // If it's for non-UI setup, that's already in the new constructor.
        // For now, assuming it's related to UI display lifecycle.
        // If MainFrame(false) is used, this might try to do UI things.
        // However, ModuleInterface.init() is usually for non-display setup.
        // The original call to this.init() in the default constructor is now initUI() via this(true).
        // Let's ensure this ModuleInterface.init() doesn't redo UI stuff if not intended.
        // For now, let's assume it's okay or not called in the test path.
        // If tests call start(), then it becomes an issue.

        // If ModuleInterface.init() is for non-UI logic, it should be called by both constructors.
        // If it implies UI readiness, then it should only be called if initializeUI was true.
        // The current structure: new MainFrame() -> this(true) -> initUI().
        // new MainFrame(false) does not call initUI().
        // The @Override init() here is from ModuleInterface.
        // This method is NOT the old init() method.
        System.out.println("ModuleInterface.init() called on MainFrame.");
        // If this method is critical for non-UI logic that tests rely on, that logic
        // should be in the MainFrame(boolean) constructor.
        // If it's for UI, it should be part of initUI().
    }


    @Override
    public void start() {
        // This implies UI is being started.
        // If MainFrame was constructed with initializeUI = false, this might be problematic.
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (svgCanvas == null && getContentPane().getComponentCount() == 0) {
                 // If UI was not initialized, and start is called, maybe initialize it now?
                 // Or, this implies an issue if start() is called on a non-UI-initialized MainFrame.
                 // For testing, we typically don't call start().
                System.err.println("MainFrame.start() called on a potentially non-UI-initialized frame. Canvas might be null.");
                initUI(); // Try to initialize UI if not done already.
            } else if (svgCanvas == null && getContentPane().getComponentCount() > 0) {
                // Components exist but svgCanvas is null - this state should ideally not happen if initUI was consistent.
                // For robustness, ensure canvas is there.
                ensureSvgCanvasInitialized();
            }
            setVisible(true);
        });
    }

    @Override
    public void stop() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            setVisible(false);
        });
    }

    @Override
    public void destroy() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            dispose();
        });
    }

    private void openDxfFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Abrir Arquivo DXF");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos DXF (*.dxf)", "dxf"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            System.out.println("Arquivo DXF selecionado: " + fileToOpen.getAbsolutePath());

            // Reset state for new file
            this.drawnLines.clear();
            this.drawnCircles.clear();
            this.currentScale = 1.0;
            this.translateX = 0.0;
            this.translateY = 0.0;
            this.baseSvgContent = null; // Clear previous DXF content
            this.selectedEntity = null; // Clear selection

            try (FileInputStream fis = new FileInputStream(fileToOpen)) {
                this.baseSvgContent = dxfRenderService.convertDxfToSvg(fis);
                removePreviewLine(); // These are Batik specific, ensure they are cleared if ever reintroduced
                removePreviewCircle();
            } catch (FileNotFoundException ex) {
                System.err.println("Arquivo não encontrado: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Arquivo não encontrado: " + fileToOpen.getAbsolutePath(), "Erro ao Abrir Arquivo", JOptionPane.ERROR_MESSAGE);
                this.baseSvgContent = null;
            } catch (DxfParserException ex) {
                System.err.println("Erro ao parsear DXF: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Erro ao processar o arquivo DXF: " + ex.getMessage(), "Erro de DXF", JOptionPane.ERROR_MESSAGE);
                this.baseSvgContent = null;
            } catch (IOException ex) {
                System.err.println("Erro de I/O: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Erro de leitura/escrita: " + ex.getMessage(), "Erro de I/O", JOptionPane.ERROR_MESSAGE);
                this.baseSvgContent = null;
            }
            redrawSVGCanvas(); // Redraw with new base content (or empty if error)
        }
    }

    // Simplified loadSvg: it just loads the provided complete SVG string.
    // All transformations and content aggregation happen in redrawSVGCanvas.
    public void loadSvg(String completeSvgString) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ensureSvgCanvasInitialized();
            if (svgCanvas == null) return;

            if (completeSvgString == null || completeSvgString.trim().isEmpty()) {
                try {
                    this.svgCanvas.setSvgURI(null); // Clear display
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.svgCanvas.repaint();
                return;
            }
            try {
                String encodedSvg = java.net.URLEncoder.encode(completeSvgString, "UTF-8").replace("+", "%20");
                java.net.URI svgUri = new java.net.URI("data:image/svg+xml;charset=UTF-8," + encodedSvg);
                this.svgCanvas.setSvgURI(svgUri);
                this.svgCanvas.repaint();
            } catch (Exception e) { // Catch all: URISyntax, UnsupportedEncoding, other SVGPanel issues
                 e.printStackTrace();
                 try {
                    this.svgCanvas.setSvgURI(null); // Clear display on error
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                 this.svgCanvas.repaint();
            }
        });
    }

    private void ensureSvgCanvasInitialized() {
        // This method might be called when svgCanvas is already a mock.
        // Only proceed with new SVGPanel() if it's truly null (e.g. during real UI init).
        if (this.svgCanvas == null) {
            this.svgCanvas = new SVGPanel();
            this.svgCanvas.setAntiAlias(true);
            // this.svgCanvas.setAutosize(SVGPanel.AUTOSIZE_STRETCH); // Commented out
            add(this.svgCanvas, BorderLayout.CENTER); // This is a JFrame.add()

            // Add mouse listeners only if we are initializing the real UI
            // and not when svgCanvas is a mock being set up by a test.
            // However, the current test setup replaces svgCanvas *after* new MainFrame() -> initUI()
            // So, this check might be tricky.
            // A cleaner way: tests mock svgCanvas, real UI construction uses real SVGPanel.
            // If this method is called from initUI, svgCanvas would be null before new SVGPanel().
            // If called from test setup after mock injection, it should not re-initialize.
            // The current structure: MainFrame() -> this(true) -> initUI() -> ensureSvgCanvasInitialized().
            // MainFrame(false) -> no initUI(). Test then sets mainFrame.svgCanvas.
            // So, this.svgCanvas will be null only during real UI setup.

            // Adicionar listeners de mouse - these should only be added to a real canvas
            MouseAdapter mouseListener = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (toolManager.getActiveTool() == ActiveTool.DRAW_LINE) {
                        if (lineStartPoint == null) {
                            // Primeiro clique para a linha
                            lineStartPoint = new Point2D(e.getX(), e.getY());
                            previewEndPoint = lineStartPoint; // Initialize previewEndPoint
                            System.out.println("Modo Desenhar Linha: Ponto inicial em " + lineStartPoint.x + ", " + lineStartPoint.y);
                            redrawSVGCanvas(); // Show the first point or initial preview
                        } else {
                            // Segundo clique para a linha
                            Point2D endPoint = new Point2D(e.getX(), e.getY());
                            Line2D newLine = new Line2D(lineStartPoint, endPoint);
                            System.out.println("Modo Desenhar Linha: Ponto final em " + endPoint.x + ", " + endPoint.y + ". Linha criada: " + newLine.toString());
                            drawnLines.add(newLine);
                            lineStartPoint = null; // Reset para a próxima linha
                            previewEndPoint = null; // Clear preview line
                            redrawSVGCanvas(); // Atualiza o canvas com a nova linha
                        }
                    } else if (toolManager.getActiveTool() == ActiveTool.DRAW_CIRCLE) {
                        if (circleCenterPoint == null) {
                            circleCenterPoint = new Point2D(e.getX(), e.getY());
                            previewRadius = 0; // Initialize preview radius
                            System.out.println("Modo Desenhar Círculo: Centro em " + circleCenterPoint.x + ", " + circleCenterPoint.y);
                            redrawSVGCanvas(); // Show the center point or initial preview
                        } else {
                            Point2D pointOnCircumference = new Point2D(e.getX(), e.getY());
                            double radius = Math.sqrt(Math.pow(pointOnCircumference.x - circleCenterPoint.x, 2) + Math.pow(pointOnCircumference.y - circleCenterPoint.y, 2));
                            if (radius > 0) {
                                Circle2D newCircle = new Circle2D(circleCenterPoint, radius);
                                drawnCircles.add(newCircle);
                                System.out.println("Modo Desenhar Círculo: Raio " + radius + ". Círculo criado: " + newCircle.toString());
                            }
                            circleCenterPoint = null; // Reset para o próximo círculo
                            previewRadius = 0; // Clear preview circle
                            redrawSVGCanvas(); // Atualizar canvas para mostrar o círculo finalizado
                        }
                    } else if (toolManager.getActiveTool() == ActiveTool.SELECT) {
                        selectedEntity = null; // Desselecionar ao clicar novamente
                        Point2D clickPoint = new Point2D(e.getX(), e.getY());
                        System.out.println("Modo Seleção: Clique em " + clickPoint.x + ", " + clickPoint.y);

                        // Priorizar entidades desenhadas mais recentemente (topo da lista)
                        // Verificar círculos primeiro
                        for (int i = drawnCircles.size() - 1; i >= 0; i--) {
                            Circle2D circle = drawnCircles.get(i);
                            if (isPointNearCircle(clickPoint, circle, HIT_TOLERANCE)) {
                                selectedEntity = circle;
                                break;
                            }
                        }

                        // Seleção de Linhas (só se nenhum círculo foi selecionado)
                        if (selectedEntity == null) {
                            for (int i = drawnLines.size() - 1; i >= 0; i--) {
                                Line2D line = drawnLines.get(i);
                                if (isPointNearLine(clickPoint, line, HIT_TOLERANCE)) {
                                    selectedEntity = line;
                                    break;
                                }
                            }
                        }

                        if (selectedEntity != null) {
                            System.out.println("Entidade selecionada: " + selectedEntity.toString());
                        } else {
                            System.out.println("Nenhuma entidade selecionada.");
                        }
                        redrawSVGCanvas(); // Redesenhar para mostrar feedback visual da seleção
                    } else if (toolManager.getActiveTool() == ActiveTool.ZOOM_IN) {
                        if (svgCanvas == null) return;
                        // Zoom In
                        double scaleFactorIn = 1.25;
                        applyZoom(e.getX(), e.getY(), scaleFactorIn);
                        System.out.println("Zoom In at (" + e.getX() + ", " + e.getY() + ")");
                    } else if (toolManager.getActiveTool() == ActiveTool.ZOOM_OUT) {
                        if (svgCanvas == null) return;
                        // Zoom Out
                        double scaleFactorOut = 0.8;
                        applyZoom(e.getX(), e.getY(), scaleFactorOut);
                        System.out.println("Zoom Out at (" + e.getX() + ", " + e.getY() + ")");
                    } else if (toolManager.getActiveTool() == ActiveTool.PAN) {
                        if (svgCanvas == null) return;
                        panLastMousePosition = new Point2D(e.getX(), e.getY());
                        // No actual transform change here, just record start point for dragging
                        System.out.println("Pan initiated at (" + panLastMousePosition.x + ", " + panLastMousePosition.y + ")");
                    } else {
                        // Comportamento para outras ferramentas ou nenhuma ferramenta
                        removePreviewLine();
                        removePreviewCircle();
                        System.out.println("Mouse Pressed (Ferramenta Padrão/Nenhuma: " + toolManager.getActiveTool() + ") at: " + e.getX() + ", " + e.getY());
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    System.out.println("Mouse Clicked at: " + e.getX() + ", " + e.getY());
                    // Futuramente: Finalizar uma seleção ou um ponto de um desenho
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (toolManager.getActiveTool() == ActiveTool.PAN && panLastMousePosition != null) {
                        if (svgCanvas == null) return;
                        System.out.println("Pan finalizado.");
                        panLastMousePosition = null;
                    }
                }
            };

            MouseMotionListener motionListener = new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    // System.out.println("Mouse Dragged to: " + e.getX() + ", " + e.getY()); // Comentado para não poluir

                    if (toolManager.getActiveTool() == ActiveTool.DRAW_LINE && lineStartPoint != null) {
                        previewEndPoint = new Point2D(e.getX(), e.getY());
                        redrawSVGCanvas(); // Redraw with preview line
                    } else if (toolManager.getActiveTool() == ActiveTool.DRAW_CIRCLE && circleCenterPoint != null) {
                        previewRadius = Math.sqrt(Math.pow(e.getX() - circleCenterPoint.x, 2) + Math.pow(e.getY() - circleCenterPoint.y, 2));
                        if (previewRadius < 0) previewRadius = 0; // Ensure radius is not negative
                        redrawSVGCanvas(); // Redraw with preview circle
                    } else if (toolManager.getActiveTool() == ActiveTool.PAN && panLastMousePosition != null) {
                        if (svgCanvas == null || panLastMousePosition == null) return;

                        double dx = e.getX() - panLastMousePosition.x;
                        double dy = e.getY() - panLastMousePosition.y;

                        translateX += dx;
                        translateY += dy;

                        applyTransform(); // This calls redrawSVGCanvas indirectly via repaint

                        panLastMousePosition = new Point2D(e.getX(), e.getY());
                    } else {
                        // For other tools or if conditions aren't met, ensure previews are cleared
                        // This might be redundant if removePreviewLine/Circle are called on tool switch
                        if (previewEndPoint != null || previewRadius > 0) {
                             removePreviewLine(); // This will set previewEndPoint = null and redraw
                             removePreviewCircle(); // This will set previewRadius = 0 and redraw
                        }
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    // System.out.println("Mouse Moved to: " + e.getX() + ", " + e.getY());
                    // Comentado para não poluir muito o console, mas útil para depuração
                }
            };

            this.svgCanvas.addMouseListener(mouseListener);
            this.svgCanvas.addMouseMotionListener(motionListener);
            this.svgCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private void removePreviewLine() {
        if (previewEndPoint != null) {
            previewEndPoint = null;
            redrawSVGCanvas();
        }
    }

    private void applyZoom(double mouseX, double mouseY, double scaleFactor) {
        if (svgCanvas == null) return;

        // World coordinates of the mouse point before zoom
        double preZoomWorldX = (mouseX - translateX) / currentScale;
        double preZoomWorldY = (mouseY - translateY) / currentScale;

        currentScale *= scaleFactor;

        // New translation to keep the mouse point fixed
        translateX = mouseX - preZoomWorldX * currentScale;
        translateY = mouseY - preZoomWorldY * currentScale;

        applyTransform();
    }

    private void applyTransform() {
        if (svgCanvas == null) return;

        // This is highly speculative. SVGPanel might not have these methods.
        // If it uses AffineTransform directly, the approach would be different.
        // E.g., svgCanvas.setRenderingHint(SVGPanel.KEY_TRANSFORM, createAffineTransform());
        // Or svgCanvas.setSVGUniverse(universe); universe.getRoot().setTransform(...);
        // For now, assuming simple scale and translate methods might exist or we might need
        // to manage the transform outside and re-render the SVG with the transform applied
        // if SVGPanel doesn't support dynamic transforms on the displayed document.

        // The SVG Salamander SVGPanel does not seem to have direct setScale/setTranslate methods
        // in a way that affects the rendering transform directly like Batik's JSVGCanvas.
        // It primarily loads an SVG and displays it. Transformations typically need to be
        // applied to the SVG content itself (e.g., by wrapping with a <g transform="...">)
        // or by transforming the Graphics2D context if we were overriding paintComponent.

        // Since direct manipulation methods on SVGPanel are not known/likely,
        // for this step, we will log the desired transform.
        // A full implementation would require either:
        // 1. Modifying the SVG content string to include the transform and reloading. (Complex for dynamic ops)
        // 2. Subclassing SVGPanel and overriding paintComponent to apply the AffineTransform to Graphics2D.
        // 3. Finding specific API in SVG Salamander for this (if it exists and is not obvious).

        System.out.println("Applying transform: Scale=" + currentScale + ", TranslateX=" + translateX + ", TranslateY=" + translateY);
        // svgCanvas.setScale(currentScale); // Hypothetical
        // svgCanvas.setTranslateX(translateX); // Hypothetical
        // svgCanvas.setTranslateY(translateY); // Hypothetical
        svgCanvas.repaint();
    }


    void redrawSVGCanvas() { // Changed from private to package-private for test access
        StringBuilder svgBuilder = new StringBuilder();
        // Apply current view transform to the root SVG element.
        svgBuilder.append("<svg width=\"100%\" height=\"100%\" xmlns=\"http://www.w3.org/2000/svg\">");
        svgBuilder.append("<g transform=\"translate(").append(translateX).append(",").append(translateY).append(") scale(").append(currentScale).append(")\">");

        // Append base SVG content (from DXF)
        if (baseSvgContent != null) {
            // Fragile way to strip outer <svg> tags from baseSvgContent
            // Assumes baseSvgContent is a full SVG document string.
            int firstSvgTagEnd = baseSvgContent.indexOf('>');
            // Find the last occurrence of </svg>
            int lastSvgTagStart = baseSvgContent.lastIndexOf("</svg>");

            if (firstSvgTagEnd != -1 && lastSvgTagStart != -1 && firstSvgTagEnd < lastSvgTagStart) {
                svgBuilder.append(baseSvgContent.substring(firstSvgTagEnd + 1, lastSvgTagStart));
            } else {
                // Fallback or error: if tags not found, append the whole thing (might be invalid)
                // or log an error. For now, append to see what happens.
                // svgBuilder.append(baseSvgContent);
                System.err.println("Could not strip <svg> tags from baseSvgContent, structure might be unexpected.");
            }
        }

        // Append dynamically drawn entities
        for (Line2D line : drawnLines) {
            String lineStrokeColor = "black";
            String lineStrokeWidth = "2";
            if (line == selectedEntity) { // Comparação de referência de objeto
                lineStrokeColor = "red";
                lineStrokeWidth = "3";
            }
            // Using getters for Line2D startPoint and endPoint, and public fields for Point2D x and y
            svgBuilder.append("<line x1=\"").append(line.getStartPoint().x)
                      .append("\" y1=\"").append(line.getStartPoint().y)
                      .append("\" x2=\"").append(line.getEndPoint().x)
                      .append("\" y2=\"").append(line.getEndPoint().y)
                      .append("\" stroke=\"").append(lineStrokeColor)
                      .append("\" stroke-width=\"").append(lineStrokeWidth)
                      .append("\"/>");
        }

        for (Circle2D circle : drawnCircles) {
            String circleStrokeColor = "blue";
            String circleStrokeWidth = "2";
            if (circle == selectedEntity) {
                circleStrokeColor = "red";
                circleStrokeWidth = "3";
            }
            svgBuilder.append("<circle cx=\"").append(circle.getCenter().x)
                      .append("\" cy=\"").append(circle.getCenter().y)
                      .append("\" r=\"").append(circle.getRadius())
                      .append("\" stroke=\"").append(circleStrokeColor)
                      .append("\" stroke-width=\"").append(circleStrokeWidth)
                      .append("\" fill=\"none\"/>");
        }

        // Append preview line if active
        if (toolManager.getActiveTool() == ActiveTool.DRAW_LINE && lineStartPoint != null && previewEndPoint != null) {
            svgBuilder.append("<line x1=\"").append(lineStartPoint.x)
                      .append("\" y1=\"").append(lineStartPoint.y)
                      .append("\" x2=\"").append(previewEndPoint.x)
                      .append("\" y2=\"").append(previewEndPoint.y)
                      .append("\" stroke=\"gray\" stroke-width=\"1\" stroke-dasharray=\"5,5\"/>");
        }

        // Append preview circle if active
        if (toolManager.getActiveTool() == ActiveTool.DRAW_CIRCLE && circleCenterPoint != null && previewRadius > 0) {
            svgBuilder.append("<circle cx=\"").append(circleCenterPoint.x)
                      .append("\" cy=\"").append(circleCenterPoint.y)
                      .append("\" r=\"").append(previewRadius)
                      .append("\" stroke=\"gray\" stroke-width=\"1\" stroke-dasharray=\"5,5\" fill=\"none\"/>");
        }

        svgBuilder.append("</g>"); // Close the transform group
        svgBuilder.append("</svg>");
        loadSvg(svgBuilder.toString()); // This will re-parse and display the SVG
    }

    private void removePreviewCircle() {
        if (previewRadius > 0) {
            previewRadius = 0;
            redrawSVGCanvas();
        }
    }

    private boolean isPointNearLine(Point2D p, Line2D line, double tolerance) {
        // Using getters for Line2D startPoint and endPoint, and public fields for Point2D x and y
        Point2D p1 = line.getStartPoint();
        Point2D p2 = line.getEndPoint();

        double dxL = p2.x - p1.x;
        double dyL = p2.y - p1.y;

        if (dxL == 0 && dyL == 0) { // Linha é um ponto
            return Math.sqrt(Math.pow(p.x - p1.x, 2) + Math.pow(p.y - p1.y, 2)) <= tolerance;
        }

        double t = ((p.x - p1.x) * dxL + (p.y - p1.y) * dyL) / (dxL * dxL + dyL * dyL);

        Point2D closestPoint;
        if (t < 0) {
            closestPoint = p1;
        } else if (t > 1) {
            closestPoint = p2;
        } else {
            closestPoint = new Point2D(p1.x + t * dxL, p1.y + t * dyL);
        }

        double dist = Math.sqrt(Math.pow(p.x - closestPoint.x, 2) + Math.pow(p.y - closestPoint.y, 2));
        return dist <= tolerance;
    }

    private boolean isPointNearCircle(Point2D point, Circle2D circle, double tolerance) {
        Point2D center = circle.getCenter(); // Assumindo getCenter() for Circle2D
        double radius = circle.getRadius();  // Assumindo getRadius() for Circle2D
        // Using public fields for Point2D x and y
        double distToCenter = Math.sqrt(Math.pow(point.x - center.x, 2) + Math.pow(point.y - center.y, 2));
        return Math.abs(distToCenter - radius) <= tolerance;
    }
}
