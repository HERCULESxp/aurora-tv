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

Muitas versões do Fire OS impedem essa seleção e reservam o botão Home para o launcher Amazon. Nesses casos, o Aurora pode ser aberto como aplicativo, mas o botão Home continua levando à Amazon. A partir da versão 0.2.0, existe um serviço de acessibilidade opcional para redirecionar a janela inicial da Amazon. Ele não remapeia fisicamente Home, não executa ADB em segundo plano e não modifica pacotes do sistema.

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


## Modo experimental Home / inicialização (0.2.0)

1. Instale a versão 0.2.0 e abra o Aurora ao menos uma vez.
2. Acesse **Configurações → Botão Home e inicialização → Ativar redirecionamento Home** e leia a explicação.
3. Na acessibilidade do sistema, habilite **Aurora — redirecionar início**, caso a opção esteja disponível. Ativar somente o botão interno do Aurora não habilita o serviço no sistema.
4. Volte ao Aurora e abra **Ver estado e diagnóstico**. Serviço habilitado e início da Amazon identificado são pré-condições, não garantia de funcionamento.
5. Pressione Home. A Amazon pode aparecer brevemente antes de o Aurora abrir.
6. Opcionalmente ative **Abrir Aurora ao ligar** e reinicie para testar. Fire OS pode bloquear a abertura em segundo plano ou atrasar a conexão do serviço. Forçar parada do app também pode impedir eventos de inicialização até ele ser aberto novamente.

O serviço observa somente eventos de mudança de janela do pacote `com.amazon.tv.launcher`, e só reage às classes de atividade registradas como HOME desse pacote. Não lê árvores de interface, textos de tela, teclas, credenciais ou conteúdo de outros aplicativos. Sem internet ou envio de eventos. Uma variante do firmware que reporte outra janela pode não ser reconhecida; não há fallback que intercepte indiscriminadamente o pacote todo.

O redirecionamento tem intervalo mínimo de 2 segundos. Após três tentativas em 15 segundos, uma quarta tentativa pausa o recurso. Se Home deixar de redirecionar após várias pressões rápidas, confira o diagnóstico e reative a opção. Essa proteção também evita ciclos se o Fire OS reabrir insistentemente a Amazon.

### Desativar / recuperar

No Aurora, desligue **Redirecionamento Home** e **Abertura ao ligar**. Também é possível desabilitar o serviço na acessibilidade do sistema. Se necessário, a partir de um computador já autorizado:

```sh
adb shell am force-stop dev.aurora.tv
```

Isso interrompe o Aurora, sem remover o launcher Amazon. Para remover totalmente:

```sh
adb uninstall dev.aurora.tv
```

Se o Fire OS ocultar ou bloquear o serviço de acessibilidade, informe o modelo e a versão completa do sistema para diagnóstico. Não execute comandos que substituam a lista inteira de serviços de acessibilidade: isso pode desabilitar recursos que você já usa. Não há garantia de contornar restrições do firmware somente com um APK.

### Atualizar o APK de teste

As versões de desenvolvimento podem ter certificados diferentes por serem geradas em runners distintos. Se ocorrer `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, desinstale a versão anterior e instale a nova. Isso apaga favoritos, histórico e opções; será necessário habilitar o serviço novamente. Não desinstale o launcher da Amazon.
