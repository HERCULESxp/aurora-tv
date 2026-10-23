# Validação 0.3.1

## Local

`./tests/run.sh`: quatro verificações de Recentes e 17 do novo mecanismo. Inclui HOME normal/root/app, rejeição de backlog, usuário diferente, evento que não é HOME, timestamp inválido/NaN, UID inválido; cancelamento de handoff por seleção e invalidação de callbacks antigos por epoch.

O antigo RedirectGuard e seus testes foram removidos junto com a implementação anterior. O parsing do observador foi extraído do Homestead e endurecido para rejeitar timestamps inválidos.

## CI

O workflow da Release executa testes Java, `assembleDebug` e `lintDebug` antes de publicar o APK. A exceção de target SDK expirado é documentada em app/build.gradle; nenhum outro erro de lint é ignorado.

## Obrigatório no Fire Stick

Não executado nesta sessão. Compilação e testes de lógica não comprovam funcionamento de janelas/controle no Fire OS.

1. Ativar acessibilidade e READ_LOGS, abrir o Aurora e conferir diagnóstico.
2. Testar Home de um app, da Amazon e do próprio Aurora, incluindo pressões repetidas.
3. Navegar pelos cartões, favoritos e busca enquanto a janela está ativa; conferir seção/foco/rolagem durante a passagem para a atividade.
4. Escolher um app antes e depois do handoff; confirmar que o Aurora não abre novamente por callback antigo.
5. Abrir configurações e Amazon Home, permanecer nelas e retornar por Home.
6. Conferir encerramento da janela se atividade/app falhar, interrupção do serviço, force-stop e desativação do modo Home.
7. Revogar READ_LOGS e testar fallback por janela; conceder novamente e reconectar.
8. Reiniciar o dispositivo com modo Home ligado/desligado e com serviço habilitado/desabilitado.
9. Confirmar aparência Aurora, sem importar telas do Homestead.
10. Conferir instalação limpa e eventual conflito de assinatura ao atualizar versões debug anteriores.

Execução validada: https://github.com/HERCULESxp/aurora-tv/actions/runs/39958908967 — testes, assembleDebug, lintDebug e publicação concluídos com sucesso.
