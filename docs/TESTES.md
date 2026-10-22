# Validação

## Executado neste ambiente

- Teste Java do histórico: primeira abertura, reabertura sem duplicata, limpeza de entradas vazias/duplicadas e limite dos últimos 12 apps.
- Leitura sintática dos recursos e manifesto XML, workflow YAML e fontes Java (sem resolução de tipos Android).
- Revisão de compatibilidade com API 22: ordenação usa `Collections.sort`, evitando `List.sort` de API 24.

## Ainda não executado

O ambiente não dispõe de Android SDK nem Gradle. O download do SDK não foi autorizado/concluído nesta sessão. Portanto não foi possível gerar APK, executar Android Lint, verificar o layout em emulador ou testar em Fire TV. O workflow incluído fará build e lint quando executado no GitHub.

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
