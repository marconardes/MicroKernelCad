___
# Core Module - CAD Tool
___
Este é o módulo core da ferramenta CAD, que serve como microkernel do sistema. Ele gerencia a inicialização, o ciclo de vida e a comunicação entre os módulos, além de fornecer serviços centrais necessários para o funcionamento da aplicação.

## Estrutura do Projeto

Abaixo está a estrutura inicial do módulo core:

~~~
core/
├── pom.xml                     # Arquivo de configuração do Maven para o módulo core
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/cad/core/
    │   │       ├── kernel/                # Classes do microkernel
    │   │       │   ├── Kernel.java        # Classe principal do microkernel
    │   │       │   └── ModuleManager.java # Gerenciamento e carregamento de módulos
    │   │       ├── services/              # Serviços comuns usados pelos módulos
    │   │       │   ├── LoggerService.java # Serviço para logging
    │   │       │   └── ConfigService.java # Serviço para gerenciar configurações
    │   │       └── api/                   # APIs expostas aos módulos
    │   │           ├── ModuleInterface.java # Interface para desenvolvimento dos módulos
    │   │           └── EventBus.java      # Comunicação entre módulos e o núcleo
    │   └── resources/                     # Recursos específicos do módulo core
    └── test/
        └── java/com/cad/core/             # Testes unitários e de integração
            └── KernelTest.java            # Testes para o microkernel

~~~

## Componentes Principais
~~~
    Kernel.java: A classe principal do microkernel, responsável por inicializar o sistema e gerenciar o ciclo de vida dos módulos.
    ModuleManager.java: Classe que lida com o carregamento, registro e gerenciamento dos módulos, permitindo o controle de módulos dinâmicos.
    ModuleInterface.java: Interface que define os métodos que cada módulo deve implementar para se integrar com o microkernel.
    EventBus.java: Sistema de comunicação entre os módulos e o núcleo. Permite a troca de eventos entre módulos de forma desacoplada.
    LoggerService.java: Serviço de logging centralizado para que os módulos possam registrar suas atividades de forma consistente.
    ConfigService.java: Serviço de gerenciamento de configurações, acessível por todos os módulos, que mantém as definições globais do sistema.

~~~

## Como Executar o Módulo

Este módulo não é autossuficiente e, geralmente, será inicializado como parte do sistema completo da ferramenta CAD. No entanto, para testar o core de forma independente, siga estas etapas:

    Compile o módulo usando o Maven:

    

~~~ bash
mvn clean install
~~~
Para executar classes específicas, como Kernel.java, utilize o seguinte comando:

~~~ bash
    mvn exec:java -Dexec.mainClass="com.cad.core.Kernel"
~~~



Como Contribuir

    Faça um fork do projeto.
    Crie uma nova branch para a sua funcionalidade (git checkout -b feature/nova-funcionalidade).
    Faça commit das suas alterações (git commit -m 'Adiciona nova funcionalidade').
    Envie para o branch principal (git push origin feature/nova-funcionalidade).
    Abra um Pull Request.

## Testes

Para executar os testes unitários, use o seguinte comando:



    mvn test

## Licença

Este projeto está licenciado sob a licença MIT. Consulte o arquivo LICENSE para obter mais informações.