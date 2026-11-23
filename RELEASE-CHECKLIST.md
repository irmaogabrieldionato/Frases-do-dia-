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

## Evolução de 22/11/2026 — consistência do build Android local

- Verificados os resultados finais das execuções anteriores: [testes 45056284135](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45056284135) com quatro jobs **success** (Chromium, WebKit, regras/PWA e ZIP); [pacotes 45056284170](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45056284170) com quatro jobs **success** (validação, Windows, iOS e Android/TV). Esses resultados precedem os commits desta seção.
- Identificada documentação local desatualizada em `PACKAGES.md`: orientava Android SDK 35 e Gradle 8.9, mas o CI utiliza Android API 36, Build Tools 35.0.0 e Gradle 8.13. Corrigidas as versões e acrescentados os comandos de geração AAB e Android Lint. Commit `a961f2e`.
- Acrescentado teste de regressão em `tests/release-readiness.test.cjs` que confronta os comandos Android documentados com as tarefas do workflow. Commit `19156ce`.
- **Validação pendente:** conferir a execução CI disparada pelo commit de testes; os testes não foram executados localmente nesta rodada. A correção é de documentação/teste, não altera os binários Android.
- **Próximo foco:** conferir o CI mais recente e a disponibilidade dos artefatos, auditar capturas verdadeiras e testar os instaladores em aparelhos reais quando houver acesso.

## Evolução de 22/11/2026 — proteção adicional do WebView Android

- Confirmadas execuções anteriores **success**: testes no commit `d42746f0` ([45061726138](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45061726138)) e pacotes no commit `19156ce1` ([45061716513](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45061716513)). Esses resultados **não** validam a alteração Android desta seção.
- Em `MainActivity.java`, explicitamente desabilitados acesso de origens `file://` a arquivos e URLs externas, e suporte a múltiplas janelas no WebView; mantidos bloqueios já existentes de conteúdo, arquivos, mixed content, pop-ups automáticos e recursos externos. Commit `33638a1`.
- Acrescentado teste de regressão das configurações de isolamento Android em `tests/release-readiness.test.cjs`. Commit `db8f165`.
- **Validação pendente:** conferir os novos workflows após os commits, inclusive Android Lint e APK/AAB mobile/TV. Nenhuma compilação ou execução em dispositivo local foi realizada nesta rodada. Os testes estáticos não comprovam o comportamento do WebView em aparelho real.
- **Próximo foco:** conferir os jobs recém-disparados e os artefatos, depois testar Android/TV em hardware e preparar fichas de loja com capturas reais.

## Evolução de 22/11/2026 — saturação de contadores persistidos

- Confirmados quatro jobs **success** em [testes 45067697988](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45067697988) no commit `90489bf8` e quatro jobs **success** em [pacotes 45067688645](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45067688645) de revisão anterior. Esses resultados não comprovam os commits atuais.
- Corrigida uma falha de limite em `game-core.js`: `totalWins`, `totalLosses`, `earPlayed` e `earCorrect` podiam ultrapassar o máximo aceito por `sanitizeProgress`, causando reset do contador ao recarregar o jogo. Agora os incrementos saturam em `Number.MAX_SAFE_INTEGER - 1`, preservando pontuação e registros sem alterar as 1.000 fases, os quatro modos ou a cadência de demonstração de anúncios. Commit `2d3e13c`.
- Adicionado teste de regressão com contadores no limite, verificando vitória, derrota, treino de ouvido e persistência após sanitização. Commit `a39c703`.
- **Validação pendente:** conferir o resultado de `7-note-tests.yml` e `packages-7-note.yml` nos commits novos. Não foram realizados testes locais ou físicos nesta rodada.
- **Próximo foco:** verificar os jobs e artefatos novos, depois auditar instalação Windows e execução em dispositivos reais quando disponíveis.

## Evolução de 22/11/2026 — saturação de derrotas e cadência de anúncios

