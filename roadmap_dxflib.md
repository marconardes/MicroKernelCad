# Roadmap e Status do Módulo Leitor DXF (dxflib)

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

Este documento detalha o status atual de implementação das funcionalidades planejadas para o módulo `dxflib` e serve como um roadmap para seus desenvolvimentos futuros.

## Funcionalidades Implementadas (Histórico)
- `[X]` Definição do Escopo Inicial (Entidades DXF, Versão ASCII, AutoCAD 2000/2004, Layers/Cores)
- `[X]` Análise de Referência (Kabeja - conceitual)
- `[X]` Criação da Estrutura do Módulo `dxflib` (POM, pacotes)
- `[X]` Implementação das Estruturas de Dados Base (DxfDocument, DxfEntity, Point2D/3D, etc.)
- `[X]` Implementação do Parser para Entidades DXF Iniciais (LINE, CIRCLE, ARC, LWPOLYLINE, TEXT, INSERT)
- `[X]` Adição de Testes Unitários para Entidades Iniciais
- `[X]` Implementação da Leitura de Tabelas (LAYER, LTYPE) e Seção de Blocos (BLOCK)
- `[X]` Implementação básica da Tabela DIMSTYLE (parseia nome e atributos visuais importantes como DIMASZ, DIMTXT, DIMEXO, DIMEXE, DIMGAP, DIMCLRD, DIMCLRE, DIMCLRT, DIMDEC, DIMBLK, DIMTXSTY e flags básicas como DIMTAD, DIMTIH, DIMTOH, DIMSE1, DIMSE2).
- `[X]` Adição de Testes Unitários para Layers e Blocos
- `[X]` Implementação do Conversor DXF para SVG (para entidades parseadas) - *Nota: A GUI principal pode estar usando uma renderização customizada agora.*
- `[X]` Adição de Testes Unitários para o Conversor SVG
- `[X]` Suporte para Entidade DXF DIMENSION (parseamento dos pontos de definição (10,11,13,14), texto (1), nome do estilo (3), flags de tipo (70), nome do bloco (2), ângulo de rotação (50) e extrusão (210)).
- `[X]` Suporte para Entidade DXF SPLINE: Parseamento de pontos de controle, nós, grau, flags.
- `[X]` Parseamento da Tabela STYLE (Estilos de Texto): Leitura e armazenamento de nome, fonte primária, altura, fator de largura, ângulo oblíquo, etc.
- `[X]` Parseamento da Tabela BLOCK_RECORD: Leitura e armazenamento de nome e outros atributos básicos.
- `[X]` Parseamento da Seção OBJECTS (Básico):
    - `[X]` Suporte para `DICTIONARY` (incluindo entradas nome-handle).
    - `[X]` Suporte para `SCALE` (incluindo nome, paper/drawing units, isUnitScale).
- `[X]` Tratamento de Dados Estendidos (XDATA): Capacidade de ler e armazenar XDATA para entidades.
- `[X]` Tratamento de Reatores Persistentes: Capacidade de ler e armazenar handles de reatores para entidades.
- `[X]` Adição de Testes de Integração para `DxfParser` usando arquivos `1.dxf` e `2.dxf`.
- `[X]` Javadoc básico para a maioria das classes de estrutura, objetos e entidades.

## Melhorias Futuras e Funcionalidades Planejadas

### Entidades DXF Adicionais
- `[~] (P1)` **Suporte para Entidade DXF DIMENSION:** (Status atualizado, mas ainda com pendências)
  - *Status Atual:* Parseamento dos pontos de definição (10,11,13,14), texto (1), nome do estilo (3), flags de tipo (70), nome do bloco (2), ângulo de rotação (50) e extrusão (210) implementado.
  - *Próximos Passos Críticos:*
    - `[ ] (P1)` **Extração de Texto de Dimensão de Blocos Anônimos:** Implementar a lógica para resolver referências de bloco (`*D1`, etc.) e extrair o valor textual da entidade TEXT/MTEXT dentro desses blocos.
    - `[ ] (P2)` **Suporte para Tipos Adicionais de Dimensão:** Além de Aligned e Rotated (inferidos pelos flags e pontos), adicionar suporte explícito e parseamento de atributos específicos para Angular (3-Point, 2-Line), Radial, Diametral, Ordenada.
    - `[ ] (P2)` **Interpretação Completa dos Flags (código 70):** Decodificar todos os bits do código 70 para entender completamente as propriedades da dimensão.
    - `[ ] (P3)` **Suporte para Tolerâncias e Formatação de Texto:** Parsear informações de tolerância e outras opções de formatação de texto da dimensão.
    - `[ ] (P3)` Parsear pontos de definição específicos para outros tipos de dimensão.

