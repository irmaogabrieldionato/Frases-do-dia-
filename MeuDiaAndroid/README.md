# Meu Dia — aplicativo Android nativo

Aplicativo de tarefas diárias para Android, convertido da versão web fornecida pelo autor.

## Idiomas
Na primeira abertura, a pessoa escolhe um dos 15 idiomas. O texto da interface muda e a escolha fica salva. Use o ícone 🌐 no cabeçalho para mudar depois. A lista de tarefas existente é preservada; títulos escritos pelo usuário não são traduzidos automaticamente. Árabe e urdu usam layout da direita para a esquerda.

Idiomas: inglês, mandarim, hindi, espanhol, francês, árabe padrão moderno, bengali, russo, português, indonésio, urdu, alemão, japonês, suaíli e marathi.

## Funcionalidades
- Criar tarefas (até 90 caracteres), concluir, desmarcar e excluir.
- Mostrar a porcentagem de tarefas concluídas no dia.
- Reabrir a mesma lista todos os dias; tarefas concluídas voltam a ficar pendentes após a mudança de data.
- Guardar dados no próprio Android por SharedPreferences, sem servidor, conta, Bluetooth ou Wi-Fi.
- Interface Android nativa em Java: não usa navegador ou WebView.
- Ícone vetorial nas cores do desenho original.

## Gerar APK no GitHub
1. Abra a aba **Actions** deste repositório.
2. Localize a execução **Gerar APK Meu Dia (Android)** associada ao branch `meu-dia-android`.
3. Abra a execução e, após o status verde, baixe o artefato **MeuDia-Android-APK**.
4. Descompacte o ZIP e instale `app-debug.apk` no seu Android.

O Android pode pedir autorização para instalar APKs do aplicativo de onde o arquivo foi aberto.

## Compilar localmente (opcional)
Requer Java 17, Android SDK 35 e Gradle 8.9:

```sh
gradle -p MeuDiaAndroid :app:assembleDebug
```

## Publicação
O APK gerado é de depuração, adequado para testes. Como o GitHub Actions normalmente gera uma chave de assinatura de depuração nova, a instalação sobre uma versão anterior pode falhar com "aplicativo não instalado"; nesse caso, a desinstalação da versão antiga pode apagar as tarefas. Para atualizações com preservação garantida dos dados, é necessário configurar uma chave de assinatura fixa e protegida.  Para publicar na Play Store é necessário gerar uma versão de lançamento, assinar com uma chave permanente e cumprir as exigências do Google Play. **AdMob não está instalado**; a monetização depende de cadastrar o aplicativo e integrar o SDK do Google.

Código aberto sob a licença MIT do projeto de origem. Este projeto Android fica no branch `meu-dia-android` e não substitui o aplicativo Frases de Filósofos da branch principal.
