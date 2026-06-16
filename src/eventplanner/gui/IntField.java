package eventplanner.gui;

import javax.swing.JTextField;

/**
 * Campo de texto que so faz sentido com numeros inteiros.
 *
 * HERANCA de componente Swing (Cap. 10 "Fix the Error and Resume"):
 * IntField "é-um" JTextField, herdando todo o comportamento de campo de texto
 * e adicionando apenas o metodo getInt().
 *
 * Usado no DialogoEvento para dia, mes, ano, hora e minuto. Se o usuario digitar
 * algo que nao e numero, getInt() lanca NumberFormatException, que o dialogo
 * captura e transforma numa mensagem amigavel (Cap. 10).
 */
public class IntField extends JTextField {

    /** Construtor com largura em colunas (repassa para a superclasse - super, Cap. 8). */
    public IntField(int colunas) {
        super(colunas);
    }

    /**
     * Devolve o conteudo como int.
     * @throws NumberFormatException se o texto nao for um inteiro valido
     *         (Integer.parseInt lanca essa excecao - Cap. 4 / Cap. 10).
     */
    public int getInt() throws NumberFormatException {
        return Integer.parseInt(getText().trim());
    }
}
