package eventplanner.gui;

import eventplanner.modelo.Antecedencia;
import eventplanner.modelo.Categoria;
import eventplanner.modelo.Evento;
import eventplanner.modelo.Participante;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.DateTimeException; // (ALEM das aulas) erro de data/hora inexistente
import java.time.LocalDate;
import java.time.LocalTime;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Formulario (JANELA DE DIALOGO MODAL) para criar ou editar um evento.
 *
 * HERANCA (Cap. 4 "Class Inheritance"): "é-um" JDialog (uma janela especializada).
 *
 * Componentes (Cap. 4/13):
 *  - JTextField para titulo e local;
 *  - IntField (nosso JTextField que so aceita inteiro) para dia/mes/ano e hora/min;
 *  - JRadioButton + ButtonGroup para a categoria (Cap. 13 "Radio Buttons");
 *  - JComboBox para a antecedencia do lembrete;
 *  - JList para a lista de participantes.
 *
 * Validacao de data/hora: usa try/catch (Cap. 10). Se o usuario digitar algo
 * que nao e numero, ou uma data inexistente (ex.: 30/02), mostra JOptionPane e
 * mantem o formulario aberto para correcao ("Fix the Error and Resume" - Cap. 10).
 *
 * OBS (ALEM das aulas): JComboBox e DefaultListModel nao aparecem nos slides
 * (JList e JRadioButton aparecem). Usamos JComboBox por ser compacto para 3 opcoes.
 */
public class DialogoEvento extends JDialog {

    // ---- Campos do formulario ----
    private final JTextField campoTitulo = new JTextField(20);
    private final IntField campoDia = new IntField(2);
    private final IntField campoMes = new IntField(2);
    private final IntField campoAno = new IntField(4);
    private final IntField campoHora = new IntField(2);
    private final IntField campoMinuto = new IntField(2);
    private final JTextField campoLocal = new JTextField(20);
    private final JTextArea campoDescricao = new JTextArea(3, 20);

    private final Categoria[] categorias = Categoria.values();
    private final JRadioButton[] radiosCategoria = new JRadioButton[categorias.length];
    private final JComboBox<Antecedencia> comboAntecedencia =
            new JComboBox<Antecedencia>(Antecedencia.values());

    // ---- Participantes ----
    private final JTextField campoNomePart = new JTextField(8);
    private final JTextField campoEmailPart = new JTextField(10);
    private final DefaultListModel<Participante> modeloParticipantes = new DefaultListModel<Participante>();
    private final JList<Participante> listaParticipantes = new JList<Participante>(modeloParticipantes);

    // ---- Resultado ----
    // Fica null se o usuario cancelar; recebe o Evento montado se ele salvar.
    private Evento eventoResultado = null;

