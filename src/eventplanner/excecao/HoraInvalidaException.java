package eventplanner.excecao;

/**
 * Lancada quando a hora informada e invalida (fora de 00:00..23:59 ou nao numerica).
 * HERANCA (Cap. 8): "é-uma" ValidacaoException.
 */
public class HoraInvalidaException extends ValidacaoException {

    public HoraInvalidaException(String detalhe) {
        super("Hora invalida: " + detalhe);
    }
}
