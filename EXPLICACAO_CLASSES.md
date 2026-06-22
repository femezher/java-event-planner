# Java Event Planner — Explicação das Classes do Diagrama UML

> Documento de apoio (SCC0504). Explica **cada classe** do diagrama
> `diagrama_classes.puml` / `.dot`: o que faz, **de quem herda ou qual interface
> implementa e por quê**, e o **papel de cada método**.
>
> Toda a terminologia segue o livro/aulas *Java, Java, Java — Object-Oriented
> Problem Solving* (Morelli & Walde) usado no curso. As referências de capítulo
> (Cap. 3, 4, 8, 10, 13) são as mesmas que aparecem nos comentários do código.

---

## 1. Conceitos das aulas usados no projeto

Antes de ir classe por classe, vale fixar os conceitos que justificam o desenho.

### Herança ("é-um") — Cap. 8
- **Definição (aula):** *"Class inheritance é o mecanismo pelo qual uma classe
  adquire (herda) os métodos e variáveis de suas superclasses."*
- **Regra (aula):** *"Subclasses herdam todas as variáveis e métodos **públicos e
  protegidos** (exceto construtores)."* Por isso, quando uma classe nossa
  `extends JFrame`, ela já "ganha" `setVisible`, `pack`, `add`, etc.
- **Notação UML:** triângulo vazado apontando para a superclasse (`JFrame <|-- JanelaPrincipal`).
- **No projeto:** usamos herança principalmente para **especializar componentes
  Swing** (igual ao exemplo `ToggleButton extends JButton` das aulas).

### Realização de interface ("é-um" também) — Cap. 8
- **Definição (aula):** *"Uma interface é como uma classe Java que contém apenas
  métodos abstratos e constantes."* Implementar uma interface é assumir um **papel**.
- A aula mostra que *"um Cat é ao mesmo tempo um Animal **e** um Speakable"* —
  ou seja, uma classe pode herdar de uma superclasse **e** implementar interfaces.
  É exatamente o caso da `JanelaPrincipal` (é um `JFrame` e também é
  `OuvinteDeDia` e `Atualizavel`).
- **Notação UML:** triângulo vazado **tracejado** (`Scrollable <|.. PainelLinhaTempo`).

### Polimorfismo e dynamic binding — Cap. 8
- **Três formas de polimorfismo (aula):** (1) **sobrescrever** (override) um método
  herdado; (2) **implementar** um método abstrato; (3) **implementar** uma interface.
  As três se apoiam no **dynamic binding** (a JVM escolhe, em tempo de execução,
  a implementação certa conforme o tipo real do objeto).
- **No projeto:** `toString()` sobrescrito em `Evento`/`Participante`/enums;
  `paintComponent()` sobrescrito nos painéis de desenho; a GUI conversa com a
  persistência só pelo tipo `RepositorioEventos` (interface), sem saber que por
  baixo é CSV.

### Composição / agregação ("tem-um") — Cap. 3/8
- **"tem-um":** um objeto guarda outros como atributos. Ex.: um `Evento` **tem uma**
  `Categoria` e **tem um** `Vector<Participante>`.
- **Notação UML:** losango **cheio** = composição (o todo controla a vida das
  partes); losango **vazado** = agregação (associação mais fraca).

### `super` e construtores — Cap. 8
- **Aula:** *"Construtores não são herdados"*; a subclasse chama o construtor da
  superclasse com `super(...)`. Ex.: `IntField(int colunas)` chama `super(colunas)`
  para reaproveitar o construtor do `JTextField`.

### Hierarquia Swing (de onde herdamos) — Cap. 13
A aula apresenta esta árvore (simplificada). É a "régua" para entender de quem
cada painel/janela nossa herda:

