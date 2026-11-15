# Checklist Orbis 1.4 — Android nativo

Checklist Orbis é um aplicativo independente de código aberto, licenciado sob MIT, com possibilidade de uso e comercialização. A agenda mensal é implementada diretamente com componentes Android e Java da plataforma: **não depende de Google Agenda, APIs do Google Agenda, serviços online ou biblioteca de calendário proprietária**. As ferramentas de compilação Android são usadas apenas para gerar o APK.

## Prioridades e cores

Cada tarefa possui uma prioridade entre cinco cores, selecionável ao criar ou editar:
1. Verde — prioridade 1
2. Azul — prioridade 2 (padrão)
3. Amarelo — prioridade 3
4. Laranja — prioridade 4
5. Vermelho — prioridade 5

Os cartões mostram um identificador com a cor e o nome da prioridade. A barra de filtros oferece **Todas, Verde, Azul, Amarelo, Laranja e Vermelho**. A seleção mostra somente as tarefas da prioridade escolhida no dia e recalcula o progresso geral com base nas tarefas filtradas. Os pontos coloridos no calendário representam a prioridade mais alta prevista para cada data dentro do filtro atual. As opções de filtro não alteram nem removem tarefas.

As tarefas criadas antes desta versão são mantidas e recebem prioridade 2 (azul) por padrão. A prioridade selecionada é armazenada no registro da tarefa.

## Agenda e progresso

- Calendário nativo mensal com dias selecionáveis, navegação por mês e botão Hoje.
- Uma única ocorrência ou repetição diária, semanal por dias escolhidos, mensal ou anual.
- Início agendável; término opcional por data ou por quantidade de ocorrências.
- Progresso individual de 0% a 100% em cada ocorrência e por data; em 100% a tarefa fica riscada.
- Exclusão de uma única ocorrência ou da série inteira.
- Dados armazenados localmente no celular, sem conta, servidores, internet nem sincronização com Google Agenda.
- Interface com 15 idiomas, inclusive as etiquetas de cor e os filtros.

## Compilação

No GitHub, abra a branch `checklist-orbis` e o workflow `Gerar APK Checklist Orbis (Android)`. Baixe o artefato `Checklist-Orbis-Android-APK` e extraia `app-debug.apk`.

Compilação local: Java 17, Android SDK 35 e Gradle 8.9:

```sh
gradle -p ChecklistOrbisAndroid :app:assembleDebug
```

## Atualizações e segurança

O identificador Android `com.orbis.meudia` e as preferências locais `meu-dia-dados-v1` foram mantidos para migração de dados. Entretanto, diferentes builds debug do GitHub Actions podem ter chaves de assinatura distintas, e a instalação de um APK sobre outro pode falhar. Não desinstale a versão antiga sem proteger suas tarefas; o projeto ainda precisa de uma assinatura fixa para atualizações sem perda de dados.

Licença: MIT, consulte `LICENSE` na raiz da branch.