- `[ ] (P1)` **Suporte para Entidade DXF HATCH:**
  - *Detalhe:* Parsear e representar a entidade DXF HATCH, incluindo tipo de padrão, nome do padrão, ângulo, escala, limites (loops de arestas).
- `[X]` **Suporte para Entidade DXF SPLINE:** (Implementação básica concluída)
- `[ ] (P1)` **Suporte para Entidade DXF ELLIPSE:**
  - *Detalhe:* Parsear e representar a entidade DXF ELLIPSE.
- `[ ] (P2)` **Suporte para Entidade DXF MTEXT (Texto Multilinha):**
  - *Detalhe:* Parsear e representar a entidade DXF MTEXT, incluindo códigos de formatação embutidos.
- `[ ] (P2)` **Suporte para Entidade DXF POLYLINE (complexa, 2D e 3D):**
  - *Detalhe:* Parsear e representar a entidade DXF POLYLINE (diferente de LWPOLYLINE).
- `[ ] (P2)` **Suporte para Entidade DXF POINT:**
  - *Detalhe:* Parsear e representar a entidade DXF POINT.
- `[ ] (P3)` **Suporte para Entidade DXF SOLID (2D):**
- `[ ] (P3)` Suporte para outras entidades conforme necessidade (3DFACE, LEADER, TOLERANCE, WIPEOUT, IMAGE, REGION, BODY).

### Seções e Tabelas DXF
- `[ ] (P1)` **Parseamento Completo da Seção HEADER:**
  - *Detalhe:* Ler e armazenar todas as variáveis de sistema relevantes da seção HEADER.
- `[X]` **Parseamento Detalhado da Tabela DIMSTYLE:** (Considerar como substancialmente implementado para os exemplos)
  - *Status Atual:* Nome e atributos visuais chave (DIMASZ, DIMTXT, DIMGAP, DIMCLRD, DIMCLRE, DIMCLRT, DIMDEC, DIMBLK, DIMTXSTY, e flags/settings como DIMTAD, DIMTIH, DIMTOH, DIMSE1, DIMSE2) são parseados.
  - *Nota:* Pode haver atributos menos comuns ainda não cobertos, mas o essencial para os arquivos de exemplo está presente.
- `[X]` **Parseamento da Tabela BLOCK_RECORD:** (Implementado)
- `[X]` **Parseamento da Tabela STYLE (Estilos de Texto):** (Implementado)
- `[ ] (P2)` **Parseamento da Seção CLASSES:**
- `[X]` **Revisão da Tabela LTYPE:** (Considerado implementado, melhorias futuras podem ser pontuais)
- `[X]` **Revisão da Tabela LAYER:** (Considerado implementado, melhorias futuras podem ser pontuais)
- `[ ] (P3)` Parseamento de outras tabelas (VPORT, VIEW, UCS, APPID) conforme a necessidade.

### Funcionalidades do Parser e Estrutura
- `[~] (P2)` **Tratamento de Handles e Referências entre Objetos:**
  - *Status Atual:* Handles são lidos e armazenados para a maioria dos objetos e entidades. Dicionários armazenam referências nome-handle. A resolução completa dessas referências (ex: vincular `DxfDimension.dimensionStyleName` ao objeto `DxfDimStyle` correspondente) ainda não é feita proativamente pelo parser em um passo de "linkagem", mas os dados estão disponíveis.
- `[X]` **Tratamento de Dados Estendidos (XDATA):** (Implementado)
- `[X]` **Tratamento de Reatores Persistentes:** (Implementado)
- `[ ] (P3)` **Recuperação de Erros e Robustez:**
- `[ ] (P3)` **Suporte para diferentes versões do DXF:**
- `[ ] (P4)` **Escrita de Arquivos DXF:**

### Melhorias de Qualidade e Testes
- `[~] (P1)` **Testes Unitários Abrangentes:**
  - *Status Atual:* Testes existem para funcionalidades iniciais e algumas mais recentes. Cobertura precisa ser expandida.
- `[X]` **Testes de Integração:**
  - *Status Atual:* Testes de integração básicos para `1.dxf` e `2.dxf` foram adicionados e estão passando.
- `[ ] (P3)` **Validação contra Especificação DXF:**
- `[X]` **Documentação (Javadoc):** Adicionado Javadoc para as principais classes de estrutura, objetos e entidades.
