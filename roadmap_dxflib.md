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
- `[~]` Implementação básica da Tabela DIMSTYLE (parseia nome e alguns atributos visuais importantes como DIMASZ, DIMTXT, DIMEXO, DIMEXE, DIMGAP, DIMCLRD, DIMCLRE, DIMCLRT, DIMDEC)
- `[X]` Adição de Testes Unitários para Layers e Blocos
- `[X]` Implementação do Conversor DXF para SVG (para entidades parseadas) - *Nota: A GUI principal pode estar usando uma renderização customizada agora.*
- `[X]` Adição de Testes Unitários para o Conversor SVG
- `[~]` Suporte para Entidade DXF DIMENSION (parseamento básico da entidade e alguns atributos comuns como pontos de definição, texto, nome do estilo, flags do tipo, nome do bloco anônimo)

## Melhorias Futuras e Funcionalidades Planejadas

### Entidades DXF Adicionais
- `[~] (P1)` **Suporte para Entidade DXF DIMENSION:**
  - *Status Atual:* Parseamento básico da entidade e alguns atributos visuais do DIMSTYLE associado implementados. Os pontos de definição da dimensão (10,20,30; 11,21,31; 13,23,33; 14,24,34), o texto explícito (1), o nome do estilo (3), os flags de tipo (70) e o nome do bloco de dimensão (2) são lidos.
  - *Próximos Passos Críticos:*
    - `[ ] (P1)` **Extração de Texto de Dimensão de Blocos Anônimos:** Implementar a lógica para resolver referências de bloco (`*D1`, etc.) e extrair o valor textual da entidade TEXT/MTEXT dentro desses blocos. Isso é crucial para exibir o valor correto da cota quando não é explicitamente sobrescrito pelo código 1.
    - `[ ] (P2)` **Suporte para Tipos Adicionais de Dimensão:** Além de Aligned e Rotated (inferidos pelos flags e pontos), adicionar suporte explícito e parseamento de atributos específicos para Angular (3-Point, 2-Line), Radial, Diametral, Ordenada.
    - `[ ] (P2)` **Interpretação Completa dos Flags (código 70):** Decodificar todos os bits do código 70 para entender completamente as propriedades da dimensão (ex: tipo de dimensão, se o texto está sobrescrito, se a posição do texto foi modificada, etc.).
    - `[ ] (P3)` **Suporte para Tolerâncias e Formatação de Texto:** Parsear informações de tolerância (códigos 73, 74, 75, etc.) e outras opções de formatação de texto da dimensão.
    - `[ ] (P3)` Parsear pontos de definição específicos para outros tipos de dimensão (ex: ponto de definição de ângulo para DIMANGULAR - códigos 15, 25, 35).

- `[ ] (P1)` **Suporte para Entidade DXF HATCH:**
  - *Detalhe:* Parsear e representar a entidade DXF HATCH, incluindo tipo de padrão (ex: SOLID, PREDEFINED, USERDEFINED, CUSTOM), nome do padrão, ângulo, escala, limites (loops de arestas - POLYLINE, LINE, ARC, SPLINE).
- `[ ] (P1)` **Suporte para Entidade DXF SPLINE:**
  - *Detalhe:* Parsear e representar a entidade DXF SPLINE, incluindo pontos de controle (10,20,30), nós (40), pesos (41, opcional), grau (71), flags (70).
- `[ ] (P1)` **Suporte para Entidade DXF ELLIPSE:**
  - *Detalhe:* Parsear e representar a entidade DXF ELLIPSE, incluindo centro (10,20,30), extremidade do eixo maior (11,21,31), razão do eixo menor para o maior (40), ângulos de início e fim (41,42).
- `[ ] (P2)` **Suporte para Entidade DXF MTEXT (Texto Multilinha):**
  - *Detalhe:* Parsear e representar a entidade DXF MTEXT, incluindo ponto de inserção (10,20,30), altura do texto (40), direção de referência do retângulo (11,21), códigos de formatação embutidos (ex: `\P`, `\C`, `\H`, `\T`, etc.), ângulo de rotação (50), estilo de texto (7).
- `[ ] (P2)` **Suporte para Entidade DXF POLYLINE (complexa, 2D e 3D):**
  - *Detalhe:* Parsear e representar a entidade DXF POLYLINE (diferente de LWPOLYLINE), que pode ser 3D e ter vértices (`VERTEX`) com atributos complexos (flags, bulge, etc.). Requer parseamento aninhado de entidades VERTEX e SEQEND.
- `[ ] (P2)` **Suporte para Entidade DXF POINT:**
  - *Detalhe:* Parsear e representar a entidade DXF POINT, incluindo sua localização (10,20,30) e possível modo de exibição/tamanho (controlado por variáveis de HEADER como PDMODE, PDSIZE).
- `[ ] (P3)` **Suporte para Entidade DXF SOLID (2D):**
  - *Detalhe:* Parsear e representar a entidade DXF SOLID, definida por 3 ou 4 pontos de canto (10-13, 20-23, 30-33).
- `[ ] (P3)` Suporte para outras entidades conforme necessidade (ex: 3DFACE, LEADER, TOLERANCE, WIPEOUT, IMAGE, REGION, BODY).

