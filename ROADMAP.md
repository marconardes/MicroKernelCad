# Roadmap e Status do Projeto CAD Modular

**Legenda de Status:**
- `[X]` Implementado
- `[~]` Parcialmente Implementado ou Versão Básica Existente
- `[ ]` Não Implementado

**Legenda de Prioridade (para itens não implementados `[ ]` ou `[~]`):**
- `(P1)` Alta Prioridade
- `(P2)` Média Prioridade
- `(P3)` Baixa Prioridade
---

Este documento detalha o status atual de implementação das funcionalidades planejadas para a ferramenta CAD modular e serve como um roadmap para desenvolvimentos futuros.

## Checklist do Projeto e Status Atual

**Arquitetura e Funcionalidades Centrais:**
- [X] Estrutura Modular com Microkernel (`core`): Implementado e funcional.
- [X] Gerenciamento de Módulos (`ModuleManager` no `core`): Implementado.
- [X] Sistema de Eventos (`EventBus` no `core`): Estrutura presente (uso específico a ser detalhado).
- `[ ] (P2)` **Serviço de Logging (`LoggerService` no `core`):** Estrutura presente, utilização efetiva a ser verificada.
    - *Detalhe:* Integrar um framework de logging (ex: SLF4J + Logback) e disseminar o uso de logs significativos em todos os módulos para facilitar a depuração e monitoramento.
- `[ ] (P2)` **Serviço de Configuração (`ConfigService` no `core`):** Estrutura presente, utilização efetiva a ser verificada.
    - *Detalhe:* Implementar a capacidade de carregar/salvar configurações da aplicação (ex: de um arquivo `.properties` ou JSON) e fornecer acesso a essas configurações para outros módulos.

**Módulos de Funcionalidade:**
*Módulo de Geometria (`geometry`):*
- `[ ] (P1)` **Implementação de operações geométricas básicas (criação de linhas, círculos, etc.):**
    - *Detalhe:* Definir e implementar classes para representar entidades geométricas 2D básicas (Ponto, Linha, Círculo, Arco, Polilinha). Incluir métodos para criação e manipulação inicial dessas entidades. Essencial para qualquer funcionalidade de desenho.
- `[ ] (P2)` **Implementação de operações geométricas avançadas (booleanas, etc.):**
    - *Detalhe:* Desenvolver algoritmos para operações booleanas (união, interseção, diferença) entre formas 2D, cálculo de offset, filetes e chanfros.
*Módulo de Renderização (`rendering`):*
- `[ ] (P2)` **Capacidade de renderizar modelos 2D (foco inicial em formato DXF):**
    - *Detalhe:* Além da conversão para SVG, explorar a renderização direta de entidades geométricas do `Módulo de Geometria` na GUI. Isso pode envolver a criação de um motor de renderização 2D customizado ou a adaptação de bibliotecas existentes para desenhar em um `java.awt.Graphics2D` (ou similar) dentro do `JSVGCanvas` ou de um painel dedicado. Permitiria maior controle sobre zoom, pan, e seleção visual.
- `[ ] (P3)` **Capacidade de renderizar modelos 3D.**
- [X] Integração com a área de visualização da GUI (usando `dxflib` para carregar DXF e renderizar via SVG).
*Módulo de Física (`physics`):*
- `[ ] (P3)` **Implementação de simulações físicas.**
*Módulo de Exportação (`export`):*
- [X] Submódulo para exportação PDF (`modules/export/pdf`): Estrutura presente.
- [X] Submódulo para exportação STL (`modules/export/stl`): Estrutura presente.
- `[ ] (P2)` **Funcionalidade de exportação efetivamente implementada em cada submódulo:**
    - *Detalhe:* Implementar a lógica para converter o `DxfDocument` ou as entidades do `Módulo de Geometria` para os formatos PDF e STL e permitir que o usuário salve o resultado em arquivo.
*Sistema de Plugins (`plugins`):*
- [X] Estrutura para carregamento de plugins: Presente (`custom_plugin` como exemplo).
- `[ ] (P3)` **Documentação/API clara para desenvolvimento de plugins.**

**Interface Gráfica do Usuário (`gui`):**
- [X] Criação do Módulo `gui`: Concluído.
- [X] Janela Principal da Aplicação (`MainFrame.java`): Implementada (JFrame básico).
- [X] Menu Básico: Implementado ("Arquivo" > "Sair", "Abrir DXF...").
- [X] Placeholder para Área de Visualização CAD: Adicionado e funcional com renderização SVG.
- [X] Integração da Lógica de Renderização: Concluído (módulo `rendering` conectado ao placeholder da GUI para exibição de DXF como SVG).
- `[ ] (P1)` **Interação com o Mouse para Desenho/Seleção:**
    - *Detalhe:* Implementar listeners de mouse na área de visualização para:
- `[ ] (P1)` **Barra de Ferramentas com Ações CAD:**
    - *Detalhe:* Adicionar uma `JToolBar` ao `MainFrame` com botões para as ações CAD mais comuns (ex: Nova Linha, Novo Círculo, Selecionar, Zoom, Pan). As ações de desenho dependerão da "Interação com o Mouse".
