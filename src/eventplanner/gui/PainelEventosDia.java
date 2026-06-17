package eventplanner.gui;

import eventplanner.excecao.ValidacaoException;
import eventplanner.modelo.Evento;
import eventplanner.modelo.Participante;
import eventplanner.servico.GerenciadorEventos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Painel da AGENDA DO DIA (lado direito da janela).
 *
 * Mostra a lista de eventos do dia selecionado (JList + JScrollPane - Cap. 13),
 * um painel de detalhes com os participantes, e os botoes Novo/Editar/Excluir.
 *
 * HERANCA: "é-um" JPanel.
 */
public class PainelEventosDia extends JPanel {

    /**
     * Interface usada para AVISAR a janela principal que os eventos mudaram
     * (assim ela atualiza o calendario e salva em disco). Papel/contrato - Cap. 8.
     */
    public interface Atualizavel {
        void aoAlterarEventos();
    }

    private final GerenciadorEventos gerenciador;
    private final Frame dono;            // janela dona dos dialogos (modal)
    private final Atualizavel atualizavel;

    private LocalDate diaAtual;
    private final JLabel titulo;
    private final DefaultListModel<Evento> modeloLista; // modelo de dados da JList
    private final JList<Evento> lista;
    private final JTextArea detalhes;

    public PainelEventosDia(GerenciadorEventos gerenciador, Frame dono, Atualizavel atualizavel) {
        this.gerenciador = gerenciador;
        this.dono = dono;
        this.atualizavel = atualizavel;
        this.diaAtual = LocalDate.now();

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(320, 0)); // largura fixa do painel direito

        // -------- Topo: titulo + botoes --------
        JPanel topo = new JPanel(new BorderLayout());
        titulo = new JLabel("Eventos");
        topo.add(titulo, BorderLayout.NORTH);

        JPanel botoes = new JPanel(new GridLayout(1, 3));
        JButton botaoNovo = new JButton("Novo");
        JButton botaoEditar = new JButton("Editar");
        JButton botaoExcluir = new JButton("Excluir");
        botoes.add(botaoNovo);
        botoes.add(botaoEditar);
        botoes.add(botaoExcluir);
        topo.add(botoes, BorderLayout.SOUTH);
        add(topo, BorderLayout.NORTH);

        // -------- Centro: lista de eventos do dia --------
        modeloLista = new DefaultListModel<Evento>();
        lista = new JList<Evento>(modeloLista);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(lista), BorderLayout.CENTER); // JScrollPane = barra de rolagem (Cap. 13)

        // -------- Rodape: detalhes do evento selecionado --------
        detalhes = new JTextArea(8, 20);
        detalhes.setEditable(false);
        detalhes.setLineWrap(true);
        detalhes.setWrapStyleWord(true);
        add(new JScrollPane(detalhes), BorderLayout.SOUTH);

        // Ao selecionar um item da lista, mostra os detalhes (evento de selecao).
        lista.addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                mostrarDetalhes(lista.getSelectedValue());
            }
        });

        // Liga as acoes dos botoes (eventos de botao - Cap. 4).
        botaoNovo.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { aoNovo(); }
        });
        botaoEditar.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { aoEditar(); }
        });
        botaoExcluir.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { aoExcluir(); }
        });

        mostrarDia(diaAtual);
    }

    /** Mostra os eventos de um dia especifico (chamado pelo calendario). */
    public void mostrarDia(LocalDate dia) {
        this.diaAtual = dia;
        titulo.setText("Eventos de " + formatar(dia));
        preencherLista(gerenciador.eventosDoDia(dia));
    }

    /** Mostra uma lista qualquer (usado para resultados de busca). */
    public void mostrarResultados(String tituloTexto, Vector<Evento> eventos) {
        titulo.setText(tituloTexto);
        preencherLista(eventos);
    }

    /** Recarrega a lista do dia atual (apos adicionar/editar/excluir). */
    public void recarregar() {
        mostrarDia(diaAtual);
    }

    // ----------------------------- acoes -----------------------------

    private void aoNovo() {
        // Abre o formulario ja com a data do dia selecionado.
        DialogoEvento dialogo = new DialogoEvento(dono, diaAtual, null);
        dialogo.setVisible(true); // modal: trava aqui ate fechar
        Evento novo = dialogo.getEventoResultado();
        if (novo != null) {
            try {
                gerenciador.adicionar(novo);   // valida titulo/email (pode lancar excecao)
                atualizavel.aoAlterarEventos(); // atualiza calendario + salva
                recarregar();
            } catch (ValidacaoException ex) {
                // Mostra a mensagem amigavel, sem stack trace (Cap. 10).
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Dados invalidos", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void aoEditar() {
        Evento selecionado = lista.getSelectedValue();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um evento para editar.");
            return;
        }
        DialogoEvento dialogo = new DialogoEvento(dono, selecionado.getData(), selecionado);
        dialogo.setVisible(true);
        Evento editado = dialogo.getEventoResultado();
        if (editado != null) {
            try {
                gerenciador.editar(selecionado, editado);
                atualizavel.aoAlterarEventos();
                recarregar();
            } catch (ValidacaoException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Dados invalidos", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void aoExcluir() {
        Evento selecionado = lista.getSelectedValue();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um evento para excluir.");
            return;
        }
        // Pede confirmacao (Confirm Dialog - Cap. 10).
        int opcao = JOptionPane.showConfirmDialog(this,
                "Excluir \"" + selecionado.getTitulo() + "\"?",
                "Confirmar exclusao", JOptionPane.YES_NO_OPTION);
        if (opcao == JOptionPane.YES_OPTION) {
            gerenciador.remover(selecionado);
            atualizavel.aoAlterarEventos();
            recarregar();
        }
    }

    // ----------------------------- apoio -----------------------------

    private void preencherLista(Vector<Evento> eventos) {
        modeloLista.clear();
        for (int i = 0; i < eventos.size(); i++) {
            modeloLista.addElement(eventos.elementAt(i));
        }
        detalhes.setText("");
    }

    /** Monta o texto de detalhes, incluindo a lista de participantes. */
    private void mostrarDetalhes(Evento e) {
        if (e == null) {
            detalhes.setText("");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Titulo: ").append(e.getTitulo()).append("\n");
        sb.append("Data: ").append(formatar(e.getData())).append("\n");
        sb.append("Hora: ").append(e.getHora()).append("\n");
        sb.append("Local: ").append(e.getLocal()).append("\n");
        sb.append("Categoria: ").append(e.getCategoria().getRotulo()).append("\n");
        sb.append("Lembrete: ").append(e.getAntecedencia().getRotulo()).append("\n");
        sb.append("Descricao: ").append(e.getDescricao()).append("\n");
        sb.append("Participantes:\n");
        Vector<Participante> ps = e.getParticipantes();
        if (ps.isEmpty()) {
            sb.append("  (nenhum)\n");
        } else {
            for (int i = 0; i < ps.size(); i++) {
                sb.append("  - ").append(ps.elementAt(i).toString()).append("\n");
            }
        }
        detalhes.setText(sb.toString());
        detalhes.setCaretPosition(0); // rola para o topo
    }

    /**
     * Formata uma data como dd/MM/aaaa sem precisar de DateTimeFormatter.
     * (getDayOfMonth/getMonthValue/getYear sao do LocalDate - ALEM das aulas.)
     */
    private String formatar(LocalDate d) {
        return String.format("%02d/%02d/%04d", d.getDayOfMonth(), d.getMonthValue(), d.getYear());
    }
}
