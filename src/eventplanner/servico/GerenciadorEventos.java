package eventplanner.servico;

import eventplanner.excecao.DataInvalidaException;
import eventplanner.excecao.EmailInvalidoException;
import eventplanner.excecao.HoraInvalidaException;
import eventplanner.excecao.TituloVazioException;
import eventplanner.excecao.ValidacaoException;
import eventplanner.modelo.Evento;
import eventplanner.modelo.Participante;
import eventplanner.persistencia.RepositorioEventos;

import java.io.IOException;
import java.time.LocalDate;        // (ALEM das aulas)
import java.time.LocalDateTime;    // (ALEM das aulas)
import java.util.Vector;

/**
 * "Cerebro" da aplicacao (papel de CONTROLLER no MVC - Cap. 13).
 *
 * Responsabilidades:
 *  - manter a colecao de eventos em memoria (Vector - Cap.10/Class13);
 *  - VALIDAR dados, lancando excecoes proprias (Cap. 10);
 *  - buscar eventos (por dia e por texto);
 *  - calcular os lembretes das proximas 24h;
 *  - delegar salvar/carregar para o RepositorioEventos (interface - Cap. 8).
 *
 * A GUI conversa SO com esta classe; nunca mexe em arquivo diretamente.
 */
public class GerenciadorEventos {

    private final RepositorioEventos repositorio; // interface (polimorfismo)
    private final Vector<Evento> eventos;         // colecao em memoria

    /**
     * Ao criar o gerenciador, ja carregamos os eventos salvos (robustez:
     * carregar() nunca quebra, no pior caso devolve lista vazia).
     */
    public GerenciadorEventos(RepositorioEventos repositorio) {
        this.repositorio = repositorio;
        this.eventos = repositorio.carregar();
    }

    // ============================ OPERACOES CRUD ============================

    /** Adiciona um evento NOVO, apos validar. */
    public void adicionar(Evento e) throws ValidacaoException {
        validar(e);
        eventos.add(e);
    }

    /**
     * Substitui um evento existente por uma versao editada.
     * Se o antigo nao for encontrado, simplesmente adiciona o novo.
     */
    public void editar(Evento antigo, Evento novo) throws ValidacaoException {
        validar(novo);
        int i = eventos.indexOf(antigo); // usa equals padrao (mesma referencia)
        if (i >= 0) {
            eventos.set(i, novo);
        } else {
            eventos.add(novo);
        }
    }

    /** Remove um evento da colecao. */
    public void remover(Evento e) {
        eventos.remove(e);
    }

    /** Devolve TODOS os eventos (referencia da colecao interna). */
    public Vector<Evento> getEventos() {
        return eventos;
    }

    // ============================ CONSULTAS ============================

    /**
     * Eventos de um dia especifico.
     * (Comparacao de datas com LocalDate.equals - ALEM das aulas.)
     */
    public Vector<Evento> eventosDoDia(LocalDate dia) {
        Vector<Evento> resultado = new Vector<Evento>();
        for (int i = 0; i < eventos.size(); i++) {
            Evento e = eventos.elementAt(i);
            if (e.getData().equals(dia)) {
                resultado.add(e);
            }
        }
        return resultado;
    }

    /** True se houver pelo menos um evento naquele dia (usado para destacar no calendario). */
    public boolean temEventoNoDia(LocalDate dia) {
        return !eventosDoDia(dia).isEmpty();
    }

    /**
     * Busca por palavra-chave no titulo, descricao ou local
     * (requisito "buscar por palavra-chave"). Ignora maiusculas/minusculas.
     */
    public Vector<Evento> buscarPorTexto(String termo) {
        Vector<Evento> resultado = new Vector<Evento>();
        if (termo == null) return resultado;
        String alvo = termo.toLowerCase();
        for (int i = 0; i < eventos.size(); i++) {
            Evento e = eventos.elementAt(i);
            String campos = (e.getTitulo() + " " + e.getDescricao() + " " + e.getLocal()).toLowerCase();
            if (campos.contains(alvo)) {
                resultado.add(e);
            }
        }
        return resultado;
    }

    /**
     * Lembretes a exibir na inicializacao: eventos cujo INSTANTE DE LEMBRETE
     * (data/hora do evento menos a antecedencia) cai entre AGORA e AGORA + 24h.
     *
     * OBS (ALEM das aulas): LocalDateTime.now() pega o instante atual;
     * isBefore/isAfter comparam instantes. Sem isso, precisariamos calcular
     * datas manualmente (origem comum de bugs).
     */
    public Vector<Evento> lembretesProximos() {
        Vector<Evento> resultado = new Vector<Evento>();
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limite = agora.plusHours(24);
        for (int i = 0; i < eventos.size(); i++) {
            Evento e = eventos.elementAt(i);
            LocalDateTime lembrete = e.getInstanteLembrete();
            // dentro da janela [agora, agora+24h]
            if (!lembrete.isBefore(agora) && !lembrete.isAfter(limite)) {
                resultado.add(e);
            }
        }
        return resultado;
    }

    // ============================ PERSISTENCIA ============================

    /** Salva tudo em disco (pode lancar IOException - Cap. 10). */
    public void salvar() throws IOException {
        repositorio.salvar(eventos);
    }

    // ============================ VALIDACAO ============================

    /**
     * Valida um evento ANTES de aceita-lo. Lanca a excecao especifica para que
     * a GUI mostre a mensagem certa via JOptionPane (Cap. 10).
     */
    private void validar(Evento e) throws ValidacaoException {
        // Titulo nao pode ser vazio.
        if (e.getTitulo() == null || e.getTitulo().trim().isEmpty()) {
            throw new TituloVazioException();
        }
        // Data e hora precisam existir (a conversao de numeros para data ja
        // acontece na GUI; aqui garantimos que nao vieram nulas).
        if (e.getData() == null) {
            throw new DataInvalidaException("a data nao foi informada.");
        }
        if (e.getHora() == null) {
            throw new HoraInvalidaException("a hora nao foi informada.");
        }
        // Cada participante precisa de e-mail valido (contem "@").
        Vector<Participante> ps = e.getParticipantes();
        for (int i = 0; i < ps.size(); i++) {
            String email = ps.elementAt(i).getEmail();
            if (email == null || !email.contains("@")) {
                throw new EmailInvalidoException(email);
            }
        }
    }
}
