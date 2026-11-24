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


## Evolução de 23/11/2026 — recuperação quando Web Audio API está indisponível

- Conferidos os documentos `RELEASE-CHECKLIST.md`, `PACKAGES.md` e `README-7-NOTE.md`. As execuções anteriores [45145919766](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45145919766) (testes Chromium/WebKit/regras/ZIP) e [45145920174](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45145920174) (validação, Android/TV, Windows e iOS) apresentaram **4/4 jobs success** cada. Os resultados são anteriores ao novo teste.
- Adicionado cenário Playwright em `tests/browser-smoke.cjs` com uma página isolada sem `AudioContext` nem `webkitAudioContext`: o modo Memória deve exibir mensagem explicativa, liberar novamente o botão de reprodução e manter nível, vitórias e derrotas inalterados. Commit `36386ab`.
- **Validação pendente:** conferir Chromium/WebKit após o commit; o teste não foi executado localmente nem em aparelho físico nesta rodada. Não foram alterados os arquivos de outros aplicativos.
- **Próximo foco:** verificar CI atualizado e depois avançar para ensaio de reprodução integral e estabilidade do áudio das mil notas em hardware real, sem confundir cancelamento com execução completa.


## Evolução de 23/11/2026 — recuperação de contexto de áudio encerrado

- Lidos `RELEASE-CHECKLIST.md`, `PACKAGES.md` e `README-7-NOTE.md`; conferidos os resultados atuais do GitHub Actions. Testes no commit `42cda582` [45152631426](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45152631426) **success**; pacotes no commit `36386ab2` [45152598713](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45152598713) **success**. Esses resultados precedem esta alteração.
- Corrigido `app.js`: ao encontrar `AudioContext.state === "closed"`, o jogo recria o contexto na próxima tentativa, em vez de reutilizar permanentemente um contexto impossível de retomar. Commit `3777fa5`.
- Adicionado teste de navegador `tests/browser-smoke.cjs`: primeira tentativa recebe um contexto encerrado e falha de forma recuperável; segunda tentativa usa um contexto válido e chega ao estado de entrada, preservando nível e vitórias. Commit `1f0c12c`.
- **Validação pendente:** confirmar os novos jobs Chromium/WebKit e empacotamento após os commits; os testes não foram executados localmente nem em dispositivo físico nesta rodada. A simulação de contexto encerrado não substitui um ensaio de interrupção de áudio real no iOS/Android.
- **Próximo foco:** inspecionar o CI desta revisão, medir reprodução integral de 1.000 notas no modo Memória e verificar estabilidade do áudio em aparelhos reais.


## Evolução de 23/11/2026 — interrupção de áudio durante a reprodução

- Conferidos os documentos `RELEASE-CHECKLIST.md`, `PACKAGES.md` e `README-7-NOTE.md`. Os workflows anteriores concluíram com **success**: [testes 45158346471](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45158346471) (commit `53eecd3`) e [pacotes 45158325226](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45158325226) (commit `1f0c12c`).
- Identificado risco adicional: durante uma sequência longa, o contexto de áudio pode deixar de estar `running` depois da preparação inicial. O laço continuava silenciosamente e podia liberar a fase de resposta como se as notas tivessem sido ouvidas.
- `app.js`: interromper a reprodução e devolver o estado para `idle` quando o áudio parar durante a sequência ou antes da transição final para entrada; informar ao jogador que deve reproduzir novamente. Commit `2845ee1`.
- `tests/release-readiness.test.cjs`: regressão estática para exigir verificações durante e ao final da reprodução. Commit `f6bd839`.
- **Validação pendente:** conferir GitHub Actions após estes commits, especialmente Chromium/WebKit; teste estático não simula uma interrupção real de hardware. Não foi executado teste físico nem afirmada aprovação dos commits atuais.
- **Próximo foco:** cenário dinâmico de interrupção do AudioContext durante a fase 1.000 e medição real da reprodução completa, além de validar artefatos de publicação.

