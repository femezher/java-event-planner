package eventplanner.excecao;

/**
 * Lancada quando a data informada nao existe ou esta em formato invalido
 * (ex.: 30/02, mes 13, texto nao numerico).
 * HERANCA (Cap. 8): "é-uma" ValidacaoException.
 */
public class DataInvalidaException extends ValidacaoException {

    /** @param detalhe explica o que esta errado (ex.: "dia/mes/ano fora da faixa"). */
    public DataInvalidaException(String detalhe) {
        super("Data invalida: " + detalhe);
    }
}
