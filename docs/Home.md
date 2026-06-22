# Java Event Planner — Documentação

Agenda de eventos **desktop em Java + Swing** (disciplina SCC0504 — POO, versão
Compacta). A janela abre em tela cheia, dividida em **três colunas
redimensionáveis**: calendário mensal · lista do dia · linha do tempo (estilo
Outlook, modos Dia/Semana), com busca, lembretes na inicialização, persistência
em arquivo e validação de entradas.

> Esta pasta `docs/` funciona como um **wiki versionado junto com o código**.
> A mesma documentação também está publicada na [aba **Wiki**](../../wiki) do repositório.

## Índice

| Página | Conteúdo |
|---|---|
| [Classes (UML)](Classes.md) | **Ficha de cada classe com a imagem do diagrama**, herança, polimorfismo e exceptions |
| [Arquitetura](Arquitetura.md) | Camadas MVC, pacotes, diagrama de classes e o fluxo de um clique |
| [Conceitos de POO](Conceitos-POO.md) | Herança, polimorfismo, interfaces, composição e exceções |
| [Como Executar](Como-Executar.md) | Compilar e rodar (macOS/Linux/Windows) |
| [Formato do Arquivo (CSV)](Formato-CSV.md) | Estrutura de `dados/eventos.csv` e robustez na carga |

## Documentos relacionados (na raiz do repositório)

- **[README](../README.md)** — visão geral e instruções rápidas.
- **[EXPLICACAO_CLASSES.md](../EXPLICACAO_CLASSES.md)** — explicação detalhada, classe a classe, alinhada às aulas.
- **[Fichas_Classes_UML.pdf](../Fichas_Classes_UML.pdf)** — uma página por classe (Herança · Polimorfismo · Exceptions + recorte do UML).
- **Diagrama de classes** — [`diagrama_classes.puml`](../diagrama_classes.puml) · [`.dot`](../diagrama_classes.dot) · [PNG](../diagrama_classes.png) · [SVG](../diagrama_classes.svg).

## Mapa rápido do código

```
src/eventplanner/
├── app/           EventPlannerApp (main)
├── modelo/        Evento, Participante, Categoria (enum), Antecedencia (enum)
├── excecao/       ValidacaoException + TituloVazio/DataInvalida/HoraInvalida/EmailInvalido
├── persistencia/  RepositorioEventos (interface) + RepositorioEventosCSV
├── servico/       GerenciadorEventos (regras / validação / busca / lembretes)
└── gui/           JanelaPrincipal, PainelCalendario, PainelEventosDia,
                   PainelAgenda, PainelLinhaTempo, IconeCategorias, DialogoEvento, IntField
dados/eventos.csv  agenda de exemplo
```