    /**
     * @param dono           janela-pai (a modalidade trava a janela principal)
     * @param dataInicial    data sugerida ao abrir (o dia clicado no calendario)
     * @param eventoParaEditar  se != null, o formulario abre preenchido (modo edicao)
     */
    public DialogoEvento(Frame dono, LocalDate dataInicial, Evento eventoParaEditar) {
        super(dono, true); // true = modal (Cap. 10 "modal dialog")
        setTitle(eventoParaEditar == null ? "Novo evento" : "Editar evento");
        setLayout(new BorderLayout(8, 8));

        // =================== Formulario principal ===================
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));

        form.add(new JLabel("Titulo:"));
        form.add(campoTitulo);

        form.add(new JLabel("Data (dia/mes/ano):"));
        JPanel painelData = new JPanel();
        painelData.add(campoDia);
        painelData.add(new JLabel("/"));
        painelData.add(campoMes);
        painelData.add(new JLabel("/"));
        painelData.add(campoAno);
        form.add(painelData);

        form.add(new JLabel("Hora (hora:min):"));
        JPanel painelHora = new JPanel();
        painelHora.add(campoHora);
        painelHora.add(new JLabel(":"));
        painelHora.add(campoMinuto);
        form.add(painelHora);

        form.add(new JLabel("Local:"));
        form.add(campoLocal);

        // Categoria como botoes de radio mutuamente exclusivos (Cap. 13).
        form.add(new JLabel("Categoria:"));
        JPanel painelCategoria = new JPanel();
        ButtonGroup grupoCategoria = new ButtonGroup(); // garante que so 1 fica marcado
        for (int i = 0; i < categorias.length; i++) {
            radiosCategoria[i] = new JRadioButton(categorias[i].getRotulo());
            // Cada opcao recebe a COR da categoria (a mesma usada no card da
            // linha do tempo), em negrito, para o usuario associar tipo <-> cor.
            radiosCategoria[i].setForeground(categorias[i].getCor());
            radiosCategoria[i].setFont(radiosCategoria[i].getFont().deriveFont(Font.BOLD));
            grupoCategoria.add(radiosCategoria[i]);
            painelCategoria.add(radiosCategoria[i]);
        }
        radiosCategoria[0].setSelected(true); // padrao: primeira categoria
        form.add(painelCategoria);

        form.add(new JLabel("Lembrete:"));
        form.add(comboAntecedencia);

        form.add(new JLabel("Descricao:"));
        campoDescricao.setLineWrap(true);
        form.add(new JScrollPane(campoDescricao));

        add(form, BorderLayout.NORTH);

        // =================== Participantes ===================
        JPanel painelParticipantes = new JPanel(new BorderLayout(4, 4));
        painelParticipantes.setBorder(BorderFactory.createTitledBorder("Participantes")); // Cap. 13

        JPanel adicionarParticipante = new JPanel();
        adicionarParticipante.add(new JLabel("Nome:"));
        adicionarParticipante.add(campoNomePart);
        adicionarParticipante.add(new JLabel("E-mail:"));
        adicionarParticipante.add(campoEmailPart);
        JButton botaoAddPart = new JButton("Adicionar");
        adicionarParticipante.add(botaoAddPart);
        painelParticipantes.add(adicionarParticipante, BorderLayout.NORTH);

        painelParticipantes.add(new JScrollPane(listaParticipantes), BorderLayout.CENTER);

        JButton botaoRemovePart = new JButton("Remover selecionado");
        painelParticipantes.add(botaoRemovePart, BorderLayout.SOUTH);

        add(painelParticipantes, BorderLayout.CENTER);

        // =================== Botoes Salvar / Cancelar ===================
        JPanel painelBotoes = new JPanel();
        JButton botaoSalvar = new JButton("Salvar");
        JButton botaoCancelar = new JButton("Cancelar");
        painelBotoes.add(botaoSalvar);
        painelBotoes.add(botaoCancelar);
        add(painelBotoes, BorderLayout.SOUTH);

        // =================== Ligacoes de eventos (Cap. 4) ===================
        botaoAddPart.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { aoAdicionarParticipante(); }
        });
        botaoRemovePart.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                int i = listaParticipantes.getSelectedIndex();
                if (i >= 0) modeloParticipantes.remove(i);
            }
        });
        botaoSalvar.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { aoSalvar(); }
        });
        botaoCancelar.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                eventoResultado = null; // cancelou
                dispose();              // fecha o dialogo
            }
        });

        // Preenche os campos (modo novo ou modo edicao).
        if (eventoParaEditar == null) {
            preencherComData(dataInicial);
        } else {
            preencherCom(eventoParaEditar);
        }

        pack();                      // ajusta o tamanho ao conteudo
        setLocationRelativeTo(dono); // centraliza sobre a janela principal
    }

    /** Resultado do dialogo: o Evento montado, ou null se cancelado. */
    public Evento getEventoResultado() {
        return eventoResultado;
    }

    // =========================== acoes internas ===========================

    private void aoAdicionarParticipante() {
        String nome = campoNomePart.getText().trim();
        String email = campoEmailPart.getText().trim();
        if (nome.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe nome e e-mail do participante.");
            return;
        }
        // Observacao: a validacao definitiva do e-mail acontece ao salvar, no
        // GerenciadorEventos, que lanca EmailInvalidoException se faltar "@".
        modeloParticipantes.addElement(new Participante(nome, email));
        campoNomePart.setText("");
        campoEmailPart.setText("");
    }

    /**
     * Monta o Evento a partir dos campos. Trata erros de data/hora com try/catch
     * (Cap. 10) e mantem o dialogo aberto se algo estiver errado.
     */
    private void aoSalvar() {
        // ---- Data ----
        LocalDate data;
        try {
            // getInt() pode lancar NumberFormatException (Cap. 10).
            int dia = campoDia.getInt();
            int mes = campoMes.getInt();
            int ano = campoAno.getInt();
            // LocalDate.of valida se a data existe; 30/02 lanca DateTimeException.
            data = LocalDate.of(ano, mes, dia);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Data invalida: use apenas numeros.",
                    "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        } catch (DateTimeException ex) {
            JOptionPane.showMessageDialog(this, "Data invalida: esse dia nao existe.",
                    "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ---- Hora ----
        LocalTime hora;
        try {
            int h = campoHora.getInt();
            int min = campoMinuto.getInt();
            hora = LocalTime.of(h, min); // valida 0..23 e 0..59
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Hora invalida: use apenas numeros.",
                    "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        } catch (DateTimeException ex) {
            JOptionPane.showMessageDialog(this, "Hora invalida: use hora 0-23 e minuto 0-59.",
                    "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ---- Categoria escolhida (radio marcado) ----
        Categoria categoria = categorias[0];
        for (int i = 0; i < radiosCategoria.length; i++) {
            if (radiosCategoria[i].isSelected()) {
                categoria = categorias[i];
                break;
            }
        }

        Antecedencia antecedencia = (Antecedencia) comboAntecedencia.getSelectedItem();

        // Monta o evento. O titulo vazio NAO e barrado aqui de proposito:
        // quem valida e o GerenciadorEventos (lanca TituloVazioException),
        // demonstrando o uso das excecoes proprias (Cap. 10).
        Evento evento = new Evento(
                campoTitulo.getText().trim(),
                data, hora,
                campoLocal.getText().trim(),
                campoDescricao.getText().trim(),
                categoria, antecedencia);

        // Copia os participantes da lista para o evento.
        for (int i = 0; i < modeloParticipantes.size(); i++) {
            evento.addParticipante(modeloParticipantes.get(i));
        }

        eventoResultado = evento; // sinaliza sucesso para quem abriu o dialogo
        dispose();                // fecha
    }

    // =========================== preenchimento ===========================

    /** Modo NOVO: coloca a data sugerida e uma hora padrao. */
    private void preencherComData(LocalDate data) {
        campoDia.setText(String.valueOf(data.getDayOfMonth()));
        campoMes.setText(String.valueOf(data.getMonthValue()));
        campoAno.setText(String.valueOf(data.getYear()));
        campoHora.setText("12");
        campoMinuto.setText("00");
    }

    /** Modo EDICAO: copia todos os dados do evento para os campos. */
    private void preencherCom(Evento e) {
        campoTitulo.setText(e.getTitulo());
        campoDia.setText(String.valueOf(e.getData().getDayOfMonth()));
        campoMes.setText(String.valueOf(e.getData().getMonthValue()));
        campoAno.setText(String.valueOf(e.getData().getYear()));
        campoHora.setText(String.valueOf(e.getHora().getHour()));
        campoMinuto.setText(String.valueOf(e.getHora().getMinute()));
        campoLocal.setText(e.getLocal());
        campoDescricao.setText(e.getDescricao());

        // Marca o radio da categoria correta.
        for (int i = 0; i < categorias.length; i++) {
            if (categorias[i] == e.getCategoria()) {
                radiosCategoria[i].setSelected(true);
            }
        }
        comboAntecedencia.setSelectedItem(e.getAntecedencia());

        // Copia os participantes existentes para a lista editavel.
        for (int i = 0; i < e.getParticipantes().size(); i++) {
            Participante p = e.getParticipantes().elementAt(i);
            modeloParticipantes.addElement(new Participante(p.getNome(), p.getEmail()));
        }
    }
}
