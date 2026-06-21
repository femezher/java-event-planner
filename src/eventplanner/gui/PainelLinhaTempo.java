package eventplanner.gui;

import eventplanner.modelo.Evento;
import eventplanner.servico.GerenciadorEventos;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.ToolTipManager;

/**
 * LINHA DO TEMPO (estilo "visualizacao de dia/semana" do Outlook).
 *
 * A esquerda fica a regua das horas (00:00..23:00); a direita, uma ou mais
 * colunas (1 = dia, 7 = semana). Cada evento vira um CARD colorido pela sua
 * categoria, posicionado na altura correspondente ao seu horario. Eventos que
 * se sobrepoem sao divididos em subcolunas lado a lado.
 *
 * HERANCA (Cap. 4/13): "é-um" JPanel.
 * IMPLEMENTA Scrollable (interface - Cap. 8) para se comportar bem dentro de um
 * JScrollPane (rolagem suave; a visao de dia ocupa toda a largura disponivel).
 *
 * DESENHO: sobrescreve paintComponent (Cap. 13 "Drawing"), usando Graphics2D
 * para desenhar a grade, os rotulos das horas e os retangulos dos eventos.
 *
 * OBS (ALEM das aulas): LocalDate/LocalTime fazem a matematica de datas/horas.
 */
public class PainelLinhaTempo extends JPanel implements Scrollable {

    // ---- Constantes de layout (package-private p/ a JanelaAgenda reutilizar) ----
    static final int HORA_INICIO = 0;        // primeira hora exibida
    static final int HORA_FIM = 24;          // ultima linha (00:00 do dia seguinte)
    static final int ALTURA_HORA = 46;       // pixels por hora
    static final int LARGURA_GUTTER = 58;    // faixa das horas (lado esquerdo)
    private static final int DURACAO_MIN = 60; // duracao assumida de cada evento (so ha hora inicial)
    private static final int LARGURA_COLUNA_MIN = 150;

    private final Frame dono;                 // janela dona dos dialogos
    private final GerenciadorEventos gerenciador;
    private final LocalDate[] dias;           // 1 coluna (dia) ou 7 (semana)
    private final Runnable aoAlterar;         // callback p/ atualizar o resto da app

    // Retangulos desenhados nesta pintura (para tooltip e clique).
    private final ArrayList<Card> cards = new ArrayList<Card>();

