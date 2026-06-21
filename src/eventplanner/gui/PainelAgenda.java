package eventplanner.gui;

import eventplanner.servico.GerenciadorEventos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * TERCEIRA COLUNA da janela principal: a agenda em LINHA DO TEMPO (estilo
 * Outlook), embutida na propria janela (nao e mais uma janela separada).
 *
 * HERANCA (Cap. 4/13): "é-um" JPanel.
 *
 * Oferece dois modos (botoes de alternancia - Cap. 13): "Dia" (uma coluna) e
 * "Semana" (Dom..Sab, sete colunas), com navegacao < / > e botao "Hoje". O
 * desenho dos cards fica a cargo do PainelLinhaTempo; aqui montamos a barra de
 * controle, o cabecalho fixo das colunas e a rolagem.
 */
public class PainelAgenda extends JPanel {

    private static final String[] DIAS_LONGOS = {
        "Domingo", "Segunda", "Terca", "Quarta", "Quinta", "Sexta", "Sabado"
    };
    private static final String[] DIAS_CURTOS = { "Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sab" };
    private static final String[] NOMES_MES = {
        "Janeiro", "Fevereiro", "Marco", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };

    private final Frame dono;                 // janela dona dos dialogos (modal)
    private final GerenciadorEventos gerenciador;
    private final Runnable aoAlterarGlobal;   // atualiza calendario/agenda lateral

    private LocalDate referencia;             // dia mostrado (ou dia base da semana)
    private boolean modoSemana;

    private final JLabel rotulo = new JLabel("", SwingConstants.CENTER);
    private final JPanel centro = new JPanel(new BorderLayout());
    private final JToggleButton btnDia = new JToggleButton("Dia");
    private final JToggleButton btnSemana = new JToggleButton("Semana");

    public PainelAgenda(Frame dono, GerenciadorEventos gerenciador,
                        LocalDate dia, Runnable aoAlterarGlobal) {
        this.dono = dono;
        this.gerenciador = gerenciador;
        this.referencia = dia;
        this.modoSemana = false;
        this.aoAlterarGlobal = aoAlterarGlobal;

        setLayout(new BorderLayout(4, 4));
        setPreferredSize(new Dimension(560, 620)); // largura/altura inicial da coluna

        // -------- Barra de controle (NORTE) --------
        JPanel topo = new JPanel(new BorderLayout());

        // Alternancia Dia/Semana (a esquerda).
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(btnDia);
        grupo.add(btnSemana);
        btnDia.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { modoSemana = false; reconstruir(); }
        });
        btnSemana.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { modoSemana = true; reconstruir(); }
        });
        JPanel esquerda = new JPanel();
        esquerda.add(btnDia);
        esquerda.add(btnSemana);

        // Navegacao (no centro).
        JButton anterior = new JButton("<");
        JButton proximo = new JButton(">");
        JButton hoje = new JButton("Hoje");
        anterior.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { navegar(-1); }
        });
        proximo.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { navegar(1); }
        });
        hoje.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { referencia = LocalDate.now(); reconstruir(); }
        });
        rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD, 13f));
        JPanel meio = new JPanel();
        meio.add(anterior);
        meio.add(rotulo);
        meio.add(proximo);
        meio.add(hoje);

        topo.add(esquerda, BorderLayout.WEST);
        topo.add(meio, BorderLayout.CENTER);
        add(topo, BorderLayout.NORTH);

        add(centro, BorderLayout.CENTER);

        reconstruir();
    }

    // ----------------------------- API publica -----------------------------

    /** Mostra a agenda de um dia (chamado pelo calendario ao selecionar um dia). */
    public void mostrarDia(LocalDate dia) {
        this.referencia = dia;
        reconstruir();
    }

    /** Re-desenha a coluna (apos eventos serem criados/editados/excluidos). */
    public void recarregar() {
        reconstruir();
    }

    // ----------------------------- apoio -----------------------------

    /** Anda no tempo: um dia (modo dia) ou sete dias (modo semana). */
    private void navegar(int direcao) {
        referencia = modoSemana ? referencia.plusDays(7L * direcao) : referencia.plusDays(direcao);
        reconstruir();
    }

    /** Domingo da semana que contem a data (calendario comeca em Dom). */
    private LocalDate domingoDaSemana(LocalDate d) {
        int dow = d.getDayOfWeek().getValue() % 7; // Seg=1..Dom=0
        return d.minusDays(dow);
    }

    /** (Re)constroi a linha do tempo conforme o modo atual. */
    private void reconstruir() {
        btnDia.setSelected(!modoSemana);
        btnSemana.setSelected(modoSemana);

        LocalDate[] dias;
        String[] titulos;

        if (modoSemana) {
            LocalDate inicio = domingoDaSemana(referencia);
            dias = new LocalDate[7];
            titulos = new String[7];
            for (int i = 0; i < 7; i++) {
                dias[i] = inicio.plusDays(i);
                titulos[i] = DIAS_CURTOS[i] + " " + dois(dias[i].getDayOfMonth()) + "/" + dois(dias[i].getMonthValue());
            }
            LocalDate fim = inicio.plusDays(6);
            rotulo.setText(dois(inicio.getDayOfMonth()) + "/" + dois(inicio.getMonthValue())
                    + " - " + dois(fim.getDayOfMonth()) + "/" + dois(fim.getMonthValue()) + "/" + fim.getYear());
        } else {
            dias = new LocalDate[]{ referencia };
            int dow = referencia.getDayOfWeek().getValue() % 7;
            titulos = new String[]{
                DIAS_LONGOS[dow] + ", " + dois(referencia.getDayOfMonth()) + "/" + dois(referencia.getMonthValue())
            };
            rotulo.setText(DIAS_LONGOS[dow] + ", " + dois(referencia.getDayOfMonth()) + " de "
                    + NOMES_MES[referencia.getMonthValue() - 1] + " de " + referencia.getYear());
        }

        final PainelLinhaTempo linha = new PainelLinhaTempo(dono, gerenciador, dias, aoAlterarGlobal);
        final JScrollPane rolagem = new JScrollPane(linha);
        rolagem.setColumnHeaderView(linha.criarCabecalho(titulos)); // cabecalho fixo no topo
        rolagem.getVerticalScrollBar().setUnitIncrement(16);

        centro.removeAll();
        centro.add(rolagem, BorderLayout.CENTER);
        centro.revalidate();
        centro.repaint();

        // Posiciona a rolagem por volta das 7h (foco no horario comercial).
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                rolagem.getVerticalScrollBar().setValue(7 * PainelLinhaTempo.ALTURA_HORA);
            }
        });
    }

    private static String dois(int v) { return String.format("%02d", v); }
}
