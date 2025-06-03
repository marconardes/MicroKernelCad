package com.cad.gui;

import com.cad.core.api.ModuleInterface;
import com.cad.dxflib.common.Point2D;
import com.cad.dxflib.parser.DxfParserException;
import com.cad.gui.tool.ActiveTool;
import com.cad.gui.tool.ToolManager;
import com.cad.modules.geometry.entities.Circle2D;
import com.cad.modules.geometry.entities.Line2D;
import com.cad.modules.rendering.DxfRenderService;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.svg.SVGCircleElement;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLineElement;

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

    private JSVGCanvas svgCanvas;
    private DxfRenderService dxfRenderService; // Novo campo
    private ToolManager toolManager; // Added ToolManager field
    private Point2D lineStartPoint = null; // Added for line drawing state
    private SVGLineElement previewLineSvgElement;
    private List<Line2D> drawnLines = new ArrayList<>();
    private Point2D circleCenterPoint = null;
    private SVGCircleElement previewCircleSvgElement;
    private List<Circle2D> drawnCircles = new ArrayList<>();
    private Object selectedEntity = null;
    private final double HIT_TOLERANCE = 5.0; // Tolerância em pixels
    private Point2D panLastMousePosition = null;

    public MainFrame() {
        // Call init to set up the frame
        this.init();
    }

    @Override
    public void init() {
        this.toolManager = new ToolManager(); // Instantiate ToolManager

        setTitle("CAD Tool");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // svgCanvas = new JSVGCanvas(); // REMOVE THIS
        // add(svgCanvas, BorderLayout.CENTER); // REMOVE THIS

        dxfRenderService = new DxfRenderService(); // Instanciar o serviço

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
    public void start() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ensureSvgCanvasInitialized(); // Initialize canvas before showing
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

            this.drawnLines.clear();
            // Não é necessário chamar redrawSVGCanvas() aqui, pois loadSvg() será chamado
            // com o conteúdo do DXF ou com null/erro, que já redesenha ou limpa.

            this.drawnCircles.clear(); // Adicionado para limpar círculos desenhados
            try (FileInputStream fis = new FileInputStream(fileToOpen)) {
                String svgContent = dxfRenderService.convertDxfToSvg(fis);
                loadSvg(svgContent);
                removePreviewLine();
                removePreviewCircle(); // Adicionado
            } catch (FileNotFoundException ex) {
                System.err.println("Arquivo não encontrado: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Arquivo não encontrado: " + fileToOpen.getAbsolutePath(), "Erro ao Abrir Arquivo", JOptionPane.ERROR_MESSAGE);
                loadSvg(null); // Limpa a visualização
                removePreviewLine();
                removePreviewCircle(); // Adicionado
            } catch (DxfParserException ex) {
                System.err.println("Erro ao parsear DXF: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Erro ao processar o arquivo DXF: " + ex.getMessage(), "Erro de DXF", JOptionPane.ERROR_MESSAGE);
                loadSvg(null); // Limpa a visualização
                removePreviewLine();
                removePreviewCircle(); // Adicionado
            } catch (IOException ex) {
                System.err.println("Erro de I/O: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Erro de leitura/escrita: " + ex.getMessage(), "Erro de I/O", JOptionPane.ERROR_MESSAGE);
                loadSvg(null); // Limpa a visualização
                removePreviewLine();
                removePreviewCircle(); // Adicionado
            }
        }
    }

    public void loadSvg(String svgContent) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ensureSvgCanvasInitialized(); // Ensure canvas exists before using it
            if (svgContent == null || svgContent.trim().isEmpty()) {
                this.svgCanvas.setSVGDocument(null); // Limpa o canvas using the field
                // Opcional: Carregar um SVG que diz "Erro" ou "Vazio"
                // String errorSvg = "<svg width='100%' height='100%'><text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' font-size='20' fill='red'>Erro ao carregar SVG ou arquivo vazio.</text></svg>";
                // try {
                //     String svgDataUri = "data:image/svg+xml;charset=UTF-8," + java.net.URLEncoder.encode(errorSvg, "UTF-8").replace("+", "%20");
                //     this.svgCanvas.setURI(svgDataUri);
                // } catch (java.io.UnsupportedEncodingException e) { /* ignore */ }
                return;
            }
            try {
                // Usando setURI para contornar problemas anteriores com SAXSVGDocumentFactory
                // O conteúdo SVG precisa ser devidamente codificado para um data URI
                String svgDataUri = "data:image/svg+xml;charset=UTF-8," + java.net.URLEncoder.encode(svgContent, "UTF-8").replace("+", "%20");
                this.svgCanvas.setURI(svgDataUri);

            } catch (java.io.UnsupportedEncodingException e) {
                e.printStackTrace(); // Deveria tratar isso de forma mais elegante
                this.svgCanvas.setSVGDocument(null); // Limpa em caso de erro de codificação
            } catch (Exception e) { // Catch other potential Batik exceptions from setURI
                 e.printStackTrace();
                 this.svgCanvas.setSVGDocument(null); // Limpa em caso de erro
            }
            // Removida a referência explícita à SAXSVGDocumentFactory por enquanto
            // devido a problemas de compilação anteriores na sub-tarefa.
            // A abordagem setURI com data URI é mais robusta neste momento.
        });
    }

    private void ensureSvgCanvasInitialized() {
        if (this.svgCanvas == null) {
            this.svgCanvas = new JSVGCanvas();
            // Ensure this is happening on the EDT if called outside of SwingUtilities.invokeLater blocks,
            // though add() itself should be EDT-safe if frame is not yet visible.
            // For simplicity here, assuming direct call is okay as start() and loadSvg() use invokeLater.
            add(this.svgCanvas, BorderLayout.CENTER);
            // Revalidate/repaint might be needed if frame was already visible, but here it's pre-visibility.
            // if (isShowing()) { // Or check visibility
            //     revalidate();
            //     repaint();
            // }

            // Adicionar listeners de mouse
            MouseAdapter mouseListener = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (toolManager.getActiveTool() == ActiveTool.DRAW_LINE) {
                        if (lineStartPoint == null) {
                            // Primeiro clique para a linha
                            removePreviewLine(); // Limpa pre-visualização anterior
                            lineStartPoint = new Point2D(e.getX(), e.getY());
                            System.out.println("Modo Desenhar Linha: Ponto inicial em " + lineStartPoint.x + ", " + lineStartPoint.y);
                        } else {
                            // Segundo clique para a linha
                            removePreviewLine(); // Limpa pre-visualização ao finalizar linha
                            Point2D endPoint = new Point2D(e.getX(), e.getY());
                            Line2D newLine = new Line2D(lineStartPoint, endPoint);
                            System.out.println("Modo Desenhar Linha: Ponto final em " + endPoint.x + ", " + endPoint.y + ". Linha criada: " + newLine.toString());
                            drawnLines.add(newLine);
                            redrawSVGCanvas(); // Atualiza o canvas com a nova linha
                            lineStartPoint = null; // Reset para a próxima linha
                        }
                    } else if (toolManager.getActiveTool() == ActiveTool.DRAW_CIRCLE) {
                        removePreviewLine();
                        removePreviewCircle(); // Limpar qualquer pré-visualização de círculo anterior
                        if (circleCenterPoint == null) {
                            circleCenterPoint = new Point2D(e.getX(), e.getY());
                            System.out.println("Modo Desenhar Círculo: Centro em " + circleCenterPoint.x + ", " + circleCenterPoint.y);
                            // Não há pré-visualização ainda, só o centro foi definido.
                        } else {
                            Point2D pointOnCircumference = new Point2D(e.getX(), e.getY());
                            double radius = Math.sqrt(Math.pow(pointOnCircumference.x - circleCenterPoint.x, 2) + Math.pow(pointOnCircumference.y - circleCenterPoint.y, 2));
                            if (radius > 0) { // Evitar círculos de raio zero se o clique for no mesmo ponto
                                Circle2D newCircle = new Circle2D(circleCenterPoint, radius);
                                drawnCircles.add(newCircle);
                                System.out.println("Modo Desenhar Círculo: Raio " + radius + ". Círculo criado: " + newCircle.toString());
                                redrawSVGCanvas(); // Atualizar canvas para mostrar o círculo finalizado
                            }
                            circleCenterPoint = null; // Reset para o próximo círculo
                            removePreviewCircle(); // Limpar a pré-visualização final
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
                        AffineTransform at = svgCanvas.getRenderingTransform();
                        double scaleFactor = 1.25;
                        at.preConcatenate(AffineTransform.getTranslateInstance(-e.getX(), -e.getY()));
                        at.preConcatenate(AffineTransform.getScaleInstance(scaleFactor, scaleFactor));
                        at.preConcatenate(AffineTransform.getTranslateInstance(e.getX(), e.getY()));
                        svgCanvas.setRenderingTransform(at);
                        System.out.println("Zoom In no ponto: " + e.getX() + ", " + e.getY());
                    } else if (toolManager.getActiveTool() == ActiveTool.ZOOM_OUT) {
                        if (svgCanvas == null) return;
                        AffineTransform at = svgCanvas.getRenderingTransform();
                        double scaleFactor = 0.8;
                        at.preConcatenate(AffineTransform.getTranslateInstance(-e.getX(), -e.getY()));
                        at.preConcatenate(AffineTransform.getScaleInstance(scaleFactor, scaleFactor));
                        at.preConcatenate(AffineTransform.getTranslateInstance(e.getX(), e.getY()));
                        svgCanvas.setRenderingTransform(at);
                        System.out.println("Zoom Out no ponto: " + e.getX() + ", " + e.getY());
                    } else if (toolManager.getActiveTool() == ActiveTool.PAN) {
                        if (svgCanvas == null) return;
                        panLastMousePosition = new Point2D(e.getX(), e.getY());
                        System.out.println("Pan iniciado em: " + panLastMousePosition.x + ", " + panLastMousePosition.y);
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
                        removePreviewCircle(); // Garante que a pré-visualização de círculo não apareça
                        SVGDocument svgDocument = svgCanvas.getSVGDocument();
                        if (svgDocument == null) return;

                        if (previewLineSvgElement == null) {
                            DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
                            String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
                            previewLineSvgElement = (SVGLineElement) impl.createSVGElement(svgNS, "line");
                            previewLineSvgElement.setAttributeNS(null, "x1", String.valueOf(lineStartPoint.x));
                            previewLineSvgElement.setAttributeNS(null, "y1", String.valueOf(lineStartPoint.y));
                            previewLineSvgElement.setAttributeNS(null, "stroke", "gray"); // Cor da pré-visualização
                            previewLineSvgElement.setAttributeNS(null, "stroke-width", "1"); // Espessura da pré-visualização
                            previewLineSvgElement.setAttributeNS(null, "stroke-dasharray", "5,5"); // Estilo tracejado
                            if (svgDocument.getRootElement() != null) {
                                svgDocument.getRootElement().appendChild(previewLineSvgElement);
                            } else {
                                previewLineSvgElement = null; // Reset para tentar recriar
                                return;
                            }
                        }
                        previewLineSvgElement.setAttributeNS(null, "x2", String.valueOf(e.getX()));
                        previewLineSvgElement.setAttributeNS(null, "y2", String.valueOf(e.getY()));
                    } else if (toolManager.getActiveTool() == ActiveTool.DRAW_CIRCLE && circleCenterPoint != null) {
                        removePreviewLine(); // Garante que a pré-visualização de linha não apareça
                        SVGDocument svgDocument = svgCanvas.getSVGDocument();
                        if (svgDocument == null) return; // Não pode desenhar sem documento

                        double currentRadius = Math.sqrt(Math.pow(e.getX() - circleCenterPoint.x, 2) + Math.pow(e.getY() - circleCenterPoint.y, 2));

                        if (previewCircleSvgElement == null) {
                            DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
                            String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
                            previewCircleSvgElement = (SVGCircleElement) impl.createSVGElement(svgNS, "circle");
                            previewCircleSvgElement.setAttributeNS(null, "cx", String.valueOf(circleCenterPoint.x));
                            previewCircleSvgElement.setAttributeNS(null, "cy", String.valueOf(circleCenterPoint.y));
                            previewCircleSvgElement.setAttributeNS(null, "stroke", "gray");
                            previewCircleSvgElement.setAttributeNS(null, "stroke-width", "1");
                            previewCircleSvgElement.setAttributeNS(null, "stroke-dasharray", "5,5");
                            previewCircleSvgElement.setAttributeNS(null, "fill", "none");
                            if (svgDocument.getRootElement() != null) {
                                svgDocument.getRootElement().appendChild(previewCircleSvgElement);
                            } else {
                                // Se não há root element, a pré-visualização não pode ser adicionada.
                                previewCircleSvgElement = null; // Reseta para tentar recriar no próximo evento
                                return;
                            }
                        }
                        previewCircleSvgElement.setAttributeNS(null, "r", String.valueOf(currentRadius));
                    } else if (toolManager.getActiveTool() == ActiveTool.PAN && panLastMousePosition != null) {
                        if (svgCanvas == null) return;
                        removePreviewLine();
                        removePreviewCircle();

                        double dx = e.getX() - panLastMousePosition.x;
                        double dy = e.getY() - panLastMousePosition.y;

                        AffineTransform at = svgCanvas.getRenderingTransform();
                        AffineTransform translation = AffineTransform.getTranslateInstance(dx, dy);
                        at.preConcatenate(translation);
                        svgCanvas.setRenderingTransform(at);

                        panLastMousePosition = new Point2D(e.getX(), e.getY());
                    } else {
                        // Se nenhuma ferramenta de desenho/pan específica estiver manipulando o drag, remove as pré-visualizações.
                        removePreviewLine();
                        removePreviewCircle();
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
        if (previewLineSvgElement != null) {
            // Ensure removal happens on the Batik update thread if necessary,
            // though direct DOM manipulation should be fine if JSVGCanvas handles it.
            // For safety, one might wrap this in svgCanvas.getUpdateManager().getUpdateRunnableQueue().invokeLater(...);
            // However, given its usage, direct removal is often acceptable.
            if (previewLineSvgElement.getParentNode() != null) {
                previewLineSvgElement.getParentNode().removeChild(previewLineSvgElement);
            }
            previewLineSvgElement = null;
        }
    }

    private void redrawSVGCanvas() {
        StringBuilder svgBuilder = new StringBuilder();
        svgBuilder.append("<svg width=\"100%\" height=\"100%\" xmlns=\"http://www.w3.org/2000/svg\">");

        // TODO: Add logic here to render existing DXF content if loaded.
        // This might involve serializing svgCanvas.getSVGDocument() or parts of it.

        for (Line2D line : drawnLines) {
            String lineStrokeColor = "black";
            String lineStrokeWidth = "2";
            if (line == selectedEntity) { // Comparação de referência de objeto
                lineStrokeColor = "red";
                lineStrokeWidth = "3";
            }
            svgBuilder.append("<line x1=\"").append(line.start.x) // ou line.getStart().x
                      .append("\" y1=\"").append(line.start.y) // ou line.getStart().y
                      .append("\" x2=\"").append(line.end.x)   // ou line.getEnd().x
                      .append("\" y2=\"").append(line.end.y)   // ou line.getEnd().y
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

        svgBuilder.append("</svg>");
        loadSvg(svgBuilder.toString());
    }

    private void removePreviewCircle() {
        if (previewCircleSvgElement != null) {
            if (previewCircleSvgElement.getParentNode() != null) {
                previewCircleSvgElement.getParentNode().removeChild(previewCircleSvgElement);
            }
            previewCircleSvgElement = null;
        }
    }

    private boolean isPointNearLine(Point2D p, Line2D line, double tolerance) {
        Point2D p1 = line.start; // Acesso direto
        Point2D p2 = line.end;   // Acesso direto

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
        Point2D center = circle.getCenter(); // Assumindo getCenter()
        double radius = circle.getRadius();  // Assumindo getRadius()
        double distToCenter = Math.sqrt(Math.pow(point.x - center.x, 2) + Math.pow(point.y - center.y, 2));
        return Math.abs(distToCenter - radius) <= tolerance;
    }
}