## Evolução de 23/11/2026 — falha transitória de dependências Android no CI

- Lidos `RELEASE-CHECKLIST.md`, `PACKAGES.md` e `README-7-NOTE.md` e conferidos jobs e logs reais do Actions.
- Testes [45167538293](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45167538293): **4/4 success** (regras, Chromium, WebKit e ZIP), validando a revisão anterior de áudio.
- Pacotes [45167538036](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45167538036): **3/4 success**. Validação, iOS e Windows aprovados; **Android/TV falhou antes de gerar artefatos**. Log do job `137668465024` mostra HTTP **429 Too Many Requests** do Maven Central ao buscar `com.google.code.findbugs:jsr305:3.0.2`. Não há evidência nessa execução de erro de Android Lint ou código do aplicativo: a resolução de dependências foi interrompida antes.
- Alterado exclusivamente `.github/workflows/packages-7-note.yml` para repetir a execução completa do Gradle até três vezes, com esperas progressivas de 45 e 90 segundos; falhas persistentes continuam retornando erro. Commit `b22132d`.
- **Validação pendente:** conferir nova execução do workflow no commit `b22132d` e exigir APK/AAB Android/TV e relatórios Lint válidos; o retry não prova que o serviço externo responderá. Não houve teste físico, assinatura ou publicação.
- **Próximo foco:** se a falha 429 persistir, investigar cache/dependências e disponibilidade do Maven Central; depois implementar teste dinâmico de interrupção de áudio na fase 1.000.

## Evolução de 23/11/2026 — CI Android recuperado e interrupção dinâmica na fase 1.000

- Reconsultados `RELEASE-CHECKLIST.md`, `PACKAGES.md` e `README-7-NOTE.md` e os jobs mais recentes do GitHub Actions.
- A execução de pacotes após o retry de Maven [45176404003](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45176404003), no commit `b22132d`, concluiu com **4/4 success**, inclusive o job **Android 36 e TV 35 - APK e AAB**. O bloqueio transitório HTTP 429 não reapareceu nessa execução. Isso confirma o job de compilação, não testes em aparelhos nem assinatura de release.
- Testes no commit `18729ba`: [45176430499](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45176430499), **4/4 success**, incluindo Chromium, WebKit, regras e ZIP.
- Acrescentado cenário **dinâmico** em `tests/browser-smoke.cjs`: iniciar a reprodução da fase 1.000, simular `AudioContext.state` suspenso durante o laço, aguardar mensagem de interrupção, confirmar desbloqueio e ausência de alteração de nível, vitórias ou derrotas. Commits `3f87ad3` e `6eeb171`. O teste não reproduz as mil notas integralmente nem simula hardware real.
- **Validação pendente:** aguardar o workflow Chromium/WebKit do commit `6eeb171`; não foram executados Playwright local ou testes físicos nesta rodada. Inspecionar também os artefatos efetivos do workflow Android/TV antes de afirmar integridade de pacotes.
- **Próximo foco:** corrigir eventual regressão no CI novo, auditar os artefatos Android/TV e medir reprodução integral e estabilidade da fase final em hardware real. Permanecem pendentes assinatura, políticas/contato públicos confirmados e revisão humana das lojas.


## Evolução de 23/11/2026 — auditoria de bundles Android/TV

