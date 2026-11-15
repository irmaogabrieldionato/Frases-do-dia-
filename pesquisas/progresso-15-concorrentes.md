# Evolução de 15 concorrentes — registro de execução

Atualizado em 2026-11-15. Branch exclusiva: `checklist-orbis`. Não alterar `main`.

## Estado
- Concorrente atual: **1/15 — Todoist**, em análise e implementação incremental.
- Próxima ação: adicionar backup e restauração local antes de outras funcionalidades, validar compilação e registrar resultado.
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
