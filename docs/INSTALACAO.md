# Instalação no Fire TV

## 1. Confira o aparelho

Em Configurações → Meu Fire TV → Sobre, confira modelo e versão do sistema. Este projeto é um APK Android para Fire OS; não funciona em Vega OS. A compatibilidade real deve ser validada no seu modelo.

## 2. Instale por ADB

No computador, instale Android SDK Platform Tools pelo site oficial do Android. No Fire TV, habilite as opções de desenvolvedor (em alguns modelos, selecione o nome do dispositivo sete vezes na tela Sobre) e ative Depuração ADB. Os nomes dos menus variam por versão.

Conecte computador e Fire TV à mesma rede confiável. Substitua `IP_DO_FIRE_TV` pelo endereço mostrado no aparelho:

```sh
adb connect IP_DO_FIRE_TV:5555
```

Aceite a autorização exibida na TV. Depois execute:

```sh
adb install -r app-debug.apk
adb shell am start -n dev.aurora.tv/.MainActivity
```

O Aurora também deve aparecer na lista de aplicativos do Fire TV. Se não aparecer imediatamente, abra-o usando o comando acima.

## 3. Usar como tela inicial

Dentro do Aurora, vá a Configurações → Escolher app de início. Se o sistema oferecer um seletor, escolha Aurora TV. Em Android TV compatível, o sistema também pode apresentar essa escolha ao pressionar Home.

Muitas versões do Fire OS impedem essa seleção e reservam o botão Home para o launcher Amazon. Nesses casos, o Aurora pode ser aberto como aplicativo, mas o botão Home continua levando à Amazon. Este projeto não instala serviços de acessibilidade, não executa ADB em segundo plano e não modifica pacotes do sistema.

Não há procedimento universal de substituição seguro para todos os Fire Sticks. Antes de qualquer ajuste específico, identifique o modelo e a versão exata do Fire OS. Não desative o launcher original: isso pode deixar o dispositivo sem tela inicial funcional.

## Controle remoto

| Botão | Ação |
|---|---|
| Direcional | Mover o foco e navegar nas fileiras |
| OK | Abrir app ou selecionar opção |
| OK pressionado / Menu (☰) | Favoritar ou ver informações do app |
| Voltar | Voltar ao início; no início, focar menu lateral |
| Home | Comportamento definido pelo Fire OS |

## Remover

Use as configurações do Fire TV ou:

```sh
adb uninstall dev.aurora.tv
```

A desinstalação remove favoritos e histórico. Desative a Depuração ADB quando terminar.
