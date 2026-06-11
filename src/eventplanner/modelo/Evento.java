package eventplanner.modelo;

import java.time.LocalDate;     // data sem hora  (ALEM das aulas)
import java.time.LocalTime;     // hora sem data  (ALEM das aulas)
import java.time.LocalDateTime; // data + hora    (ALEM das aulas)
import java.util.Vector;        // colecao dinamica (VISTO nas aulas - Cap. 10 / Class13)

/**
 * Evento da agenda. E a classe central do "Model".
 *
 * Guarda: titulo, data, hora, local, descricao, categoria, antecedencia do
 * lembrete e a lista de participantes.
 *
 * Conceitos de POO:
 *  - Encapsulamento: todos os atributos sao private (Cap. 3 / Class Definition).
 *  - COMPOSICAO: um Evento "TEM UMA" Categoria, "TEM UMA" Antecedencia e
 *    "TEM UM" Vector<Participante>. (Composicao = "tem-um"; herança = "é-um".)
 *
 * OBS (ALEM das aulas): java.time.LocalDate/LocalTime/LocalDateTime sao usados
 * para representar datas/horas com seguranca. Nas aulas nao houve API de datas;
 * por isso cada uso vem comentado.
 */
public class Evento {

    private String titulo;
    private LocalDate data;            // ex.: 2026-06-25
    private LocalTime hora;            // ex.: 14:30
    private String local;
    private String descricao;
    private Categoria categoria;
    private Antecedencia antecedencia;
    private Vector<Participante> participantes; // Vector visto em Cap.10/Class13

    /**
     * Construtor: recebe os dados externos por parametro e inicializa o estado
     * do objeto (Cap. 3 "Constructor Methods"). A lista de participantes comeca
     * vazia e e preenchida depois com addParticipante().
     */
    public Evento(String titulo, LocalDate data, LocalTime hora, String local,
                  String descricao, Categoria categoria, Antecedencia antecedencia) {
        this.titulo = titulo;
        this.data = data;
        this.hora = hora;
        this.local = local;
        this.descricao = descricao;
        this.categoria = categoria;
        this.antecedencia = antecedencia;
        this.participantes = new Vector<Participante>(); // cria a colecao vazia
    }

    // -------------------- Acessores / Modificadores (Cap. 3) --------------------
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public Antecedencia getAntecedencia() { return antecedencia; }
    public void setAntecedencia(Antecedencia antecedencia) { this.antecedencia = antecedencia; }

    public Vector<Participante> getParticipantes() { return participantes; }

    /** Adiciona um participante a colecao (delega ao Vector - Cap.10/Class13). */
    public void addParticipante(Participante p) {
        participantes.add(p);
    }

    /**
     * Junta data + hora num unico instante (LocalDateTime).
     * OBS (ALEM das aulas): LocalDateTime.of combina a data e a hora.
     */
    public LocalDateTime getDataHora() {
        return LocalDateTime.of(data, hora);
    }

    /**
     * Instante em que o lembrete deve disparar = (data e hora do evento) menos
     * a antecedencia escolhida.
     * OBS (ALEM das aulas): .minus(Duration) subtrai um intervalo de tempo.
     */
    public LocalDateTime getInstanteLembrete() {
        return getDataHora().minus(antecedencia.getDuracao());
    }

    /**
     * Sobrescreve toString() (polimorfismo - Cap. 8): descreve o evento numa
     * linha, util para listas e depuracao.
     */
    @Override
    public String toString() {
        return titulo + " (" + data + " " + hora + ") - " + local
                + " [" + categoria.getRotulo() + "]";
    }
}
