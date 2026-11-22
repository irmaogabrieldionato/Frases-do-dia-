# Checklist Orbis — Plano mestre de desenvolvimento colaborativo para Claude, ChatGPT e outras IAs

> **Status:** plano operacional / backlog, não declaração de funcionalidades concluídas.
> **Repositório:** https://github.com/irmaogabrieldionato/Frases-do-dia-
> **Branch ÚNICA autorizada para alterações:** `checklist-orbis`; **NUNCA escrever, mesclar ou criar PR direcionado a `main`**.
> **Aplicativo:** `ChecklistOrbisAndroid/` — Android nativo Java; namespace e applicationId `com.orbis.meudia`.
> **Data de referência:** 2026-11-22. Cada agente deve verificar fatos atuais no repositório antes de qualquer mudança.

## 0. LEITURA OBRIGATÓRIA E REGRA DE PRECEDÊNCIA

Antes de programar, ler integralmente ou consultar as seções pertinentes destes documentos na própria branch:
1. `pesquisas/progresso-15-concorrentes.md` (histórico das execuções 1–139; último registro e pendências têm prioridade sobre textos antigos).
2. `pesquisas/concorrentes-checklist-orbis-2026-11-15.md` (inventário inicial, com fontes externas a reconfirmar).
3. **Este plano mestre**; `ChecklistOrbisAndroid/AGENTS.md` (protocolo de trabalho entre IAs); `ChecklistOrbisAndroid/README.md`; workflow `.github/workflows/build-checklist-orbis.yml`.
4. [Issue #2](https://github.com/irmaogabrieldionato/Frases-do-dia-/issues/2) — decisões posteriores do autor sobre mídia, contas, Supabase gratuito e postergação.
5. Código existente e testes — nunca presumir que uma característica do documento já está implementada, funcionando ou validada.

**Decisão posterior expressa, que prevalece sobre as listas antigas repetidas em prompts:** ordem competitiva atual: **1 Microsoft To Do, 2 Google Tasks, 3 Any.do, 4 Structured, 5 Taskito, 6 HabitNow, 7 Habitica, 8 Amazing Marvin, 9 Trello, 10 Notion Calendar, 11 Tasks.org, 12 Loop Habit Tracker, 13 Super Productivity, 14 TickTick, 15 Todoist**. A pesquisa histórica do Todoist continua válida como material de comparação, mas a implementação por concorrente não deve saltar a fila. Se o autor mudar expressamente essa ordem de novo, registrar a decisão com data e aplicar a versão mais recente. **Nenhum dos 15 foi declarado totalmente analisado/concluído.**

**Outra decisão posterior:** já foram implementados **2/24 incrementos de código** sem backend (evoluções 138 e 139); faltam **22 incrementos reais**, propostos como 140–161. Não contar commits só de documentação, CI, revisão ou traduções da *mesma* melhoria como novos incrementos. Depois dos 24, reavaliar o projeto Supabase **dedicado** e gratuito. Contagem por entregas de funcionalidade validadas/documentadas, não apenas por número de conversas.

## 1. ESTADO REAL VERIFICADO (não confundir com ideia futura)

- Java 17, Android compileSdk/targetSdk 35, minSdk 23; Gradle Kotlin DSL `ChecklistOrbisAndroid/app/build.gradle.kts`; pacote `com.orbis.meudia`. Tela principal `MainActivity.java`; `Schedule.java`, `TaskTime.java`, `DailySuggestions.java`, `TaskAttachments.java`, `BackupRestoreCommit.java`, `TaskDeadline.java`, `LocalReminder*.java`, `Languages.java` e `AppGuide.java`.
- Funcionalidades existentes a preservar: calendário mensal interno, navegação de datas, tarefas e descrições, recorrências e exclusões, histórico de progresso por dia 0–100%, cinco níveis de prioridade com cores/filtros, busca/organização, backups JSON, lembretes locais e manual interno offline. O aviso visual ⚠ de conflitos usa as tarefas exibidas após filtros (não cobre tarefas ocultadas por filtros).
- 15 códigos de idioma atuais: `en, zh-CN, hi, es, fr, ar, bn, ru, pt-BR, id, ur, de, ja, sw, mr`. A infraestrutura de interface contém os 15. **O manual AppGuide ainda tem textos em português/inglês e fallback em inglês nas demais línguas**; corrigir sem reduzir idiomas. Árabe e urdu requerem RTL e teste de acessibilidade.
- Backup JSON **não carrega bytes de fotos/vídeos**; não prometer restauração de mídia na nuvem ou entre aparelhos. As referências locais são sanitizadas em exportação/importação.
- O esquema `pesquisas/supabase-checklist-orbis-schema-proposto.sql` e seus testes de segurança estão **propostos, não aplicados**. Login real/sync cross-device ainda não implantados; jamais anunciar como funcionamento real.
- Assinatura release estável, testes instrumentados/em telefone e pacote pronto para loja **pendentes**. APKs debug não são prova de release comercial assinada.
- **CI verificado**: o [run 45009488099](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/45009488099), SHA `d29b8b29850d087450000b377169f5a9e9d609ca`, estava `completed/success` em 2026-11-22. Verificar o SHA exato de *todo commit posterior*; não reutilizar esse resultado para código novo.
- `ChecklistOrbisAndroid/README.md` contém uma descrição histórica da versão 1.4 e pode estar defasado; atualizar conforme mudanças verificadas, sem reescrever histórico.
- O workflow só inicia automaticamente quando um push em `checklist-orbis` muda `ChecklistOrbisAndroid/**` ou `.github/workflows/build-checklist-orbis.yml`. **Alterar somente `pesquisas/**` não executa CI automaticamente.**

## 2. INVARIANTES INEGOCIÁVEIS

1. **Agenda própria e offline:** jamais conectar, autenticar, consultar, sincronizar, importar, inserir SDK ou API do Google Agenda; não copiar marca, ícones, UI ou código de terceiros. O fato de o Android ser uma plataforma Google não é permissão para conectar ao Google Agenda.
2. **Preservar dados**: não trocar `applicationId`, nome das preferências `meu-dia-dados-v1`, IDs históricos de tarefas, estrutura de `history`, regras de recorrência ou dados sem migração comprovadamente reversível. Não apagar informações do usuário ao atualizar.
3. **15 idiomas e acessibilidade**: nenhuma nova ação de interface importante sem texto acessível e rota de tradução para todos os idiomas; testar RTL para `ar` e `ur`, TalkBack, contraste, zoom de fonte, toque ≥ 48dp quando aplicável.
4. **Segurança**: nenhum token, senha, segredo, keystore, `service_role` ou credencial privada no Git, logs, APK ou comentários. Chave publicável do Supabase só quando houver projeto autorizado e RLS validada. Não coletar dados nem enviar telemetria sem consentimento.
5. **Supabase**: somente projeto próprio Checklist Orbis e plano gratuito durante testes; **NÃO** tocar projeto `24 H2O`; não contratar, subir de plano, pagar, conectar billing ou gerar cobrança. Retomada depois dos 24 incrementos, ou por nova ordem expressa do autor.
6. **Mídia futura por conta**: fotos e vídeos, quando houver sync, pertencerão à conta remota, acessíveis entre aparelhos via login. Cache local offline não substitui armazenamento principal. A solução somente local pode ser prototipada como UI, mas não anunciada como produto final multiaparelho.
7. **Sem copiar concorrentes**: pesquisa de documentação oficial para inspirar requisitos; escrever algoritmos, UI, desenhos e textos originais, sob licença compatível e com revisão de dependências.
8. **Uma melhoria por gate**: implementar funcionalidade, testar, corrigir, confirmar regressões e CI para SHA final; só depois aceitar o próximo incremento. Artefatos de CI podem expirar; registrar IDs e SHA.
9. **Não alterar `main`** direta ou indiretamente, nem abrir PR para essa branch. Se houver proposta de revisão, base e head devem ser `checklist-orbis` e branch temporária somente com autorização específica; padrão preferido é commit pequeno direto em `checklist-orbis`, após confirmar exclusividade do arquivo.
10. **Separar níveis de evidência**: teste JVM, CI Android, instalação manual, instrumentação, teste de segurança remoto e release assinado são gates distintos. Nunca chamar build de teste físico.

## 3. DISTRIBUIÇÃO DO TRABALHO ENTRE IAs — EVITAR COLISÕES

| Papel sugerido | Entrega principal | Áreas preferenciais | Limites |
|---|---|---|---|
| Agente pesquisador (Claude ou outro) | Matriz de lacunas, fontes oficiais datadas, impacto, licenças | `pesquisas/**` | Sem alterar Java simultaneamente |
| Agente de interface Android | UX, manual, calendário, acessibilidade e idiomas | `AppGuide.java`, `Languages.java`, UI | Obtém exclusividade de `MainActivity.java` antes de editar |
| Agente de lógica | Recorrência, horários, metas numéricas, backup | Classes Java puras em `com.orbis.meudia` | Evita modificar UI sem integração combinada |
| Agente de testes / QA | Testes JUnit, cenários regressivos, matriz aparelhos | `app/src/test/**`, plano de QA | Não declara teste em dispositivo sem executá-lo |
| Agente de segurança / dados | Privacidade, backups, RLS futura, threat model | `pesquisas/**`, SQL proposto, revisão estática | Não executar SQL remoto antes do gate |
| **Integrador único** | Confere branch/SHA, aplica commits, verifica CI, atualiza progresso | Arquivos afetados, documentação | Somente um integrador grava na branch compartilhada por vez |

**Protocolo para Claude e demais agentes**
- Abrir/consultar uma issue ou comentário de coordenação com tarefa, arquivos pretendidos, dependências, critérios de aceite e SHA-base. Declarar: `EM ANÁLISE` → `EM IMPLEMENTAÇÃO` → `PRONTO PARA REVISÃO` → `CI VALIDADO` → `CONCLUÍDO`; estados `BLOQUEADO` e `REGRESSÃO` quando necessário.
- **Nunca editar em paralelo o mesmo arquivo.** `MainActivity.java` e `Languages.java` são pontos de conflito: reservar o arquivo para um agente/integrador. Outros enviam plano ou patch, não push concorrente.
- Antes de cada push: obter HEAD atual da `checklist-orbis`, conferir que a base não mudou, reexaminar diffs, manter commit pequeno e com mensagem `feat(orbis): ...`, `fix(orbis): ...`, `test(orbis): ...` ou `docs(orbis): ...`. Não fazer `force push` nem reescrever histórico.
- Trabalhar com alterações atômicas: lógica e testes separados quando apropriado, CI do **SHA final**, documentação com links verificáveis. Se falhar o CI, priorizar reparo antes de nova funcionalidade.
- Ao reportar: descrever exatamente o que foi mudado, testes realmente executados, commit SHA, URL do run, URL/ID do artefato (se houver), riscos e próxima tarefa. Não inventar aprovação, APK, integração ou compatibilidade.
- Critério de aceite exige: alteração real + cenários positivos/negativos + preservar dados + traduções e acessibilidade para nova UI + aprovação CI + anotação das pendências físicas. Se CI não concluído, estado **aguardando validação**, não `CONCLUÍDO`.
- Se o usuário enviar outra ordem mais recente, registrá-la e atualizar este plano, issue e log antes de iniciar novo recurso.

## 4. BACKLOG ATÔMICO — 22 INCREMENTOS LOCAIS RESTANTES (140–161)

Cada `ORB-xxx` = **uma alteração testável de produto/código**. Numeração é organizacional; não renomear trabalhos concluídos se houver execuções intermediárias. A seguir estão objetivos, classes prováveis, casos de teste e critérios de saída. Nenhum item abaixo é considerado entregue apenas por constar no plano.

### ORB-140 — Legenda acessível do conflito ⚠
**Implementar** descrição verbal original (ex.: “Conflito de horário”) no cartão da tarefa, sem comunicar o estado exclusivamente por símbolo/cor. **Arquivos**: `MainActivity.java`, `Languages.java`, testes de `TaskTime` já existentes. **Teste**: tarefas sobrepostas, adjacentes, sem horário, ocultas por filtro; `contentDescription` ou texto visível acessível; RTL. **Aceite**: feedback para leitor de tela e 15 idiomas, sem alterar dados.

### ORB-141 — Detalhes dos conflitos e navegação orientada
**Implementar** ação no aviso de conflito que explique quais tarefas visíveis colidem e seus intervalos, sem editar automaticamente horários. **Arquivos**: `TaskTime.java`, `MainActivity.java`, novas provas unitárias do modelo de conflito. **Teste**: três tarefas simultâneas, janelas consecutivas, títulos longos e lista vazia. **Aceite**: contagens corretas, títulos sem duplicação, rolagem/diálogo acessível.

### ORB-142 — Manual multilíngue completo (15 idiomas)
**Implementar** localização integral do `AppGuide.java` usando chaves `Languages.java`, em vez de fallback inglês para 13 línguas. **Teste**: todas as chaves possuem 15 entradas, não exibem chaves internas ou `null`; verificar `ar`/`ur` RTL. **Aceite**: texto coerente para todos os códigos atuais e nenhuma regressão no botão “Como usar”.

### ORB-143 — Manual contextual com exemplos de personalização
**Implementar** exemplos autorais interativos ou seções recolhíveis de calendário, cores, horário, duração, repetição, porcentagem, projetos, etiquetas e backup. Não induzir ações que alterem dados reais; separar demonstração de produção. **Teste**: abrir/fechar, rolagem, telas pequenas, fonte grande e offline. **Aceite**: explicação dos desenhos/ícones e acessibilidade sem internet.

### ORB-144 — Pré-visualização de backup antes da restauração
**Implementar** resumo seguro com contagem de tarefas e alerta explícito de substituição *antes* do commit de restauração. **Arquivos**: `BackupRestoreCommit.java`, `MainActivity.java`, testes. **Teste**: JSON vazio, versão anterior, anexo local omitido, seleção cancelada, repetição. **Aceite**: nenhuma mutação antes de confirmação.

### ORB-145 — Restauração defensiva e compatibilidade histórica
**Implementar** validação adicional de JSON (campos opcionais/limites/duplicatas/tipos) mantendo compatibilidade com backups legados; tratamento de erro sem perda do estado atual. **Teste**: arquivo truncado, UTF-8, chaves ausentes, histórico de recorrência, arquivo > 5 MiB. **Aceite**: all-or-nothing e mensagens compreensíveis.

### ORB-146 — Exportação CSV opcional de tarefas/históricos
**Implementar** CSV local original separado do backup JSON; RFC 4180 (aspas, vírgulas, CRLF, Unicode); campos e datas documentados, sem mídia nem credenciais. **Teste**: texto com aspas, quebras de linha, emojis, aspas duplas, sem tarefas. **Aceite**: CSV exportável; não confundir CSV com formato de restauração.

### ORB-147 — Configuração e permissão clara de lembretes locais
**Implementar** UX para permissões Android 13+, estado concedido/negado, reagendamento quando aplicável e explicação no manual; sem alarmes de localização/serviço externo. **Teste**: permissão negada, Android anterior, atividade reaberta e nenhuma notificação duplicada. **Aceite**: app funciona com permissão negada.

### ORB-148 — Robustez de intervalos, virada de dia e fuso horário
**Implementar** comportamento explicitamente documentado para intervalos atravessando 00h e mudanças de fuso sem dupla contagem ou falsos conflitos. **Teste**: 23:50–00:10, DST/fusos diferentes, horários inválidos, recorrência. **Aceite**: operações determinísticas e sem alterar registros antigos.

### ORB-149 — Navegação do calendário acessível e desempenho
**Implementar** rótulos de dia/mês, foco e estados selecionados, contrastes e suporte a fonte grande, mantendo 15 idiomas. **Teste**: meses com 28–31 dias, ano bissexto, rotatividade, TalkBack em aparelho a executar. **Aceite**: interação de calendário sem depender de cor.

### ORB-150 — Visão semanal autoral
**Implementar** visão semanal derivada do mesmo modelo de dados/recorrência, com datas, indicadores e tarefas; não criar segunda fonte de verdade. **Teste**: transição entre anos e meses, segunda/domingo conforme locale, filtros e histórico separado. **Aceite**: alternar mensal/semanal sem alterar dados.

### ORB-151 — Visão diária com linha do tempo
**Implementar** lista autoral em ordem cronológica, duração, sobreposição sinalizada e tarefas sem horário em região separada. **Teste**: ordem estável, tarefas simultâneas, duração faltante, filtros. **Aceite**: acessível e compatível com dados legados.

### ORB-152 — Filtros salvos e reset seguro
**Implementar** nomes/presets locais de filtros combinados com ordenação, pesquisa e prioridade, sem persistir dados de tarefa nesses presets. **Teste**: preset inválido, campos desconhecidos, mudanças de tags/projetos. **Aceite**: reset recuperável; filtros não excluem tarefas.

### ORB-153 — Modelo de metas numéricas offline
**Implementar** campos opcionais de unidade/quantidade alvo/realizada (páginas, repetições, km etc.) com migração compatível; preservar modo 0–100% atual. **Arquivos**: classe Java pura nova e serialização. **Teste**: zero, negativo, decimal, excesso de meta, unidade ausente, leitura de dados antigos. **Aceite**: nenhum histórico apagado.

### ORB-154 — Editor e visualização de metas numéricas
**Implementar** inputs de metas na UI e progresso calculado previsivelmente, com distinção entre progresso manual e quantitativo. **Teste**: mudança de unidade/alvo, alvo zero, teclado, validação e acessibilidade. **Aceite**: não sobrescrever percentuais antigos silenciosamente.

### ORB-155 — Projeção local de prazo
**Implementar** cálculo de ritmo necessário/estimado a partir de meta numérica, dias ativos e data de entrega, sem IA remota. **Teste**: sem prazo, prazo passado, fim de semana, divisão fracionária, meta concluída. **Aceite**: apresentar projeção como estimativa, não promessa.

### ORB-156 — Redistribuição assistida e reversível
**Implementar** proposta de redistribuição de quantidade restante pelos dias futuros. Exigir confirmação explícita antes de aplicar; conservar histórico por data. **Teste**: pular dia, exceder meta, excluir dia, recorrência, cancelamento. **Aceite**: nenhuma edição automática de datas anteriores.

### ORB-157 — Resumos semanais/mensais locais
**Implementar** estatísticas por prioridade, número de tarefas e progresso; excluir duplas contagens de ocorrências no intervalo. **Teste**: semana cruzando mês, dias sem tarefas, tarefas recorrentes, percentual arredondado. **Aceite**: coerência com visão diária filtrada e legenda explicativa.

### ORB-158 — Modelos reutilizáveis de tarefas
**Implementar** templates locais para estudo/leitura/trabalho/hidratação; criar tarefa nova com novo ID sem reutilizar histórico, anexos ou lembretes anteriores. **Teste**: duplicação e independência de objeto, recorrência, projetos e subtarefas. **Aceite**: modelos são configuráveis, não substituem tarefa existente.

### ORB-159 — Seleção múltipla com ação segura
**Implementar** operações em lote pequenas (ex.: alterar prioridade), com prévia, confirmação quando destrutiva e undo quando possível. **Teste**: conjunto vazio, filtros, dados antigos, cancelamento e seleção de recorrências. **Aceite**: nenhuma exclusão em massa silenciosa.

### ORB-160 — Privacidade de backups e proteção do armazenamento Android
**Implementar** UX de advertência para arquivo JSON não criptografado e auditoria das regras de Android Auto Backup; tratar dados sensíveis sem remover backups existentes por acidente. **Teste**: exportação cancelada, compartilhamento indevido, limite de tamanho, permissões. **Aceite**: riscos explícitos e dados preservados. Criptografia só com estratégia segura de recuperação de chave, sem prometer invulnerabilidade.

### ORB-161 — Gate final pré-Supabase: melhoria real de confiabilidade
**Implementar** ao menos uma correção concreta derivada de testes end-to-end de persistência/migração/calendário/recorrências (não contar só CI/documentação). Acrescentar regressão reproduzível e validar SHA final. **Teste**: instalação sem atualização destrutiva (quando assinatura permitir), export/import, idioma/RTL, alarmes, 0–100%, cinco prioridades, calendário, histórico. **Aceite**: incremento 24/24 validado e bloqueios físicos/release registrados; não mascarar o que não pôde ser testado.

### Dependências e priorização
- ORB-140, 141 e 142 resolvem dívida imediata criada pelas evoluções 138–139. ORB-143 depende de 142.
- ORB-144→145 preserva integridade; ORB-146 deve manter exportação JSON inalterada.
- ORB-153→154→155→156 é um bloco de lógica numérica, sequencial e reversível.
- ORB-149 precede novas visões 150 e 151; não criar estados incompatíveis.
- ORB-161 exige evidências do grupo todo. Se surgir regressão grave, a correção toma prioridade; **não somar “um passo” apenas por alterar a documentação**.

## 5. PORTÃO PARA RETOMADA DO SUPABASE — APÓS ORB-161

**Gate administrativo (não começar antes da janela autorizada):**
1. Confirmar organização/projeto Supabase exclusivo do Checklist Orbis, no **plano gratuito**; não reutilizar `24 H2O`, nunca alterar cobranças, sem opção paga.
2. Revalidar preços/cotas/limitações na documentação atual, sem supor armazenamento gratuito ilimitado.
3. Revisar `supabase-checklist-orbis-schema-proposto.sql` e `supabase-checklist-orbis-testes-seguranca.sql`; projeto de teste isolado, migração idempotente e rollback.
4. Executar testes negativos com duas contas: usuário A não pode ler, editar, excluir ou baixar mídia privada de B; incluir RLS SELECT/INSERT/UPDATE/DELETE e `storage.objects`.
5. Auth por e-mail real: confirmação de e-mail, redefinição de senha, logout, token expirado, troca de aparelho, revogação. Guardar sessão no armazenamento seguro do Android; nunca `service_role` no app.
6. Projetar cache offline + fila de sincronização, IDs locais/UUID, revisões/conflitos, tombstones e políticas de merge, incluindo migração de registros existentes sem duplicação nem perda.
7. Anexos: foto/vídeo sob descrição; validação MIME/tamanho; caminhos por proprietário; bucket privado; quota por usuário; upload/download autenticados e interrupções retomáveis, limites do Free. Não afirmar que seleção local equivale a backup remoto.
8. Compartilhamento entre contas **somente com consentimento**: convite por identificador seguro, aceitar/rejeitar, permissões estritas, auditoria/expiração, bloqueio de spam. Nenhuma busca pública irrestrita de pessoas.
9. Testar dois dispositivos reais ou emuladores isolados e funcionamento sem rede, conflito simultâneo, exclusão e restauração; publicar somente depois das evidências.
10. Planos futuros por MB/GB são apenas backlog comercial. Não implementar cobrança, checkout ou assinatura durante testes gratuitos.

**Separação de ambientes:** dados de desenvolvimento e teste não devem contaminar produção. Bloqueadores são registrados sem improvisar com credenciais ou serviços de outro projeto.

## 6. PESQUISA DOS 15 CONCORRENTES — MATRIZ E ENTREGÁVEIS

A cada concorrente, o pesquisador deve criar uma subseção datada em `pesquisas/progresso-15-concorrentes.md` contendo:
- Nome, posição na **ordem vigente**, data, fontes **oficiais** atuais e URLs; distinguir anúncio oficial de função testada.
- Funcionalidades: **JÁ EXISTE / PARCIAL / AUSENTE / BLOQUEADA / INELEGÍVEL / NÃO VERIFICADA**.
- Valor para o usuário Orbis; privacidade/offline; originalidade/licença, custo, permissões, riscos; prioridade e dependências.
- Para cada implementação elegível: issue/tarefa, alterações, testes, CI, evidências de regressão, APK debug, pendências instrumentadas e assinatura.
- **Conclusão do concorrente** somente após auditar todos os itens elegíveis ou explicar individualmente bloqueios e rejeições, com fontes. Atingir 24/24 não significa atingir 15/15.

| Ordem | Concorrente | Foco de pesquisa autoral | Fonte inicial a reconfirmar |
|---|---|---|---|
| 1 | Microsoft To Do | Meu Dia, sugestão, lembretes, listas, recorrência, acessibilidade | https://support.microsoft.com/en-us/todo/plan-and-connect-with-microsoft-to-do |
| 2 | Google Tasks | tarefas/subtarefas, vencimentos e organização **sem qualquer integração com Google Agenda** | https://support.google.com/tasks/answer/7675772 |
| 3 | Any.do | planejamento diário, listas, prioridade e repetição | https://www.any.do/personal |
| 4 | Structured | linha do tempo local, ordem cronológica, blocos | https://structured.app/ |
| 5 | Taskito | timeline, time blocking, planejamentos e arrastar | https://www.taskito.io/ |
| 6 | HabitNow | hábitos, calendário, lembretes, estatísticas, backup | https://play.google.com/store/apps/details?id=com.habitnow |
| 7 | Habitica | recompensas e motivação originais (sem copiar marca/mecânicas protegidas) | https://habitica.com/ |
| 8 | Amazing Marvin | estratégias de foco e personalização modulares | https://amazingmarvin.com/features/ |
| 9 | Trello | quadros locais, cartões, etiquetas, checklists | https://support.atlassian.com/trello/docs/using-trello |
| 10 | Notion Calendar | blocos de tempo e tarefas relacionadas sem conectores de calendário externos | https://www.notion.com/help/guides/getting-started-with-notion-calendar |
| 11 | Tasks.org | offline, import/export, subtarefas, filtros e lembretes | https://tasks.org/ |
| 12 | Loop Habit Tracker | recorrência de hábitos, sequência, estatísticas e CSV | https://github.com/iSoron/uhabits |
| 13 | Super Productivity | Pomodoro, rastreamento de tempo, projetos, foco | https://github.com/super-productivity/super-productivity |
| 14 | TickTick | hábitos, matriz de prioridade, temporizador e métricas | https://www.ticktick.com/features |
| 15 | Todoist | projetos, filtros, etiquetas, metas, conclusão e relatórios | https://www.todoist.com/help/todoist/features |

Esses links são **pontos de partida históricos**; pesquisar documentos oficiais novamente quando chegar a vez, pois conteúdo e políticas podem mudar. Não inserir SDKs, sync, calendários externos ou copiar código. Cada concorrente pode exigir múltiplas iterações além de 161.

## 7. MATRIZ MÍNIMA DE REGRESSÃO POR MUDANÇA

| Dimensão | Cenários obrigatórios |
|---|---|
| Datas | 28/29 fev, 30/31, ano novo, seleção de outro dia, formato local |
| Recorrência | única, diária, semanal, mensal, anual, fim por data/contagem, exceções |
| Histórico | 0, 1, 50, 99, 100%; recorrências em dias diferentes; persistência |
| Prioridades | verde, azul, amarelo, laranja, vermelho; filtro simples/combinado |
| Organização | busca, tags, projetos, seções, ordenação, tarefas ocultas |
| Tempo | faltante, simultâneo, adjacente, inválido, virada de dia, fuso |
| Backup | exportação, cancelamento, JSON truncado, restauração legado, 5 MiB, mídia excluída |
| Idiomas | todos os 15; árabe e urdu RTL; caracteres não latinos, números |
| UX | tamanho de fonte, leitor de tela, foco, contraste, paisagem, tela estreita |
| Android | API mínima 23, target 35, notificações 13+, reboot, modo offline |
| Segurança | sem segredo na fonte, sem vazamento de conteúdo para outro app/conta |
| Release | ID de app estável, assinatura compatível, atualização sem apagar dados |

**Comandos existentes do workflow:**
```sh
gradle -p ChecklistOrbisAndroid :app:testDebugUnitTest --no-daemon
gradle -p ChecklistOrbisAndroid :app:assembleDebug --no-daemon
```
Requisitos de ambiente documentados: Java 17, Android SDK 35, Gradle 8.9. O workflow publica `Checklist-Orbis-Android-APK`; o artefato `app-debug.apk` é debug e sua assinatura **não equivale à de release**. Para evidência, inspecionar em GitHub Actions a execução com `head_branch=checklist-orbis`, `head_sha` exato do commit, `conclusion=success`, jobs/steps e ID/link do artefato; não basta run de SHA anterior.

## 8. PADRÃO PARA COMMITS, ISSUES E RELATÓRIOS

**Formato obrigatório de relato por incremento:**
```md
### ORB-___ / Execução ___ / data UTC
- Estado: PLANEJADO | EM PROGRESSO | EM REVISÃO | CI PENDENTE | VALIDADO | BLOQUEADO
- Brief e concorrente estudado / fontes oficiais:
- Branch: checklist-orbis | SHA-base: ...
- Arquivos alterados e por quê:
- Compatibilidade com dados anteriores:
- Testes adicionados (nome, casos) e executados:
- CI: URL, SHA exato, conclusão e horário da consulta:
- APK: link e ID do artefato ou "não confirmado":
- Regressões observadas/corrigidas:
- Segurança, acessibilidade, idiomas:
- Instrumentação/aparelho físico: executado com evidência | pendente
- Assinatura release: validada com evidência | bloqueada
- Próximo incremento e bloqueios:
```
Atualizar o log cronológico sem apagar entradas anteriores. Referências de commits devem ter links reais; nunca fornecer links presumidos ou artefatos não verificados. Não marcar concorrente como concluído por simplesmente registrar pesquisa.

## 9. QUALIDADE DA ENTREGA E CRITÉRIO FINAL

- Critério **para cada incremento**: código revisado, validação de entradas, teste(s) automático(s), CI do SHA final aprovado, dados preservados, idiomas/acessibilidade considerados, documentação de evidências e riscos; se algum gate faltar, escrever **pendente**, não ocultar.
- Critério **para concluir o ciclo 24/24**: todas as 24 funcionalidades locais com evidências, correções identificadas e documentação; só então iniciar o trabalho conectado autorizado.
- Critério **para dizer que os 15 concorrentes foram concluídos**: matriz de todos os recursos relevantes por concorrente, evidências e razões de bloqueio ou rejeição documentadas; implementação de elegíveis validada. A conclusão competitiva é independente do número de incrementos.
- Critério **para release de loja**: assinatura estável privada + APK/AAB release testado + upgrade sem perda + política de privacidade e consentimento + compatibilidade Android e análise de segurança, além de qualquer exigência vigente da loja. Nenhuma aprovação só porque existe um debug APK.

**Instrução resumida para qualquer IA que entrar no projeto:** LEIA os dois documentos históricos + este plano + AGENTS.md; DESCUBRA SHA/CI atuais; RESERVE seu trabalho sem colisões; IMPLEMENTE um item pequeno em `checklist-orbis`; TESTE e REGISTRE a evidência; NÃO TOQUE em `main`, Google Agenda, Supabase alheio, chaves ou cobrança.
