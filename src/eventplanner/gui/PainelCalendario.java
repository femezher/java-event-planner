package eventplanner.gui;

import eventplanner.modelo.Evento;
import eventplanner.servico.GerenciadorEventos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;  // (ALEM das aulas)
import java.time.YearMonth;  // (ALEM das aulas) representa "mes/ano"
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Painel do CALENDARIO MENSAL (lado esquerdo da janela).
 *
 * HERANCA (Cap. 4/13): "é-um" JPanel.
 * Usa GridLayout (Cap. 13 "The GridLayout Manager") para montar a grade de 7
 * colunas (Dom..Sab) e BorderLayout para a barra de navegacao no topo.
 *
 * Destaca os dias que tem evento (com a cor da categoria - opcional "categorias
 * coloridas") e marca o dia de hoje com uma borda.
 *
 * OBS (ALEM das aulas): YearMonth/LocalDate fazem a "matematica" do calendario
 * (quantos dias o mes tem, em que coluna cai o dia 1). Sem isso, seria preciso
 * calcular dia-da-semana e ano bissexto na mao.
 */
public class PainelCalendario extends JPanel {

    /**
     * "Ouvinte" avisado quando o usuario clica num dia.
     * E uma INTERFACE (define um papel - Cap. 8): a JanelaPrincipal a implementa
     * para reagir ao clique mostrando os eventos do dia.
     */
    public interface OuvinteDeDia {
        void diaSelecionado(LocalDate dia);
    }

    // Nomes em portugues (evita depender de configuracao regional do sistema).
    private static final String[] NOMES_MES = {
        "Janeiro", "Fevereiro", "Marco", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };
    private static final String[] DIAS_SEMANA = { "Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sab" };

    private final GerenciadorEventos gerenciador;
    private final OuvinteDeDia ouvinte;

    private YearMonth mesAtual;        // mes exibido no momento
    private final JLabel rotuloMes;    // texto "Junho 2026"
    private final JPanel grade;        // onde ficam os botoes dos dias

    public PainelCalendario(GerenciadorEventos gerenciador, OuvinteDeDia ouvinte) {
        this.gerenciador = gerenciador;
        this.ouvinte = ouvinte;
        this.mesAtual = YearMonth.now(); // mes corrente (ALEM das aulas)

        setLayout(new BorderLayout());

        // ---------- Barra de navegacao (NORTE): < , mes , > , Hoje ----------
        JPanel navegacao = new JPanel(new BorderLayout());
        JButton botaoAnterior = new JButton("<");
        JButton botaoProximo = new JButton(">");
        JButton botaoHoje = new JButton("Hoje"); // opcional "botao Hoje"
        rotuloMes = new JLabel("", SwingConstants.CENTER);

        // Listeners de navegacao (evento de botao - Cap. 4).
        botaoAnterior.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                mesAtual = mesAtual.minusMonths(1); // mes anterior
                atualizar();
            }
        });
        botaoProximo.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                mesAtual = mesAtual.plusMonths(1); // proximo mes
                atualizar();
            }
        });
        botaoHoje.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                mesAtual = YearMonth.now();
                atualizar();
                ouvinte.diaSelecionado(LocalDate.now()); // mostra os eventos de hoje
            }
        });

        JPanel topoDireita = new JPanel();
        topoDireita.add(botaoHoje);
        navegacao.add(botaoAnterior, BorderLayout.WEST);
        navegacao.add(rotuloMes, BorderLayout.CENTER);
        navegacao.add(botaoProximo, BorderLayout.EAST);

        JPanel topo = new JPanel(new BorderLayout());
        topo.add(navegacao, BorderLayout.CENTER);
        topo.add(topoDireita, BorderLayout.EAST);
        add(topo, BorderLayout.NORTH);

        // ---------- Centro: cabecalho (Dom..Sab) + grade de dias ----------
        JPanel centro = new JPanel(new BorderLayout());

        JPanel cabecalho = new JPanel(new GridLayout(1, 7));
        for (String d : DIAS_SEMANA) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            cabecalho.add(l);
        }
        centro.add(cabecalho, BorderLayout.NORTH);

        grade = new JPanel(new GridLayout(0, 7)); // 0 linhas = quantas forem necessarias, 7 colunas
        centro.add(grade, BorderLayout.CENTER);

        add(centro, BorderLayout.CENTER);

        atualizar(); // monta o mes inicial
    }

    /**
     * (Re)constroi a grade do mes atual. Chamado ao trocar de mes e sempre que
     * os eventos mudam (para atualizar os destaques).
     */
    public void atualizar() {
        // Rotulo "Mes Ano".
        rotuloMes.setText(NOMES_MES[mesAtual.getMonthValue() - 1] + " " + mesAtual.getYear());

        grade.removeAll(); // limpa os botoes antigos

        LocalDate primeiroDia = mesAtual.atDay(1);          // dia 1 do mes (ALEM das aulas)
        int diasNoMes = mesAtual.lengthOfMonth();           // 28..31 (trata bissexto sozinho)
        // getDayOfWeek().getValue(): segunda=1 ... domingo=7. Queremos domingo=0.
        int colunaInicial = primeiroDia.getDayOfWeek().getValue() % 7;

        // Celulas vazias antes do dia 1, para alinhar na coluna certa.
        for (int i = 0; i < colunaInicial; i++) {
            grade.add(new JLabel(""));
        }

        LocalDate hoje = LocalDate.now();

        // Um botao para cada dia do mes.
        for (int dia = 1; dia <= diasNoMes; dia++) {
            final LocalDate data = mesAtual.atDay(dia); // 'final' p/ usar no listener interno
            JButton botaoDia = new JButton(String.valueOf(dia));
            botaoDia.setOpaque(true); // necessario para a cor de fundo aparecer

            // Destaque: se ha evento no dia, pinta com a cor da categoria do 1o evento.
            Vector<Evento> doDia = gerenciador.eventosDoDia(data);
            if (!doDia.isEmpty()) {
                Color cor = doDia.firstElement().getCategoria().getCor();
                botaoDia.setBackground(cor);
                botaoDia.setForeground(Color.WHITE);
                botaoDia.setToolTipText(doDia.size() + " evento(s)");
            }

            // Marca o dia de hoje com uma borda escura.
            if (data.equals(hoje)) {
                botaoDia.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
            }

            // Ao clicar, avisa o ouvinte (a JanelaPrincipal) qual dia foi escolhido.
            botaoDia.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    ouvinte.diaSelecionado(data);
                }
            });

            grade.add(botaoDia);
        }

        // Recalcula o layout e redesenha (necessario apos add/remove dinamico).
        grade.revalidate();
        grade.repaint();
    }
}
