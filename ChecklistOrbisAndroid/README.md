# Checklist Orbis 1.3 — aplicativo Android

Aplicativo nativo e offline, em 15 idiomas, com calendário mensal e tarefas recorrentes.

## Calendário e agendamento
- Calendário mensal com botão Hoje, navegação entre meses e datas clicáveis.
- Marcadores nos dias com tarefas.
- Ao criar ou editar uma tarefa, escolha a data de início e a recorrência: uma vez, todos os dias, dias específicos da semana, mensalmente ou anualmente.
- As recorrências podem não ter término, parar em uma data ou terminar após 1 a 10.000 ocorrências.
- Exclua somente uma ocorrência ou a série completa.
- Cada ocorrência tem sua própria barra 0% a 100%; a tarefa aparece riscada em 100%. O progresso de um dia não modifica os demais, e o histórico permanece ao trocar de data.
- O cartão geral mostra a média do progresso das tarefas agendadas para a data selecionada.
- Após atualizar a mesma instalação, tarefas anteriores continuam disponíveis como tarefas diárias, preservando o progresso do último dia salvo.
- Sem internet ou conta, e sem integração ou sincronização com Google Agenda. A visualização mensal é apenas inspirada em agendas.

## APK
No GitHub Actions da branch checklist-orbis, abra Gerar APK Checklist Orbis (Android). Baixe Checklist-Orbis-Android-APK.zip e extraia app-debug.apk.

Compilação local: Java 17, Android SDK 35, Gradle 8.9, comando: gradle -p ChecklistOrbisAndroid :app:assembleDebug.

## Atualização segura
A identificação Android com.orbis.meudia e a chave local meu-dia-dados-v1 permanecem as mesmas. Porém os APKs debug produzidos em diferentes execuções podem ter assinaturas distintas. Se não instalar sobre a versão anterior, não desinstale a instalação com tarefas importantes sem backup. Para distribuição estável, usar uma chave de assinatura fixa e protegida.

Licença: MIT.