- Conferidos `RELEASE-CHECKLIST.md`, `PACKAGES.md` e `README-7-NOTE.md` na branch isolada. Execuções anteriores [testes 45188222494](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45188222494) e [pacotes 45188222363](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45188222363): **4/4 jobs success** cada, incluindo Chromium, WebKit e Android/TV.
- Consultada a lista efetiva de artefatos do empacotamento anterior: **8 artefatos não expirados**, incluindo os dois APK debug, os dois AAB sem assinatura, relatório Lint, iOS simulador, iOS dispositivo sem assinatura e Windows x64. A presença de artefato não substitui inspeção binária nem instalação física.
- Reforçada a etapa de auditoria de `.github/workflows/packages-7-note.yml`: cada AAB agora precisa passar por `unzip -t`, conter `base/assets/game-core.js`, `base/assets/privacy.html` e `base/manifest/AndroidManifest.xml`; o workflow também imprime SHA-256 de cada AAB e APK para rastreabilidade. Commit `da420ba9`.
- **Validação pendente:** confirmar o próximo job Android/TV após `da420ba9`, inclusive nomes reais dos AAB e estrutura interna; ainda não foi executado o novo passo, nem download/instalação dos binários ou teste físico nesta rodada. Se o padrão de nome diferir, ajustar o caminho sem relaxar as verificações.
- **Próximo foco:** conferir o CI atualizado e os hashes; seguir com reprodução integral das mil notas e preparação de fichas com screenshots reais. Permanecem pendentes assinatura, testes físicos, política/contato públicos confirmados e revisão humana.

## Evolução de 23/11/2026 — integridade e rastreabilidade dos APK/AAB

- Reconsultados `RELEASE-CHECKLIST.md`, `PACKAGES.md`, `README-7-NOTE.md` e os jobs conhecidos mais recentes: [testes 45188222494](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45188222494) e [pacotes 45188222363](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45188222363), ambos com quatro jobs `success`. **Essas execuções são anteriores ao novo commit**; a aprovação da nova auditoria não foi verificada.
- No workflow exclusivo do jogo, adicionada verificação `unzip -t` dos dois APK debug; mantidas as verificações de integridade e recursos dos dois AAB. Gerado `SHA256SUMS.txt` contendo hashes dos quatro binários e publicado como artefato `7-note-Android-TV-SHA256` com retenção de 30 dias. Commit `62880437`.
- **Validação pendente:** conferir o próximo job Android/TV e a existência do artefato de checksums; verificar os quatro hashes contra os binários baixados. Não foi executado Gradle localmente, nem download de pacotes ou teste em dispositivos reais nesta rodada. Nenhum APK/AAB foi assinado ou enviado a lojas.
- **Próximo foco:** investigar eventual falha no novo passo de auditoria e prosseguir com testes reais da fase 1.000 e revisão de distribuição.

## Evolução de 23/11/2026 — checksums portáteis Android/TV

- Conferidos os três documentos obrigatórios. O último workflow de testes no commit `772764d` foi **success** ([45211650807](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45211650807)), com quatro jobs aprovados. O empacotamento anterior no commit `6288043` foi **success** ([45211603196](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45211603196)); não valida as mudanças atuais.
- Detectada limitação operacional: o arquivo SHA256SUMS.txt anterior continha caminhos internos do runner, impossibilitando verificação direta após extrair os artefatos separados.
- Alterado o workflow para copiar os quatro APK/AAB para uma área de auditoria, gerar hashes com nomes de arquivo portáteis e executar `sha256sum --check --strict SHA256SUMS.txt` antes do upload. Commit `8162cb6`.
- Acrescentado teste estático para exigir manifesto portátil e validação de hashes. Commit `49a4e08`.
- **Validação pendente:** conferir execução dos novos jobs, existência do artefato SHA256 e teste real de `sha256sum -c` com os quatro arquivos baixados. O ZIP dos checksums contém apenas o manifesto, não os APK/AAB; baixar os quatro pacotes separadamente e colocá-los no mesmo diretório do manifesto antes de conferir.
- Não foram alterados código do jogo, 1.000 fases, quatro modos, `main` ou `24-h2o-android`. Sem testes físicos, assinaturas ou publicação.

## Evolução de 23/11/2026 — verificador reproduzível de APK/AAB

