# 7-Note Musical Game — Checklist de lançamento v1.4.0

Auditoria técnica em 22/11/2026. Um build aprovado não é aprovação para publicação pelas lojas.

## Testes automatizados

- Regras e sequência de 1000 fases, início em Dó–Si–Ré, encerramento após fase 1000.
- Anúncio de demonstração a cada duas derrotas.
- Navegador Chromium: mobile, controle remoto, memória, ouvido, compositor, ajustes, persistência, conclusão e offline.
- Android e TV: APK debug e AAB sem assinatura, lint e verificação da API de destino no manifesto compilado.
- iOS: compilar para simulador e dispositivo físico sem assinatura; conferir recursos incluídos.
- Windows: Electron atualizado, auditoria de dependências, instalador e portátil de desenvolvimento.

Os resultados devem ser conferidos após o commit mais recente:
- Testes: https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/workflows/7-note-tests.yml
- Compilações: https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/workflows/packages-7-note.yml

## Pendências externas antes de publicar

1. Verificar acesso ao Play Console e Apple Developer, responsáveis pela publicação.
2. Assinar os AAB com uma chave própria e configurar Play App Signing.
3. Criar IPA com assinatura de equipe Apple, concluir testes TestFlight e enviar App Store Connect.
4. Realizar testes reais em Android, iPhone, Android TV/TV Box e Windows (além dos testes automáticos).
5. Disponibilizar esta política em URL estável, confirmar contato oficial e preencher declarações de privacidade.
6. Criar páginas de loja com capturas de tela reais, descrições, classificação etária e metadados.
7. Concluir eventual teste fechado obrigatório da Play Console, conforme o tipo e data da conta.
8. Se forem incluídos anúncios pagos, integrar SDK aprovado, consentimento e documentos adequados, sem anúncios falsos.
9. Considerar a guideline 4.2 da Apple: um aplicativo que apenas replique um website pode ser rejeitado; o conteúdo interativo e a experiência nativa devem ser avaliados.
10. Assinar aplicativos Windows caso deseje evitar avisos de reputação do SmartScreen.

Ainda não foram feitas publicações em loja. Nenhuma credencial de assinatura é solicitada ou registrada no GitHub Actions.

## Referências oficiais

- Google Play API 36 (celulares), TV API 34+ (exceção): https://support.google.com/googleplay/android-developer/answer/11926878
- Android Gradle Plugin 8.13.x e Gradle 8.13: https://developer.android.com/build/releases/agp-8-13-0-release-notes
- Apple Xcode 26+: https://developer.apple.com/news/upcoming-requirements/
- Apple guideline 4.2: https://developer.apple.com/app-store/review/guidelines/
- Android TV: https://developer.android.com/develop/adaptive-apps/quality-guidelines/tv-app-quality
- Política local: privacy.html

## Evolução de 22/11/2026 — isolamento do cache offline

- Corrigido `sw.js`: o service worker só intercepta as URLs exatas dos nove recursos declarados em `APP_FILES`, sem parâmetros de consulta. Requisições de outras rotas, apps no mesmo domínio e conteúdo externo não são mais capturadas nem armazenadas.
- A resposta só é gravada se for HTTP OK e do tipo `basic`; removido fallback de navegação para `index.html` em URLs não pertencentes ao jogo.
- Cache atualizado de v5 para v6 para invalidar entradas antigas; preservada a remoção das versões anteriores durante `activate`.
- Adicionado teste estático de regressão em `tests/game.test.cjs`. Commits: `ff05f2b` e `3411545`.
- **Validação pendente:** verificar resultados dos workflows após esses commits; nenhuma execução Chromium, WebKit, Android Lint ou teste físico foi afirmada nesta rodada.
- **Próximo foco:** conferir CI e executar testes de funcionamento offline após atualização de versão, especialmente em navegação PWA.

## Evolução de 22/11/2026 — verificação CI e navegação iOS

- Conferidos os resultados publicados dos workflows: testes de regras/Chromium/WebKit em `f347f650` **success** (execução [45039296300](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45039296300)); pacotes Android/TV/iOS/Windows em `3411545e` **success** (execução [45039284098](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45039284098)). Os resultados são anteriores à correção iOS desta rodada.
- Auditoria encontrou navegação `WKWebView` permissiva para qualquer URL `file:`, inclusive arquivos locais fora do diretório Web do jogo. Restringida a páginas cujo caminho normalizado está dentro do diretório `Web/` do bundle, com cancelamento de URLs externas e caminhos fora da pasta. Commit `76fc7d83`.
- Acrescentada verificação de regressão ao teste de empacotamento iOS em `tests/native-packages.test.cjs`. Commit `1dfaa52f`.
- **Validação pendente:** aguardar a nova execução de `7-note-tests.yml` e `packages-7-note.yml` depois destes commits; o teste estático não substitui a compilação Xcode nem um teste em iPhone real.
- **Próximo foco:** confirmar compilação iOS e artefatos da nova revisão; executar teste de navegação de URLs locais/externas em simulador, se disponível, e continuar preparação de metadados das lojas.

## Evolução de 22/11/2026 — isolamento de rede no Electron

- Confirmado que a última execução de testes no commit `7131bba0` foi **success** ([45044839458](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45044839458)) e que os pacotes no commit `1dfaa52f` foram **success** ([45044827481](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45044827481)). Isso confirma CI anterior, não a correção Windows atual.
- Adicionado bloqueio em `session.webRequest.onBeforeRequest` no Electron para impedir requisições de recursos remotos ou arquivos fora do diretório local `web/`, além das restrições de navegação e permissões já existentes. Commit `26d5ef6`.
- Acrescentadas asserções estáticas de regressão de isolamento de requisições em `tests/native-packages.test.cjs`. Commit `255215a`.
- **Validação pendente:** conferir o CI após o commit de testes e executar instalação/uso real dos executáveis Windows, incluindo áudio, funcionamento offline e bloqueio de recursos remotos. Nenhum teste físico foi afirmado.
- **Próximo foco:** auditar resultados de CI e artefatos Windows, depois preparar capturas reais e metadados de loja sem publicar nem alterar outros apps.

## Evolução de 22/11/2026 — contador de conquistas e artefatos

- Conferidos os workflows anteriores: testes do commit `4d31a377` concluídos com **success** ([45050660136](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45050660136)); empacotamento do commit `255215a0` concluído com **success** ([45050650986](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45050650986)). Este último disponibilizou oito artefatos: APK/AAB Android, APK/AAB TV, iOS simulador, iOS dispositivo sem assinatura, Windows x64 e relatórios Android Lint. Os resultados não validam automaticamente os commits desta seção.
- Corrigido contador inicial incorreto de conquistas em `index.html`: exibia `0/5`, embora `game-core.js` defina seis conquistas, inclusive a final de 1.000 fases. Commit `a7fe414`.
- Adicionado teste automatizado em `tests/release-readiness.test.cjs` que compara o contador inicial com a quantidade real retornada pelo motor, evitando divergência futura. Commit `9b204a6`.
- **Validação pendente:** verificar os workflows após o commit de teste; não foi executado teste físico nem foi afirmada aprovação de CI desta revisão.
- **Próximo foco:** conferir os novos testes e relatórios Android Lint, depois validar a versão Windows instalada e as capturas verdadeiras para lojas.
