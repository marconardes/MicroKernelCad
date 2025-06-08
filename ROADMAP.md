# Roadmap e Status do Projeto CAD Modular

**Legenda de Status:**
- `[X]` Implementado
- `[~]` Parcialmente Implementado ou Versão Básica Existente
- `[ ]` Não Implementado

**Legenda de Prioridade (para itens não implementados `[ ]` ou `[~]`):**
- `(P1)` Mais Crítico Agora
- `(P2)` Alta Prioridade
- `(P3)` Média Prioridade
- `(P4)` Baixa Prioridade
- `(P5)` Muito Baixa Prioridade / Considerações Futuras

---

Este documento detalha o status atual de implementação das funcionalidades planejadas para a ferramenta CAD modular e serve como um roadmap para desenvolvimentos futuros.

## Checklist do Projeto e Status Atual

### Arquitetura e Funcionalidades Centrais
- `[X]` Estrutura Modular com Microkernel (`core`): Implementado e funcional.
- `[X]` Gerenciamento de Módulos (`ModuleManager` no `core`): Implementado.
- `[X]` Sistema de Eventos (`EventBus` no `core`): Estrutura presente (uso específico a ser detalhado).
- `[ ] (P2)` **Serviço de Logging (`LoggerService` no `core`):** Estrutura presente, utilização efetiva a ser verificada.
  - *Detalhe:* Integrar um framework de logging (ex: SLF4J + Logback) e disseminar o uso de logs significativos em todos os módulos para facilitar a depuração e monitoramento.
- `[ ] (P2)` **Serviço de Configuração (`ConfigService` no `core`):** Estrutura presente, utilização efetiva a ser verificada.
  - *Detalhe:* Implementar a capacidade de carregar/salvar configurações da aplicação (ex: de um arquivo `.properties` ou JSON) e fornecer acesso a essas configurações para outros módulos.

### Módulos de Funcionalidade

#### Módulo de Geometria (`geometry`)
- `[X]` **Implementação de operações geométricas básicas (criação de linhas, círculos, etc.):**
  - *Detalhe:* Definir e implementar classes para representar entidades geométricas 2D básicas (Ponto reutilizado do `dxflib`, Linha, Círculo (`Circle2D.java` adicionado), Arco, Polilinha implementados em `com.cad.modules.geometry.entities`). Incluir métodos para criação e manipulação inicial dessas entidades. Testes unitários básicos para getters e construtores foram adicionados. Essencial para qualquer funcionalidade de desenho.
- `[ ] (P3)` **Implementação de operações geométricas avançadas (booleanas, etc.):**
  - *Detalhe:* Desenvolver algoritmos para operações booleanas (união, interseção, diferença) entre formas 2D, cálculo de offset, filetes e chanfros.

#### Módulo de Renderização (`rendering`)
- `[~] (P2)` **Capacidade de renderizar modelos 2D (foco inicial em formato DXF):**
  - *Detalhe:* Além da conversão para SVG, explorar a renderização direta de entidades geométricas do `Módulo de Geometria` na GUI. Entidades desenhadas manualmente (`Line2D`, `Circle2D`) são renderizadas sobre o conteúdo DXF carregado no painel SVG. A integração para desenhar *sobre* o DXF é uma melhoria futura (ver GUI). Isso pode envolver a criação de um motor de renderização 2D customizado ou a adaptação de bibliotecas existentes para desenhar em um `java.awt.Graphics2D` (ou similar) dentro do `JSVGCanvas` ou de um painel dedicado. Permitiria maior controle sobre zoom, pan, e seleção visual.
- `[ ] (P5)` **Capacidade de renderizar modelos 3D.**
- `[X]` Integração com a área de visualização da GUI (usando `dxflib` para carregar DXF e renderizar via SVG).

#### Módulo de Física (`physics`)
- `[ ] (P5)` **Implementação de simulações físicas.**

