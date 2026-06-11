package eventplanner.modelo;

import java.awt.Color;

/**
 * Categoria de um evento (reuniao, aniversario, compromisso).
 *
 * Cada categoria carrega um rotulo legivel e uma COR, atendendo ao opcional
 * "categorias coloridas" do enunciado.
 *
 * OBS (recurso ALEM das aulas): usamos um ENUM. Enums nao aparecem nos slides;
 * nas aulas valores fixos seriam "public static final int". Escolhemos enum
 * porque ele agrupa, de forma segura, o conjunto fixo de categorias junto com
 * os dados de cada uma (rotulo + cor) e ja impede valores invalidos.
 */
public enum Categoria {

    // Cada constante chama o construtor do enum passando rotulo e cor.
    REUNIAO("Reuniao", new Color(0x42, 0x85, 0xF4)),      // azul
    ANIVERSARIO("Aniversario", new Color(0xEA, 0x43, 0x35)), // vermelho
    COMPROMISSO("Compromisso", new Color(0x34, 0xA8, 0x53)); // verde

    // Atributos privados (encapsulamento - Cap. 3 / Class Definition).
    private final String rotulo;
    private final Color cor;

    // Construtor do enum: inicializa os atributos de cada constante.
    Categoria(String rotulo, Color cor) {
        this.rotulo = rotulo;
        this.cor = cor;
    }

    // Metodo acessor (accessor/getter - Cap. 3).
    public String getRotulo() {
        return rotulo;
    }

    public Color getCor() {
        return cor;
    }

    /**
     * Sobrescreve toString() (polimorfismo - Cap. 8) para que a categoria
     * apareca com o rotulo amigavel em listas e combos da interface.
     */
    @Override
    public String toString() {
        return rotulo;
    }
}