```
java.lang.Object
   └─ java.awt.Component
        └─ java.awt.Container
             ├─ Window ─ Frame ─ JFrame        (janela de topo)
             │        └─ Dialog ─ JDialog       (diálogo modal)
             └─ JComponent
                  ├─ AbstractButton ─ JButton / JToggleButton
                  ├─ JTextComponent ─ JTextField
                  ├─ JPanel   (painel/contêiner leve)
                  ├─ JList, JScrollPane, JLabel, JMenuBar, JMenuItem ...
```
- **Importante (aula):** *"JFrame, JDialog, JApplet e JWindow são heavyweight"*
  (janelas de topo, dependem do sistema nativo); o resto é *lightweight* (Java puro).
- Por isso a janela principal herda de **JFrame**, o formulário de **JDialog** e os
  painéis de **JPanel**.

### MVC (Model-View-Controller) — Cap. 13
- **Aula:** componentes têm 3 aspectos — **estado (model)**, **aparência (view)** e
  **comportamento (controller)**.
- **No projeto** organizamos os pacotes nesse espírito:
  - **Model (estado/dados):** `modelo` (`Evento`, `Participante`, `Categoria`, `Antecedencia`).
  - **View (aparência):** `gui` (janelas e painéis Swing).
  - **Controller (comportamento/regras):** `servico` (`GerenciadorEventos`) + a
    camada `persistencia`.

---

## 2. Mapa dos pacotes

| Pacote | Papel (MVC) | Classes |
|---|---|---|
| `app` | inicialização | `EventPlannerApp` |
| `modelo` | **Model** (estado/dados) | `Evento`, `Participante`, `Categoria` (enum), `Antecedencia` (enum) |
| `excecao` | validação | `ValidacaoException` (+ 4 subclasses) |
| `persistencia` | **Controller** (I/O) | `RepositorioEventos` (interface), `RepositorioEventosCSV` |
| `servico` | **Controller** (regras) | `GerenciadorEventos` |
| `gui` | **View** | `JanelaPrincipal`, `PainelCalendario`, `PainelEventosDia`, `PainelAgenda`, `PainelLinhaTempo`, `IconeCategorias`, `DialogoEvento`, `IntField` |

---

## 3. Pacote `app`

### `EventPlannerApp`
- **O que é:** ponto de entrada da aplicação (o `main`).
- **Herança/interface:** nenhuma — é só uma classe utilitária com o método estático.
- **Por que existe:** isolar a **montagem** do programa (qual repositório, qual
  gerenciador, abrir a janela). É o lugar que "liga os fios" entre as camadas.
- **Métodos:**
  - `main(String[] args)` — sequência de inicialização:
    1. define o *look and feel* multiplataforma (`UIManager.setCrossPlatform...`,
       padrão do Cap. 13) para os botões aparecerem iguais em qualquer SO;
    2. cria a persistência concreta `new RepositorioEventosCSV("dados/eventos.csv")`
       e a entrega ao `GerenciadorEventos` (que já carrega os eventos salvos);
    3. cria/mostra a `JanelaPrincipal` dentro de `SwingUtilities.invokeLater(...)`
       (a GUI deve ser criada na *thread* de eventos — boa prática além das aulas);
    4. exibe os **lembretes das próximas 24h** num `JOptionPane` (Cap. 10).

---

## 4. Pacote `modelo` (Model — estado/dados)

### `Evento` — classe central do Model
- **O que é:** representa um compromisso da agenda (título, data, hora, local,
  descrição, categoria, antecedência do lembrete e participantes).
- **Herança:** não estende ninguém (apenas, implicitamente, `Object`, como toda
  classe Java — Cap. 8).
- **Composição ("tem-um"):** um `Evento` **tem uma** `Categoria`, **tem uma**
  `Antecedencia` e **tem um** `Vector<Participante>`. No UML: `Evento *-- Participante`.
- **Encapsulamento (Cap. 3):** todos os atributos são `private`; o acesso é por
  *getters/setters*.
- **Métodos principais:**
  - *getters/setters* de cada campo — acesso controlado ao estado (Cap. 3).
  - `addParticipante(Participante p)` — delega ao `Vector` (`add`).
  - `getParticipantes()` — devolve a coleção de participantes.
  - `getDataHora() : LocalDateTime` — junta data + hora num único instante.
  - `getInstanteLembrete() : LocalDateTime` — calcula **quando avisar** =
    `getDataHora().minus(antecedencia.getDuracao())`.
  - `toString()` — **sobrescrito** (polimorfismo, Cap. 8): descreve o evento numa
    linha (`título (data hora) - local [categoria]`), usado nas `JList`.