- Conferido workflow de testes anterior no commit `f2d27da`: **success**, execução [45073120034](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45073120034). Esse resultado precede a correção atual.
- Identificado defeito residual: o limite par `Number.MAX_SAFE_INTEGER - 1` fazia `adDue` permanecer verdadeiro em todas as derrotas adicionais quando o contador saturava, podendo repetir a tela de anúncio demonstrativo em cada derrota.
- Corrigido `game-core.js` para saturar `totalLosses` no limite ímpar `Number.MAX_SAFE_INTEGER - 2`, incluindo migração segura do valor máximo par previamente persistido, sem zerar o histórico. Demais contadores permanecem inalterados. Commit `ae0fd916`.
- Atualizados os testes de limite e adicionado caso específico que simula derrotas repetidas após a saturação, preservando a regra de uma tentativa a cada duas derrotas enquanto o contador ainda avança. Commit `bbe4868e`.
- **Validação pendente:** confirmar os resultados de CI disparados após estes commits; nenhum teste local, navegador ou aparelho físico foi executado nesta rodada. A saturação ocorre somente em um limite numérico extremo, mas a correção impede repetição indefinida de publicidade demonstrativa.
- **Próximo foco:** conferir CI, artefatos Android/TV/iOS/Windows e relatórios de lint, sem alterar branches de outros projetos.

## Evolução de 22/11/2026 — desempenho na fase 1.000

- Conferidos os últimos workflows: testes no commit `3b08b0a4` [45078890011](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45078890011) **success** e pacotes no commit `bbe4868e` [45078877746](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45078877746) **success**. As falhas intermediárias em `ae0fd916` foram superadas nos commits seguintes; esses resultados são anteriores à otimização atual.
- Otimizado `app.js`: a sequência determinística da fase é gerada uma vez e reutilizada tanto na reprodução quanto nas entradas do jogador, evitando reconstruir até 1.000 notas a cada tecla na fase final. A cache é invalidada automaticamente quando `progress.level` muda. Não altera o motor, a ordem das notas, as 1.000 fases, os modos nem a persistência.
- Adicionado teste estático de regressão em `tests/release-readiness.test.cjs` para verificar reutilização em reprodução e avaliação.
- **Validação pendente:** conferir os novos workflows após os commits. Não foi realizado benchmark de hardware ou teste físico nesta rodada; o teste estático não substitui medição de desempenho.
- **Próximo foco:** medir a latência de entrada na fase 1.000 em Chromium/WebKit e confirmar pacotes atualizados.

## Evolução de 22/11/2026 — renderização de conquistas na fase 1.000

- Confirmados testes no commit `83712168` com **success** ([45084386409](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45084386409)) e pacotes no commit `b6eaccb6` com **success** ([45084384095](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45084384095)). São resultados anteriores à otimização desta seção.
- Identificada reconstrução integral do DOM das seis conquistas a cada resposta musical, inclusive nas 1.000 respostas da fase final. Em `app.js`, agora o painel é reconstruído somente quando muda uma das seis condições efetivas de desbloqueio; a renderização inicial continua obrigatória. Commits `38f470f` e `b8cff67`.
- Acrescentado teste de regressão para as condições de cache em `tests/release-readiness.test.cjs`. Commit `3579bb8`.
- **Validação pendente:** confirmar CI após os commits desta seção e medir latência em Chromium/WebKit/dispositivo real. Não foi executado benchmark ou teste físico nesta rodada. O teste novo é estático, não mede FPS.
- **Próximo foco:** verificar os novos workflows e executar medição automatizada de latência de entrada e renderização da fase 1.000.

## Evolução de 22/11/2026 — regressão dinâmica de 1.000 entradas

- Conferidos os quatro jobs **success** dos workflows anteriores: testes [45084386409](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45084386409) e pacotes [45084384095](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45084384095). Esses resultados precedem os commits desta seção.
- Acrescentado cenário Playwright em `tests/browser-smoke.cjs` para Chromium e WebKit: carregar a fase 1.000 ainda não concluída, entrar no modo Livre, realizar 1.000 entradas de teclado musical sem som, medir duração e verificar que o painel de seis conquistas conserva os mesmos nós DOM quando nenhuma condição de desbloqueio muda. Commit `9f4e043`.
- **Limite da medição:** a duração é apenas diagnóstico registrado nos logs do CI, sem limite absoluto arbitrário entre máquinas; o cenário não mede reprodução auditiva das 1.000 notas nem latência em aparelho físico. Não se afirma aprovação dos novos testes antes da conclusão do workflow.
- **Próximo foco:** conferir o novo CI Chromium/WebKit e comparar duração, depois medir reprodução e resposta no modo Memória da fase final em dispositivos reais.


