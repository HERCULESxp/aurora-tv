# Aurora TV

Launcher Android para Fire TV, com interface em português, sem anúncios e sem rastreamento. Inspirado no layout de referência: menu lateral, destaque panorâmico, fundo escuro e fileiras de aplicativos. O destaque usa uma paisagem original desenhada no dispositivo.

**Estado:** versão experimental 0.2.1 compilada, com 13 verificações Java e Android Lint aprovados. Ainda não testada em Fire Stick físico; funcionamento do redirecionamento depende do firmware.

[Baixar APK 0.2.1](https://github.com/HERCULESxp/aurora-tv/releases/download/v0.2.1/aurora-tv-0.2.1.apk) · [Release](https://github.com/HERCULESxp/aurora-tv/releases/tag/v0.2.1) · [Build validado](https://github.com/HERCULESxp/aurora-tv/actions/runs/39811733790)

Download público sem login. Para ativar Home e abertura ao ligar, veja [as instruções](docs/INSTALACAO.md#modo-experimental-home--inicialização-020).

## Funcionalidades

- Descobre e abre os apps instalados, priorizando atividades para TV.
- Navegação com direcional, OK, Voltar e Menu do controle remoto.
- Favoritos persistentes: segure OK em um cartão ou pressione Menu.
- Últimos 12 aplicativos abertos pelo Aurora, salvos localmente.
- Busca por nome, relógio e acesso às configurações do sistema.
- Registro como aplicativo HOME e como app de TV.
- 0.2.1: redirecionamento opcional da janela inicial Amazon via acessibilidade, proteção contra ciclos e tentativa opcional de abertura ao ligar.
- Sem permissão de internet, anúncios, analytics ou dependência de serviços Google.

O Aurora não agrega catálogos de streaming, não reproduz filmes e não acessa o progresso privado de Netflix, Prime Video ou outros serviços. “Recentes” lista apps abertos pelo launcher, não episódios assistidos. Os ícones vêm dos aplicativos instalados. A imagem de referência não é distribuída no projeto.

## Gerar o APK

### GitHub Actions

Envie o projeto para o GitHub. Em **Actions → Android APK**, execute o workflow ou aguarde a execução iniciada pelo push. Após o sucesso, baixe o artefato `aurora-tv-debug`, descompacte e use `app-debug.apk`.

O artefato é um APK de desenvolvimento. A chave debug do runner pode mudar entre execuções; para atualizações permanentes, configure uma chave de assinatura própria, guardada fora do repositório. Uma assinatura diferente exige desinstalar a versão anterior, apagando suas preferências.

### Android Studio / terminal

Requer JDK 17, Gradle 8.9 e Android SDK 35. Abra a pasta no Android Studio ou configure `ANDROID_HOME` para seu SDK e execute:

```sh
gradle :app:assembleDebug :app:lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

Este projeto não inclui Gradle Wrapper; instale Gradle 8.9 ou gere o wrapper com `gradle wrapper --gradle-version 8.9` antes de usar `./gradlew`. O CI provisiona a versão especificada automaticamente.

Teste independente do Android SDK:

```sh
./tests/run.sh
```

## Instalar no Fire Stick

Veja [o guia de instalação](docs/INSTALACAO.md). O APK exige um dispositivo Android/Fire OS com API 22 ou superior. Dispositivos com Vega OS não executam este APK.

**Instalar o app não garante substituir o botão Home.** A Amazon bloqueia launchers alternativos em diversas versões do Fire OS. O Aurora registra a intenção HOME padrão do Android e oferece redirecionamento experimental via acessibilidade. Esse método depende de o Fire OS permitir o serviço e a abertura de atividades; não remove os bloqueios do sistema. Não desative nem desinstale o launcher da Amazon para experimentar este projeto.

## Estrutura

- `MainActivity.java`: interface nativa e integração com os aplicativos.
- `RecentApps.java`: histórico limitado e sem duplicatas.
- `.github/workflows/android.yml`: compilação, lint e artefato APK.
- `docs/TESTES.md`: validações executadas e roteiro no aparelho.

Java com APIs nativas mantém o aplicativo pequeno e dispensa WebView e bibliotecas de UI. Favoritos são ordenados alfabeticamente. O histórico considera somente apps abertos pelo Aurora. A descoberta é atualizada ao retornar à tela inicial.