#### Módulo de Exportação (`export`)
- `[X]` Submódulo para exportação PDF (`modules/export/pdf`): Estrutura presente.
- `[X]` Submódulo para exportação STL (`modules/export/stl`): Estrutura presente.
- `[ ] (P1)` **Funcionalidade de exportação efetivamente implementada em cada submódulo:**
  - *Detalhe:* Implementar a lógica para converter o `DxfDocument` ou as entidades do `Módulo de Geometria` para os formatos PDF e STL e permitir que o usuário salve o resultado em arquivo.

#### Sistema de Plugins (`plugins`)
- `[X]` Estrutura para carregamento de plugins: Presente (`custom_plugin` como exemplo).
- `[ ] (P5)` **Documentação/API clara para desenvolvimento de plugins.**

### Interface Gráfica do Usuário (`gui`)
- `[X]` Criação do Módulo `gui`: Concluído.
- `[X]` Janela Principal da Aplicação (`MainFrame.java`): Implementada (JFrame básico).
- `[X]` Menu Básico: Implementado ("Arquivo" > "Sair", "Abrir DXF...").
- `[X]` Placeholder para Área de Visualização CAD: Adicionado e funcional com renderização SVG.
- `[X]` Integração da Lógica de Renderização: Concluído (módulo `rendering` conectado ao placeholder da GUI para exibição de DXF como SVG).
- `[X]` **Interação com o Mouse para Desenho/Seleção:**
  - *Detalhe:* Implementar listeners de mouse na área de visualização para: (desenho de novas entidades, seleção de entidades existentes, etc.).
    - *Status: Listeners básicos (mousePressed, mouseDragged, mouseClicked, mouseReleased) adicionados e funcionais. Lógica de estado de ferramenta (`ToolManager`, `ActiveTool`) implementada. Captura de pontos, pré-visualização dinâmica e renderização final para `Line2D` e `Circle2D` implementadas. Detecção de hit para seleção de `Line2D` e `Circle2D` funcional. Feedback visual elaborado (cursores dinâmicos, destaque de seleção) implementado.*
    - **Próximos Passos (Melhorias):**
      - `[ ] (P1)` Integrar renderização de entidades desenhadas com conteúdo DXF (desenhar sobre, não substituir).
      - `[ ] (P3)` Implementar destaque de pré-seleção (hover) para entidades.
      - `[ ] (P2)` Suporte para seleção múltipla de entidades.
      - `[ ] (P2)` Desenho de outras entidades (Arco, Polilinha, etc.).
- `[X]` **Barra de Ferramentas com Ações CAD:**
  - *Detalhe:* Adicionar uma `JToolBar` ao `MainFrame` com botões para as ações CAD mais comuns (ex: Nova Linha, Novo Círculo, Selecionar, Zoom, Pan). As ações de desenho dependerão da "Interação com o Mouse".
    - *Status: JToolBar adicionada. `JToggleButtons` com `ButtonGroup` para feedback visual da ferramenta ativa. ActionListeners implementados para botões definirem a ferramenta ativa no `ToolManager`. Funcionalidades de Zoom In/Out (por clique) e Pan (por arrastar) implementadas e conectadas.*
    - **Próximos Passos (Melhorias):**
      - `[ ] (P1)` Implementar zoom pela roda do mouse.
      - `[ ] (P1)` Implementar "Zoom to Extents/Fit".
      - `[ ] (P3)` Adicionar mais ações/ferramentas à barra conforme necessário.
- `[X]` **Refatorar GUI para remover dependência do Batik e usar SVG Salamander.**
  - *Detalhe: Substituído JSVGCanvas (Batik) por SVGPanel (SVG Salamander) para renderização de SVG. Funcionalidades de carregamento de SVG, zoom/pan centrado, e pré-visualização de desenho (linha, círculo) foram reimplementadas. A transformação e pré-visualização são feitas via regeneração da string SVG.*
- `[ ] (P1)` **Painel de Propriedades de Objetos:**
  - *Detalhe:* Criar um painel (ex: `JPanel` em um `JSplitPane` com a área de visualização) que exiba as propriedades da(s) entidade(s) selecionada(s) (ex: coordenadas, raio, cor, camada) e permita sua edição.
