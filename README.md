# Reprodutor de CD e DVD

Reprodutor Android para drives ópticos USB, com foco em Android TV e Google TV. A interface inicial usa preto, azul royal e roxo, com navegação pelo controle remoto.

## Reprodução

1. Conecte um drive de CD/DVD USB compatível com Mass Storage Bulk-Only e insira o disco.
2. Se o Android perguntar qual app deve abrir o drive, escolha o Reprodutor e conceda o acesso USB. Pelo launcher do app, abra **Conectar drive** e selecione **Mount USB Drive**.
3. Em um DVD-Video, selecione **Abrir menu do DVD**. As setas movem a seleção do disco, **OK** confirma e a tecla **Menu**, quando disponível no controle, volta ao menu principal.

A leitura do DVD é feita sob demanda a partir do drive, com navegação pelo LibVLC. Foram testados em uma TV Android 11 o menu original do DVD *Oficina G3 — O Tempo*, o submenu de músicas, a seleção de dois capítulos e o retorno ao menu.

O backend também oferece leitura de faixas de CD de áudio e de arquivos de discos de dados. O CD não foi validado no teste físico acima. A biblioteca da tela inicial, a busca de metadados pela web e o visualizador de áudio ainda estão em desenvolvimento. A compatibilidade depende do drive, da TV e do disco. A ponte nativa opcional de CSS do DiscMount fica desativada no build padrão.

## Build e testes

Use JDK 17 e a plataforma Android SDK 37. Configure o SDK pelo Android Studio ou pela variável `ANDROID_HOME`.

```powershell
.\gradlew.bat :app:assembleDebug :discmount-core:botTest
```

No Linux ou macOS, use `./gradlew` no lugar de `.\gradlew.bat`. O APK de desenvolvimento é gerado em `app/build/outputs/apk/debug/app-debug.apk`. O teste `botTest` exercita o transporte USB em simulação e dispensa um drive conectado.

## Licença e créditos

O projeto usa a [GNU GPL v3 ou posterior](LICENSE). A leitura óptica deriva do [DiscMount](https://github.com/everywherei/Android-TV-BLURAY-DVD-CD-PLAYER/tree/c716f8ce9d6d0afe907ac4021abea4062a954f13) e a reprodução usa o [LibVLC](https://code.videolan.org/videolan/vlc-android). Os avisos de terceiros e o resumo das adaptações estão em [third_party/discmount/THIRD_PARTY_NOTICES.md](third_party/discmount/THIRD_PARTY_NOTICES.md).