## Evolução de 22/11/2026 — cache do painel do compositor

- Identificada reconstrução desnecessária de todos os chips do compositor em cada renderização, inclusive nas 1.000 entradas do modo Livre e durante a fase final de Memória.
- `app.js`: cache pela sequência de notas da melodia; o DOM dos chips só é reconstruído quando a composição muda. O estado dos botões Desfazer e Limpar continua sendo atualizado em todas as renderizações. Commit: `2e99ca19`.
- `tests/release-readiness.test.cjs`: adicionada verificação estática de regressão da cache. Commit: `9363e28b`.
- Conferido resultado anterior de quatro jobs aprovados em [testes 45084386409](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45084386409), anterior a estas mudanças.
- **Validação pendente:** verificar novos jobs Chromium/WebKit e pacotes; não foi medido ganho real em mil entradas, nem executado teste físico. Preservadas as 1.000 fases e todas as regras de jogo.

## Evolução de 23/11/2026 — regressão dinâmica do compositor na fase 1.000

- Revisados `RELEASE-CHECKLIST.md`, `PACKAGES.md` e `README-7-NOTE.md`; inspecionado o cenário Playwright existente de 1.000 entradas e os seletores reais em `index.html`.
- `tests/browser-smoke.cjs`: o cenário existente agora verifica também que o primeiro chip da composição persiste como o mesmo nó DOM durante 1.000 entradas no modo Livre, além da estabilidade do painel de conquistas. Commits `62cda8c` e `35cf5ed` (correção do seletor para `#sequence-chips`).
- Consulta de execuções associadas ao commit anterior `e4d80a6` pelo conector retornou lista vazia; esse endpoint consulta apenas execuções disparadas por pull request, portanto **não comprova ausência nem aprovação de CI por push**. A execução nova Chromium/WebKit e os artefatos precisam ser conferidos na página GitHub Actions.
- **Validação pendente:** teste Playwright ainda não executado localmente nesta rodada; conferir Chromium/WebKit e os jobs de empacotamento posteriores a `35cf5ed`. Nenhuma medição física ou publicação realizada.
- **Próximo foco:** verificar CI novo, investigar eventual falha e medir reprodução/entrada real da fase 1.000 no modo Memória.

## Evolução de 23/11/2026 — auditoria dinâmica de toda a campanha

- Confirmado que a execução de testes [45107120638](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45107120638), referente ao commit `82d2d07`, terminou com **success** em 23/11/2026 às 03:39 UTC. Este resultado é anterior à alteração desta seção.
- Em `tests/browser-smoke.cjs`, acrescentada auditoria no motor efetivamente carregado pela interface: todas as 1.000 fases têm exatamente seu número de notas, preservam integralmente o prefixo anterior, respeitam as nove notas iniciais aprovadas e rejeitam a fase 1.001. Commit `a55c521`.
- **Validação pendente:** verificar o CI Chromium/WebKit após `a55c521`; o teste novo ainda não foi executado localmente nesta rodada. Não equivale a tocar ou ouvir fisicamente as mil notas, nem valida instalações em aparelhos reais.
- **Próximo foco:** conferir os resultados deste commit, depois medir reprodução da sequência completa no modo Memória e revisar artefatos e Android Lint atualizados.


## Evolução de 23/11/2026 — transição real da última nota

