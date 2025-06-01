# Roadmap e Status do Projeto CAD Modular

**Legenda de Status:**
- `[X]` Implementado
- `[~]` Parcialmente Implementado ou Versão Básica Existente
- `[ ]` Não Implementado

**Grau de Importância (Prioridade):**
- **Alta:** Essencial para funcionalidade central ou próximos passos críticos.
- **Média:** Importante para usabilidade, cobertura de casos comuns ou funcionalidades robustas.
- **Baixa:** Melhorias, funcionalidades avançadas, casos raros ou de menor impacto imediato.

---

Este documento detalha o status atual de implementação das funcionalidades planejadas para a ferramenta CAD modular e serve como um roadmap para desenvolvimentos futuros.

## Checklist do Projeto e Status Atual

**Arquitetura e Funcionalidades Centrais:**
- [X] Estrutura Modular com Microkernel (`core`): Implementado e funcional. (Importância: **Alta**)
- [X] Gerenciamento de Módulos (`ModuleManager` no `core`): Implementado. (Importância: **Alta**)
- [X] Sistema de Eventos (`EventBus` no `core`): Estrutura presente. (Importância: **Média**)
- [ ] Serviço de Logging (`LoggerService` no `core`): Estrutura presente. (Importância: **Média**)
- [ ] Serviço de Configuração (`ConfigService` no `core`): Estrutura presente. (Importância: **Média**)

**Módulos de Funcionalidade:**

*   **Módulo Leitor DXF (`dxflib`)** (Importância Geral: **Alta**)
    - [X] Definição do Escopo Inicial (Entidades DXF, Versão ASCII, AutoCAD 2000/2004, Layers/Cores)
    - [X] Análise de Referência (Kabeja - conceitual)
    - [X] Criação da Estrutura do Módulo `dxflib` (POM, pacotes)
    - [X] Implementação das Estruturas de Dados Base (DxfDocument, DxfEntity, Point2D/3D, etc.)
    - [X] Implementação do Parser para Entidades DXF Iniciais (LINE, CIRCLE, ARC, LWPOLYLINE, TEXT, INSERT)
    - [X] Adição de Testes Unitários para Entidades Iniciais
    - [X] Implementação da Leitura de Seções (TABLES (LAYER, LTYPE), BLOCKS)
    - [X] Adição de Testes Unitários para Tabelas (LAYER, LTYPE) e Blocos
    - `[~]` Implementação do Conversor DXF para SVG (para entidades parseadas)
        - `[~]` Suporte básico para LINE, CIRCLE, ARC, LWPOLYLINE (bulges como linhas), TEXT, INSERT
        - `[X]` Suporte completo para Bulges em LWPOLYLINE (conversão para arcos SVG) (Importância: **Alta**)
        - `[ ]` Implementar alinhamento completo para TEXT (horizontal/vertical) (Importância: **Média**)
        - `[ ]` Implementar suporte para MTEXT (parsing básico e conversão SVG) (Importância: **Média**)
        - `[~]` Cores: Mapeamento ACI expandido, resolução BYLAYER/BYBLOCK básica (Importância: **Média**)
        - `[ ]` Cores: Suporte a True Color DXF (Importância: **Baixa**)
        - `[~]` Tipos de Linha: Suporte básico a `stroke-dasharray` via parsing LTYPE (Importância: **Média**)
        - `[ ]` Tipos de Linha: Suporte completo à escala (LTSCALE, CELTSCALE, etc.) (Importância: **Baixa**)
        - `[X]` Agrupamento opcional de elementos SVG por layer (Importância: **Média**)
    - `[~]` Adição de Testes Unitários para o Conversor SVG
        - `[X]` Testes para LINE, CIRCLE, estrutura SVG/viewBox
        - `[X]` Testes para ARC, LWPOLYLINE (com e sem bulges), TEXT, INSERT (transformações)
        - `[X]` Testes para cores ACI básicas e agrupamento por layer
        - `[X]` Testes para `stroke-dasharray` básico
        - `[ ]` Testes para alinhamento de TEXT
        - `[ ]` Testes para MTEXT
        - `[ ]` Testes para Cores Avançadas (True Color, mais ACI)
        - `[ ]` Testes para escala de Tipos de Linha

*   **Módulo de Renderização (`rendering`)** (Importância Geral: **Alta**)
    - [ ] Capacidade de renderizar modelos 2D (foco inicial em formato DXF). (Importância: **Alta**)
        - `[ ]` Integração com `dxflib` para carregar dados DXF.
        - `[ ]` Uso de biblioteca de renderização SVG (ex: Apache Batik) para exibir o SVG gerado pela `dxflib`.
    - [ ] Capacidade de renderizar modelos 3D. (Importância: **Baixa**)
    - `[ ]` Integração com a área de visualização da GUI. (Importância: **Alta**)

*   **Módulo de Geometria (`geometry`)** (Importância Geral: **Média**)
    - [ ] Implementação de operações geométricas básicas (criação de linhas, círculos, etc.). (Importância: **Média**)
    - [ ] Implementação de operações geométricas avançadas (booleanas, etc.). (Importância: **Baixa**)

*   **Módulo de Física (`physics`)** (Importância Geral: **Baixa**)
    - [ ] Implementação de simulações físicas.

*   **Módulo de Exportação (`export`)** (Importância Geral: **Média**)
    - [X] Submódulo para exportação PDF (`modules/export/pdf`): Estrutura presente.
    - [X] Submódulo para exportação STL (`modules/export/stl`): Estrutura presente.
    - [ ] Funcionalidade de exportação efetivamente implementada em cada submódulo. (Importância: **Média**)

*   **Sistema de Plugins (`plugins`)** (Importância Geral: **Baixa**)
    - [X] Estrutura para carregamento de plugins: Presente (`custom_plugin` como exemplo).
    - [ ] Documentação/API clara para desenvolvimento de plugins. (Importância: **Baixa**)

**Interface Gráfica do Usuário (`gui`):** (Importância Geral: **Alta**)
- [X] Criação do Módulo `gui`: Concluído.
- [X] Janela Principal da Aplicação (`MainFrame.java`): Implementada (JFrame básico).
- [X] Menu Básico: Implementado ("Arquivo" > "Sair").
- [X] Placeholder para Área de Visualização CAD: Adicionado (JPanel vazio).
- `[ ]` Integração da Lógica de Renderização (conectar `rendering` ao placeholder da GUI). (Importância: **Alta**)
- `[ ]` Barra de Ferramentas com Ações CAD (Abrir, Salvar, Zoom, Pan). (Importância: **Alta**)
- `[ ]` Painel de Propriedades de Objetos. (Importância: **Média**)
- `[ ]` Gerenciamento de Camadas (Layers) na GUI (toggle de visibilidade). (Importância: **Média**)
- `[ ]` Interação com o Mouse para Desenho/Seleção (foco inicial em visualização: Zoom/Pan). (Importância: **Alta**)

**Melhorias Futuras Planejadas:**
- `[ ]` Suporte para leitura e renderização de arquivos DWG (investigar bibliotecas e complexidade). (Importância: **Média/Alta** - mas após DXF)
- `[ ]` Outras Entidades DXF (HATCH, DIMENSION, SPLINE, ELLIPSE, etc.) - adicionar ao `dxflib`. (Importância: **Baixa/Média** - dependendo da entidade)

---
