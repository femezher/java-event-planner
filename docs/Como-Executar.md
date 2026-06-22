# Como Executar

Requisito: **Java 8 ou superior** (testado em Java 11/21). Rode **a partir da raiz
do projeto**, para que o caminho relativo `dados/eventos.csv` seja encontrado.

## macOS / Linux

```bash
# 1) compilar todos os .java para a pasta build/
mkdir -p build
javac -d build $(find src -name "*.java")

# 2) executar
java -cp build eventplanner.app.EventPlannerApp
```

## Windows (PowerShell)

```powershell
mkdir build
javac -d build (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName })
java -cp build eventplanner.app.EventPlannerApp
```

## O que esperar

1. A janela abre **maximizada (tela cheia)**, com três colunas: calendário, lista
   do dia e linha do tempo.
2. Se houver eventos cujo lembrete cai nas próximas 24h, aparece um aviso
   (`JOptionPane`) na inicialização.
3. Os eventos são lidos e gravados em **`dados/eventos.csv`** (criado
   automaticamente se não existir).

## Dicas

- **Novo evento:** botão *Novo* na coluna do dia, ou **duplo-clique** numa área
  vazia da linha do tempo.
- **Editar:** selecionar na lista e *Editar*, ou **duplo-clique** sobre um card.
- **Linha do tempo:** alternar entre **Dia** e **Semana**; navegar com `<` / `>` e
  voltar com **Hoje**.
- **Buscar:** caixa de busca no topo (procura em título, descrição e local).
