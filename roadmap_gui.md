# Roadmap e Status do Módulo de Interface Gráfica (gui)

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

Este documento detalha o status atual de implementação das funcionalidades planejadas para o módulo `gui` e serve como um roadmap para seus desenvolvimentos futuros.

## Funcionalidades Implementadas e Planejadas
- `[X]` Criação do Módulo `gui`: Concluído.
- `[X]` Janela Principal da Aplicação (`MainFrame.java`): Implementada (JFrame básico).
- `[X]` Menu Básico: Implementado ("Arquivo" > "Sair", "Abrir DXF...").
- `[X]` Placeholder para Área de Visualização CAD: Adicionado (`CustomCadPanel.java`). Renderização atual via `java.awt.Graphics2D` para entidades DXF (via `DxfRenderService`) e entidades desenhadas.
- `[X]` Integração da Lógica de Renderização: Concluído (módulo `rendering` fornece `DxfRenderService` que é usado pelo `CustomCadPanel` da GUI).
- `[X]` **Interação com o Mouse para Desenho/Seleção:**
  - *Detalhe:* Implementar listeners de mouse na área de visualização para desenho de novas entidades, seleção de entidades existentes, etc.
    - *Status: Listeners básicos (mousePressed, mouseDragged, mouseClicked, mouseReleased) adicionados e funcionais. Lógica de estado de ferramenta (`ToolManager`, `ActiveTool`) implementada. Captura de pontos, pré-visualização dinâmica e renderização final para `Line2D` e `Circle2D` implementadas. Detecção de hit para seleção de `Line2D` e `Circle2D` (desenhadas e de DXF) funcional. Feedback visual elaborado (cursores dinâmicos, destaque de seleção) implementado.*
    - **Próximos Passos (Melhorias):**
      - `[ ] (P1)` **Integrar renderização de entidades desenhadas com conteúdo DXF:**
        - *Detalhe Adicional:* Garantir que as transformações de visualização (zoom/pan) sejam aplicadas consistentemente a ambos os tipos de conteúdo (entidades DXF carregadas e entidades desenhadas pelo usuário). As novas entidades devem ser adicionadas a uma estrutura de dados (ex: uma lista no `CustomCadPanel` ou `DxfDocument`) que é iterada durante o redesenho do painel. O `DxfRenderService` pode precisar ser estendido ou complementado para desenhar entidades da geometria local.
      - `[ ] (P3)` **Implementar destaque de pré-seleção (hover) para entidades:**
        - *Detalhe:* Ao passar o mouse sobre uma entidade, ela deve ser destacada visualmente para indicar que é selecionável. Requer detecção eficiente de "mouse over".
      - `[ ] (P2)` **Suporte para seleção múltipla de entidades:**
        - *Detalhe:* Permitir que o usuário selecione várias entidades (ex: usando Shift+clique, ou desenhando uma janela de seleção). O painel de propriedades e outras ações devem então operar sobre todas as entidades selecionadas.
      - `[ ] (P2)` **Desenho de outras entidades (Arco, Polilinha, etc.):**
        - *Detalhe:* Adicionar ferramentas e lógica de interação para desenhar outras primitivas geométricas.
        - *Nota:* Esta tarefa tem forte dependência da implementação das respectivas ferramentas de desenho no `ToolManager` e do suporte das entidades no módulo `com.cad.modules.geometry.entities`.
- `[X]` **Barra de Ferramentas com Ações CAD:**
  - *Detalhe:* Adicionar uma `JToolBar` ao `MainFrame` com botões para as ações CAD mais comuns.
    - *Status: JToolBar adicionada. `JToggleButtons` com `ButtonGroup` para feedback visual da ferramenta ativa. ActionListeners implementados para botões definirem a ferramenta ativa no `ToolManager`. Funcionalidades de Zoom In/Out (por clique) e Pan (por arrastar) implementadas e conectadas.*
    - **Próximos Passos (Melhorias):**
      - `[ ] (P1)` **Implementar zoom pela roda do mouse:**
        - *Detalhe:* Permitir zoom in/out usando a roda do mouse, idealmente centrado na posição do cursor.
      - `[ ] (P1)` **Implementar "Zoom to Extents/Fit":**
        - *Detalhe:* Adicionar um botão/ação que ajuste o zoom e pan para mostrar todo o conteúdo do desenho na área de visualização. Requer calcular os limites (`Bounds`) de todas as entidades visíveis.
      - `[ ] (P3)` Adicionar mais ações/ferramentas à barra conforme necessário (ex: Salvar, Desfazer/Refazer, ferramentas de modificação).
