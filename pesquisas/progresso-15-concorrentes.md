# Evolução de 15 concorrentes — registro de execução

Atualizado em 2026-11-15. Branch exclusiva: `checklist-orbis`. Não alterar `main`.

## Estado
- Concorrente atual: **1/15 — Todoist**, em análise e implementação incremental.
- Próxima ação: confirmar compilação final do backup traduzido e implementar descrições de tarefas, com testes por funcionalidade.
- Segurança da distribuição: assinatura estável de release **bloqueada** até haver keystore privado e configuração de secrets seguros. Nunca gravar chaves no Git.
- Implementações existentes verificadas no código: calendário mensal nativo, recorrências, histórico de progresso por data, cinco prioridades, filtros, 15 idiomas, armazenamento local e interface offline.
- 0/15 concorrentes concluídos; não avançar sem registrar cada funcionalidade pendente, implementada ou bloqueada.

## Todoist — inventário oficial
Fonte: https://www.todoist.com/help/todoist/features e https://www.todoist.com/pt-BR/help/todoist/get-started/todoist-glossary-cA60laWMH

Já presentes: tarefas, data, recorrência básica, prioridade (5 níveis Orbis), filtro simples por prioridade, calendário, progresso diário.
Ainda ausentes ou parciais: descrições, subtarefas, projetos e seções, etiquetas, busca, ordenação, filtros combinados/salvos, duplicação, reagendamento, prazo separado de data, hora e duração, lembretes locais, histórico de alterações, lista de concluídas, estatísticas e metas, modelos, atalhos e widgets Android, backup/exportação, seleção múltipla, quadro Kanban, notas/anexos.
Bloqueados ou condicionais: colaboração/atribuição, sincronização, comentários compartilhados, lembretes de localização e integração de terceiros exigem infraestrutura, permissões ou desenho de privacidade; não copiar interface nem código proprietário.
Não reproduzir literalmente: sistema Karma e identidade de marca; eventual gamificação será original.

## Critério de avanço
Cada melhoria exige código, compilação GitHub Actions aprovada, checagem de regressões disponíveis e link do commit. Não confundir compilação com teste instrumentado ou em dispositivo.

## Execução 1 — 2026-11-15
- Inventário do Todoist e fila de funcionalidades documentados acima; nenhum outro concorrente iniciado.
- **Implementado: exportação e restauração local de backup JSON**, por meio do seletor de arquivos do Android, sem API externa; confirmação antes de substituir os dados; validação de esquema e limite de 5 MiB. Commit [e62fc170](https://github.com/irmaogabrieldionato/Frases-do-dia-/commit/e62fc1705698eb2e13c56bae5c88d577168257f2).
- **Traduções em 15 idiomas** para os novos controles e mensagens. Commit [90438741](https://github.com/irmaogabrieldionato/Frases-do-dia-/commit/90438741bebfa793c6ae460bf39689e2b847d1e2).
- **Documentação de segurança**: JSON não criptografado; não exporta idioma; teste físico ainda necessário. Commit [ad6505bc](https://github.com/irmaogabrieldionato/Frases-do-dia-/commit/ad6505bcdd20519851dfd4ad0b328cb78ce7b3e2).
- **CI**: compilação Android aprovada para o commit inicial de backup [run 43744214005](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/43744214005). Compilação com tradução [run 43744252538](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/43744252538): aguardando resultado ao registrar esta etapa.
- **Regressões**: revisão estática das rotas de calendário, idiomas, recorrência, histórico e prioridades; não foram executados testes instrumentados nem teste em dispositivo. Não alegar ausência comprovada de regressões.
- **Próximo recurso Todoist**: descrição de tarefas, depois subtarefas, projetos, etiquetas e busca, sempre um recurso por ciclo de compilação.
