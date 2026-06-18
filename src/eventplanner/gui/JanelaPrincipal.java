package eventplanner.gui;

import eventplanner.modelo.Evento;
import eventplanner.servico.GerenciadorEventos;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Janela principal da aplicacao.
 *
 * HERANCA (Cap. 4/13): "é-um" JFrame (janela de topo).
 * IMPLEMENTA duas interfaces (polimorfismo - Cap. 8):
 *  - PainelCalendario.OuvinteDeDia  -> reage ao clique num dia do calendario;
 *  - PainelEventosDia.Atualizavel   -> reage a mudancas nos eventos.
 *
 * Layout (BorderLayout - Cap. 13): calendario no CENTRO, agenda do dia a LESTE,
 * barra de busca ao NORTE e um menu "Arquivo" no topo.
 */
public class JanelaPrincipal extends JFrame
        implements PainelCalendario.OuvinteDeDia, PainelEventosDia.Atualizavel {

    private final GerenciadorEventos gerenciador;
    private final PainelCalendario painelCalendario;
    private final PainelEventosDia painelDia;
    private final JTextField campoBusca = new JTextField(16);

    public JanelaPrincipal(GerenciadorEventos gerenciador) {
        super("Java Event Planner - Agenda (SCC0504)");
        this.gerenciador = gerenciador;

        setLayout(new BorderLayout(6, 6));

        // ---------- NORTE: barra de busca ----------
        JPanel barraBusca = new JPanel();
        barraBusca.add(new JLabel("Buscar:"));
        barraBusca.add(campoBusca);
        JButton botaoBuscar = new JButton("Buscar");
        JButton botaoLimpar = new JButton("Limpar");
        barraBusca.add(botaoBuscar);
        barraBusca.add(botaoLimpar);
        add(barraBusca, BorderLayout.NORTH);

        // ---------- CENTRO/LESTE: calendario + agenda do dia ----------
        // 'this' e passado como ouvinte: a propria janela trata os eventos.
        painelCalendario = new PainelCalendario(gerenciador, this);
        painelDia = new PainelEventosDia(gerenciador, this, this);
        add(painelCalendario, BorderLayout.CENTER);
        add(painelDia, BorderLayout.EAST);

        // ---------- Menu Arquivo (Cap. 13 "Menus") ----------
        construirMenu();

        // ---------- Acoes da busca ----------
        botaoBuscar.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { buscar(); }
        });
        botaoLimpar.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                campoBusca.setText("");
                painelDia.mostrarDia(LocalDate.now());
            }
        });

        // ---------- Fechar a janela: salvar antes de sair ----------
        // Padrao do Cap. 13: classe interna anonima que estende WindowAdapter.
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                salvarComTratamento();
                System.exit(0);
            }
        });

        pack();
        setLocationRelativeTo(null); // centraliza na tela
    }

    private void construirMenu() {
        JMenuBar barra = new JMenuBar();
        JMenu menuArquivo = new JMenu("Arquivo");

        JMenuItem itemSalvar = new JMenuItem("Salvar");
        itemSalvar.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                salvarComTratamento();
                JOptionPane.showMessageDialog(JanelaPrincipal.this, "Agenda salva com sucesso.");
            }
        });

        JMenuItem itemSair = new JMenuItem("Sair");
        itemSair.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                salvarComTratamento();
                System.exit(0);
            }
        });

        menuArquivo.add(itemSalvar);
        menuArquivo.addSeparator();
        menuArquivo.add(itemSair);
        barra.add(menuArquivo);
        setJMenuBar(barra);
    }

    private void buscar() {
        String termo = campoBusca.getText().trim();
        Vector<Evento> resultados = gerenciador.buscarPorTexto(termo);
        painelDia.mostrarResultados(
                "Busca: \"" + termo + "\" (" + resultados.size() + ")", resultados);
    }

    /** Salva os dados tratando IOException com mensagem amigavel (Cap. 10). */
    private void salvarComTratamento() {
        try {
            gerenciador.salvar();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Nao foi possivel salvar a agenda: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===================== implementacao das interfaces =====================

    /** Chamado pelo PainelCalendario quando um dia e clicado. */
    @Override
    public void diaSelecionado(LocalDate dia) {
        painelDia.mostrarDia(dia);
    }

    /** Chamado pelo PainelEventosDia quando eventos sao criados/editados/excluidos. */
    @Override
    public void aoAlterarEventos() {
        painelCalendario.atualizar(); // re-pinta os destaques do mes
        salvarComTratamento();        // persiste imediatamente
    }
}
