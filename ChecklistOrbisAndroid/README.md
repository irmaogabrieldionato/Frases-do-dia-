# Checklist Orbis — aplicativo Android nativo

Aplicativo open-source com listas diárias, agora com progresso individual de cada tarefa.

## Funcionamento

- Cada tarefa tem uma barra deslizante de 0 a 100%, logo abaixo do título.
- Ajustar a barra salva o progresso automaticamente.
- Somente ao chegar em **100%** o título da tarefa fica riscado e o botão de conclusão aparece marcado.
- Reduzir de 100% para 99% ou menos remove o risco, permitindo continuar.
- O botão original da tarefa permite alternar entre 100% e 0%.
- O cartão superior mostra a média dos percentuais de todas as tarefas, enquanto o texto informa quantas tarefas estão 100% completas.
- À meia-noite (ao voltar à tela), os percentuais do dia são zerados.
- As tarefas já criadas nas versões anteriores continuam salvas quando se atualiza com a mesma assinatura Android. A versão anterior só armazenava concluída/não concluída; isso é migrado automaticamente para 100%/0%.
- Todos os dados são locais. O aplicativo funciona offline.

## Idiomas

Na primeira execução, selecione um dos 15 idiomas disponíveis: inglês, chinês mandarim, hindi, espanhol, francês, árabe padrão moderno, bengali, russo, português, indonésio, urdu, alemão, japonês, suaíli e marathi. O ícone 🌐 permite alterar a língua depois. Títulos de tarefas escritos pelo usuário não são traduzidos automaticamente.

## Compilação APK no GitHub

O workflow **Gerar APK Checklist Orbis (Android)** compila usando Java 17 e Gradle 8.9.
Na aba Actions da branch `checklist-orbis`, baixe o artefato `Checklist-Orbis-Android-APK` e extraia `app-debug.apk`.

O projeto Android está na pasta `ChecklistOrbisAndroid`. Para compilar localmente:
```sh
gradle -p ChecklistOrbisAndroid :app:assembleDebug
```

## Aviso sobre atualizações

A identificação de pacote permanece `com.orbis.meudia` e o armazenamento local mantém a mesma chave, para preservar a compatibilidade com versões anteriores. Porém os APKs de depuração do GitHub Actions podem ser assinados por chaves diferentes a cada compilação; portanto uma instalação sobre a anterior pode falhar. Não desinstale se houver tarefas importantes sem backup. Uma versão de publicação deve usar uma chave de assinatura permanente e protegida.

## Organização no GitHub

Esta versão está na branch `checklist-orbis`. O repositório original contém outro app na branch principal; o nome do repositório só pode ser alterado pela administração do GitHub, não por este código. Para ter o nome `checklist-orbis` também na URL principal, será necessário criar um repositório dedicado ou renomear outro sem prejudicar os projetos existentes.

Licença: MIT.
