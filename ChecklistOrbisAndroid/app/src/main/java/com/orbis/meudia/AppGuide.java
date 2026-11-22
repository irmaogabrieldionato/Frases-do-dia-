package com.orbis.meudia;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Offline, original in-app user guide. No network, tracking or permissions. */
public final class AppGuide {
    private AppGuide() {}

    private static TextView section(Activity activity, String title, String description) {
        TextView view = new TextView(activity);
        view.setText(title + "\n" + description);
        view.setTextColor(Color.rgb(25, 51, 58));
        view.setTextSize(16);
        view.setPadding(20, 16, 20, 16);
        view.setContentDescription(title + ". " + description);
        return view;
    }

    public static void show(Activity activity, String language) {
        boolean portuguese = "pt".equals(language) || "pt-BR".equals(language);
        ScrollView scroll = new ScrollView(activity);
        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(12, 12, 12, 12);
        scroll.addView(content);
        String[][] entries = portuguese ? new String[][] {
            {"📅 Agenda", "Selecione um dia no calendário para ver e organizar as tarefas dessa data. O calendário é próprio do Checklist Orbis."},
            {"＋ Tarefas", "Crie uma tarefa, preencha título e descrição e configure horários e recorrências disponíveis no editor."},
            {"🎨 Cores de prioridade", "Verde: nível 1; azul: nível 2; amarelo: nível 3; laranja: nível 4; vermelho: nível 5. Escolha a prioridade ao editar a tarefa e use os filtros para mostrar apenas uma cor."},
            {"✓ Progresso", "Ajuste o percentual de 0 a 100%. Tarefas recorrentes mantêm o progresso separado por data."},
            {"⚠ Conflito de horário", "O símbolo indica tarefas exibidas que se sobrepõem no mesmo dia. Horários consecutivos não são conflitos. Filtros podem ocultar outras tarefas conflitantes."},
            {"🔎 Organização", "Use pesquisa, projetos, seções, etiquetas, filtros e ordenação para encontrar tarefas. As opções disponíveis aparecem na tela principal."},
            {"↻ Recorrência", "Configure a repetição no editor da tarefa. O histórico de cada ocorrência permanece associado à sua data."},
            {"↓ Exportar / ↑ Restaurar", "Exporte um backup JSON e guarde-o em local seguro. Restaurar substitui ou recupera os dados conforme o fluxo exibido; confira antes de confirmar. Fotos e vídeos locais não são incluídos no JSON."},
            {"🌐 Idiomas", "Toque no globo no cabeçalho para trocar o idioma."},
            {"☁ Conta e sincronização", "O acesso entre aparelhos pelo Supabase ainda está em desenvolvimento. Até ser disponibilizado e testado, não considere seus dados sincronizados automaticamente."}
        } : new String[][] {
            {"📅 Calendar", "Select a date to view and organize tasks. This calendar belongs to Checklist Orbis and does not use Google Calendar."},
            {"＋ Tasks", "Create a task, enter a title and description, and set times and recurrence in the task editor."},
            {"🎨 Priority colors", "Green: level 1; blue: 2; yellow: 3; orange: 4; red: 5. Set priority in the editor and filter by color."},
            {"✓ Progress", "Set progress from 0 to 100%. Recurring tasks keep separate progress for each date."},
            {"⚠ Time conflicts", "The symbol marks displayed tasks with overlapping times. Consecutive times do not conflict; filters may hide other tasks."},
            {"🔎 Organization", "Use search, projects, sections, tags, filters and sorting."},
            {"↻ Recurrence", "Configure repeating tasks in the editor; each occurrence has its own dated history."},
            {"↓ Export / ↑ Restore", "Keep JSON backups somewhere safe. Confirm restore actions carefully. Local photos and videos are not included in JSON backups."},
            {"🌐 Language", "Tap the globe in the header to change the language."},
            {"☁ Account and sync", "Supabase multi-device sync is not yet available. Do not assume your tasks are automatically synchronized."}
        };
        for (String[] entry : entries) content.addView(section(activity, entry[0], entry[1]));
        new AlertDialog.Builder(activity)
                .setTitle(portuguese ? "Como usar o Checklist Orbis" : "Checklist Orbis user guide")
                .setView(scroll)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