- Verificados os três documentos de lançamento e os resultados mais recentes do Actions: testes no commit `441d869` [45112729241](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45112729241) **success**; testes no commit `a55c521` [45112717954](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45112717954) **success**; pacotes no commit `a55c521` [45112717971](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45112717971) **success**. Esses resultados são anteriores à nova alteração.
- Acrescentada verificação dinâmica no Playwright da transição do penúltimo e último acerto da fase 1.000 pelo motor real: resultado `correct` antes da última nota, `win` na última, `completed=true`, 1.000 vitórias, nível fixado em 1.000, conquista final desbloqueada, persistência após serialização e impossibilidade de pontuar novamente após concluir. Commits `05bd4bc` e `62581d4` (ajuste da ordem dos parâmetros).
- **Validação pendente:** conferir o workflow de testes disparado após `62581d4`, em Chromium e WebKit. Não foi executado Playwright local, teste físico ou medição de reprodução sonora de 1.000 notas nesta rodada.
- **Próximo foco:** conferir CI e artefatos atualizados; medir reprodução/latência da fase 1.000 no modo Memória sem reduzir a duração real da sequência; continuar revisão de instalação e política de privacidade.

## Evolução de 23/11/2026 — cancelamento ao navegar e retorno do histórico

- Conferidos `RELEASE-CHECKLIST.md`, `PACKAGES.md`, `README-7-NOTE.md` e os jobs do Actions. Testes no commit `fe4e62f` [45119180101](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45119180101): **4/4 success**, incluindo Chromium e WebKit. Empacotamento anterior [45119168336](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45119168336): **4/4 success**, incluindo Android/TV, Windows e iOS. Esses resultados **precedem** as alterações desta seção.
- `app.js`: adicionada limpeza explícita de reprodução em `pagehide` (navegação e entrada em bfcache), com invalidação dos timers pendentes e liberação da interface. Commit `b81054f`.
- `tests/browser-smoke.cjs`: adicionado teste dinâmico de cancelamento durante a reprodução, verificando que o botão volta a ficar disponível e não bloqueia após um timer pendente. Commit `10522e3`.
- **Validação pendente:** conferir Chromium/WebKit e os pacotes nos workflows dos commits novos. Não foram executados testes locais nem em hardware físico nesta rodada; `pagehide` sintético não substitui ensaio de navegação real em iOS/Android.
- **Próximo foco:** validar CI novo e medir reprodução da sequência de mil notas no modo Memória; revisar instalação e assinatura dos artefatos sem publicar.

## Evolução de 23/11/2026 — navegação real e retorno do histórico

- Confirmados os workflows mais recentes do commit `330fc1f`: testes [45125124105](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45125124105) **success** e pacotes [45125110255](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45125110255) **success**. Esses resultados são anteriores ao teste novo.
- Adicionado em `tests/browser-smoke.cjs` cenário de **navegação real** durante reprodução da Memória: abrir `privacy.html`, voltar pelo histórico, confirmar que a reprodução pode ser reiniciada e que timers anteriores não voltam a bloquear o botão. Complementa o evento `pagehide` sintético já testado. Commit `028fe50`.
- **Validação pendente:** conferir Chromium/WebKit após este commit; não foi executado Playwright local nem teste em aparelho físico nesta rodada. A navegação real do navegador automatizado não substitui o ensaio de bfcache em Safari/iOS nativo.
- **Próximo foco:** investigar eventual falha no novo CI e medir a reprodução da sequência de 1.000 notas no modo Memória, além de revisar assinaturas e testes físicos.

## Evolução de 23/11/2026 — interrupção da reprodução na fase 1.000

- Conferidos os três documentos obrigatórios e os resultados recentes do GitHub Actions: testes [45133845024](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45133845024) e pacotes [45133832793](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45133832793) concluídos com **success** nos commits anteriores.
- Ampliado o teste real de navegador em `tests/browser-smoke.cjs`: inicia a reprodução da fase 1.000 no modo Memória, verifica bloqueio durante a execução, simula `pagehide`, verifica desbloqueio e ausência de vitória indevida após temporizadores pendentes. Commit `1ea5f43`.
- O tempo impresso é apenas o tempo do cenário incluindo a espera de cancelamento; **não é benchmark de reprodução de 1.000 notas**, que exigiria duração prolongada e testes em dispositivos reais.
- **Validação pendente:** conferir os jobs Chromium/WebKit disparados pelo novo commit e os artefatos; não foram realizados testes locais nem físicos nesta rodada.
- **Próximo foco:** validar CI atualizado, depois medir reprodução integral e estabilidade do áudio na fase 1.000, sem alterar outras branches.