- `[ ] (P2)` **Painel de Propriedades de Objetos:**
    - *Detalhe:* Criar um painel (ex: `JPanel` em um `JSplitPane` com a área de visualização) que exiba as propriedades da(s) entidade(s) selecionada(s) (ex: coordenadas, raio, cor, camada) e permita sua edição.
- `[ ] (P2)` **Gerenciamento de Camadas (Layers):**
    - *Detalhe:* Desenvolver um painel ou diálogo para listar as camadas presentes no desenho, permitindo ao usuário criar novas camadas, renomeá-las, definir sua cor, visibilidade, e a camada ativa para desenho.

## Módulo Leitor DXF (dxflib)
- [X] Definição do Escopo Inicial (Entidades DXF, Versão ASCII, AutoCAD 2000/2004, Layers/Cores)
- [X] Análise de Referência (Kabeja - conceitual)
- [X] Criação da Estrutura do Módulo `dxflib` (POM, pacotes)
- [X] Implementação das Estruturas de Dados Base (DxfDocument, DxfEntity, Point2D/3D, etc.)
- [X] Implementação do Parser para Entidades DXF Iniciais (LINE, CIRCLE, ARC, LWPOLYLINE, TEXT, INSERT)
- [X] Adição de Testes Unitários para Entidades Iniciais
- [X] Implementação da Leitura de Tabelas (LAYER) e Seção de Blocos (BLOCK)
- [X] Adição de Testes Unitários para Layers e Blocos
- [X] Implementação do Conversor DXF para SVG (para entidades parseadas)
- [X] Adição de Testes Unitários para o Conversor SVG

## Melhorias Futuras Planejadas
- `[ ] (P3)` **Suporte para leitura e renderização de arquivos DWG (investigar bibliotecas e complexidade).**
=======
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
    - `[~]` Adição de Testes Unitários para o Conversor SVG

*   **Módulo de Renderização (`rendering`)** (Importância Geral: **Alta**)
    - [ ] Capacidade de renderizar modelos 2D (foco inicial em formato DXF). (Importância: **Alta**)
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

**Interface Gráfica do Usuário (`gui`):**
- [X] Criação do Módulo `gui`: Concluído.
- [X] Separação do Módulo `launcher`: Ponto de entrada da aplicação movido para o módulo 'launcher'.
- [X] Janela Principal da Aplicação (`MainFrame.java`): Implementada (JFrame básico, implementa ModuleInterface).
- [X] Menu Básico: Implementado ("Arquivo" > "Sair", "Abrir DXF...").
- [X] Placeholder para Área de Visualização CAD: Adicionado e funcional com renderização SVG.
- [X] Integração da Lógica de Renderização: Concluído (módulo `rendering` conectado ao placeholder da GUI para exibição de DXF como SVG).
- `[ ] (P1)` **Interação com o Mouse para Desenho/Seleção:**
    - *Detalhe:* Implementar listeners de mouse na área de visualização para: (desenho de novas entidades, seleção de entidades existentes, etc.)
- `[ ] (P1)` **Barra de Ferramentas com Ações CAD:**
    - *Detalhe:* Adicionar uma `JToolBar` ao `MainFrame` com botões para as ações CAD mais comuns (ex: Nova Linha, Novo Círculo, Selecionar, Zoom, Pan). As ações de desenho dependerão da "Interação com o Mouse".
- `[ ] (P2)` **Painel de Propriedades de Objetos:**
    - *Detalhe:* Criar um painel (ex: `JPanel` em um `JSplitPane` com a área de visualização) que exiba as propriedades da(s) entidade(s) selecionada(s) (ex: coordenadas, raio, cor, camada) e permita sua edição.
- `[ ] (P2)` **Gerenciamento de Camadas (Layers):**
    - *Detalhe:* Desenvolver um painel ou diálogo para listar as camadas presentes no desenho, permitindo ao usuário criar novas camadas, renomeá-las, definir sua cor, visibilidade, e a camada ativa para desenho.
- `[ ] (P2) Refatorar MainFrame para Testabilidade Headless`
    - *Detalhe:* Modificar `MainFrame.java` para permitir que seja instanciado e seus métodos de ciclo de vida básicos sejam testados em um ambiente headless sem lançar `HeadlessException`. Isso pode envolver a separação da lógica de UI da instanciação de componentes, permitindo mocks, ou outras técnicas para isolar a lógica da UI da renderização gráfica real durante os testes de unidade.
- `[ ] (P2) Integrar Ciclo de Vida do Módulo GUI com Kernel via ModuleInterface`
    - *Detalhe:* Utilizar a `ModuleInterface` implementada por `com.cad.gui.MainFrame` para que o `Kernel` (ou `ModuleManager`) possa gerenciar o ciclo de vida do módulo `gui` (chamando `init`, `start`, `stop`, `destroy`).

**Melhorias Futuras Planejadas:**
- `[ ]` Suporte para leitura e renderização de arquivos DWG (investigar bibliotecas e complexidade). (Importância: **Média/Alta** - mas após DXF)
- `[ ]` Outras Entidades DXF (HATCH, DIMENSION, SPLINE, ELLIPSE, etc.) - adicionar ao `dxflib`. (Importância: **Baixa/Média** - dependendo da entidade)

---
