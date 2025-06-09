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
- `[X] (P2)` **Capacidade de renderizar modelos 2D (foco inicial em formato DXF):**
  - *Detalhe:* Implementado um motor de renderização 2D customizado (`CustomCadPanel` usando `java.awt.Graphics2D`). Conteúdo DXF (parseado para `SVGUniverse` em memória) e entidades geométricas desenhadas manualmente (`Line2D`, `Circle2D`) são renderizados diretamente via `Graphics2D`. Esta abordagem fornece maior controle sobre o processo de renderização, melhorando significativamente a performance de interações como zoom, pan e desenho de previews em comparação com a abordagem anterior de regeneração total de SVG.
- `[ ] (P5)` **Capacidade de renderizar modelos 3D.**
- `[X]` Integração com a área de visualização da GUI (usando `dxflib` para carregar DXF e renderizar via SVG). # Esta linha pode precisar ser reavaliada ou removida se a renderização SVG via dxflib não for mais o método principal para a GUI. Por ora, mantenho conforme o escopo da tarefa.

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
- Consulte o arquivo [roadmap_gui.md](roadmap_gui.md) para o roadmap detalhado deste módulo.

## Manutenção e Melhorias Recentes
- `[X]` **Correções Diversas e Melhorias de Código:**
  - *Detalhe:* Corrigidas referências de `Point2D.Double` para `java.awt.geom.Point2D.Double` em `CustomCadPanel.java` para evitar ambiguidades. Adicionado método `distanceTo(Point2D other)` em `Point2D.java` (dxflib) para cálculos de distância. Refinada a obtenção de diagramas SVG em `CustomCadPanel.java`, armazenando o nome do diagrama carregado (`currentDiagramName`) e utilizando `svgUniverseFromDxf.getStreamBuiltURI(this.currentDiagramName)` para recuperação precisa do diagrama.
  - *Nota: Outros itens de manutenção específicos da GUI foram movidos para roadmap_gui.md*

## Módulo Leitor DXF (dxflib)
- Consulte o arquivo [roadmap_dxflib.md](roadmap_dxflib.md) para o roadmap detalhado deste módulo.

## Melhorias Futuras Planejadas
- `[ ] (P5)` **Suporte para leitura e renderização de arquivos DWG (investigar bibliotecas e complexidade).**
