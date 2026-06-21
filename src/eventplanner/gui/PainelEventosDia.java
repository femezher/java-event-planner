package eventplanner.gui;

import eventplanner.excecao.ValidacaoException;
import eventplanner.modelo.Evento;
import eventplanner.modelo.Participante;
import eventplanner.servico.GerenciadorEventos;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
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
        // JList que ACOMPANHA a largura do viewport (nao cresce com o maior card):
        // assim o renderizador pode quebrar o texto na largura visivel, sem que
        // a celula passe da borda do painel nem surja rolagem horizontal.
        lista = new JList<Evento>(modeloLista) {
            @Override public boolean getScrollableTracksViewportWidth() { return true; }
        };
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Renderizador proprio (polimorfismo - Cap. 8): quebra o texto do evento
        // em varias linhas conforme a LARGURA atual da coluna, em vez de cortar.
        lista.setCellRenderer(new RenderizadorEventoQuebra());
        // Quando a coluna muda de tamanho, manda o JList recalcular as alturas
        // das celulas (com a nova largura) para a quebra acompanhar a janela.
        lista.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                lista.setFixedCellHeight(10);  // forca o recalculo de layout...
                lista.setFixedCellHeight(-1);  // ...e volta a usar a altura do renderizador
            }
        });
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

    /**
     * Renderizador de celula que QUEBRA o texto em varias linhas conforme a
     * largura da lista (em vez de deixar o nome do evento atravessar a borda).
     *
     * HERANCA/POLIMORFISMO (Cap. 8): estende DefaultListCellRenderer e usa um
     * rotulo HTML com largura fixa, recurso do Swing para fazer o texto fluir.
     */
    private static class RenderizadorEventoQuebra extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object valor,
                int indice, boolean selecionado, boolean comFoco) {
            JLabel rotulo = (JLabel) super.getListCellRendererComponent(
                    lista, valor, indice, selecionado, comFoco);
            int largura = lista.getWidth();
            if (largura > 24 && valor != null) {
                // 'width:Npx' faz o HTML quebrar a linha na largura disponivel.
                rotulo.setText("<html><body style='width:" + (largura - 24) + "px'>"
                        + escaparHtml(valor.toString()) + "</body></html>");
            }
            return rotulo;
        }

        /** Evita que '<', '>' ou '&' do titulo quebrem o HTML do rotulo. */
        private static String escaparHtml(String s) {
            return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }
}
