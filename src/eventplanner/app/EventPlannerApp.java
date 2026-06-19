package eventplanner.app;

import eventplanner.gui.JanelaPrincipal;
import eventplanner.modelo.Evento;
import eventplanner.persistencia.RepositorioEventos;
import eventplanner.persistencia.RepositorioEventosCSV;
import eventplanner.servico.GerenciadorEventos;

import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Ponto de entrada da aplicacao (metodo main).
 *
 * Sequencia de inicializacao:
 *  1. cria o repositorio (CSV) e o gerenciador (que ja carrega os eventos salvos);
 *  2. abre a janela principal;
 *  3. mostra os LEMBRETES das proximas 24h num JOptionPane (Cap. 10) - sem thread,
 *     conforme a versao Compacta (lembretes so na inicializacao).
 *
 * Observacao de design: a interface grafica do Swing deve ser criada na
 * "thread de eventos" (EDT). Por isso usamos SwingUtilities.invokeLater.
 * OBS (ALEM das aulas): invokeLater nao aparece nos slides (que fazem
 * "new Janela()" direto no main); usamos por ser a forma correta/segura.
 */
public class EventPlannerApp {

    public static void main(String[] args) {

        // Look and Feel multiplataforma (Metal). O Cap. 13 mostra exatamente
        // UIManager.setLookAndFeel(...). Aqui garante que as cores de fundo dos
        // botoes do calendario aparecam igual em qualquer sistema operacional.
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // Se falhar, segue com o look padrao (nao e critico).
        }

        // Camada de persistencia (interface) + implementacao concreta (CSV).
        RepositorioEventos repositorio = new RepositorioEventosCSV("dados/eventos.csv");
        // O gerenciador ja carrega os eventos do arquivo no construtor.
        final GerenciadorEventos gerenciador = new GerenciadorEventos(repositorio);

        // Cria e mostra a GUI na thread correta (classe interna anonima - Cap. 13).
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JanelaPrincipal janela = new JanelaPrincipal(gerenciador);
                janela.setSize(720, 460);
                janela.setVisible(true);

                // Lembretes das proximas 24h.
                Vector<Evento> lembretes = gerenciador.lembretesProximos();
                if (!lembretes.isEmpty()) {
                    StringBuilder sb = new StringBuilder("Lembretes para as proximas 24h:\n\n");
                    for (int i = 0; i < lembretes.size(); i++) {
                        sb.append("  - ").append(lembretes.elementAt(i).toString()).append("\n");
                    }
                    JOptionPane.showMessageDialog(janela, sb.toString(),
                            "Lembretes", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
    }
}
