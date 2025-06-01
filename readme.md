# Projeto de Ferramenta CAD Modular em Java

## Visão Geral

Este projeto é uma ferramenta CAD (Desenho Assistido por Computador) modular, desenvolvida em Java com o sistema de build Maven. Ele utiliza uma arquitetura de microkernel, onde um módulo central (`core`) gerencia os demais módulos e fornece serviços básicos. Esta arquitetura visa flexibilidade e extensibilidade, permitindo que novas funcionalidades sejam adicionadas como módulos independentes.

O módulo `core` atua como o coração da aplicação, responsável pela inicialização do sistema, gerenciamento do ciclo de vida dos outros módulos, e facilitação da comunicação entre eles (potencialmente através de um sistema de barramento de eventos como o `EventBus` identificado). Crucialmente, o `core` também é responsável por iniciar a interface gráfica do usuário (GUI).

## Módulos Principais e Funcionalidades

A aplicação é composta pelos seguintes módulos principais:

*   **`core` (Núcleo/Microkernel):**
    *   Responsável pela inicialização da aplicação, incluindo a GUI.
    *   Gerencia o ciclo de vida dos demais módulos (carregamento, inicialização, descarregamento).
    *   Fornece serviços centrais como logging, configuração e um sistema de eventos (`EventBus`) para comunicação desacoplada entre módulos.
*   **`gui` (Interface Gráfica do Usuário):**
    *   Fornece a janela principal da aplicação e todos os elementos de interação com o usuário (menus, barras de ferramentas, área de desenho).
    *   Implementado utilizando Java Swing.
*   **`modules/geometry` (Geometria):**
    *   Contém a lógica para a criação, manipulação e cálculos de formas geométricas 2D e 3D.
    *   Define as entidades base do desenho (pontos, linhas, curvas, superfícies, sólidos).
*   **`modules/rendering` (Renderização):**
    *   Responsável pela visualização e renderização dos modelos CAD na área de desenho da GUI.
    *   Traduz os dados geométricos em representações visuais.
*   **`modules/physics` (Física):**
    *   Provê capacidades de simulação física aplicadas aos modelos CAD, permitindo análises de stress, movimento, etc. (se aplicável ao escopo).
*   **`modules/export` (Exportação):**
    *   Agrupa submódulos que permitem exportar os projetos CAD para diversos formatos de arquivo padrão da indústria.
    *   Atualmente, possui estrutura para exportação em PDF (`modules/export/pdf`) e STL (`modules/export/stl`).
*   **`plugins` (Sistema de Plugins):**
    *   Oferece uma arquitetura para estender as funcionalidades da ferramenta CAD através de plugins externos.
    *   Inclui um exemplo (`plugins/custom_plugin`) demonstrando a integração.

## Como Compilar e Construir

O projeto utiliza Maven como sistema de build. Para compilar todos os módulos e instalar os artefatos no repositório local, execute o seguinte comando na raiz do projeto:

```bash
mvn clean install
```

Isso garantirá que todos os módulos sejam compilados e que as dependências entre eles sejam resolvidas.

## Como Executar

Para executar a aplicação com a interface gráfica, após compilar todos os módulos com `mvn clean install`, execute a classe principal do módulo `core`. O módulo `core` é responsável por inicializar o kernel e subsequentemente a interface gráfica.

Utilize o seguinte comando Maven:

```bash
mvn exec:java -Dexec.mainClass="com.cad.core.kernel.Kernel" -pl core
```
Este comando instrui o Maven a executar o método `main` da classe `com.cad.core.kernel.Kernel` dentro do contexto do módulo `core`.

## Roadmap e Status do Projeto

Para um detalhamento do status atual de implementação das funcionalidades e o roadmap de desenvolvimento futuro, consulte o nosso [Roadmap do Projeto](ROADMAP.md).

## Contribuindo

Contribuições são bem-vindas! Se você deseja contribuir, por favor:
1. Faça um fork do repositório.
2. Crie uma nova branch para sua feature ou correção (`git checkout -b minha-feature`).
3. Faça commit de suas mudanças (`git commit -am 'Adiciona nova feature'`).
4. Faça push para a branch (`git push origin minha-feature`).
5. Crie um novo Pull Request.

Por favor, certifique-se de que seu código segue as convenções do projeto e inclua testes para novas funcionalidades.