    public PainelLinhaTempo(Frame dono, GerenciadorEventos gerenciador,
                            LocalDate[] dias, Runnable aoAlterar) {
        this.dono = dono;
        this.gerenciador = gerenciador;
        this.dias = dias;
        this.aoAlterar = aoAlterar;
        setBackground(Color.WHITE);
        ToolTipManager.sharedInstance().registerComponent(this); // habilita tooltips dinamicos

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { aoClicar(e); }
        });
    }

    // ----------------------------- geometria -----------------------------

    private int larguraColuna() {
        int util = getWidth() - LARGURA_GUTTER;
        return Math.max(60, util / dias.length);
    }
    private int xColuna(int i) { return LARGURA_GUTTER + i * larguraColuna(); }
    private int alturaTotal() { return (HORA_FIM - HORA_INICIO) * ALTURA_HORA; }

    @Override public Dimension getPreferredSize() {
        return new Dimension(LARGURA_GUTTER + dias.length * LARGURA_COLUNA_MIN, alturaTotal());
    }

    // ----------------------------- desenho -----------------------------

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        cards.clear();

        int largura = getWidth();
        int colW = larguraColuna();

        // Linhas das horas + rotulos na regua da esquerda.
        g.setFont(getFont().deriveFont(11f));
        for (int h = HORA_INICIO; h <= HORA_FIM; h++) {
            int y = (h - HORA_INICIO) * ALTURA_HORA;
            g.setColor(new Color(0xE0, 0xE0, 0xE0));
            g.drawLine(LARGURA_GUTTER, y, largura, y);
            if (h < HORA_FIM) {
                g.setColor(new Color(0x70, 0x70, 0x70));
                g.drawString(String.format("%02d:00", h), 8, y + 14);
            }
        }

        // Separadores verticais entre as colunas.
        g.setColor(new Color(0xCC, 0xCC, 0xCC));
        for (int i = 0; i <= dias.length; i++) {
            int x = LARGURA_GUTTER + i * colW;
            g.drawLine(x, 0, x, alturaTotal());
        }

        // Cards de cada coluna (dia).
        for (int c = 0; c < dias.length; c++) {
            desenharColuna(g, c, colW);
        }
    }

    /** Desenha os eventos de uma coluna, resolvendo sobreposicoes em subcolunas. */
    private void desenharColuna(Graphics2D g, int c, int colW) {
        Vector<Evento> doDia = gerenciador.eventosDoDia(dias[c]);
        ArrayList<Evento> lista = new ArrayList<Evento>(doDia);
        lista.sort((a, b) -> a.getHora().compareTo(b.getHora())); // por horario

        int n = lista.size();
        int[] subCol = new int[n]; // em qual subcoluna cada evento fica
        int[] subTot = new int[n]; // quantas subcolunas tem o grupo do evento

        // Agrupa eventos que se sobrepoem e aloca subcolunas (algoritmo guloso).
        int i = 0;
        while (i < n) {
            int maxFim = minutos(lista.get(i)) + DURACAO_MIN;
            int j = i + 1;
            while (j < n && minutos(lista.get(j)) < maxFim) {
                maxFim = Math.max(maxFim, minutos(lista.get(j)) + DURACAO_MIN);
                j++;
            }
            // grupo = [i, j): aloca cada evento na primeira subcoluna livre.
            ArrayList<Integer> fimDaSub = new ArrayList<Integer>();
            for (int k = i; k < j; k++) {
                int ini = minutos(lista.get(k));
                int col = -1;
                for (int s = 0; s < fimDaSub.size(); s++) {
                    if (fimDaSub.get(s) <= ini) { col = s; break; }
                }
                if (col == -1) { col = fimDaSub.size(); fimDaSub.add(0); }
                fimDaSub.set(col, ini + DURACAO_MIN);
                subCol[k] = col;
            }
            int tot = Math.max(1, fimDaSub.size());
            for (int k = i; k < j; k++) subTot[k] = tot;
            i = j;
        }

        for (int k = 0; k < n; k++) {
            Evento e = lista.get(k);
            int ini = minutos(e);
            int subW = (colW - 4) / subTot[k];
            int x = xColuna(c) + 2 + subCol[k] * subW;
            int w = subW - 3;
            int y = (int) Math.round((ini / 60.0 - HORA_INICIO) * ALTURA_HORA);
            int hh = (int) Math.round((DURACAO_MIN / 60.0) * ALTURA_HORA) - 2;

            Color cor = e.getCategoria().getCor();
            g.setColor(cor);
            g.fillRoundRect(x, y, w, hh, 8, 8);
            g.setColor(cor.darker());
            g.drawRoundRect(x, y, w, hh, 8, 8);
            g.fillRect(x, y, 4, hh); // barrinha lateral mais escura

            // Texto do card (recortado para nao vazar).
            Shape clipAntigo = g.getClip();
            g.setClip(x + 2, y, w - 4, hh);
            g.setColor(Color.WHITE);
            Font base = getFont();
            g.setFont(base.deriveFont(Font.BOLD, 11f));
            g.drawString(e.getHora().toString() + "  " + e.getTitulo(), x + 7, y + 14);
            if (e.getLocal() != null && !e.getLocal().isEmpty()) {
                g.setFont(base.deriveFont(10f));
                g.drawString(e.getLocal(), x + 7, y + 27);
            }
            g.setClip(clipAntigo);

            cards.add(new Card(new Rectangle(x, y, w, hh), e));
        }
    }

    private int minutos(Evento e) {
        LocalTime t = e.getHora();
        return t.getHour() * 60 + t.getMinute();
    }

    // ----------------------------- interacao -----------------------------

    @Override
    public String getToolTipText(MouseEvent ev) {
        Evento e = eventoEm(ev.getPoint());
        if (e == null) return null;
        return "<html><b>" + escape(e.getTitulo()) + "</b><br>"
                + "Hora: " + e.getHora() + "<br>"
                + "Local: " + escape(e.getLocal()) + "<br>"
                + "Categoria: " + e.getCategoria().getRotulo() + "</html>";
    }

    private Evento eventoEm(Point p) {
        for (int i = cards.size() - 1; i >= 0; i--) {
            if (cards.get(i).r.contains(p)) return cards.get(i).e;
        }
        return null;
    }

    /** Duplo-clique: sobre um card edita; em area vazia cria evento naquele dia. */
    private void aoClicar(MouseEvent ev) {
        if (ev.getClickCount() != 2) return;
        Evento e = eventoEm(ev.getPoint());
        if (e != null) {
            DialogoEvento d = new DialogoEvento(dono, e.getData(), e);
            d.setVisible(true);
            Evento editado = d.getEventoResultado();
            if (editado != null) aplicar(() -> gerenciador.editar(e, editado));
        } else {
            int c = (ev.getX() - LARGURA_GUTTER) / Math.max(1, larguraColuna());
            if (c < 0 || c >= dias.length) return;
            DialogoEvento d = new DialogoEvento(dono, dias[c], null);
            d.setVisible(true);
            Evento novo = d.getEventoResultado();
            if (novo != null) aplicar(() -> gerenciador.adicionar(novo));
        }
    }

    /** Executa uma alteracao (add/editar) tratando a validacao e atualizando a tela. */
    private void aplicar(AcaoComExcecao acao) {
        try {
            acao.run();
            if (aoAlterar != null) aoAlterar.run();
            repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Dados invalidos", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Pequena interface funcional para encapsular uma acao que pode lancar excecao. */
    private interface AcaoComExcecao { void run() throws Exception; }

    private static String escape(String s) {
        return s == null ? "" : s.replace("<", "&lt;");
    }

    // ----------------------------- cabecalho -----------------------------

    /**
     * Cria o componente de CABECALHO das colunas (datas/dias) para ser usado como
     * columnHeaderView do JScrollPane, ficando fixo durante a rolagem vertical.
     */
    JComponent criarCabecalho(String[] titulos) {
        return new Cabecalho(titulos);
    }

    private class Cabecalho extends JPanel {
        private final String[] titulos;
        Cabecalho(String[] titulos) {
            this.titulos = titulos;
            setBackground(new Color(0xF2, 0xF2, 0xF2));
            setPreferredSize(new Dimension(10, 34));
        }
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int colW = larguraColuna(); // mesma largura do corpo (mesma largura no scrollpane)
            g.setColor(new Color(0xCC, 0xCC, 0xCC));
            g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            g.setFont(getFont().deriveFont(Font.BOLD, 12f));
            FontMetrics fm = g.getFontMetrics();
            for (int i = 0; i < titulos.length; i++) {
                int x = LARGURA_GUTTER + i * colW;
                g.setColor(new Color(0xCC, 0xCC, 0xCC));
                g.drawLine(x, 0, x, getHeight());
                g.setColor(Color.DARK_GRAY);
                int tw = fm.stringWidth(titulos[i]);
                g.drawString(titulos[i], x + Math.max(4, (colW - tw) / 2), 21);
            }
        }
    }

    // guarda um retangulo desenhado e o evento correspondente
    private static class Card {
        final Rectangle r;
        final Evento e;
        Card(Rectangle r, Evento e) { this.r = r; this.e = e; }
    }

    // ----------------------------- Scrollable -----------------------------

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle v, int orient, int dir) { return 16; }
    @Override public int getScrollableBlockIncrement(Rectangle v, int orient, int dir) { return ALTURA_HORA * 3; }
    // Na visao de DIA (1 coluna) o painel ocupa toda a largura; na de semana, rola.
    @Override public boolean getScrollableTracksViewportWidth() { return dias.length == 1; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}
