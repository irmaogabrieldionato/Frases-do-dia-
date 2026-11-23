# Pacotes multiplataforma do 7-Note Musical Game

Os projetos compartilham a mesma lógica musical e são compilados pela rotina do GitHub Actions na branch 7-note-musical-game.

## Onde baixar

Abra: https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/workflows/packages-7-note.yml

Selecione uma execução com todos os processos aprovados, desça até a seção Artifacts e baixe o ZIP do destino desejado. Os arquivos de cada execução ficam disponíveis por 30 dias.

| Plataforma | Pacote gerado | Status de instalação |
| --- | --- | --- |
| Android | 7-note-Android-APK-debug, com app-mobile-debug.apk | APK de desenvolvimento instalável diretamente em Android |
| Android TV e TV Box | 7-note-TV-Box-APK-debug, com app-tv-debug.apk | APK de desenvolvimento instalável em dispositivos Android compatíveis |
| Google Play (Android) | 7-note-Android-AAB-unsigned | Bundle de release **sem assinatura**: precisa assinar para publicar |
| Google Play (TV) | 7-note-TV-Box-AAB-unsigned | Bundle de release **sem assinatura**: precisa assinar para publicar |
| iOS simulador | 7-note-iOS-Simulator | ZIP com SevenNote.app para Xcode Simulator |
| iPhone e iPad físico | 7-note-iOS-Device-UNSIGNED | ZIP com .app de dispositivo, **sem assinatura**; não é IPA instalável |
| Windows x64 | 7-note-Windows-x64-EXE | Setup.exe (instalador) e Portable.exe (portátil), sem assinatura digital |

### Conferir integridade dos pacotes Android e TV

Na [execução de empacotamento](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/workflows/packages-7-note.yml), baixe **da mesma execução** os cinco artefatos `7-note-Android-TV-SHA256`, `7-note-Android-APK-debug`, `7-note-TV-Box-APK-debug`, `7-note-Android-AAB-unsigned` e `7-note-TV-Box-AAB-unsigned`. Extraia os quatro binários e o `SHA256SUMS.txt` em uma única pasta, sem renomeá-los. Na raiz do código desta mesma revisão, execute:

    node scripts/verify-android-packages.cjs /caminho/para/pasta

O comando confere os quatro nomes, impede entradas duplicadas e rejeita qualquer pacote ausente, vazio ou com hash divergente. A saída deve mostrar quatro linhas `OK`; qualquer erro retorna código de saída diferente de zero. **Não misture arquivos de execuções diferentes.** Os hashes verificam integridade, mas não comprovam autenticidade da origem, assinatura de loja ou segurança do binário.

### Android e TV Box

Android: API mínima 24 (Android 7), API de destino 36 para o Google Play, pacote com.orbis.sevennote. Instalação técnica: adb install -r app-mobile-debug.apk.

TV: API de destino 35. Aplicativo separado com.orbis.sevennote.tv; inicia em paisagem, oferece categorias para launcher Android TV e launcher Android genérico. Use setas do controle remoto (D-pad), OK/Enter para selecionar e Voltar para sair ou fechar diálogos. TV Boxes sem Android não são compatíveis. Alguns aparelhos exigem Android System WebView atualizado.

Para publicar no Google Play, o desenvolvedor deverá configurar uma chave de assinatura privada e Play App Signing. Os arquivos debug não são a versão final para a loja.

### iOS

O projeto fica em native/ios/project.yml e usa SwiftUI e WKWebView. Para usar simulador, extraia o ZIP e execute xcrun simctl install booted SevenNote.app com o simulador iniciado.

Para usar no iPhone ou iPad real, é necessário Mac com Xcode, equipe Apple, certificados e um perfil de provisionamento. O build Device-UNSIGNED precisa ser assinado antes de instalar. Para TestFlight/App Store, é necessária a conta Apple Developer e a exportação de um IPA assinado. Não há certificado Apple conectado neste projeto, por isso não prometemos um IPA instalável.

### Windows

O pacote Windows possui um instalador NSIS x64 e um executável portátil x64. Os dois funcionam offline. Sem certificado de assinatura de código, o Windows SmartScreen poderá apresentar alertas de reputação. Teste os programas antes de distribuir.

### Builds locais

Na raiz do projeto, com Node.js e Python 3 instalados:

    node scripts/sync-native.mjs
    python3 scripts/artwork.py
    npm test

Android (JDK 17, Android SDK API 36 e Build Tools 35.0.0, Gradle 8.13):

    cd native/android
    gradle :app:assembleMobileDebug :app:assembleTvDebug :app:bundleMobileRelease :app:bundleTvRelease :app:lintMobileRelease :app:lintTvRelease

iOS (Mac com Xcode e XcodeGen):

    cd native/ios
    xcodegen generate
    xcodebuild -project SevenNote.xcodeproj -scheme SevenNote -sdk iphonesimulator -configuration Release -destination "generic/platform=iOS Simulator" CODE_SIGNING_ALLOWED=NO build

Windows (Node 22, no Windows):

    cd native/windows
    npm install
    npm run build

### Observações

O jogo permanece offline, com modos Memória, Ouvido, Livre e Compositor, e mantém o anúncio demonstrativo a cada duas derrotas. **Ainda não existe rede de anúncios real conectada**. Os dados ficam salvos localmente em cada plataforma; não há sincronização entre dispositivos. Nenhum pacote foi enviado às lojas.