- Reconsultados os três documentos obrigatórios e os jobs de [testes 45218199845](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45218199845) e [pacotes 45218199751](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45218199751): **4/4 jobs success** em cada workflow. O empacotamento disponibilizou nove artefatos não expirados, incluindo manifesto SHA256, quatro pacotes Android/TV e relatório Lint.
- Adicionado `scripts/verify-android-packages.cjs` para conferir localmente os quatro pacotes baixados da mesma execução, exigindo manifesto de exatamente quatro entradas, nomes conhecidos, hashes válidos, arquivos regulares não vazios e correspondência SHA-256. Commit `b2e7f6b`.
- Adicionado teste automatizado com arquivos temporários que verifica sucesso, adulteração, entrada duplicada e arquivo ausente. Commit `afcfb79`.
- Documentado o procedimento em `PACKAGES.md`, inclusive o alerta para não misturar artefatos de execuções diferentes. Commit `3d5eb85`.
- **Validação pendente:** os jobs aprovados são anteriores aos novos commits; conferir o próximo CI para o teste adicionado e executar o verificador contra os binários efetivamente baixados. Não foram baixados nem instalados APK/AAB nesta rodada. Os hashes não substituem assinatura nem testes em hardware.
- **Próximo foco:** conferir novo CI, depois reproduzir e medir integralmente a sequência de mil notas em Chromium/WebKit e equipamentos reais, mantendo pendentes as assinaturas, a política pública/contato confirmados e a revisão de loja.

## Evolução de 23/11/2026 — proteção do manifesto SHA-256

- Reconsultados `RELEASE-CHECKLIST.md`, `PACKAGES.md` e `README-7-NOTE.md` na branch exclusiva. Conferidos os jobs das execuções anteriores [testes 45218199845](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45218199845) e [pacotes 45218199751](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45218199751): **4/4 success** em cada uma. Essas execuções são anteriores às alterações desta seção.
- Identificado risco residual no verificador local: `SHA256SUMS.txt` era lido sem limitar seu tamanho e podia ser um link simbólico. Agora é exigido arquivo regular, não vazio, com até 512 bytes, suficiente para exatamente quatro entradas SHA-256. Commit `3e0eeaf`.
- Acrescentados testes de regressão para manifesto acima do limite e manifesto como link simbólico, além dos casos existentes de alteração de binário, duplicata e arquivo ausente. Commit `33407ad`.
- **Validação pendente:** conferir CI Chromium/WebKit e empacotamento após `33407ad`; a consulta de status combinado do commit retornou lista vazia e não prova sucesso ou ausência de workflows. Não foram executados testes locais nem verificações com APK/AAB reais nesta rodada.
- **Próximo foco:** validar os testes novos, baixar os quatro pacotes e manifesto da mesma execução e verificar os hashes; continuar ensaios de áudio da fase 1.000 e testes físicos. Nenhuma assinatura, publicação ou alteração em outros aplicativos foi realizada.

## Evolução de 23/11/2026 — saída atômica do verificador SHA-256

- Conferido CI anterior na branch exclusiva: testes [45240024062](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45240024062) **success** no commit `87fc0b14`; empacotamento [45239969726](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45239969726) **success** no commit `33407ade`. São resultados anteriores às alterações atuais.
- Corrigido `scripts/verify-android-packages.cjs`: antes, imprimia `OK` para os primeiros pacotes mesmo quando o quarto falhava, criando uma saída parcialmente enganosa. Agora só anuncia os quatro `OK` após a validação completa dos quatro hashes. Commit `3a9dd563`.
- Adicionado teste de regressão em `tests/release-readiness.test.cjs` que corrompe o último pacote, exige ausência de mensagens `OK` e confirma quatro mensagens apenas na verificação integral. Commit `d00e8540`.
- **Validação pendente:** conferir os workflows disparados após os novos commits; nenhuma execução local ou teste físico foi afirmado nesta rodada.
- **Próximo foco:** validar CI e baixar/verificar os quatro binários reais do mesmo empacotamento, sem misturar execuções; continuar testes físicos e preparação para lojas.