### `Participante`
- **O que é:** dado simples — `nome` + `email`.
- **Herança/interface:** nenhuma.
- **Métodos:** `getNome`, `getEmail`, `setNome`, `setEmail` (Cap. 3) e `toString()`
  **sobrescrito**, que devolve `"Nome <email>"` (assim o participante já aparece
  formatado dentro de `JList`/`println`).

### `Categoria` — `enum`
- **O que é:** conjunto **fixo** de tipos de evento: `REUNIAO`, `ANIVERSARIO`,
  `COMPROMISSO`. Cada constante carrega um **rótulo** e uma **cor**.
- **Por que `enum` (recurso ALÉM das aulas):** nas aulas, valores fixos seriam
  `public static final int`. O `enum` foi escolhido porque agrupa, de forma segura,
  o conjunto fixo **junto com os dados de cada item** (rótulo + cor) e já impede
  valores inválidos.
- **Métodos:** `getRotulo() : String`, `getCor() : Color` e `toString()`
  **sobrescrito** (devolve o rótulo, para aparecer bonito em combos/listas).

### `Antecedencia` — `enum`
- **O que é:** com quanto tempo antes avisar: `UM_DIA`, `TRES_DIAS`, `UMA_SEMANA`.
  Cada constante guarda um rótulo e uma `Duration`.
- **Por que `enum`:** mesma justificativa de `Categoria`.
- **Métodos:** `getDuracao() : Duration` (usado em `Evento.getInstanteLembrete`),
  `getRotulo()` e `toString()` **sobrescrito**.

---

## 5. Pacote `excecao` (validação — Cap. 10)

A aula de exceções mostra que toda exceção **herda de `Exception`** e que podemos
criar **exceções próprias** para dar mensagens específicas. Montamos uma pequena
**hierarquia** para isso.

### `ValidacaoException`
- **Herança:** `extends Exception` → é uma exceção **checked** (Cap. 10): o compilador
  obriga `throws`/`try-catch`. Por isso `GerenciadorEventos.adicionar(...)` declara
  `throws ValidacaoException` e a GUI a captura com `try/catch`.
- **Por que existe:** ser a **superclasse comum** de todos os erros de validação.
  Assim a GUI pode capturar `ValidacaoException` (o tipo geral) e tratar qualquer
  erro de dados de uma vez — **polimorfismo de exceções**.

### `TituloVazioException`, `DataInvalidaException`, `HoraInvalidaException`, `EmailInvalidoException`
- **Herança:** todas `extends ValidacaoException`.
- **Por que herdar dela:** cada uma representa **um motivo específico** de falha
  (título em branco, data nula, hora nula, e-mail sem `@`). Como são subclasses,
  continuam sendo "uma `ValidacaoException`" — quem quiser tratar caso a caso pode;
  quem quiser tratar em bloco, captura a superclasse.
- **Métodos:** basicamente o **construtor**, que repassa a mensagem amigável para
  `super(...)` (a `Exception` guarda a mensagem) — Cap. 8/10.

---

## 6. Pacote `persistencia` (Controller — entrada/saída)

### `RepositorioEventos` — **interface**
- **O que é:** o **contrato** de "quem sabe guardar e recuperar eventos".
- **Por que interface (Cap. 8):** a aula recomenda interfaces para definir **papéis**
  e ganhar flexibilidade. Hoje guardamos em CSV; amanhã poderíamos criar um
  `RepositorioEventosBD` (banco) **sem mudar nada** no resto do programa — graças ao
  **dynamic binding**.
- **Métodos (abstratos):**
  - `salvar(Vector<Evento>) throws IOException` — escrever pode falhar (exceção checked).
  - `carregar() : Vector<Evento>` — **não** lança exceção: se o arquivo não existe
    ou está corrompido, devolve o que conseguiu ler (no mínimo um `Vector` vazio).

