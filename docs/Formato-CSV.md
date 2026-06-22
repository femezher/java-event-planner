# Formato do Arquivo (`dados/eventos.csv`)

A persistência é feita em **CSV de texto puro**, um **evento por linha**, gravado
por `RepositorioEventosCSV` (com `PrintStream`) e lido com `Scanner`. Linhas
começando com `#` são **comentários** e são ignoradas.

## Colunas (separadas por vírgula)

```
titulo,data,hora,local,categoria,antecedencia,descricao,participantes
```

| Campo | Formato / valores | Exemplo |
|---|---|---|
| `titulo` | texto (sem vírgula) | `Reuniao de TCC` |
| `data` | ISO `AAAA-MM-DD` | `2026-06-29` |
| `hora` | ISO `HH:MM` (24h) | `14:30` |
| `local` | texto (sem vírgula) | `Sala 3-001` |
| `categoria` | `REUNIAO` · `ANIVERSARIO` · `COMPROMISSO` | `REUNIAO` |
| `antecedencia` | `UM_DIA` · `TRES_DIAS` · `UMA_SEMANA` | `UM_DIA` |
| `descricao` | texto (pode ser vazio) | `Revisar o capitulo 2` |
| `participantes` | `nome:email;nome:email` (opcional) | `Ana:ana@usp.br;Beto:beto@usp.br` |

> **Importante:** não use vírgula dentro dos campos — a leitura separa por vírgula.
> Os valores de `categoria`/`antecedencia` devem casar exatamente com os nomes das
> constantes dos `enum` (`Categoria.valueOf` / `Antecedencia.valueOf`).

## Exemplo

```csv
# titulo,data,hora,local,categoria,antecedencia,descricao,participantes
Reuniao de TCC,2026-06-21,14:30,Sala 3-001,REUNIAO,UM_DIA,Revisar o capitulo 2,Ana:ana@usp.br;Beto:beto@usp.br
Aniversario da Maria,2026-06-22,09:00,Casa,ANIVERSARIO,TRES_DIAS,Levar bolo,
Aula de ingles,2026-06-23,19:30,Cultura Inglesa,COMPROMISSO,UM_DIA,Unidade 5,
```

## Robustez (tolerância a falhas)

- **Arquivo ausente:** a aplicação começa com a **agenda vazia**, sem erro.
- **Linha corrompida:** é **ignorada** (tratada em `try/catch`) e as demais
  continuam carregando.
- **Escrita segura:** o arquivo é fechado no bloco `finally`; a pasta `dados/` é
  criada automaticamente se não existir.

Mais detalhes: [Arquitetura](Arquitetura.md) e
[EXPLICACAO_CLASSES.md](../EXPLICACAO_CLASSES.md).
