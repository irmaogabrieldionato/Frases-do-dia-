# Claude — instruções para colaborar no Checklist Orbis

Claude, você está participando de um **projeto Android Java real, comercializável, com vários colaboradores de IA**. A prioridade é continuidade segura, não volume de alterações.

## Entrada obrigatória

Leia, nesta ordem, na **branch `checklist-orbis`**:
1. `../pesquisas/progresso-15-concorrentes.md` — últimos registros e evidências.
2. `../pesquisas/concorrentes-checklist-orbis-2026-11-15.md` — catálogo e links oficiais.
3. `../pesquisas/PLANO-MESTRE-COLABORACAO-IAS-CHECKLIST-ORBIS.md` — **plano detalhado e os 22 próximos incrementos**.
4. `AGENTS.md`, `README.md`, workflow GitHub Actions e [issue de coordenação](https://github.com/irmaogabrieldionato/Frases-do-dia-/issues).
5. Código real, testes e HEAD atualizado: os planos descrevem intenções, não são prova de implementação.

**Regra absoluta:** nunca criar, editar, mesclar, rebater, enviar PR para ou alterar a branch `main`. Somente `checklist-orbis`; somente projeto `ChecklistOrbisAndroid/**` e documentação Checklist Orbis relacionada.

## Contrato de colaboração

- Entre como **analista ou autor de patch** até verificar que nenhum outro agente reservou o mesmo arquivo. A integração na branch compartilhada cabe a um único integrador por vez. Em especial, não disputar `MainActivity.java` ou `Languages.java` com outro agente.
- Para cada ação, indicar tarefa `ORB-140` a `ORB-161`, arquivos, SHA-base, cenários de teste e riscos. Quando precisar de um arquivo reservado, solicitar transferência ou elaborar patch sem gravar.
- Não saltar etapas nem marcar uma melhoria completa sem regressões verificadas e GitHub Actions para SHA final. Documentação isolada/CI isolada não conta como incremento funcional no ciclo 24.
- A pesquisa de **Microsoft To Do** é a primeira na ordem vigente. Estudar os demais 14 sequencialmente, sem imitar layouts, marcas ou copiar código. Referenciar fontes oficiais e data da checagem.
- Preservar agenda autoral sem nenhuma conexão Google Agenda, 15 idiomas, RTL árabe/urdu, prioridades verde/azul/amarelo/laranja/vermelho, filtros, histórico por data, 0–100%, recorrências, backup, lembretes e offline.
- **Supabase está postergado**: usuário quer terminar 24 incrementos locais; progresso **2/24** (138 e 139) antes de voltar à implantação. Na retomada somente projeto dedicado **gratuito**, não `24 H2O`; nunca acionar pagamento ou expor credenciais.
- Anexos e tarefas serão associados às contas remotas quando houver serviço autorizado, com cache offline opcional. Não anunciar sync, login, upload real ou segurança RLS como já validados.
- Assegurar compatibilidade com preferência `meu-dia-dados-v1` e Android `com.orbis.meudia`; não remover dados existentes. A assinatura release estável e testes em dispositivo ainda são pendentes.

## Ordem de execução por tarefa

1. Diagnosticar função e testes atuais, fonte oficial do concorrente se pertinente.
2. Propor alteração mínima e critérios positivos/negativos; verificar compatibilidade e privacidade.
3. Reservar arquivos (issue/comentário); usar commits pequenos com `feat(orbis)`, `fix(orbis)`, `test(orbis)`.
4. Executar testes JVM e build debug, revisar diffs e CI da branch/SHA exatos; corrigir regressões antes de seguir.
5. Registrar hash, URL de commit, run, artefato APK se realmente gerado; indicar testes físicos e assinatura release como **pendentes** se não ocorreram.
6. Acrescentar à seção cronológica do progresso sem apagar evidências anteriores, atualizar issue/checklist, liberar reserva de arquivo.

## Atalhos

```bash
git status
git branch --show-current
git rev-parse HEAD
gradle -p ChecklistOrbisAndroid :app:testDebugUnitTest --no-daemon
gradle -p ChecklistOrbisAndroid :app:assembleDebug --no-daemon
```

**Não reproduzir e não expor:** senhas, secrets do GitHub, service_role Supabase, chaves de assinatura, dados privados, logs pessoais ou URLs não verificadas.

## Entrega esperada do Claude

Resumo objetivo de: `tarefa / código produzido / arquivos / testes e resultado real / SHA / CI SHA correspondente / APK real ou pendente / riscos / próximo passo`. Sempre distinguir **projeto em desenvolvimento** de **funcionalidade disponível ao usuário**. Se algo estiver bloqueado, produzir documentação técnica reprodutível em vez de improvisar uma integração insegura.

Consulte `AGENTS.md` e o **Plano Mestre** para detalhes completos. Estas instruções não alteram as decisões do usuário; prevalece a ordem explícita mais recente dele.
