# Validação

## Executado neste ambiente

- Teste Java do histórico: primeira abertura, reabertura sem duplicata, limpeza de entradas vazias/duplicadas e limite dos últimos 12 apps.
- Leitura sintática dos recursos e manifesto XML, workflow YAML e fontes Java (sem resolução de tipos Android).
- Revisão de compatibilidade com API 22: ordenação usa `Collections.sort`, evitando `List.sort` de API 24.

## Executado no GitHub Actions

[Execução aprovada](https://github.com/HERCULESxp/aurora-tv/actions/runs/39790290844), commit `e4775710292b6219b0059eed1bec87eeb0c7d6f5`:

- Quatro verificações do histórico aprovadas.
- `:app:assembleDebug` e `:app:lintDebug` concluídos com sucesso.
- APK debug gerado e publicado como artefato `aurora-tv-debug`.

Foram corrigidos o pacote obsoleto `tools` na instalação do SDK e o atributo `windowLightStatusBar`, incompatível com API 22.

## Ainda não executado

Não foi verificado o layout em emulador nem o funcionamento em Fire TV. Lint aprovado não substitui teste no aparelho.

## Roteiro para aparelho / emulador

1. Compilar e executar `gradle :app:assembleDebug :app:lintDebug`.
2. Instalar em API 22 e em Android atual, além do modelo Fire TV alvo.
3. Conferir a tela em 720p e 1080p, margens, textos e foco.
4. Navegar por todos os itens somente com direcional, OK e Voltar; testar rolagem horizontal e vertical.
5. Abrir apps com entrada Leanback e Launcher comum; confirmar retorno ao Aurora.
6. Favoritar pelo OK pressionado e Menu; encerrar e reabrir para confirmar persistência.
7. Abrir mais de 12 apps e conferir ordem e limite de Recentes.
8. Buscar termos existentes e inexistentes com teclado virtual; voltar à lista.
9. Instalar/desinstalar outro app e retornar ao Aurora; conferir atualização da lista.
10. Testar Configurações e Escolher app de início, incluindo versões que não oferecem esse seletor.
11. Confirmar comportamento do botão Home no Fire OS, sem desativar o launcher original.
12. Limpar histórico e conferir estado vazio.


## Validação adicional 0.2.0

- Teste `RedirectGuardTest`: nove verificações cobrindo recurso desligado, janela não reconhecida, intervalo mínimo, limite de tentativas e recuperação após a janela de tempo.
- No aparelho: habilitar/desabilitar o serviço; conferir diagnóstico; pressionar Home de apps diferentes; confirmar que configurações Amazon não causam redirecionamento; testar pausa por repetição; reiniciar com abertura ao ligar ligada/desligada; testar recuperação por force-stop.
- Ainda não validado em Fire Stick físico. Serviço habilitado não comprova que o firmware permite abertura em segundo plano.

Build, 13 verificações Java, Android Lint e publicação da 0.2.0 aprovados na [execução 39807012550](https://github.com/HERCULESxp/aurora-tv/actions/runs/39807012550), commit `b03a6a60ba69b36be430c97a917877f44baee3cb`.
