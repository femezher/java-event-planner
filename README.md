# Java Event Planner — Agenda de Eventos (SCC0504)

Aplicação desktop em **Java + Swing** para gerenciar eventos num **calendário
mensal**, com lista diária, busca, lembretes na inicialização, persistência em
arquivo e validação de entradas. Versão **Compacta (SCC0504)**.

> Plano completo, justificativa de POO e decisões de design: ver **`PLANO_DO_PROJETO.md`**.
> Diagrama de classes: ver **`diagrama_classes.puml`**.

---

## Como compilar e executar

Requer **Java 8 ou superior** (testado em Java 11). A partir da raiz do projeto:

```bash
# 1) compilar todos os .java para a pasta build/
mkdir -p build
javac -d build $(find src -name "*.java")

# 2) executar (a partir da raiz, para achar dados/eventos.csv)
java -cp build eventplanner.app.EventPlannerApp
```

No Windows (PowerShell), o passo 1 pode ser:

```powershell
mkdir build
javac -d build (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName })
java -cp build eventplanner.app.EventPlannerApp
```

Os eventos ficam em **`dados/eventos.csv`** (criado automaticamente se não existir).

---

## Estrutura do projeto

```
src/eventplanner/
├── modelo/        Evento, Participante, Categoria (enum), Antecedencia (enum)
├── excecao/       ValidacaoException + TituloVazio/DataInvalida/HoraInvalida/EmailInvalido
├── persistencia/  RepositorioEventos (interface) + RepositorioEventosCSV
├── servico/       GerenciadorEventos  (regras de negócio / validação / busca / lembretes)
├── gui/           JanelaPrincipal, PainelCalendario, PainelEventosDia, DialogoEvento, IntField
└── app/           EventPlannerApp  (main)
dados/eventos.csv  arquivo de dados de exemplo
```

Arquitetura em camadas no estilo **MVC** (Cap. 13): a GUI fala com o `servico`,
que fala com a `persistencia`. Detalhes no `PLANO_DO_PROJETO.md`.

---

## Funcionalidades (versão Compacta) e onde estão no código

| Funcionalidade | Implementação |
|---|---|
| Calendário mensal + navegação + "Hoje" | `gui/PainelCalendario.java` |
| Dias com evento destacados (cor da categoria) | `gui/PainelCalendario.java` |
| Lista do dia + detalhes + participantes | `gui/PainelEventosDia.java` |
| Criar / editar / excluir evento | `gui/DialogoEvento.java`, `servico/GerenciadorEventos.java` |
| Buscar por palavra-chave | `servico/GerenciadorEventos#buscarPorTexto` |
| Lembretes (próximas 24h, na inicialização) | `servico/GerenciadorEventos#lembretesProximos`, `app/EventPlannerApp` |
| Persistência CSV + carga tolerante a falhas | `persistencia/RepositorioEventosCSV.java` |
| Validação + mensagens amigáveis (sem stack trace) | pacote `excecao` + `JOptionPane` |
| Categorias coloridas (opcional) | `modelo/Categoria.java` |
| Participantes nome+e-mail (opcional) | `modelo/Participante.java` |

---

## Conceitos de POO aplicados (resumo para o relatório)

- **Encapsulamento:** atributos `private` + getters/setters (Cap. 3).
- **Herança:** `JanelaPrincipal extends JFrame`, `IntField extends JTextField`,
  e a hierarquia de exceções `... extends ValidacaoException extends Exception`.
- **Polimorfismo:** `toString()` sobrescrito; interface `RepositorioEventos`;
  `catch (ValidacaoException)` pega qualquer subclasse.
- **Abstração:** interface `RepositorioEventos` (troca CSV por outro sem afetar o resto).
- **Composição:** `Evento` tem `Vector<Participante>`, `Categoria`, `Antecedencia`.
- **Exceções:** `try/catch/finally`, exceções próprias, `JOptionPane` (Cap. 10).

---

## Observação sobre recursos além das aulas

Conforme combinado, alguns recursos **não vistos em aula** foram usados e estão
**comentados no código** no ponto de uso: `java.time` (datas/horas), `enum`,
_generics_ em `Vector<Evento>`, `JComboBox`/`DefaultListModel` e
`SwingUtilities.invokeLater`. Toda a base (Swing, eventos, herança, interfaces,
exceções, `Vector`, `Scanner`, `PrintStream`) está dentro do conteúdo das aulas.
