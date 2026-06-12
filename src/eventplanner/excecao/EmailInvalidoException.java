package eventplanner.excecao;

/**
 * Lancada quando o e-mail de um participante e invalido (sem "@").
 * HERANCA (Cap. 8): "é-uma" ValidacaoException.
 */
public class EmailInvalidoException extends ValidacaoException {

    public EmailInvalidoException(String email) {
        super("E-mail invalido: \"" + email + "\". Use o formato nome@dominio.");
    }
}