## Evolução de 23/11/2026 — verificação SHA-256 com memória limitada

- Reconsultados os três documentos obrigatórios e os jobs conhecidos mais recentes: [testes 45250668254](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45250668254) e [pacotes 45250668238](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45250668238), ambos com **4/4 jobs success**, inclusive Chromium, WebKit, Android/TV, iOS e Windows. São resultados anteriores à mudança atual.
- Corrigido `scripts/verify-android-packages.cjs`: o cálculo SHA-256 antes usava `readFileSync` para carregar cada APK/AAB inteiro na memória. Agora utiliza leitura sequencial em blocos de 1 MiB, mantendo o resultado SHA-256, as validações e a saída atômica. Commit `ce106935`.
- Acrescentada regressão estática em `tests/release-readiness.test.cjs` para impedir retorno ao carregamento integral. Commit `ba008d70`.
- **Validação pendente:** confirmar CI dos novos commits e executar o verificador com os quatro APK/AAB reais baixados de uma mesma execução. Não foram baixados pacotes nem executados testes físicos nesta rodada; os testes anteriores não aprovam automaticamente esta alteração.
- **Próximo foco:** conferir CI e manifesto de hashes reais; continuar testes de áudio, offline e hardware e preparar metadados de lojas sem publicar. Permanecem pendentes assinaturas, política e suporte públicos confirmados e revisão humana.

## Evolução de 23/11/2026 — validação do descritor APK/AAB

- Reconsultados RELEASE-CHECKLIST.md, PACKAGES.md e README-7-NOTE.md na branch exclusiva. Os jobs conhecidos de [testes 45250668254](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45250668254) e [pacotes 45250668238](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45250668238) terminaram com quatro jobs success cada, mas antecedem esta revisão.
- Identificada janela de troca de arquivo entre lstatSync e openSync no verificador SHA-256. O descritor aberto agora passa por fstatSync, que exige arquivo regular não vazio e identidade dev/ino igual à observada antes da abertura. Commit `1baf9214`.
- Acrescentada regressão estática para fstatSync, tipo, identidade e fechamento do descritor. Commit `4eed761f`.
- **Validação pendente:** confirmar GitHub Actions dos novos commits, executar teste dinâmico de troca de caminho e baixar os quatro binários reais com manifesto da mesma execução para verificação independente. Nenhum teste local/físico nem assinatura/publicação foi realizado nesta rodada.
- **Próximo foco:** priorizar CI, depois verificação real de APK/AAB, áudio na fase 1000, acessibilidade e dispositivos físicos. As pendências externas de identidade, políticas públicas, suporte e contas de loja permanecem.


## Evolução de 23/11/2026 — verificador Node aplicado aos binários do CI

- Reconsultados os três documentos obrigatórios. Jobs conhecidos de testes 45250668254 e pacotes 45250668238: 4/4 success em cada execução; anteriores à revisão atual.
- O workflow de pacotes agora executa `node scripts/verify-android-packages.cjs native/android/checksums` sobre os quatro APK/AAB reais preparados pelo build, depois da validação independente com sha256sum e antes do upload. Commit `c8857607`. Uma divergência impede a distribuição dos artefatos.
- Validação pendente: confirmar a execução do novo passo no Android/TV. A consulta de workflows por commit é limitada a eventos de pull request e não confirma o resultado de push. Não houve download independente nem teste físico nesta rodada. Assinaturas e requisitos das lojas continuam pendentes.

## Evolução de 24/11/2026 — recuperação de áudio no treino de ouvido