### `RepositorioEventosCSV`
- **Realização de interface:** `implements RepositorioEventos` (UML: triângulo
  vazado tracejado). É a **implementação concreta** que grava num arquivo `.csv`.
- **Por que implementar a interface:** permite que o `GerenciadorEventos` enxergue
  apenas `RepositorioEventos` (o papel), não o CSV — é o **polimorfismo** das aulas.
- **Atributo:** `arquivo : File` (associa o caminho a um objeto — Cap. 4).
- **Métodos:**
  - `salvar(...)` — usa `PrintStream` + `FileOutputStream` e fecha no `finally`
    (padrão "tudo junto" do Cap. 13/10); grava 1 evento por linha.
  - `carregar()` — usa `Scanner` + `File` (Cap. 4); ignora linhas em branco/comentário,
    e trata **cada linha isoladamente** num `try/catch` para que uma linha corrompida
    não derrube as demais (robustez exigida no enunciado).
  - (privados) `formatarLinha(Evento)` e `parseLinha(String)` — convertem
    `Evento ↔ texto CSV`.

---

## 7. Pacote `servico` (Controller — regras)

### `GerenciadorEventos`
- **O que é:** o "cérebro" da aplicação (papel de **Controller** no MVC). A GUI
  **só** conversa com esta classe; nunca mexe em arquivo diretamente.
- **Herança/interface:** nenhuma — é uma classe de serviço.
- **Composição/agregação:**
  - **tem-um** `Vector<Evento>` (a coleção em memória) → no UML, `o-- Evento` (agregação).
  - **usa** um `RepositorioEventos` (interface) para salvar/carregar → associação "usa".
- **Métodos:**
  - **CRUD:** `adicionar(e) throws ValidacaoException`, `editar(antigo, novo)
    throws ValidacaoException`, `remover(e)`, `getEventos()`.
  - **Consultas:** `eventosDoDia(LocalDate)`, `temEventoNoDia(LocalDate)`,
    `buscarPorTexto(String)` (procura no título/descrição/local, ignorando caixa).
  - **Lembretes:** `lembretesProximos()` — eventos cujo instante de lembrete cai
    entre **agora** e **agora + 24h**.
  - **Persistência:** `salvar() throws IOException` — **delega** ao repositório.
  - **Validação (privado):** `validar(Evento)` — lança a subclasse de
    `ValidacaoException` correta (título vazio, data/hora nula, e-mail sem `@`).

---

## 8. Pacote `gui` (View — Swing)

> Todas as classes de janela/painel aqui usam **herança de componentes Swing**
> (Cap. 13), exatamente como o exemplo `ToggleButton extends JButton` da aula:
> herdamos o comportamento pronto e **acrescentamos** o que é específico do projeto.

### `JanelaPrincipal`
- **Herança:** `extends JFrame` — *é-uma* **janela de topo** (heavyweight, Cap. 13).
  Herda `setVisible`, `pack`, `setJMenuBar`, `add`, etc.
- **Interfaces (papéis) — multiplo, igual ao "Cat é Animal e Speakable" da aula:**
  - `implements PainelCalendario.OuvinteDeDia` → reage ao **clique num dia** do calendário;
  - `implements PainelEventosDia.Atualizavel` → reage a **mudanças nos eventos**.
- **Por que essas escolhas:** ser a janela principal (JFrame) **e** ao mesmo tempo o
  "ouvinte" central que os painéis avisam — é o ponto onde as 3 colunas se coordenam.
- **Composição:** **tem** as 3 colunas — `PainelCalendario`, `PainelEventosDia`,
  `PainelAgenda` — montadas em `JSplitPane` (Cap. 13 "Split Panes").
