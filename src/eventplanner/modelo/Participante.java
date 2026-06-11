package eventplanner.modelo;

/**
 * Participante de um evento: um nome e um e-mail.
 * Atende ao opcional "lista de participantes (nome + e-mail)".
 *
 * Classe simples de DADOS (parte do "Model" no MVC - Cap. 13):
 * atributos privados + metodos accessor/mutator (Cap. 3).
 */
public class Participante {

    // Encapsulamento: atributos privados, acessados via metodos.
    private String nome;
    private String email;

    /** Construtor: inicializa as variaveis de instancia (Cap. 3). */
    public Participante(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }

    // ---- Metodos acessores (getters) e modificadores (setters) - Cap. 3 ----
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sobrescreve toString() (Cap. 8): assim o participante se mostra como
     * "Nome <email>" automaticamente dentro de JList, System.out.println, etc.
     */
    @Override
    public String toString() {
        return nome + " <" + email + ">";
    }
}
