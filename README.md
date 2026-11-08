# KZ OS

Ambiente desktop local para Android, desenvolvido em Kotlin e Jetpack Compose. O projeto é um aplicativo Android — não substitui o sistema Android nem virtualiza hardware.

## Estado inicial (0.1.0)

- Desktop com papel de parede selecionável, menu iniciar, barra de tarefas e janelas arrastáveis/minimizáveis/fecháveis.
- Calculadora com parser aritmético local.
- Bloco de notas persistido no armazenamento privado do app.
- Gerenciador de arquivos com criação de arquivos/pastas, renomeação e exclusão.
- Terminal local com comandos `help`, `ls`, `pwd`, `clear`, `neofetch` e `echo`.
- Configurações de tema claro/escuro e papel de parede persistidas localmente.
- Meu Computador e monitor com informações do host e métricas explicitamente identificadas como simulação.
- Navegador com campo de endereço. A navegação online será adicionada em uma etapa posterior.

## Abrir e executar

1. Instale o Android Studio atual com Android SDK 35.
2. Abra este diretório no Android Studio e aguarde a sincronização Gradle.
3. Execute `app` em um emulador ou dispositivo Android 8.0+.

Para gerar um APK debug pelo terminal, com Gradle instalado: `gradle assembleDebug`. O APK fica em `app/build/outputs/apk/debug/app-debug.apk`.

## Limitações conhecidas

Este é o primeiro incremento e ainda não foi compilado/testado neste ambiente. O app não é um sistema operacional independente: roda como aplicativo Android. CPU/GPU/RAM/SSD virtuais são simulações/telemetria didática; não existe virtualização de hardware. A navegação web ainda não carrega páginas. A persistência do gerenciador usa o diretório privado do aplicativo.