### Seções e Tabelas DXF
- `[ ] (P1)` **Parseamento Completo da Seção HEADER:**
  - *Detalhe:* Ler e armazenar todas as variáveis de sistema relevantes da seção HEADER (ex: $MEASUREMENT, $INSUNITS, $LUNITS, $LUPREC, $ANGBASE, $ANGDIR, $DIMSCALE, $LTSCALE, $PDMODE, $PDSIZE, etc.) no `DxfDocument` para permitir interpretação correta de unidades, formatação e exibição.
- `[~] (P1)` **Parseamento Detalhado da Tabela DIMSTYLE:**
  - *Status Atual:* Nome e atributos visuais básicos são parseados.
  - *Detalhe:* Continuar a implementação para parsear *todos* os atributos relevantes da tabela DIMSTYLE (mais de 100 códigos de grupo possíveis) para permitir uma representação fiel das cotas. Isso inclui variáveis de texto, setas, linhas, unidades, tolerâncias, etc. (ex: DIMBLK, DIMSAH, DIMTAD, DIMTIH, DIMTOH, DIMPOST, DIMAZIN, DIMDEC, etc.).
- `[ ] (P1)` **Parseamento da Tabela BLOCK_RECORD:**
  - *Detalhe:* Ler e armazenar informações da tabela BLOCK_RECORD, que contém metadados sobre as definições de bloco (ex: nome, handle, se é anônimo, se é XREF, unidades do bloco). Importante para gerenciamento de blocos.
- `[ ] (P2)` **Parseamento da Tabela STYLE (Estilos de Texto):**
  - *Detalhe:* Ler e armazenar completamente as definições de estilo de texto da tabela STYLE, incluindo nome do estilo (2), nome da fonte primária (3), altura do texto (40), fator de largura (41), ângulo oblíquo (50), flags de geração de texto (70), nome da fonte grande (4, se usada).
- `[ ] (P2)` **Parseamento da Seção CLASSES:**
  - *Detalhe:* Implementar o parseamento da seção CLASSES, se necessário para dar suporte a entidades ou objetos personalizados que podem ser encontrados em alguns arquivos DXF, embora geralmente não seja essencial para entidades padrão.
- `[ ] (P3)` **Revisão da Tabela LTYPE:**
  - *Detalhe:* Garantir que todos os atributos padrão de LTYPE (nome, descrição, alinhamento, número de elementos de padrão, comprimento total do padrão, elementos do padrão) são corretamente parseados e armazenados.
- `[ ] (P3)` **Revisão da Tabela LAYER:**
  - *Detalhe:* Garantir que todos os atributos padrão de LAYER (nome, cor, tipo de linha, flags de visibilidade/congelamento/plotagem, peso de linha, handle de plotstyle) são corretamente parseados e armazenados.
- `[ ] (P3)` Parseamento de outras tabelas (VPORT, VIEW, UCS, APPID) conforme a necessidade.

### Funcionalidades do Parser e Estrutura
- `[ ] (P1)` **Extração de Texto de Dimensão de Blocos Anônimos:** (Movido para sub-item de DIMENSION)
- `[ ] (P2)` **Tratamento de Handles e Referências entre Objetos:**
  - *Detalhe:* Implementar um mecanismo para armazenar e resolver handles de objetos DXF (código 5 e códigos de handle específicos como 105, 330-369, etc.). Isso é essencial para vincular entidades a seus estilos, camadas, blocos e outras entidades relacionadas.
- `[ ] (P2)` **Tratamento de Dados Estendidos (XDATA):**
  - *Detalhe:* Implementar a capacidade de ler e armazenar dados estendidos (XDATA) associados a entidades (códigos 1000-1071). XDATA é usado por aplicações para anexar informações personalizadas.
- `[ ] (P3)` **Recuperação de Erros e Robustez:**
  - *Detalhe:* Melhorar a robustez do parser para lidar com arquivos DXF malformados ou com estruturas inesperadas, permitindo a leitura parcial do arquivo em vez de falha total, quando possível. Logar erros e avisos de forma clara.
- `[ ] (P3)` **Suporte para diferentes versões do DXF (além de AC1009/R12 e AC1015/2000):**
  - *Detalhe:* Avaliar e implementar compatibilidade com versões mais recentes (ex: AC1018/2004, AC1021/2007, etc.) ou mais antigas do formato DXF, se necessário, prestando atenção a códigos de grupo obsoletos ou novos.
- `[ ] (P4)` **Escrita de Arquivos DXF:**
  - *Detalhe:* Implementar a funcionalidade para salvar um `DxfDocument` de volta para o formato de arquivo DXF ASCII. Isso requer a escrita de todas as seções (HEADER, CLASSES, TABLES, BLOCKS, ENTITIES, OBJECTS) e o correto tratamento de handles e referências.

### Melhorias de Qualidade e Testes
- `[ ] (P1)` **Testes Unitários Abrangentes:**
  - *Detalhe:* Adicionar mais testes unitários para todas as entidades, tabelas e funcionalidades do parser, usando arquivos DXF de exemplo variados e complexos. Cobrir casos de borda e diferentes versões do DXF. Testar a correta atribuição de todos os campos parseados.
- `[ ] (P2)` **Testes de Integração:**
  - *Detalhe:* Criar testes que verifiquem o parseamento de arquivos DXF completos e a subsequente consistência dos dados no objeto `DxfDocument` (ex: todas as entidades de um bloco estão presentes, todas as entidades referenciam layers existentes).
- `[ ] (P3)` **Validação contra Especificação DXF:**
  - *Detalhe:* Onde possível, validar os dados parseados contra as regras e limites especificados pela Autodesk para o formato DXF.
