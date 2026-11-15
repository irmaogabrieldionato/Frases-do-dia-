# Evolução de 15 concorrentes — registro de execução

Atualizado em 2026-11-15. Branch exclusiva: `checklist-orbis`. Não alterar `main`.

## Estado
- Concorrente atual: **1/15 — Todoist**, em análise e implementação incremental.
- Próxima ação: verificar CI das seções e testar filtros, persistência e backup; depois implementar etiquetas do Todoist.
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

## Execução 5 — 2026-11-15
- **Todoist (1/15)**: pesquisa e inventário anterior reutilizados; não reiniciar concorrente. CI da busca confirmado sucesso [run 43760758343](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/43760758343).
- **Implementado projeto simples/local por tarefa**: campo de nome de projeto (até 60 caracteres) no editor; seletor dinâmico de projetos existentes; filtragem combinada com dia, busca e prioridade; etiqueta de projeto no cartão; indicadores de calendário respeitam projeto escolhido. Traduções em todos os 15 idiomas. Commits [f2c23c1e](https://github.com/irmaogabrieldionato/Frases-do-dia-/commit/f2c23c1e2e2fb5d9405e92764d47f0ed26b4ed52) e [ea29396e](https://github.com/irmaogabrieldionato/Frases-do-dia-/commit/ea29396e916fb6f88a1d31c388d3f03bb1a18532).
- Projeto é um atributo textual de tarefa, não um sistema completo de projetos com seções, gerenciamento de projetos, permissões ou colaboração. O campo é incluído automaticamente em exportação JSON porque o backup serializa a tarefa. Tarefas antigas sem projeto continuam visíveis no filtro geral.
- **CI**: primeira compilação [run 43766539327](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/43766539327) em execução durante a atualização; conferir também a execução do commit de correção antes de marcar validado. Nenhum teste instrumentado ou em aparelho físico executado.
- **Regressões**: revisão estática da combinação dos filtros e persistência; não alegar validação funcional sem testes. Assinatura de release estável ainda bloqueada por falta de keystore/segredos privados.
- **Próximo recurso Todoist**: seções de projeto e etiquetas, após confirmação de CI; registrar separadamente cada etapa.

## Execução 6 — 2026-11-15
- **Todoist (1/15)**: CI dos projetos da execução 5 confirmado **sucesso** nos runs [43766539327](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/43766539327) e [43766556586](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/43766556586).
- **Implementadas seções locais de projeto**: campo opcional de até 60 caracteres no editor, exibido no cartão; seletor de seções com opções derivadas das tarefas do projeto escolhido; filtro combinável com projeto, prioridade, pesquisa e dia; pontos do calendário respeitam seção. Seção sem projeto não é salva. Commits [f6d4cd40](https://github.com/irmaogabrieldionato/Frases-do-dia-/commit/f6d4cd4029db59bdce4750653e17e017e288e8ad), [5d1c5ed1](https://github.com/irmaogabrieldionato/Frases-do-dia-/commit/5d1c5ed1aaa2da185d10fe4d950677616f4ff4a4), [30e1c3e7](https://github.com/irmaogabrieldionato/Frases-do-dia-/commit/30e1c3e7d41644a1fb012fc2fc7284cad903a51f). Etiquetas novas traduzidas nos 15 idiomas.
- **Compatibilidade**: campo JSON opcional `section`, serializado automaticamente no backup; dados antigos continuam válidos. Sem Google Agenda, sem serviços de terceiros. Código autoral; licença MIT do repositório preservada.
- **CI**: runs da alteração inicial [43771897647](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/43771897647) e da correção de filtro [43771905324](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/runs/43771905324) estavam em execução ao consultar. Confirmar run da tradução e compilação final antes de marcar validado.
- **Limitações**: revisão estática de filtros e persistência; ainda sem testes instrumentados, aparelho físico ou restauração de backup com seções. Seções são atributos textuais, sem ordenação manual nem gerenciamento independente. Assinatura estável de release continua bloqueada por falta de keystore privado.
- **Próximo recurso Todoist**: etiquetas (tags), após CI aprovado e correções necessárias.
