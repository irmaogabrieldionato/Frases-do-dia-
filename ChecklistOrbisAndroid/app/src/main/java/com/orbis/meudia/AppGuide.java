package com.orbis.meudia;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

    /** Visual legend uses the same five priority colors as the task editor. */
    private static void addPriorityLegend(Activity activity, LinearLayout content, boolean portuguese) {
        final int[] colors = {0xFF2E7D32, 0xFF1565C0, 0xFFF9A825, 0xFFEF6C00, 0xFFC62828};
        final String[] pt = {"1 · Verde", "2 · Azul", "3 · Amarelo", "4 · Laranja", "5 · Vermelho"};
        final String[] en = {"1 · Green", "2 · Blue", "3 · Yellow", "4 · Orange", "5 · Red"};
        content.addView(section(activity,
                portuguese ? "Personalizar prioridades" : "Customize priorities",
                portuguese ? "No editor da tarefa, selecione uma das cinco cores. Na tela principal, use o filtro da mesma cor para visualizar essas tarefas."
                        : "Choose a priority color in the task editor, then use the matching filter on the main screen."));
        for (int i = 0; i < colors.length; i++) {
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(20, 8, 20, 8);
            View swatch = new View(activity);
            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.OVAL);
            background.setColor(colors[i]);
            background.setStroke(1, 0xFF455A64);
            swatch.setBackground(background);
            int size = (int)(24 * activity.getResources().getDisplayMetrics().density + 0.5f);
            row.addView(swatch, new LinearLayout.LayoutParams(size, size));
            TextView name = new TextView(activity);
            name.setText((portuguese ? pt : en)[i]);
            name.setTextColor(0xFF19333A);
            name.setTextSize(16);
            name.setPadding(16, 0, 0, 0);
            row.addView(name);
            row.setContentDescription((portuguese ? pt : en)[i]);
            content.addView(row);
        }
    }


    /** Visual priority swatches with translated names and accessible descriptions. */
    private static void addLocalizedPriorityLegend(Activity activity, LinearLayout content, String language) {
        final int[] colors = {0xFF2E7D32, 0xFF1565C0, 0xFFF9A825, 0xFFEF6C00, 0xFFC62828};
        String[] labels = GuideTopics.priorityColors(language);
        for (int i = 0; i < colors.length; i++) {
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(20, 8, 20, 8);
            View swatch = new View(activity);
            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.OVAL);
            background.setColor(colors[i]);
            background.setStroke(1, 0xFF455A64);
            swatch.setBackground(background);
            int size = (int) (24 * activity.getResources().getDisplayMetrics().density + 0.5f);
            row.addView(swatch, new LinearLayout.LayoutParams(size, size));
            TextView name = new TextView(activity);
            String label = (i + 1) + " · " + labels[i];
            name.setText(label);
            name.setTextColor(0xFF19333A);
            name.setTextSize(16);
            name.setPadding(16, 0, 0, 0);
            row.addView(name);
            row.setContentDescription(label);
            content.addView(row);
        }
    }

    public static void show(Activity activity, String language) {
        if (!"pt-BR".equals(language) && !"pt".equals(language) && !"en".equals(language)) {
            ScrollView localizedScroll = new ScrollView(activity);
            LinearLayout localizedContent = new LinearLayout(activity);
            localizedContent.setOrientation(LinearLayout.VERTICAL);
            localizedContent.setPadding(12, 12, 12, 12);
            String[] headings = GuideTopics.headings(language);
            String[] details = GuideTopics.details(language);
            String overview = GuideTranslations.overview(language);
            // A concise translated overview remains visible while the topics give
            // users an accessible index; the full illustrated guide is still being localized.
            localizedContent.addView(section(activity,
                    GuideTranslations.title(language), overview));
            for (int i = 0; i < headings.length; i++) {
                TextView heading = section(activity, (i + 1) + ". " + headings[i], details[i]);
                heading.setTextSize(17);
                heading.setTypeface(null, android.graphics.Typeface.BOLD);
                localizedContent.addView(heading);
                if (i == 1) addLocalizedPriorityLegend(activity, localizedContent, language);
            }
            localizedScroll.addView(localizedContent);
            new AlertDialog.Builder(activity)
                    .setTitle(GuideTranslations.title(language))
                    .setView(localizedScroll)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }
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
        for (String[] entry : entries) {
            content.addView(section(activity, entry[0], entry[1]));
            if (entry == entries[2]) addPriorityLegend(activity, content, portuguese);
        }
        content.addView(section(activity,
                portuguese ? "Personalizar a agenda" : "Customize your calendar",
                portuguese
                    ? "1. Toque numa data para mudar o dia selecionado. 2. Crie ou edite tarefas desse dia. 3. Defina horários e recorrências no editor. 4. Escolha a cor de prioridade. 5. Use filtros e ordenação para organizar a lista; os filtros não apagam tarefas. 6. Exporte um backup antes de restaurar dados."
                    : "1. Tap a date to select it. 2. Add or edit tasks for that day. 3. Set times and recurrence in the editor. 4. Choose a priority color. 5. Use filters and sorting to organize the list; filtering never deletes tasks. 6. Export a backup before restoring data."));
        new AlertDialog.Builder(activity)
                .setTitle(portuguese ? "Como usar o Checklist Orbis" : "Checklist Orbis user guide")
                .setView(scroll)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
