# Conceitos de POO aplicados

Mapeamento dos conceitos das aulas (Cap. 3, 4, 8, 10, 13) ao código. A explicação
completa, classe a classe, está em **[EXPLICACAO_CLASSES.md](../EXPLICACAO_CLASSES.md)**
e nas **[Fichas_Classes_UML.pdf](../Fichas_Classes_UML.pdf)**.

## Encapsulamento (Cap. 3)
Atributos `private` + `get/set` em todo o `modelo`. Protege o estado e centraliza
a validação.

## Herança "é-um" (Cap. 8) — quem herda de quê e por quê
- **Componentes Swing:** `JanelaPrincipal extends JFrame`; os painéis
  (`PainelCalendario`, `PainelEventosDia`, `PainelAgenda`, `PainelLinhaTempo`)
  `extends JPanel`; `DialogoEvento extends JDialog`; `IntField extends JTextField`.
  Reaproveita todo o comportamento pronto do Swing e só acrescenta o específico
  (mesmo padrão do exemplo `ToggleButton extends JButton` das aulas).
- **Exceções:** `TituloVazio/DataInvalida/HoraInvalida/EmailInvalido`
  `extends ValidacaoException extends Exception`.
- **Decisão consciente:** **não** criamos subclasses de `Evento` por categoria —
  a categoria muda só dados (rótulo, cor), não comportamento; por isso é um `enum`,
  não herança.

## Polimorfismo — as 3 formas (Cap. 8)
1. **Sobrescrever** método herdado: `toString()` em `Evento`/`Participante`/enums;
   `paintComponent()` em `PainelLinhaTempo`.
2. **Implementar interface (do Swing):** `PainelLinhaTempo implements Scrollable`;
   `IconeCategorias implements Icon`.
3. **Interface-papel + dynamic binding:** o serviço usa `RepositorioEventos` sem
   saber que por baixo é CSV; o `catch (ValidacaoException)` pega qualquer subclasse.

> A `JanelaPrincipal` é um `JFrame` **e também** `OuvinteDeDia` e `Atualizavel` —
> herança de classe + realização de interfaces ("um `Cat` é `Animal` e `Speakable`").

## Composição/agregação "tem-um" (Cap. 3/8)
`Evento` **tem um** `Vector<Participante>`, uma `Categoria` e uma `Antecedencia`
(composição). `GerenciadorEventos` **agrega** a coleção `Vector<Evento>`.

## Exceções (Cap. 10)
`try/catch/finally`, **exceções próprias** (`ValidacaoException` e subclasses) e
`JOptionPane` para mensagens amigáveis — o usuário nunca vê um stack trace.
`RepositorioEventosCSV` usa `try/finally` para fechar o arquivo e trata cada linha
isoladamente (tolerância a arquivo ausente/corrompido).

## Recursos além das aulas (comentados no código)
`java.time` (datas/horas), `enum`, _generics_ em `Vector<Evento>`,
`JComboBox`/`DefaultListModel`, `SwingUtilities.invokeLater` e o desenho com
`Graphics2D`. Toda a base (Swing, eventos, herança, interfaces, exceções, `Vector`,
`Scanner`, `PrintStream`) está dentro do conteúdo das aulas.
