package com.orbis.meudia;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final int TEAL = Color.rgb(20, 108, 99);
    private static final int INK = Color.rgb(25, 51, 58);
    private static final int MUTED = Color.rgb(101, 124, 131);
    private static final int BG = Color.rgb(243, 247, 245);
    private static final int LINE = Color.rgb(228, 238, 234);
    private static final int WHITE = Color.WHITE;

    private final Locale brazil = new Locale("pt", "BR");
    private SharedPreferences preferences;
    private JSONArray tasks = new JSONArray();
    private String day;
    private TextView dateLabel;
    private TextView percentLabel;
    private TextView progressLabel;
    private TextView countLabel;
    private TextView emptyLabel;
    private LinearLayout tasksBox;
    private FrameLayout progressTrack;
    private View progressFill;
    private EditText taskInput;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        preferences = getSharedPreferences("meu-dia-dados-v1", MODE_PRIVATE);
        loadTasks();
        makeInterface();
        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tasksBox != null) {
            resetWhenNewDay();
            render();
        }
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private JSONObject task(String title) {
        JSONObject value = new JSONObject();
        try {
            value.put("id", UUID.randomUUID().toString());
            value.put("title", title);
            value.put("done", false);
        } catch (JSONException ignored) { }
        return value;
    }

    private void loadTasks() {
        day = preferences.getString("day", today());
        String saved = preferences.getString("tasks", null);
        if (saved != null) {
            try {
                tasks = new JSONArray(saved);
            } catch (JSONException ignored) {
                tasks = new JSONArray();
            }
        } else {
            tasks.put(task("Arrumar a cama"));
            tasks.put(task("Beber água"));
            tasks.put(task("Fazer exercícios"));
            tasks.put(task("Estudar 30 minutos"));
        }
        resetWhenNewDay();
        save();
    }

    private void resetWhenNewDay() {
        String now = today();
        if (!now.equals(day)) {
            day = now;
            for (int i = 0; i < tasks.length(); i++) {
                JSONObject item = tasks.optJSONObject(i);
                if (item != null) {
                    try { item.put("done", false); }
                    catch (JSONException ignored) { }
                }
            }
            save();
        }
    }

    private void save() {
        preferences.edit()
            .putString("day", day)
            .putString("tasks", tasks.toString())
            .apply();
    }

    private GradientDrawable rounded(int color, float radius, int border) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(radius));
        if (border != 0) bg.setStroke(dp(1), border);
        return bg;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView result = new TextView(this);
        result.setText(value);
        result.setTextSize(size);
        result.setTextColor(color);
        if (bold) result.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return result;
    }

    private LinearLayout vertical() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        return box;
    }

    private LinearLayout horizontal() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        return box;
    }

    private LinearLayout.LayoutParams lp(int width, int height) {
        return new LinearLayout.LayoutParams(width < 0 ? width : dp(width),
                height < 0 ? height : dp(height));
    }

    private void makeInterface() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        scroll.setClipToPadding(false);

        LinearLayout outer = vertical();
        outer.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(outer);

        int availableWidth = getResources().getDisplayMetrics().widthPixels;
        LinearLayout content = vertical();
        content.setPadding(dp(18), dp(24), dp(18), dp(48));
        outer.addView(content, new LinearLayout.LayoutParams(
                Math.min(availableWidth, dp(576)), LinearLayout.LayoutParams.WRAP_CONTENT));

        if (Build.VERSION.SDK_INT >= 35) {
            scroll.setOnApplyWindowInsetsListener((view, insets) -> {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                view.setPadding(0, bars.top, 0, bars.bottom);
                return insets;
            });
        }
        setContentView(scroll);

        LinearLayout header = horizontal();
        header.setGravity(Gravity.TOP);
        LinearLayout heading = vertical();
        header.addView(heading, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView eyebrow = text("SUA ROTINA, NO SEU RITMO", 12, TEAL, true);
        eyebrow.setLetterSpacing(0.1f);
        heading.addView(eyebrow);

        TextView title = text("Meu Dia ✦", 34, INK, true);
        LinearLayout.LayoutParams titleLp = lp(-1, -2);
        titleLp.topMargin = dp(3);
        heading.addView(title, titleLp);
        dateLabel = text("", 14, MUTED, false);
        LinearLayout.LayoutParams dateLp = lp(-1, -2);
        dateLp.topMargin = dp(6);
        heading.addView(dateLabel, dateLp);

        ImageView symbol = new ImageView(this);
        symbol.setImageResource(R.drawable.ic_header_check);
        symbol.setPadding(dp(10), dp(10), dp(10), dp(10));
        symbol.setBackground(rounded(TEAL, 16, 0));
        symbol.setContentDescription("Meu Dia: lista de tarefas");
        LinearLayout.LayoutParams symbolLp = lp(48, 48);
        symbolLp.leftMargin = dp(8);
        header.addView(symbol, symbolLp);

        LinearLayout.LayoutParams headerLp = lp(-1, -2);
        headerLp.bottomMargin = dp(24);
        content.addView(header, headerLp);

        LinearLayout card = vertical();
        card.setPadding(dp(24), dp(24), dp(24), dp(23));
        card.setBackground(rounded(TEAL, 23, 0));
        card.setElevation(dp(5));
        content.addView(card, lp(-1, -2));

        LinearLayout cardTop = horizontal();
        LinearLayout progressWords = vertical();
        cardTop.addView(progressWords, new LinearLayout.LayoutParams(0, -2, 1f));
        progressWords.addView(text("Progresso de hoje", 15, WHITE, true));
        progressLabel = text("", 13, Color.rgb(209, 240, 232), false);
        LinearLayout.LayoutParams progressTextLp = lp(-1, -2);
        progressTextLp.topMargin = dp(5);
        progressWords.addView(progressLabel, progressTextLp);

        percentLabel = text("0%", 34, WHITE, true);
        LinearLayout.LayoutParams perLp = lp(-2, -2);
        perLp.leftMargin = dp(12);
        cardTop.addView(percentLabel, perLp);
        card.addView(cardTop, lp(-1, -2));

        progressTrack = new FrameLayout(this);
        progressTrack.setBackground(rounded(Color.rgb(64, 140, 128), 12, 0));
        progressTrack.setClipToOutline(true);
        progressFill = new View(this);
        progressFill.setBackground(rounded(Color.rgb(220, 249, 230), 12, 0));
        progressTrack.addView(progressFill, new FrameLayout.LayoutParams(0, -1));
        LinearLayout.LayoutParams trackLp = lp(-1, 11);
        trackLp.topMargin = dp(21);
        card.addView(progressTrack, trackLp);

        LinearLayout section = horizontal();
        TextView headingTasks = text("Minhas tarefas", 20, INK, true);
        section.addView(headingTasks, new LinearLayout.LayoutParams(0, -2, 1f));
        countLabel = text("", 13, MUTED, false);
        section.addView(countLabel);
        LinearLayout.LayoutParams sectionLp = lp(-1, -2);
        sectionLp.topMargin = dp(29);
        sectionLp.bottomMargin = dp(12);
        content.addView(section, sectionLp);

        LinearLayout form = horizontal();
        taskInput = new EditText(this);
        taskInput.setSingleLine(true);
        taskInput.setTextColor(INK);
        taskInput.setTextSize(15);
        taskInput.setHintTextColor(Color.rgb(139, 160, 157));
        taskInput.setHint("Ex.: Beber 2 litros de água");
        taskInput.setPadding(dp(14), 0, dp(14), 0);
        taskInput.setBackground(rounded(WHITE, 14, LINE));
        taskInput.setFilters(new android.text.InputFilter[] {
            new android.text.InputFilter.LengthFilter(90)
        });
        taskInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        taskInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        LinearLayout.LayoutParams inputLp = new LinearLayout.LayoutParams(0, dp(53), 1f);
        inputLp.rightMargin = dp(9);
        form.addView(taskInput, inputLp);

        TextView add = text("+ Adicionar", 14, WHITE, true);
        add.setGravity(Gravity.CENTER);
        add.setBackground(rounded(TEAL, 14, 0));
        add.setContentDescription("Adicionar tarefa");
        add.setOnClickListener(v -> addTask());
        form.addView(add, lp(112, 53));

        taskInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addTask();
                return true;
            }
            return false;
        });

        LinearLayout.LayoutParams formLp = lp(-1, -2);
        formLp.bottomMargin = dp(15);
        content.addView(form, formLp);

        tasksBox = vertical();
        content.addView(tasksBox, lp(-1, -2));

        emptyLabel = text("Sua lista está vazia. Adicione sua primeira tarefa acima ✨",
                14, MUTED, false);
        emptyLabel.setGravity(Gravity.CENTER);
        emptyLabel.setPadding(dp(14), dp(28), dp(14), dp(28));
        emptyLabel.setBackground(rounded(WHITE, 16, LINE));
        content.addView(emptyLabel, lp(-1, -2));

        TextView tip = text("ⓘ  As tarefas ficam salvas neste aparelho. A cada novo dia, elas voltam a ficar pendentes.",
                13, MUTED, false);
        tip.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams tipLp = lp(-1, -2);
        tipLp.topMargin = dp(24);
        content.addView(tip, tipLp);

        TextView footer = text("Sem cadastro · Seus dados ficam neste aparelho",
                12, Color.rgb(132, 155, 149), false);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footLp = lp(-1, -2);
        footLp.topMargin = dp(36);
        content.addView(footer, footLp);
    }

    private void addTask() {
        resetWhenNewDay();
        String title = taskInput.getText().toString().trim();
        if (title.isEmpty()) return;
        tasks.put(task(title));
        taskInput.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(taskInput.getWindowToken(), 0);
        taskInput.clearFocus();
        save();
        render();
    }

    private void render() {
        resetWhenNewDay();
        String date = new SimpleDateFormat("EEEE, d 'de' MMMM", brazil).format(new Date());
        dateLabel.setText(date.substring(0, 1).toUpperCase(brazil) + date.substring(1));
        int total = tasks.length();
        int done = 0;
        for (int i = 0; i < total; i++) {
            JSONObject job = tasks.optJSONObject(i);
            if (job != null && job.optBoolean("done")) done++;
        }
        final int percent = total == 0 ? 0 : Math.round(done * 100f / total);
        progressLabel.setText(done + " de " + total + " tarefas concluídas");
        percentLabel.setText(percent + "%");
        countLabel.setText(total + (total == 1 ? " tarefa" : " tarefas"));
        progressTrack.setContentDescription("Progresso diário: " + percent + "%");
        progressTrack.post(() -> {
            FrameLayout.LayoutParams fillLp =
                    (FrameLayout.LayoutParams) progressFill.getLayoutParams();
            fillLp.width = Math.round(progressTrack.getWidth() * percent / 100f);
            progressFill.setLayoutParams(fillLp);
        });

        tasksBox.removeAllViews();
        emptyLabel.setVisibility(total == 0 ? View.VISIBLE : View.GONE);
        for (int i = 0; i < total; i++) {
            JSONObject job = tasks.optJSONObject(i);
            if (job != null) addTaskRow(job);
        }
    }

    private void addTaskRow(JSONObject job) {
        boolean done = job.optBoolean("done");
        String title = job.optString("title", "");
        String id = job.optString("id", "");

        LinearLayout row = horizontal();
        row.setMinimumHeight(dp(66));
        row.setPadding(dp(15), dp(13), dp(15), dp(13));
        row.setBackground(rounded(WHITE, 16, LINE));
        row.setElevation(dp(2));

        TextView check = text(done ? "✓" : "", 20, WHITE, true);
        check.setGravity(Gravity.CENTER);
        check.setBackground(rounded(done ? TEAL : WHITE, 9,
                done ? TEAL : Color.rgb(181, 207, 197)));
        check.setContentDescription((done ? "Marcar como pendente: " : "Concluir: ") + title);
        check.setOnClickListener(v -> {
            resetWhenNewDay();
            try { job.put("done", !job.optBoolean("done")); }
            catch (JSONException ignored) { }
            save();
            render();
        });
        row.addView(check, lp(28, 28));

        TextView taskText = text(title, 15,
                done ? Color.rgb(141, 161, 156) : INK, false);
        if (done) taskText.setPaintFlags(taskText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        taskText.setPadding(dp(12), 0, dp(6), 0);
        row.addView(taskText, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView delete = text("×", 26, Color.rgb(155, 177, 170), false);
        delete.setGravity(Gravity.CENTER);
        delete.setContentDescription("Excluir: " + title);
        delete.setOnClickListener(v -> {
            JSONArray kept = new JSONArray();
            for (int i = 0; i < tasks.length(); i++) {
                JSONObject item = tasks.optJSONObject(i);
                if (item != null && !id.equals(item.optString("id"))) kept.put(item);
            }
            tasks = kept;
            save();
            render();
        });
        row.addView(delete, lp(33, 33));

        LinearLayout.LayoutParams rowLp = lp(-1, -2);
        rowLp.bottomMargin = dp(10);
        tasksBox.addView(row, rowLp);
    }
}
