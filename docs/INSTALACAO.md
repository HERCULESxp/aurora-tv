# Aurora 0.3.1 — instalação / método Homestead

Android/Fire OS API 22+. Não funciona em Vega OS. Nenhum comando desativa o launcher Amazon.

## Instalar

Baixe o APK na Release. Se a instalação sobre a versão anterior mostrar assinatura incompatível, desinstale o Aurora antigo e instale o novo. Isso apaga favoritos, histórico e permissões. Os APKs atuais usam assinatura debug; uma chave estável ainda não foi provisionada.

Abra o Aurora uma vez. O serviço mantém o mesmo componente `dev.aurora.tv/dev.aurora.tv.HomeRedirectService`, mas foi reescrito. As antigas opções de redirecionamento, inicialização e pausa não controlam esta versão.

## Ativar usando o shell ADB do celular

Estes comandos são executados **no shell conectado ao Fire Stick**, sem prefixo `adb shell`:

```sh
settings get secure enabled_accessibility_services
settings get secure accessibility_enabled
```

Guarde ambos os valores para restauração. **Somente se a lista estiver vazia ou retornar `null`**, execute:

```sh
settings put secure enabled_accessibility_services dev.aurora.tv/dev.aurora.tv.HomeRedirectService
settings put secure accessibility_enabled 1
```

Se houver outros serviços, acrescente `:dev.aurora.tv/dev.aurora.tv.HomeRedirectService` à lista atual, sem duplicar nem remover entradas. Desative antes outros apps de redirecionamento Home, como Home on Fire, para não competirem.

Para a detecção antecipada do Homestead:

```sh
pm grant dev.aurora.tv android.permission.READ_LOGS
am force-stop dev.aurora.tv
am start -n dev.aurora.tv/.MainActivity
```

O reinício do processo após o grant assegura a aplicação da permissão. Abra **Configurações → Home — método Homestead → Diagnóstico e ativação**. O modo Home deve estar ligado, o serviço conectado, READ_LOGS concedido e o observador conectado. Caso force-stop deixe o serviço desconectado, habilite novamente o componente na lista de acessibilidade, preservando os outros serviços.

Teste Home estando em outro app e estando na Amazon. A interface do Aurora deve aparecer como janela navegável; após cerca de 5,2 s a atividade assume. A inicialização de um app escolhido durante esse período também pode esperar o bloqueio do Fire OS terminar. Não há promessa de 170 ms para o Aurora.

**Não execute o comando WRITE_SECURE_SETTINGS do Homestead no Aurora:** a permissão não é necessária para este recorte e não está declarada.

## Configurações / Amazon

Use **Configurações do dispositivo** ou **Abrir Amazon Home** no menu do Aurora. O mecanismo fica suspenso durante essa saída. Voltar ao Aurora ou um novo Home reconhecido pelo observador retoma; sem READ_LOGS, abra o Aurora manualmente. Se abrir configurações por outros caminhos e elas forem interrompidas, desligue o modo Home enquanto as usa.

## Desligar e recuperar

No Aurora: **Configurações → Home — método Homestead → Desligar substituição Home**. Isso remove a janela e desliga o observador de logs. O launcher permanece utilizável como app comum.

Para interromper de imediato pelo shell:

```sh
am force-stop dev.aurora.tv
```

Para revogar a leitura do log:

```sh
pm revoke dev.aurora.tv android.permission.READ_LOGS
am force-stop dev.aurora.tv
am start -n dev.aurora.tv/.MainActivity
```

Para remover totalmente:

```sh
pm uninstall dev.aurora.tv
```

A janela também tem um limite de 15 segundos para não deixar o aparelho preso caso a atividade não abra. Nunca desative o launcher Amazon.

## Diagnóstico

```sh
dumpsys accessibility
dumpsys activity activities | grep -E 'mResumedActivity|mFocusedActivity'
```

Informe a tela de diagnóstico e o comportamento observado. Não envie o log completo do sistema: ele pode conter informações de outros aplicativos.
