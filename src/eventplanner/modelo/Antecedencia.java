package eventplanner.modelo;

import java.time.Duration;

/**
 * Antecedencia do lembrete: com quanto tempo antes do evento o usuario quer
 * ser avisado (1 dia, 3 dias ou 1 semana antes).
 *
 * OBS (recurso ALEM das aulas):
 *  - ENUM (mesma justificativa de Categoria).
 *  - java.time.Duration representa uma "quantidade de tempo" (ex.: 1 dia).
 *    Nao foi visto em aula; usamos para nao calcular datas "na mao".
 */
public enum Antecedencia {

    UM_DIA("1 dia antes", Duration.ofDays(1)),
    TRES_DIAS("3 dias antes", Duration.ofDays(3)),
    UMA_SEMANA("1 semana antes", Duration.ofDays(7));

    private final String rotulo;
    private final Duration duracao; // quanto tempo antes (Duration = java.time)

    Antecedencia(String rotulo, Duration duracao) {
        this.rotulo = rotulo;
        this.duracao = duracao;
    }

    public String getRotulo() {
        return rotulo;
    }

    public Duration getDuracao() {
        return duracao;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
