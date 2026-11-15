# Evolução de 15 concorrentes — registro de execução

Atualizado em 2026-11-15. Branch exclusiva: `checklist-orbis`. Não alterar `main`.

## Estado
- Concorrente atual: **1/15 — Todoist**, em análise e implementação incremental.
- Próxima ação: verificar CI da busca, corrigir falhas e ampliar testes de persistência/backup; depois projetos/seções e etiquetas do Todoist.
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
- **CI**: compilação Android aprovada para o commit inicial de backup [run 43744214005](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/43744214005). Compilação com tradução [run 43744252538](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/43744252538): **sucesso**; documentação [run 43744290599](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/43744290599): **sucesso**.
- **Regressões**: revisão estática das rotas de calendário, idiomas, recorrência, histórico e prioridades; não foram executados testes instrumentados nem teste em dispositivo. Não alegar ausência comprovada de regressões.
- **Próximo recurso Todoist**: descrição de tarefas, depois subtarefas, projetos, etiquetas e busca, sempre um recurso por ciclo de compilação.

## Execução 2 — 2026-11-15
- **Todoist (1/15)**: descrição de tarefa adicionada ao editor (até 2.000 caracteres, opcional, múltiplas linhas), exibida resumida nos cartões com toque para leitura integral. Salva no JSON da tarefa, preservada em recorrências e backup/restauração, sem alterar o histórico por dia. Commits [5b7d5e2d](https://github.com/irmaogabrieldionato/Frases-do-dia-/commit/5b7d5e2d906bd7d3291f000619a316bb06d11f88), [e5e5fd72](https://github.com/irmaogabrieldionato/Frases-do-dia-/commit/e5e5fd72746cc0446d49c5f6adaf9e9359cb2277), [d2e5a840](https://github.com/irmaogabrieldionato/Frases-do-dia-/commit/d2e5a840dac20a9fe0cecfb2c193896e51ab0f9b).
- Tradução das etiquetas e dicas da descrição para todos os 15 idiomas.
- Backup anterior compilado com sucesso, inclusive traduções. A descrição não exige migração de dados: tarefas antigas sem o campo continuam válidas.
- **CI de descrição**: [run 43749313357](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/43749313357), em execução ao registrar esta etapa. Não declarar recurso validado até a conclusão.
- **Verificação de regressões**: inspeção estática de pontos de gravação, calendário, recorrências, prioridades e backup; sem testes instrumentados ou aparelho físico. Próximo passo: conferir CI e adicionar testes automatizados de persistência/backup.
- **Próximo recurso**: subtarefas, após aprovação da compilação.

## Execução 3 — 2026-11-15
- **Todoist 1/15**: CI das descrições confirmado **sucesso** [run 43749313357](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/43749313357).
- **Subtarefas implementadas em código**: até 50 itens por tarefa, um por linha no editor; IDs estáveis e histórico de marcação independente por data de ocorrência, inclusive tarefas recorrentes; contador de itens concluídos nos cartões. A barra percentual principal continua manual e não é sobrescrita pelas subtarefas. Preserva JSON de backup e tarefas antigas sem subtarefas. Commits [c5b61340](https://github.com/irmaogabrieldionato/Frases-do-dia-/commit/c5b61340d0c664feffa211415d8d1a84f3ee00b9), [74858834](https://github.com/irmaogabrieldionato/Frases-do-dia-/commit/7485883471190bc6a24e4ae3b2302e902553fb0a) (traduções 15 idiomas), [35e0b87b](https://github.com/irmaogabrieldionato/Frases-do-dia-/commit/35e0b87b12382fccca6e0dac965951c935a0c166) (captura segura da referência Java).
- **CI**: disparado para os commits; verificar conclusão do commit final antes de marcar como validado. Testes instrumentados e físicos ainda não executados.
- **Limitações**: editar texto de uma subtarefa muda seu ID e reinicia histórico daquela subtarefa; excluir subtarefa remove seu histórico. Há limite de 4.000 caracteres no editor. Requer teste real de importação de JSON com subtarefas.
- **Próximo passo**: verificar compilação, corrigir bugs e adicionar testes; depois projetos/seções, etiquetas e busca.

## Execução 4 — 2026-11-15
- **Todoist (1/15)**: verificada conclusão **com sucesso** do CI das subtarefas: [run 43755392320](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/43755392320). Isso confirma compilação, não teste funcional em aparelho.
- **Implementada busca local de tarefas**: campo na lista filtra em tempo real títulos, descrições e títulos de subtarefas, sem rede e sem alterar o armazenamento; busca combinada com o filtro existente de prioridades e com o dia selecionado. Mensagens traduzidas nos 15 idiomas. Commit [a4bef998](https://github.com/irmaogabrieldionato/Frases-do-dia-/commit/a4bef998a82e6846f433ac7ca5f556ccd9608b79).
- **CI da busca**: [run 43760758343](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/43760758343), **sucesso** (compilação Android confirmada).
- **Revisão estática**: busca utiliza apenas os campos existentes e mantém os registros intactos; renderiza somente a lista enquanto o usuário digita, evitando recriar o campo de texto. Os resumos percentuais passam a considerar tarefas visíveis na busca, assim como já fazem com prioridades. Não foram executados testes instrumentados, nem restauração em dispositivo físico.
- **Pendentes Todoist**: projetos e seções, etiquetas, ordenação, filtros combinados/salvos, duplicação, reagendamento, prazo separado, hora/duração, lembretes, histórico, concluídas, relatórios, modelos, widgets, seleção múltipla, Kanban, anexos e colaboração condicionada.
- **Bloqueio mantido**: assinatura estável de release exige keystore privado/segredos; nunca publicar chave privada no repositório.
