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
