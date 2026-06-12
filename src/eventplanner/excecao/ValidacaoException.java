package eventplanner.excecao;

/**
 * Excecao BASE de validacao de dados do usuario.
 *
 * Segue exatamente o padrao do Cap. 10 ("Programmer-Defined Exceptions"):
 * uma excecao propria e criada estendendo a classe Exception.
 *
 * Por ser "checked" (estende Exception, nao RuntimeException), o compilador
 * OBRIGA a tratar (catch) ou declarar (throws) - Cap. 10 "Checked Exceptions".
 *
 * As subclasses (TituloVazio, DataInvalida, ...) permitem, na GUI, capturar
 * apenas "ValidacaoException" e tratar TODOS os erros de validacao de uma vez
 * (polimorfismo de excecoes - Class13 "Handlers Gerais e Especializados").
 */
public class ValidacaoException extends Exception {

    /** Recebe a mensagem amigavel que sera mostrada ao usuario. */
    public ValidacaoException(String mensagem) {
        super(mensagem); // repassa a mensagem para a superclasse Exception (super - Cap. 8)
    }
}
