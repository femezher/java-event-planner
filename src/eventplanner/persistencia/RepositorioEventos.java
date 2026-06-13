package eventplanner.persistencia;

import eventplanner.modelo.Evento;
import java.io.IOException;
import java.util.Vector;

/**
 * Contrato (INTERFACE) de quem sabe guardar e recuperar eventos.
 *
 * Por que interface? Cap. 8 ("Interfaces or Abstract Classes?") recomenda usar
 * interfaces para definir PAPEIS, aumentando flexibilidade e extensibilidade.
 * Hoje guardamos em CSV (RepositorioEventosCSV); amanha poderiamos criar um
 * RepositorioEventosBD sem mudar nada no resto do programa (polimorfismo).
 *
 * Lembrete (Class13): toda interface so tem metodos abstratos e publicos.
 */
public interface RepositorioEventos {

    /**
     * Salva todos os eventos.
     * Declara "throws IOException" porque escrever em disco pode falhar
     * (excecao CHECKED - Cap. 10 "Checked Exceptions").
     */
    void salvar(Vector<Evento> eventos) throws IOException;

    /**
     * Carrega os eventos do armazenamento.
     * NAO lanca excecao: se o arquivo nao existe ou esta corrompido, devolve o
     * que conseguiu ler (no minimo um Vector vazio) - requisito "tolerar arquivo
     * ausente/corrompido".
     */
    Vector<Evento> carregar();
}
