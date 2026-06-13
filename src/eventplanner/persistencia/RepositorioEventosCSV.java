package eventplanner.persistencia;

import eventplanner.modelo.Antecedencia;
import eventplanner.modelo.Categoria;
import eventplanner.modelo.Evento;
import eventplanner.modelo.Participante;

import java.io.File;               // associa um arquivo a um objeto (Cap. 4)
import java.io.FileOutputStream;   // fluxo de saida para arquivo (Class13)
import java.io.IOException;
import java.io.PrintStream;        // escreve texto no fluxo (Class13)
import java.time.LocalDate;        // (ALEM das aulas)
import java.time.LocalTime;        // (ALEM das aulas)
import java.util.Scanner;          // leitura de arquivo (Cap. 4)
import java.util.Vector;

/**
 * Implementacao da persistencia em arquivo CSV (texto puro, 1 evento por linha).
 *
 * IMPLEMENTA a interface RepositorioEventos => polimorfismo (Cap. 8):
 * o resto do programa enxerga so "RepositorioEventos".
 *
 * Formato de cada linha (campos separados por virgula):
 *   titulo,data,hora,local,categoria,antecedencia,descricao,participantes
 * onde participantes = "nome:email;nome:email;..."
 * Linhas comecando por '#' sao comentarios e sao ignoradas.
 *
 * Escrita: PrintStream + FileOutputStream + finally (padrao do Class13 "Tudo junto").
 * Leitura: Scanner + File (padrao do Cap. 4 "The File and Scanner Classes").
 */
public class RepositorioEventosCSV implements RepositorioEventos {

    private final File arquivo; // o arquivo onde os eventos ficam guardados

    /** @param caminho ex.: "dados/eventos.csv" */
    public RepositorioEventosCSV(String caminho) {
        this.arquivo = new File(caminho); // File associa o caminho a um objeto (Cap. 4)
    }

    // =========================== ESCRITA (salvar) ===========================
    @Override
    public void salvar(Vector<Evento> eventos) throws IOException {
        // Garante que a pasta (ex.: "dados/") exista antes de escrever.
        File pasta = arquivo.getParentFile();
        if (pasta != null && !pasta.exists()) {
            pasta.mkdirs();
        }

        PrintStream saida = null; // declarado fora do try para fechar no finally
        try {
            // FileOutputStream abre o arquivo para escrita; PrintStream escreve texto.
            // (UTF-8 garante acentuacao correta.)
            saida = new PrintStream(new FileOutputStream(arquivo), false, "UTF-8");

            // Cabecalho explicativo (sera ignorado na leitura por comecar com '#').
            saida.println("# titulo,data,hora,local,categoria,antecedencia,descricao,participantes");

            // Percorre o Vector e grava uma linha por evento.
            for (int i = 0; i < eventos.size(); i++) {
                Evento e = eventos.elementAt(i);   // elementAt: acesso por indice (Class13)
                saida.println(formatarLinha(e));
            }
        } finally {
            // finally SEMPRE executa: fecha o arquivo e libera o recurso (Cap. 10 / Class13).
            if (saida != null) {
                saida.close();
            }
        }
    }

    /** Monta a linha CSV de um evento. */
    private String formatarLinha(Evento e) {
        StringBuilder participantes = new StringBuilder();
        Vector<Participante> ps = e.getParticipantes();
        for (int i = 0; i < ps.size(); i++) {
            if (i > 0) participantes.append(';');
            participantes.append(ps.elementAt(i).getNome())
                         .append(':')
                         .append(ps.elementAt(i).getEmail());
        }

        // data.toString() => "2026-06-25"; hora.toString() => "14:30" (formato ISO).
        return e.getTitulo() + ","
                + e.getData() + ","
                + e.getHora() + ","
                + e.getLocal() + ","
                + e.getCategoria().name() + ","        // name() => "REUNIAO" (nome da constante)
                + e.getAntecedencia().name() + ","
                + e.getDescricao() + ","
                + participantes.toString();
    }

    // =========================== LEITURA (carregar) ===========================
    @Override
    public Vector<Evento> carregar() {
        Vector<Evento> eventos = new Vector<Evento>();

        // Arquivo ausente: comeca com agenda vazia, sem erro (requisito de robustez).
        if (!arquivo.exists()) {
            System.out.println("[info] Arquivo nao encontrado, iniciando agenda vazia: "
                    + arquivo.getPath());
            return eventos;
        }

        Scanner scanner = null;
        try {
            scanner = new Scanner(arquivo, "UTF-8"); // le o arquivo (Cap. 4)
            while (scanner.hasNextLine()) {
                String linha = scanner.nextLine().trim();

                // Pula linhas vazias e comentarios.
                if (linha.isEmpty() || linha.startsWith("#")) {
                    continue;
                }

                // Cada linha e tratada isoladamente: se uma estiver corrompida,
                // ela e ignorada e as demais continuam carregando (robustez).
                try {
                    eventos.add(parseLinha(linha));
                } catch (Exception ex) {
                    System.out.println("[aviso] Linha ignorada (formato invalido): " + linha);
                }
            }
        } catch (IOException e) {
            // Falha ao abrir/ler: avisa no console e devolve o que tiver (nao quebra o app).
            System.out.println("[erro] Nao foi possivel ler o arquivo: " + e.getMessage());
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return eventos;
    }

    /** Converte uma linha CSV em um objeto Evento. */
    private Evento parseLinha(String linha) {
        // split com limite -1 mantem campos vazios no fim (ex.: sem participantes).
        String[] c = linha.split(",", -1);

        // Indices: 0 titulo,1 data,2 hora,3 local,4 categoria,5 antecedencia,6 descricao,7 participantes.
        String titulo = c[0];
        LocalDate data = LocalDate.parse(c[1]);        // (ALEM das aulas) interpreta "2026-06-25"
        LocalTime hora = LocalTime.parse(c[2]);        // (ALEM das aulas) interpreta "14:30"
        String local = c[3];
        Categoria categoria = Categoria.valueOf(c[4]); // valueOf: texto -> constante do enum
        Antecedencia antecedencia = Antecedencia.valueOf(c[5]);
        String descricao = c[6];

        Evento e = new Evento(titulo, data, hora, local, descricao, categoria, antecedencia);

        // Campo de participantes (opcional).
        if (c.length > 7 && !c[7].isEmpty()) {
            String[] itens = c[7].split(";");
            for (String item : itens) {
                String[] nomeEmail = item.split(":", 2); // 2 = no maximo 2 pedacos
                if (nomeEmail.length == 2) {
                    e.addParticipante(new Participante(nomeEmail[0], nomeEmail[1]));
                }
            }
        }
        return e;
    }
}