- **Métodos:**
  - `diaSelecionado(LocalDate)` — **implementa** `OuvinteDeDia`: foca o dia na lista
    lateral e na linha do tempo.
  - `aoAlterarEventos()` — **implementa** `Atualizavel`: chama `atualizarTudo()`.
  - (privados) `construirMenu()`, `buscar()`, `salvarComTratamento()` (trata
    `IOException` com `JOptionPane`), `atualizarTudo()` (re-pinta calendário,
    recarrega a lista e a linha do tempo, e salva).

### `PainelCalendario`
- **Herança:** `extends JPanel` — *é-um* painel (contêiner leve, Cap. 13).
- **Por quê:** desenhar a grade do mês com `GridLayout` (Cap. 13) e a navegação
  `< mês >` / `Hoje`.
- **Interface interna `OuvinteDeDia`:** é o **contrato** de quem quer ser avisado
  quando o usuário clica num dia. Tem o método `diaSelecionado(dia)` e o método
  **default** `diaAbrirDetalhe(dia)` (Cap. 8: método *default* — quem não quiser
  tratar o duplo-clique não precisa implementar nada).
- **Métodos:** `atualizar()` — (re)constrói a grade do mês, destaca fins de semana,
  marca "hoje" com borda e põe, em cada dia com evento, o ícone colorido
  (`IconeCategorias`) com as cores das categorias presentes.

### `PainelEventosDia`
- **Herança:** `extends JPanel`.
- **Por quê:** mostrar a lista de eventos do dia (`JList` + `JScrollPane`, Cap. 13),
  os detalhes e os botões Novo/Editar/Excluir.
- **Interface interna `Atualizavel`:** contrato para **avisar a janela principal**
  que os eventos mudaram (assim ela atualiza o calendário e salva). Método:
  `aoAlterarEventos()`.
- **Métodos:** `mostrarDia(LocalDate)`, `mostrarResultados(titulo, eventos)` (para a
  busca), `recarregar()`; (privados) `aoNovo/aoEditar/aoExcluir` abrem o
  `DialogoEvento` e capturam `ValidacaoException` com `JOptionPane` (Cap. 10).
- **Classe interna `RenderizadorEventoQuebra`** (`extends DefaultListCellRenderer`):
  exemplo de **polimorfismo por sobrescrita** — personaliza o desenho da célula da
  lista para **quebrar o texto** na largura da coluna.

### `PainelAgenda`
- **Herança:** `extends JPanel`.
- **Por quê:** é a **3ª coluna** (linha do tempo estilo Outlook). Monta a barra de
  controle (alternância **Dia/Semana**, navegação `< >`, "Hoje"), o cabeçalho fixo
  das colunas e a rolagem (`JScrollPane`).
- **Composição:** **tem um** `PainelLinhaTempo` (quem realmente desenha os cards);
  o `PainelAgenda` o recria a cada troca de modo/dia.
- **Métodos:** `mostrarDia(LocalDate)`, `recarregar()`; (privados) `navegar(±)`,
  `reconstruir()`.

### `PainelLinhaTempo`
- **Herança:** `extends JPanel`.
- **Interface (papel):** `implements Scrollable` (Cap. 8) — para se comportar bem
  dentro de um `JScrollPane` (rolagem suave; na visão de **dia** ocupa toda a largura).
  É a **3ª forma de polimorfismo** (implementar interface) das aulas.
- **Por quê:** é a "tela de desenho" da agenda. **Sobrescreve `paintComponent`**
  (Cap. 13 "Drawing") e usa `Graphics2D` para desenhar a régua das horas, a grade e
  os **cards** dos eventos (coloridos pela categoria, posicionados pelo horário,
  divididos em subcolunas quando se sobrepõem).
- **Métodos:**
  - `paintComponent(Graphics)` — **sobrescrito**: desenha tudo.
  - `getToolTipText(MouseEvent)` — **sobrescrito**: tooltip com os dados do evento
    sob o mouse.
  - `criarCabecalho(String[])` — devolve o componente de cabeçalho fixo das colunas.
  - métodos da interface `Scrollable` (`getPreferredScrollableViewportSize`,
    `getScrollableUnitIncrement`, `getScrollableTracksViewportWidth`, etc.).
  - (interação) duplo-clique sobre um card **edita**; em área vazia **cria** um evento.

