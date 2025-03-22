CAD Tool

Esta é uma ferramenta CAD modular, desenvolvida em Java, que utiliza uma arquitetura de microkernel para oferecer um ambiente flexível e extensível. O projeto é construído usando Maven e permite que novos módulos sejam adicionados como componentes independentes, permitindo uma expansão contínua das funcionalidades.
Visão Geral

A ferramenta CAD é projetada com uma estrutura modular que segue o estilo de microkernel. O núcleo (core) fornece os serviços e a infraestrutura básica necessários, enquanto funcionalidades específicas, como geometria, renderização, física e exportação, são implementadas em módulos independentes. Isso facilita a manutenção, teste e ampliação do sistema, além de possibilitar o desenvolvimento paralelo de diferentes partes da aplicação.
Estrutura do Projeto

bash

cad-tool/
├── pom.xml                    # Arquivo de configuração principal do Maven
├── core/                      # Módulo central que atua como microkernel
├── modules/                   # Módulos funcionais independentes
│   ├── geometry/              # Módulo de operações geométricas
│   ├── rendering/             # Módulo de renderização de modelos
│   ├── physics/               # Módulo de simulação física
│   └── export/                # Módulos para exportação (PDF, STL)
├── plugins/                   # Plugins opcionais e de terceiros
└── README.md                  # Documentação do projeto

Principais Módulos

    Core: Fornece o microkernel e serviços centrais, como gerenciamento de módulos, configuração e comunicação.
    Geometry: Implementa operações geométricas básicas e avançadas.
    Rendering: Lida com a renderização visual dos modelos.
    Physics: Realiza simulações físicas para análise de comportamento dos modelos.
    Export: Permite exportar modelos em diferentes formatos, como PDF e STL.

Como Executar

Para compilar e executar o projeto, certifique-se de ter o Java 21 e o Maven instalados.

    Compile todos os módulos:

    bash

    mvn clean install

    Execute o núcleo (core) ou outros módulos individualmente conforme necessário.

Contribuição

    Faça um fork do projeto.
    Crie uma branch para a sua funcionalidade (git checkout -b feature/nova-funcionalidade).
    Faça commit das suas alterações (git commit -m 'Adiciona nova funcionalidade').
    Envie para a branch principal (git push origin feature/nova-funcionalidade).
    Abra um Pull Request.

Licença

Este projeto está licenciado sob a licença MIT. Consulte o arquivo LICENSE para mais detalhes.

Esse README.md fornece uma visão geral do projeto, descrevendo a estrutura modular e os principais módulos, além de instruções de execução e contribuição.