- `[ ] (P1)` **Gerenciamento de Camadas (Layers):**
  - *Detalhe:* Desenvolver um painel ou diálogo para listar as camadas presentes no desenho, permitindo ao usuário criar novas camadas, renomeá-las, definir sua cor, visibilidade, e a camada ativa para desenho.
- `[~] (P1)` **Refatorar MainFrame para Testabilidade Headless e Melhorar Cobertura de Testes da GUI**
  - *Detalhe:* Modificar `MainFrame.java` para separar sua lógica central (geração de SVG, gerenciamento de estado de ferramentas, cálculos de zoom/pan) de suas responsabilidades de UI Swing, movendo a lógica para uma classe não-UI. Isso permitirá que a lógica seja testada unitariamente sem `HeadlessException`. Testes unitários para `ToolManager` foram implementados e passam. A estrutura de teste para a lógica de `MainFrame` foi criada, mas os testes estão desabilitados/falhando devido a `HeadlessException` e aguardam esta refatoração.
- `[ ] (P3)` **Integrar Ciclo de Vida do Módulo GUI com Kernel via ModuleInterface**
  - *Detalhe:* Utilizar a `ModuleInterface` implementada por `com.cad.gui.MainFrame` para que o `Kernel` (ou `ModuleManager`) possa gerenciar o ciclo de vida do módulo `gui` (chamando `init`, `start`, `stop`, `destroy`).

## Módulo Leitor DXF (dxflib)
- `[X]` Definição do Escopo Inicial (Entidades DXF, Versão ASCII, AutoCAD 2000/2004, Layers/Cores)
- `[X]` Análise de Referência (Kabeja - conceitual)
- `[X]` Criação da Estrutura do Módulo `dxflib` (POM, pacotes)
- `[X]` Implementação das Estruturas de Dados Base (DxfDocument, DxfEntity, Point2D/3D, etc.)
- `[X]` Implementação do Parser para Entidades DXF Iniciais (LINE, CIRCLE, ARC, LWPOLYLINE, TEXT, INSERT)
- `[X]` Adição de Testes Unitários para Entidades Iniciais
- `[X]` Implementação da Leitura de Tabelas (LAYER) e Seção de Blocos (BLOCK)
- `[X]` Adição de Testes Unitários para Layers e Blocos
- `[X]` Implementação do Conversor DXF para SVG (para entidades parseadas)
- `[X]` Adição de Testes Unitários para o Conversor SVG

## Melhorias Futuras Planejadas
- `[ ] (P5)` **Suporte para leitura e renderização de arquivos DWG (investigar bibliotecas e complexidade).**
- `[ ] (P1)` **Suporte para Entidade DXF HATCH:**
  - *Detalhe:* Estender o `dxflib` para parsear e representar a entidade DXF HATCH.
- `[ ] (P1)` **Suporte para Entidade DXF DIMENSION:**
  - *Detalhe:* Estender o `dxflib` para parsear e representar a entidade DXF DIMENSION.
- `[ ] (P1)` **Suporte para Entidade DXF SPLINE:**
  - *Detalhe:* Estender o `dxflib` para parsear e representar a entidade DXF SPLINE.
- `[ ] (P1)` **Suporte para Entidade DXF ELLIPSE:**
  - *Detalhe:* Estender o `dxflib` para parsear e representar a entidade DXF ELLIPSE.
- `[ ] (P1)` **Suporte para Entidade DXF MTEXT:**
  - *Detalhe:* Estender o `dxflib` para parsear e representar a entidade DXF MTEXT.
- `[ ] (P1)` **Suporte para Entidade DXF POLYLINE (complexa):**
  - *Detalhe:* Estender o `dxflib` para parsear e representar a entidade DXF POLYLINE (entidade complexa com vértices).
- `[ ] (P1)` **Suporte para Entidade DXF POINT:**
  - *Detalhe:* Estender o `dxflib` para parsear e representar a entidade DXF POINT.
- `[ ] (P1)` **Suporte para Entidade DXF SOLID:**
  - *Detalhe:* Estender o `dxflib` para parsear e representar a entidade DXF SOLID, entre outras, conforme a necessidade.