- `[ ] (P1)` **Painel de Propriedades de Objetos:**
  - *Detalhe:* Criar um painel (ex: `JPanel` em um `JSplitPane` ou `JDialog`) que exiba as propriedades da(s) entidade(s) selecionada(s) (ex: coordenadas, raio, cor, camada, tipo de linha) e permita sua edição.
  - *Detalhe Adicional:* Requer comunicação bidirecional: seleção na GUI atualiza o painel; edição no painel atualiza a entidade e a visualização. Considerar quais propriedades serão editáveis inicialmente (ex: cor, camada). A atualização da entidade deve disparar um redesenho do `CustomCadPanel`.
- `[ ] (P1)` **Gerenciamento de Camadas (Layers):**
  - *Detalhe:* Desenvolver um painel ou diálogo para listar as camadas presentes no desenho (obtidas do `DxfDocument`), permitindo ao usuário criar novas camadas, renomeá-las, definir sua cor, visibilidade, tipo de linha e a camada ativa para desenho.
  - *Detalhe Adicional:* A interface deve permitir alterar a camada de objetos selecionados. A camada ativa influencia as propriedades de novas entidades desenhadas. Mudanças na visibilidade da camada devem causar um redesenho.
- `[~] (P1)` **Refatorar MainFrame para Testabilidade Headless e Melhorar Cobertura de Testes da GUI**
  - *Detalhe:* Modificar `MainFrame.java` para separar sua lógica central (gerenciamento de estado de ferramentas, cálculos de zoom/pan, manipulação do `DxfDocument` ou modelo de dados da GUI) de suas responsabilidades de UI Swing (`JFrame`, `JToolBar`, etc.), movendo a lógica para classes não-UI (ex: `CadController`, `ViewStateManager`). Isso permitirá que a lógica seja testada unitariamente sem `HeadlessException`. Testes unitários para `ToolManager` foram implementados e passam. A estrutura de teste para a lógica de `MainFrame` foi criada, mas os testes estão desabilitados/falhando devido a `HeadlessException` e aguardam esta refatoração.
- `[ ] (P3)` **Sistema de Desfazer/Refazer (Undo/Redo):**
  - *Detalhe:* Implementar um mecanismo para registrar ações do usuário (criação, exclusão, modificação de entidades) e permitir desfazer e refazer essas ações. Pode usar o padrão Command.
- `[ ] (P3)` **Integrar Ciclo de Vida do Módulo GUI com Kernel via ModuleInterface**
  - *Detalhe:* Utilizar a `ModuleInterface` implementada por `com.cad.gui.MainFrame` para que o `Kernel` (ou `ModuleManager`) possa gerenciar o ciclo de vida do módulo `gui` (chamando `init`, `start`, `stop`, `destroy`).

## Manutenção e Melhorias Recentes (Relevantes para GUI)
- `[X]` **Migração da Biblioteca SVG de SVG Salamander para Apache Batik (e posteriormente para Renderização Direta com Graphics2D):**
  - *Detalhe Histórico:* Inicialmente, a GUI usava SVG Salamander, depois migrou para Apache Batik para renderização de SVG. Posteriormente, a renderização de DXF foi alterada para usar `java.awt.Graphics2D` diretamente através do `DxfRenderService` e `CustomCadPanel` para melhor performance e controle, eliminando a dependência de bibliotecas SVG para a visualização principal do DXF. Entidades desenhadas pelo usuário também usam `Graphics2D`.
- `[X]` **Seleção de Entidades DXF Importadas e Desenhadas:**
  - *Detalhe:* Implementada a capacidade de selecionar entidades geométricas (LINE, CIRCLE) que foram importadas de arquivos DXF ou desenhadas pelo usuário. A seleção utiliza clique do mouse e uma tolerância, e as entidades selecionadas são destacadas visualmente.
- `[X]` Integração com a área de visualização da GUI (usando `dxflib` para carregar DXF e `DxfRenderService` para renderizar via `Graphics2D`).
  - *Nota: Este item foi adaptado para refletir a mudança de SVG para renderização direta com Graphics2D.*
- `[X]` **Correções Diversas e Melhorias de Código Iniciais:**
  - *Detalhe:* Corrigidas referências de `Point2D.Double` para `java.awt.geom.Point2D.Double` em `CustomCadPanel.java`. Adicionado método `distanceTo(Point2D other)` em `Point2D.java` (dxflib).