### `IconeCategorias`
- **Interface (papel):** `implements javax.swing.Icon` — o "papel" que o Swing usa
  para **desenhar um ícone** dentro de um botão/rótulo (3ª forma de polimorfismo).
- **Por quê:** num dia do calendário com eventos de várias categorias, pinta **todas
  as cores lado a lado** (requisito "mostrar as cores de todas as categorias").
- **Métodos (do contrato `Icon`):** `getIconWidth()`, `getIconHeight()` e
  `paintIcon(Component, Graphics, x, y)` — divide a largura em faixas iguais, uma
  por cor.

### `DialogoEvento`
- **Herança:** `extends JDialog` — *é-um* **diálogo modal** (Cap. 10/13). Modal =
  trava a janela principal até ser fechado.
- **Por quê:** é o **formulário** de criar/editar evento (campos de título, data,
  hora, local, descrição; `JRadioButton` + `ButtonGroup` para a categoria;
  `JComboBox` para a antecedência; `JList` para participantes).
- **Composição:** **tem** vários `IntField` (dia/mês/ano/hora/min).
- **Validação (Cap. 10):** `aoSalvar()` usa `try/catch` para `NumberFormatException`
  (texto não numérico) e `DateTimeException` (data/hora inexistente, ex.: 30/02);
  em erro, mostra `JOptionPane` e **mantém o formulário aberto** para correção
  ("Fix the Error and Resume").
- **Métodos:** `getEventoResultado() : Evento` — devolve o `Evento` montado, ou
  `null` se o usuário cancelou; (privados) `aoSalvar`, `aoAdicionarParticipante`,
  `preencherCom(Evento)` (modo edição) e `preencherComData(LocalDate)` (modo novo).

### `IntField`
- **Herança:** `extends JTextField` — *é-um* campo de texto que só faz sentido com
  números. É o exemplo clássico das aulas (`ToggleButton extends JButton`): herdar
  um componente Swing e **acrescentar** um detalhe.
- **`super` (Cap. 8):** o construtor `IntField(int colunas)` chama `super(colunas)`
  para reaproveitar o construtor do `JTextField`.
- **Métodos:** `getInt() : int` — devolve o conteúdo como inteiro; lança
  `NumberFormatException` (via `Integer.parseInt`) se o texto não for número — que o
  `DialogoEvento` captura e transforma em mensagem amigável (Cap. 10).

---

## 9. Resumo: quem herda de quê e por quê

| Classe | `extends` (herda de) | `implements` (papéis) | Motivo |
|---|---|---|---|
| `JanelaPrincipal` | `JFrame` | `OuvinteDeDia`, `Atualizavel` | ser janela de topo **e** o coordenador central das 3 colunas |
| `PainelCalendario` | `JPanel` | — | painel/contêiner do calendário (GridLayout) |
| `PainelEventosDia` | `JPanel` | — | painel da lista do dia (JList) |
| `PainelAgenda` | `JPanel` | — | painel da 3ª coluna (controles + rolagem) |
| `PainelLinhaTempo` | `JPanel` | `Scrollable` | tela de desenho que rola bem no JScrollPane |
| `IconeCategorias` | — | `Icon` | desenhar faixas de cor como ícone do Swing |
| `DialogoEvento` | `JDialog` | — | formulário modal |
| `IntField` | `JTextField` | — | campo de texto só-inteiro (`getInt`) |
| `RepositorioEventosCSV` | — | `RepositorioEventos` | implementação concreta do contrato de persistência |
| `ValidacaoException` | `Exception` | — | exceção própria (checked) |
| `TituloVazio/DataInvalida/HoraInvalida/EmailInvalido` | `ValidacaoException` | — | motivos específicos de falha |

> **Regra que justifica tudo (Cap. 8):** *"Subclasses herdam todas as variáveis e
> métodos públicos e protegidos, exceto construtores."* — por isso basta `extends`
> para reaproveitar todo o comportamento do Swing/`Exception` e só escrever o que é
> novo no projeto.
