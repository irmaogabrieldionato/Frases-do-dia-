# 7-Note Musical Game — Plano contínuo de preparação para publicação

Estado: backlog operacional pronto para uma automação ou agente com acesso autorizado ao GitHub.
Criado em 22/11/2026. **Este arquivo não substitui uma tarefa agendada ativa.**

## Escopo e limites

- Repositório: `irmaogabrieldionato/Frases-do-dia-`.
- Branch exclusiva: `7-note-musical-game`. **Nunca mudar a `main`, nem arquivos de outros aplicativos.**
- Objetivo: deixar todos os itens tecnicamente possíveis prontos antes dos pagamentos e procedimentos pessoais que as lojas exigem.
- Não comprar, pagar, publicar, abrir contas em nome do usuário, aceitar contratos, usar credenciais, mudar de titularidade, nem simular aprovação de loja.
- Não registrar segredos ou certificados privados no GitHub.
- Nunca afirmar que "só falta pagar" enquanto ainda faltarem dados fiscais/pessoais, assinatura, validação de titularidade, testes físicos ou aprovação de lojas.

## Regras que não podem regredir

1. Campanha musical de **1.000 fases exatas**; uma nota acrescentada por fase e vitória definitiva após completar a fase 1.000, sem fase 1.001.
2. Fases 1–9: `Dó`; `Dó, Si`; `Dó, Si, Ré`; `Dó, Si, Ré, Mi`; `Dó, Si, Ré, Mi, Dó`; `Dó, Si, Ré, Mi, Dó, Dó`; `Dó, Si, Ré, Mi, Dó, Dó, Ré`; fase 8 + `Sol`; fase 9 + `Fá`.
3. Quatro modos: Memória, Ouvido, Livre, Compositor, com áudio, progresso e melodias locais, sem login inventado.
4. Tela de anúncio explicitamente **demonstrativa** após cada segunda derrota; ainda não existe anúncio comercial real, e não se deve prometer receita.
5. Compatibilidade offline web/PWA, Android, Android TV/TV Box, iPhone/iPad e Windows x64.

## Ciclo de execução obrigatório para o agente

1. Ler este arquivo, `RELEASE-CHECKLIST.md`, `PACKAGES.md`, `README-7-NOTE.md` e verificar estado dos workflows e releases mais recentes da branch.
2. Selecionar o bloqueio de maior impacto ainda aberto. Fazer **alteração real** em arquivos da branch, testar e corrigir regressões.
3. Rodar/inspecionar os jobs `7-note-tests.yml` (regras, Chromium e WebKit) e `packages-7-note.yml` (Android + Lint, Android TV, iOS SDK vigente, Windows + npm audit).
4. Confirmar se houve artefato efetivo. Não confundir compilação de app **sem assinatura** com aplicativo publicável nas lojas.
5. Atualizar `RELEASE-CHECKLIST.md` com status, commit, número da execução, evidência e próximo bloqueio; evitar repetir relatórios longos.
6. Quando item depender de ação humana, registrar a menor ação necessária e avançar no próximo item possível. Comunicar apenas bloqueios e marcos importantes.

## Entregas até a etapa pessoal de publicação

### Qualidade e segurança
- [ ] Eliminar erros **e investigar warnings relevantes** do Android Lint para celular e TV.
- [ ] Executar testes de lógica para todas as 1.000 fases e vitória final, progressão, derrota e persistência.
- [ ] Executar suíte Chromium e WebKit; investigar Web Audio, reprodução prolongada de 1.000 notas, interrupções por bloqueio de tela, D-pad TV, acessibilidade e layout em múltiplas resoluções.
- [ ] Auditar dependências, licenças, URLs externas, permissões, bibliotecas e segurança do armazenamento local.
- [ ] Repetir compilações limpas Android API 36, Android TV API 35, iOS com SDK de publicação aceito e Windows.
- [ ] Verificar que políticas de privacidade correspondem **ao código real**, especialmente se futuramente entrar publicidade, analytics ou login.

### Preparação de lojas
- [ ] Criar materiais da ficha da loja (PT-BR e EN): título, descrição curta/longa, palavras-chave, novidades e textos de suporte.
- [ ] Capturar **screenshots reais da aplicação**, adequadas a cada tamanho de tela. Não utilizar imagens geradas como se fossem capturas reais.
- [ ] Conferir ícones, banners Android TV, imagens promocionais e requisitos gráficos atuais.
- [ ] Criar documentação de suporte e FAQ, diretrizes de conteúdo, classificação indicativa, declarações de coleta de dados, uso infantil e anúncios.
- [ ] Definir URL HTTPS pública estável para política de privacidade e página de suporte — se indisponível, manter pendência.
- [ ] Preparar instruções para AAB com chave de assinatura e Play App Signing **sem incluir chave privada**.
- [ ] Preparar projeto iOS com metadados, configuração de assinatura pelo titular, plano TestFlight e envio ao App Store Connect; não fingir gerar IPA final sem certificados.
- [ ] Preparar instalador e aplicativo portátil Windows, orientações sobre assinatura digital, antivírus, SmartScreen e divulgação.
- [ ] Gerar changelog, tag/release candidate da branch e notas de versão quando todos os testes pertinentes passarem.

### Dependências pessoais que NÃO se resolvem apenas pagando
- [ ] Inscrição e identidade do responsável nas contas Apple Developer/Google Play Console e eventualmente Microsoft Store.
- [ ] Aceitação de contratos e declarações legais/fiscais pela pessoa responsável.
- [ ] Certificados, keystore e perfis de assinatura controlados pelo proprietário; dados de suporte e contato válidos.
- [ ] Teste em **dispositivos físicos reais** e eventual teste fechado obrigatório do Google Play.
- [ ] Avaliação e aprovação das lojas após a submissão autorizada.
- [ ] Caso deseje anúncios reais, definir fornecedor, conta, política de crianças, consentimento e fluxo de pagamento.

## Fonte de verdade do status

- [Checklist de publicação](RELEASE-CHECKLIST.md)
- [Pacotes e instruções](PACKAGES.md)
- [Testes automáticos](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/workflows/7-note-tests.yml)
- [Builds multiplataforma](https://github.com/irmaogabrieldionato/Frases-do-dia-/actions/workflows/packages-7-note.yml)

**Para ativar execução recorrente no ChatGPT:** é necessário liberar um dos 5 espaços de tarefas já ocupados no plano do usuário. Só pausar ou substituir uma tarefa atual após escolha explícita dele.
