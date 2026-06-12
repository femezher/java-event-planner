package eventplanner.excecao;

/**
 * Lancada quando o titulo do evento esta vazio.
 * HERANCA (Cap. 8): "é-uma" ValidacaoException.
 */
public class TituloVazioException extends ValidacaoException {

    public TituloVazioException() {
        // Mensagem fixa e amigavel (sem stack trace para o usuario - Cap. 10).
        super("O titulo do evento nao pode ficar vazio.");
    }
}
