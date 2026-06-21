package eventplanner.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * Pequeno ICONE que pinta uma faixa colorida para CADA categoria presente num
 * dia do calendario. Quando o dia tem eventos de mais de uma categoria, todas
 * as cores aparecem lado a lado (requisito "mostrar as cores de todas as
 * categorias").
 *
 * HERANCA/INTERFACE (Cap. 8): implementa javax.swing.Icon, o "papel" que o
 * Swing usa para desenhar um icone dentro de um botao/rotulo.
 */
public class IconeCategorias implements Icon {

    private final Color[] cores;   // uma cor por categoria presente
    private final int largura;
    private final int altura;

    public IconeCategorias(Color[] cores, int largura, int altura) {
        this.cores = cores;
        this.largura = largura;
        this.altura = altura;
    }

    @Override public int getIconWidth() { return largura; }
    @Override public int getIconHeight() { return altura; }

    /** Divide a largura em faixas iguais, uma por cor. */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        int n = cores.length;
        if (n == 0) return;
        int faixa = largura / n;
        for (int i = 0; i < n; i++) {
            g.setColor(cores[i]);
            int fx = x + i * faixa;
            int w = (i == n - 1) ? (largura - i * faixa) : faixa; // ultima fecha o resto
            g.fillRect(fx, y, w, altura);
        }
        g.setColor(Color.GRAY);
        g.drawRect(x, y, largura - 1, altura - 1);
    }
}
