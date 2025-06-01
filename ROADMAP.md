# Roadmap e Status do Projeto CAD Modular

Este documento detalha o status atual de implementação das funcionalidades planejadas para a ferramenta CAD modular e serve como um roadmap para desenvolvimentos futuros.

## Checklist do Projeto e Status Atual

**Arquitetura e Funcionalidades Centrais:**
- [X] Estrutura Modular com Microkernel (`core`): Implementado e funcional.
- [X] Gerenciamento de Módulos (`ModuleManager` no `core`): Implementado.
- [X] Sistema de Eventos (`EventBus` no `core`): Estrutura presente (uso específico a ser detalhado).
- [ ] Serviço de Logging (`LoggerService` no `core`): Estrutura presente, utilização efetiva a ser verificada.
- [ ] Serviço de Configuração (`ConfigService` no `core`): Estrutura presente, utilização efetiva a ser verificada.

**Módulos de Funcionalidade:**
*Módulo de Geometria (`geometry`):*
- [ ] Implementação de operações geométricas básicas (criação de linhas, círculos, etc.).
- [ ] Implementação de operações geométricas avançadas (booleanas, etc.).
*Módulo de Renderização (`rendering`):*
- [ ] Capacidade de renderizar modelos 2D.
- [ ] Capacidade de renderizar modelos 3D.
- [ ] Integração com a área de visualização da GUI.
*Módulo de Física (`physics`):*
- [ ] Implementação de simulações físicas.
*Módulo de Exportação (`export`):*
- [X] Submódulo para exportação PDF (`modules/export/pdf`): Estrutura presente.
- [X] Submódulo para exportação STL (`modules/export/stl`): Estrutura presente.
- [ ] Funcionalidade de exportação efetivamente implementada em cada submódulo.
*Sistema de Plugins (`plugins`):*
- [X] Estrutura para carregamento de plugins: Presente (`custom_plugin` como exemplo).
- [ ] Documentação/API clara para desenvolvimento de plugins.

**Interface Gráfica do Usuário (`gui`):**
- [X] Criação do Módulo `gui`: Concluído.
- [X] Janela Principal da Aplicação (`MainFrame.java`): Implementada (JFrame básico).
- [X] Menu Básico: Implementado ("Arquivo" > "Sair").
- [X] Placeholder para Área de Visualização CAD: Adicionado (JPanel vazio).
- [ ] Integração da Lógica de Renderização: Pendente (conectar o módulo `rendering` ao placeholder da GUI).
- [ ] Barra de Ferramentas com Ações CAD: Pendente.
- [ ] Painel de Propriedades de Objetos: Pendente.
- [ ] Gerenciamento de Camadas (Layers): Pendente.
- [ ] Interação com o Mouse para Desenho/Seleção: Pendente.
