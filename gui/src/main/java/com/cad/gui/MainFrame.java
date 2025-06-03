package com.cad.gui;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar; // Added for JToolBar
import javax.swing.JButton;  // Added for JButton
import javax.swing.JFileChooser;
import javax.swing.JOptionPane; // Para mostrar erros
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.batik.swing.JSVGCanvas;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException; // Para exceções do serviço
import java.awt.event.MouseAdapter; // Added for mouse events
import java.awt.event.MouseEvent;   // Added for mouse events
import java.awt.event.MouseMotionListener; // Added for mouse motion events

// Nova importação para o serviço de renderização
import com.cad.modules.rendering.DxfRenderService;
import com.cad.dxflib.parser.DxfParserException; // Para exceção do serviço
import com.cad.core.api.ModuleInterface; // Added import
import com.cad.gui.tool.ActiveTool; // Added for ActiveTool
import com.cad.gui.tool.ToolManager; // Added for ToolManager
import com.cad.dxflib.common.Point2D; // Added for Point2D
import com.cad.modules.geometry.entities.Line2D; // Added for Line2D

public class MainFrame extends JFrame implements ModuleInterface {

    private JSVGCanvas svgCanvas;
    private DxfRenderService dxfRenderService; // Novo campo
    private ToolManager toolManager; // Added ToolManager field
    private Point2D lineStartPoint = null; // Added for line drawing state

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

        JButton lineButton = new JButton("Linha");
        lineButton.setToolTipText("Desenhar Linha");
        lineButton.addActionListener(e -> toolManager.setActiveTool(ActiveTool.DRAW_LINE));
        toolBar.add(lineButton);

        JButton circleButton = new JButton("Círculo");
        circleButton.setToolTipText("Desenhar Círculo");
        circleButton.addActionListener(e -> toolManager.setActiveTool(ActiveTool.DRAW_CIRCLE));
        toolBar.add(circleButton);

        toolBar.addSeparator(); // Adiciona um separador visual

        JButton selectButton = new JButton("Selecionar");
        selectButton.setToolTipText("Selecionar Entidade");
        selectButton.addActionListener(e -> toolManager.setActiveTool(ActiveTool.SELECT));
        toolBar.add(selectButton);

        toolBar.addSeparator();

        JButton zoomInButton = new JButton("Zoom In");
        zoomInButton.setToolTipText("Aumentar Zoom");
        zoomInButton.addActionListener(e -> toolManager.setActiveTool(ActiveTool.ZOOM_IN));
        toolBar.add(zoomInButton);

        JButton zoomOutButton = new JButton("Zoom Out");
        zoomOutButton.setToolTipText("Diminuir Zoom");
        zoomOutButton.addActionListener(e -> toolManager.setActiveTool(ActiveTool.ZOOM_OUT));
        toolBar.add(zoomOutButton);

        JButton panButton = new JButton("Pan");
        panButton.setToolTipText("Mover Visualização");
        panButton.addActionListener(e -> toolManager.setActiveTool(ActiveTool.PAN));
        toolBar.add(panButton);

        // Adiciona a toolbar ao JFrame. BorderLayout.PAGE_START é uma boa posição.
        add(toolBar, BorderLayout.PAGE_START);
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

            try (FileInputStream fis = new FileInputStream(fileToOpen)) {
                String svgContent = dxfRenderService.convertDxfToSvg(fis);
                loadSvg(svgContent);
            } catch (FileNotFoundException ex) {
                System.err.println("Arquivo não encontrado: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Arquivo não encontrado: " + fileToOpen.getAbsolutePath(), "Erro ao Abrir Arquivo", JOptionPane.ERROR_MESSAGE);
                loadSvg(null); // Limpa a visualização
            } catch (DxfParserException ex) {
                System.err.println("Erro ao parsear DXF: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Erro ao processar o arquivo DXF: " + ex.getMessage(), "Erro de DXF", JOptionPane.ERROR_MESSAGE);
                loadSvg(null); // Limpa a visualização
            } catch (IOException ex) {
                System.err.println("Erro de I/O: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Erro de leitura/escrita: " + ex.getMessage(), "Erro de I/O", JOptionPane.ERROR_MESSAGE);
                loadSvg(null); // Limpa a visualização
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
                            lineStartPoint = new Point2D(e.getX(), e.getY());
                            System.out.println("Modo Desenhar Linha: Ponto inicial em " + lineStartPoint.x + ", " + lineStartPoint.y);
                        } else {
                            // Segundo clique para a linha
                            Point2D endPoint = new Point2D(e.getX(), e.getY());
                            Line2D newLine = new Line2D(lineStartPoint, endPoint);
                            System.out.println("Modo Desenhar Linha: Ponto final em " + endPoint.x + ", " + endPoint.y + ". Linha criada: " + newLine.toString());
                            lineStartPoint = null; // Reset para a próxima linha
                        }
                    } else {
                        // Comportamento para outras ferramentas ou nenhuma ferramenta
                        System.out.println("Mouse Pressed (Ferramenta: " + toolManager.getActiveTool() + ") at: " + e.getX() + ", " + e.getY());
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    System.out.println("Mouse Clicked at: " + e.getX() + ", " + e.getY());
                    // Futuramente: Finalizar uma seleção ou um ponto de um desenho
                }
            };

            MouseMotionListener motionListener = new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    System.out.println("Mouse Dragged to: " + e.getX() + ", " + e.getY());
                    // Futuramente: Atualizar a pré-visualização de uma forma sendo desenhada
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    // System.out.println("Mouse Moved to: " + e.getX() + ", " + e.getY());
                    // Comentado para não poluir muito o console, mas útil para depuração
                }
            };

            this.svgCanvas.addMouseListener(mouseListener);
            this.svgCanvas.addMouseMotionListener(motionListener);
        }
    }
}
