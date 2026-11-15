# Pesquisa competitiva — Checklist Orbis
Data: 15 de novembro de 2026

## Objetivo
Diferenciar um aplicativo Android nativo, independente e offline. Base atual: calendário local com recorrências e exclusão de ocorrências, 15 idiomas, cinco prioridades (verde, azul, amarelo, laranja e vermelho), filtros e porcentagem de progresso 0–100% por tarefa e data. O APK ainda é uma versão de teste sem assinatura estável e sem sincronização multiaparelho.

## Concorrentes e funcionalidades verificadas

| Produto | Recursos documentados | Fonte oficial |
|---|---|---|
| Todoist | Calendário, etiquetas, filtros e prioridades | https://www.todoist.com/help/articles/set-a-priority-in-todoist-Wy82Jp |
| TickTick | Hábitos, Pomodoro, matriz de Eisenhower, estatísticas | https://www.ticktick.com/features |
| Microsoft To Do | Meu Dia, sugestões, lembretes e repetição | https://support.microsoft.com/en-us/todo/plan-and-connect-with-microsoft-to-do |
| Google Tasks | Subtarefas, datas, notificações e integração com Calendar | https://support.google.com/tasks/answer/7675772 |
| Any.do | Planejamento diário, etiquetas coloridas, recorrências | https://www.any.do/personal |
| Structured | Linha do tempo diária, importação de calendário e planejamento com IA | https://structured.app/ |
| Taskito | Timeline, arrastar tarefas, time blocking e linguagem natural | https://www.taskito.io/ |
| HabitNow | Hábitos, calendário, lembretes, estatísticas e backups | https://play.google.com/store/apps/details?id=com.habitnow |
| Habitica | Recompensas, níveis, missões e tarefas gamificadas | https://habitica.com/ |
| Amazing Marvin | Ferramentas para foco, estimativas, procrastinação e personalização | https://amazingmarvin.com/features/ |
| Trello | Cartões, checklists, etiquetas e automações | https://support.atlassian.com/trello/docs/using-trello |
| Notion Calendar | Projetos de Notion como eventos e blocos de tempo | https://www.notion.com/help/guides/getting-started-with-notion-calendar |
| Tasks.org | Offline, subtarefas, filtros, lembretes, sync opcional | https://tasks.org/ |
| Loop Habit Tracker | Hábitos flexíveis, lembretes, pontuação, gráficos e CSV | https://github.com/iSoron/uhabits |
| Super Productivity | Tempo, pomodoro, agenda, tags, projetos, sincronização opcional | https://github.com/super-productivity/super-productivity |
| Vikunja | Projetos colaborativos, Kanban, Gantt e tabelas | https://vikunja.io/features/ |
| Fossify Calendar | Calendário offline, repetição, várias visualizações e widgets | https://f-droid.org/packages/org.fossify.calendar/ |

## Conclusões
Não diferenciar apenas por cores, calendário, recorrências ou modo offline: estes recursos já existem em rivais. Tampouco afirmar exclusividade de barra percentual sem verificação específica.

### Posicionamento proposto
**Checklist Orbis: cada meta tem um número, cada dia tem um plano, cada avanço fica registrado.**

### Prioridade 0 — confiança e usabilidade
1. Configurar assinatura de APK estável para atualizações sem perda de dados, sem colocar chave privada no GitHub público.
2. Adicionar backup/exportação e restauração de tarefas e históricos em JSON (posteriormente CSV).
3. Validar agendamentos de repetição e fuso horário; testes instrumentados Android para calendário, transições de dia, 29 de fevereiro, 30/31, meses e intervalos.
4. Lembretes locais por horário com permissões Android, sem API externa.
5. Testar acessibilidade, contraste, idiomas RTL e redimensionamento.

### Prioridade 1 — propostas de diferenciação a validar com usuários
1. **Meta numérica real**: unidade configurável (páginas, km, litros, reais, repetições), valor inicial, alvo, valor realizado; barra 0–100% calculada automaticamente e não apenas estimada manualmente.
2. **Distribuidor automático de metas**: usuário informa dias ativos, início e prazo; o aplicativo distribui quantidade restante pelos dias futuros (respeita repetições e arredondamentos).
3. **Replanejamento inteligente local**: ao faltar ou exceder a meta do dia, sugere redistribuir o restante; nunca modifica histórico silenciosamente. Usuário confirma.
4. **Projeção de conclusão**: comparação entre progresso planejado e observado, com aviso de risco de atraso, sem punições.
5. **Resumo semanal/mensal por prioridade**: percentual e quantidade feitos por cor, metas atingidas e sequência de progresso.
6. **Subtarefas que calculam progresso da tarefa mãe**: marcações simples ou ponderação por esforço.

### Prioridade 2 — eficiência
7. Linha do tempo diária/semanal por horário e duração estimada, com aviso de sobreposição.
8. Repetição flexível (dias específicos do mês, a cada N dias, N vezes por semana, último dia útil).
9. Widgets Android para atualizar valores sem abrir app.
10. Modelos reutilizáveis por tipo: estudos, leitura, hidratação, trabalho e manutenção.

### Prioridade 3 — somente após validar o valor do app
11. Sincronização opcional entre aparelhos com infraestrutura independente ou auto-hospedada, mantendo offline como padrão.
12. Áreas familiares/equipes, compartilhamento de metas e relatórios, com consentimento claro.

## MVP recomendado (próxima versão)
Fazer backup/assinatura confiável, depois construir a tarefa com meta numérica e replanejamento simples. Exemplo: ler 500 páginas de um livro; selecionar dias ativos, prazo e quantidade lida; sugerir páginas por dia e recalcular o restante ao registrar a leitura.

## Sobre licenças
- Checklist Orbis: MIT no repositório atual, uso comercial permitido conforme os termos de sua licença.
- Super Productivity: MIT (verificar dependências e avisos ao reutilizar).
- Tasks.org e Loop: GPLv3.
- Vikunja: AGPLv3.
- Fossify Calendar: GPLv3.
- GPL/AGPL também permitem comercialização, mas exigem cuidados e obrigações de código-fonte quando sua implementação é reutilizada/distribuída. Não copiar código, artes ou identidade de aplicativos proprietários apenas por terem sido estudados.

## Critérios de validação sugeridos
- Tempo até cadastrar a primeira tarefa; uso da função de meta numérica; retenção após 7 dias.
- Quantas metas ficam adiantadas, atrasadas e concluídas no prazo.
- Taxa de uso de backups/restauração e sucesso de atualizações sem perda de dados.
- Pesquisa com usuários perguntando se a distribuição automática poupa trabalho real.

**Nota:** esta é análise documental de funcionalidades anunciadas nos sites oficiais, não testes completos de todos os aplicativos ou pesquisa com seus usuários. As prioridades são recomendações, não medidas de impacto.