- Reconsultados os três documentos obrigatórios; jobs conhecidos 45250668254 e 45250668238: 4/4 success cada, anteriores à correção. A consulta por commit é limitada a pull requests e não valida CI por push.
- Corrigido app.js: verifica áudio ativo e visibilidade antes e depois da nota, mantém respostas bloqueadas em caso de interrupção, aguarda 640 ms para nota de 620 ms e repete a mesma pergunta sem trocar o alvo. Botão secundário usa o mesmo fluxo protegido. Commit 6d544788.
- Acrescentada regressão estática em tests/release-readiness.test.cjs, commit f1e0d12f. CI novo ainda não confirmado; nenhum teste local ou físico realizado. Próximo foco: teste dinâmico de interrupção em Chromium/WebKit e auditoria dos binários reais. Campanha, pontuações, anúncios e outros aplicativos preservados.

## Evolução de 24/11/2026 — CI confirmado e regressão dinâmica do Ouvido

- Reconsultados os três documentos obrigatórios. Testes [45327433336](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45327433336) e pacotes [45327421756](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45327421756): quatro jobs success em cada, incluindo Chromium, WebKit, Lint Android/TV e builds iOS/Windows. Passo de auditoria Android aprovado; não houve download independente dos binários.
- Adicionado cenário Playwright de suspensão simulada do AudioContext no modo Ouvido, resposta bloqueada sem derrota e recuperação por nova reprodução, commit `55715a181`. Executado pelos dois navegadores no próximo CI; resultado ainda não confirmado nesta rodada. Não houve teste físico ou reprodução integral das mil notas.
- Próximo foco: confirmar novo CI e auditar artefatos baixados da mesma execução; manter pendências de assinatura, suporte público e revisão das lojas.

## Evolução de 24/11/2026 — recuperação de áudio validada e rótulo de repetição

- Consultados os três documentos e a lista de execuções por branch. Testes [45349707931](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45349707931) no commit `4e5f9c11` e pacotes [45349690008](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45349690008) no commit `55715a18`: quatro jobs success cada. Confirmada a regressão dinâmica anterior em Chromium/WebKit e Android Lint aprovado.
- Corrigido o rótulo do botão no estado `ready` do modo Ouvido: após interrupção, indica ouvir novamente em vez de próxima nota, sem alterar o alvo ou a pontuação. Commit `8f1f10ed`. Acrescentada asserção ao cenário Playwright existente, commit `148a936c`.
- Validação pendente: CI dos novos commits; nenhum teste local, download independente de binários ou teste físico realizado nesta rodada. Próximo foco: auditoria dos artefatos e reprodução integral da fase 1.000. Nenhuma publicação ou assinatura realizada.

## Evolução de 24/11/2026 — progresso acessível do treino de ouvido

- Reconsultados os três documentos obrigatórios e os oito jobs das execuções 45349707931 e 45349690008, todos success; resultados anteriores à revisão atual. Não foi possível confirmar execuções mais recentes por push nesta rodada.
- Corrigido app.js para não contar uma pergunta interrompida e ainda sem resposta como concluída na barra visual e aria-valuenow (commit 6d6ab774). Acrescentada asserção Playwright no cenário de interrupção existente (commit 8234e8c6).
- Validação pendente: CI Chromium/WebKit dos novos commits, auditoria independente dos binários e reprodução integral da fase 1.000. Nenhum teste local ou físico, assinatura ou publicação realizado nesta rodada.

## Evolução de 24/11/2026 — progresso do Ouvido após recuperação

- Reconsultados os três documentos obrigatórios. Confirmados 4/4 jobs success em [testes 45416329113](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45416329113) (f0b1d523) e 4/4 em [pacotes 45416312289](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45416312289) (8234e8c6), incluindo Android Lint e auditoria de APKs, Chromium/WebKit e auditoria de dependências Windows.
- Estendida regressão dinâmica do Ouvido: repetir a nota após interrupção mantém progresso em 0%; responder efetivamente avança para 10%, independentemente do acerto. Commit 46a54b57. Não altera regras ou código de produção.
- CI do novo teste ainda não confirmado; auditoria independente dos binários baixados, reprodução integral das mil notas e testes físicos permanecem pendentes. Nenhuma assinatura ou publicação realizada.
