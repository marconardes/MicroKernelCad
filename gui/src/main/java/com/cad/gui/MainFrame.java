package com.cad.gui;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane; // Para mostrar erros
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.batik.swing.JSVGCanvas;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException; // Para exceções do serviço

// Nova importação para o serviço de renderização
import com.cad.modules.rendering.DxfRenderService;
import com.cad.dxflib.parser.DxfParserException; // Para exceção do serviço

public class MainFrame extends JFrame {

    private JSVGCanvas svgCanvas;
    private DxfRenderService dxfRenderService; // Novo campo

    public MainFrame() {
        setTitle("CAD Tool");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        svgCanvas = new JSVGCanvas();
        add(svgCanvas, BorderLayout.CENTER);

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
            if (svgContent == null || svgContent.trim().isEmpty()) {
                svgCanvas.setSVGDocument(null); // Limpa o canvas
                // Opcional: Carregar um SVG que diz "Erro" ou "Vazio"
                // String errorSvg = "<svg width='100%' height='100%'><text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' font-size='20' fill='red'>Erro ao carregar SVG ou arquivo vazio.</text></svg>";
                // try {
                //     String svgDataUri = "data:image/svg+xml;charset=UTF-8," + java.net.URLEncoder.encode(errorSvg, "UTF-8").replace("+", "%20");
                //     svgCanvas.setURI(svgDataUri);
                // } catch (java.io.UnsupportedEncodingException e) { /* ignore */ }
                return;
            }
            try {
                // Usando setURI para contornar problemas anteriores com SAXSVGDocumentFactory
                // O conteúdo SVG precisa ser devidamente codificado para um data URI
                String svgDataUri = "data:image/svg+xml;charset=UTF-8," + java.net.URLEncoder.encode(svgContent, "UTF-8").replace("+", "%20");
                svgCanvas.setURI(svgDataUri);

            } catch (java.io.UnsupportedEncodingException e) {
                e.printStackTrace(); // Deveria tratar isso de forma mais elegante
                svgCanvas.setSVGDocument(null); // Limpa em caso de erro de codificação
            } catch (Exception e) { // Catch other potential Batik exceptions from setURI
                 e.printStackTrace();
                 svgCanvas.setSVGDocument(null); // Limpa em caso de erro
            }
            // Removida a referência explícita à SAXSVGDocumentFactory por enquanto
            // devido a problemas de compilação anteriores na sub-tarefa.
            // A abordagem setURI com data URI é mais robusta neste momento.
        });
    }

}
