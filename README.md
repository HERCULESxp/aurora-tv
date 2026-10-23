# Aurora TV

Launcher para Fire TV com o visual original do Aurora: menu lateral, paisagem escura, cartões de aplicativos, favoritos, busca e histórico local. Sem anúncios ou permissão de internet.

## 0.3.1 — método Homestead

O mecanismo anterior de redirecionamento foi removido. A nova implementação adapta somente a substituição visual da tela inicial do [Homestead](https://github.com/Sarg338/Homestead):

- Observador separado `:homekey` procura inícios HOME no log, com `READ_LOGS` concedido pelo usuário.
- Janela de acessibilidade mostra a **interface navegável do Aurora**, sem aguardar a atividade abrir.
- A atividade assume após aproximadamente 5,2 segundos, preservando seção, foco e rolagem.
- Escolher um app cancela a troca pendente para o Aurora. A janela tem limite de duração e limpeza ao interromper o serviço.
- Eventos de janela funcionam como fallback sem `READ_LOGS`.
- Configurações/Amazon Home suspendem a detecção por janela. Voltar ao Aurora ou um HOME reconhecido no log retoma.

O código não captura nem bloqueia a tecla Home diretamente. Não inclui design, VPN, túnel ADB, debloat, ajustes do sistema nem atualizador do Homestead. `WRITE_SECURE_SETTINGS` não é necessário para a detecção antecipada e não foi solicitado.

**Experimental: a adaptação 0.3.1 ainda precisa ser testada no Fire Stick.** O tempo relatado pelo Homestead não é uma medição do Aurora. Build, 21 verificações Java e Android Lint passaram (exceto o check de target expirado explicitamente excluído).

[Baixar APK 0.3.1](https://github.com/HERCULESxp/aurora-tv/releases/download/v0.3.1/aurora-tv-0.3.1.apk) · [Build validado](https://github.com/HERCULESxp/aurora-tv/actions/runs/39958908967)

[Instalação e comandos](docs/INSTALACAO.md) · [Testes](docs/TESTES.md) · [Releases](https://github.com/HERCULESxp/aurora-tv/releases)

## Compilar

JDK 17, Gradle 8.9, Android SDK 35. Sem Gradle Wrapper incluído:

```sh
gradle :app:assembleDebug :app:lintDebug
./tests/run.sh
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. O CI também publica artefatos. Para gerar wrapper localmente: `gradle wrapper --gradle-version 8.9`.

`minSdk 22`; `targetSdk 28` acompanha a estratégia Homestead para permissões de abertura em segundo plano. Distribuição por sideload; não preparada para Google Play. Apenas o lint de target expirado é excluído com justificativa; os outros checks permanecem ativos.

**Assinatura:** estes APKs ainda são debug, com chaves diferentes por runner. Uma atualização pode exigir desinstalar a versão anterior, perdendo configurações e autorizações. A chave privada de uma versão anterior não pode ser recuperada do APK. Uma assinatura permanente para futuras atualizações exige provisionar a chave fora do repositório público.

## Privacidade

A acessibilidade observa nomes de janelas, não seu conteúdo, e não filtra teclas. `READ_LOGS` é uma permissão ampla do Android; nosso observador filtra os tags `ActivityManager`/`ActivityTaskManager` do buffer `system`, usa apenas timestamp e UID de inícios HOME e descarta o texto sem salvá-lo ou enviá-lo. O observador é desligado junto com o modo Home. Sem a permissão, fica apenas o fallback de acessibilidade.

Favoritos e Recentes ficam no dispositivo. Recentes são apps abertos pelo Aurora, não episódios assistidos. Não há integração com catálogos de streaming.

## Licença

GPL-3.0-or-later. Código Home adaptado do Homestead com atribuição em [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) e [LICENSE](LICENSE). O restante mantém a interface Aurora.
