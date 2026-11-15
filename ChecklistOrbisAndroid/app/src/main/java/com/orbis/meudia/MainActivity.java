package com.orbis.meudia;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.HorizontalScrollView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Arrays;
import java.text.DateFormatSymbols;
import android.text.InputType;

public class MainActivity extends Activity {
    private static final int TEAL = Color.rgb(20, 108, 99);
    private static final int INK = Color.rgb(25, 51, 58);
    private static final int MUTED = Color.rgb(101, 124, 131);
    private static final int BG = Color.rgb(243, 247, 245);
    private static final int LINE = Color.rgb(228, 238, 234);
    private static final int WHITE = Color.WHITE;
    private static final int[] PRIORITY_COLORS = {
        Color.rgb(46,125,50),   // 1: green
        Color.rgb(21,101,192),  // 2: blue
        Color.rgb(249,168,37),  // 3: yellow
        Color.rgb(239,108,0),   // 4: orange
        Color.rgb(198,40,40)    // 5: red
    };
    private static final String[] PRIORITY_KEYS = {
        "green", "blue", "yellow", "orange", "red"
    };
    private static final int[] PRIORITY_LIGHT = {
        Color.rgb(232,245,233),
        Color.rgb(227,242,253),
        Color.rgb(255,248,225),
        Color.rgb(255,243,224),
        Color.rgb(255,235,238)
    };

    private String selectedLanguage;

    /** Each occurrence has independent progress for its calendar date. */
    private int progress(JSONObject item) {
        if(item==null)return 0;
        JSONObject history=item.optJSONObject("history");
        if(history==null)return 0;
        return Math.max(0,Math.min(100,history.optInt(selectedDay,0)));
    }

    private void setProgress(JSONObject item,int percent) {
        if(item==null)return;
        JSONObject history=item.optJSONObject("history");
        if(history==null)history=new JSONObject();
        try {
            history.put(selectedDay,Math.max(0,Math.min(100,percent)));
            item.put("history",history);
        } catch(JSONException ignored){}
    }

    private int priority(JSONObject item) {
        if(item==null)return 2;
        int level=item.optInt("priority",2);
        return level>=1 && level<=5?level:2;
    }

    private int priorityColor(int level) {
        return PRIORITY_COLORS[Math.max(1,Math.min(5,level))-1];
    }

    private boolean visible(JSONObject item){
        return Schedule.occurs(item,selectedDay)
                && (priorityFilter==0 || priority(item)==priorityFilter);
    }

    private SharedPreferences preferences;
    private JSONArray tasks = new JSONArray();
    private String day;
    private String selectedDay;
    private Calendar shownMonth;
    private LinearLayout calendarBody;
    private LinearLayout priorityFilterRow;
    private int priorityFilter=0;  // 0 = all, 1–5 = chosen level
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
        selectedLanguage = preferences.getString("app-language", null);
        if (Languages.index(selectedLanguage) < 0) {
            showLanguagePicker(true);
        } else {
            loadTasks();
            makeInterface();
            render();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(tasksBox!=null && day!=null) {
            resetWhenNewDay();
            render();
        }
    }

    private String tr(String key) {
        return Languages.text(selectedLanguage, key);
    }

    private Locale selectedLocale() {
        return Languages.locale(selectedLanguage);
    }

    /** First screen on installation; also available later from the globe button. */
    private void showLanguagePicker(boolean firstRun) {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        LinearLayout outer = vertical();
        outer.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(outer);

        LinearLayout page = vertical();
        page.setPadding(dp(22), dp(30), dp(22), dp(38));
        int width = Math.min(getResources().getDisplayMetrics().widthPixels, dp(560));
        outer.addView(page, new LinearLayout.LayoutParams(width, -2));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_launcher);
        page.addView(logo, lp(75, 75));

        TextView heading = text("Escolha seu idioma / Choose your language", 23, INK, true);
        LinearLayout.LayoutParams headingLp = lp(-1, -2);
        headingLp.topMargin = dp(14);
        page.addView(heading, headingLp);

        TextView description = text(
            "Selecione um idioma. Você poderá mudar depois.\n" +
            "Select a language. You can change it later.", 14, MUTED, false);
        LinearLayout.LayoutParams descriptionLp = lp(-1, -2);
        descriptionLp.topMargin = dp(8);
        descriptionLp.bottomMargin = dp(16);
        page.addView(description, descriptionLp);

        RadioGroup choices = new RadioGroup(this);
        choices.setOrientation(RadioGroup.VERTICAL);
        choices.setBackground(rounded(WHITE, 18, LINE));
        choices.setPadding(dp(12), dp(10), dp(12), dp(10));
        page.addView(choices, lp(-1, -2));
        final int[] current = {Math.max(0, Languages.index(selectedLanguage))};
        if (firstRun) current[0] = 8; // Português pré-selecionado; o usuário confirma.
        for (int i = 0; i < Languages.CODES.length; i++) {
            RadioButton option = new RadioButton(this);
            option.setId(1000 + i);
            option.setText(Languages.NAMES[i]);
            option.setTextSize(17);
            option.setTextColor(INK);
            option.setPadding(dp(8), dp(6), dp(5), dp(6));
            option.setButtonTintList(ColorStateList.valueOf(TEAL));
            choices.addView(option, new RadioGroup.LayoutParams(-1, dp(48)));
        }
        choices.check(1000 + current[0]);
        choices.setOnCheckedChangeListener((group, checkedId) -> current[0] = checkedId - 1000);

        TextView confirm = text("Continuar / Continue", 16, WHITE, true);
        confirm.setGravity(Gravity.CENTER);
        confirm.setBackground(rounded(TEAL, 14, 0));
        LinearLayout.LayoutParams confirmLp = lp(-1, 54);
        confirmLp.topMargin = dp(16);
        page.addView(confirm, confirmLp);
        confirm.setOnClickListener(v -> {
            if (current[0] < 0 || current[0] >= Languages.CODES.length) return;
            selectedLanguage = Languages.CODES[current[0]];
            preferences.edit().putString("app-language", selectedLanguage).apply();
            if (firstRun) loadTasks();
            makeInterface();
            render();
        });

        if (!firstRun) {
            TextView cancel = text(tr("cancel"), 15, TEAL, true);
            cancel.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams cancelLp = lp(-1, 45);
            cancelLp.topMargin = dp(8);
            page.addView(cancel, cancelLp);
            cancel.setOnClickListener(v -> {
                makeInterface();
                render();
            });
        }

        setContentView(scroll);
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
            value.put("start",selectedDay);
            value.put("mode","once");
            value.put("priority",2); // Blue by default, including legacy tasks.
            value.put("history",new JSONObject());
        } catch (JSONException ignored) { }
        return value;
    }

    private void loadTasks() {
        day=preferences.getString("day",today());
        selectedDay=today();
        shownMonth=Schedule.date(selectedDay);
        String stored=preferences.getString("tasks",null);
        if(stored!=null){
            try{tasks=new JSONArray(stored);}catch(JSONException e){tasks=new JSONArray();}
        } else {
            for(String key:new String[]{"default1","default2","default3","default4"}){
                JSONObject item=task(tr(key));
                try{item.put("mode","daily");}catch(JSONException ignored){}
                tasks.put(item);
            }
        }
        // Migration from earlier versions. Last day's slider value is retained.
        for(int i=0;i<tasks.length();i++){
            JSONObject item=tasks.optJSONObject(i);
            if(item==null)continue;
            try{
                if(!item.has("history")){
                    int old=item.has("progress")?item.optInt("progress",0):
                            (item.optBoolean("done",false)?100:0);
                    JSONObject history=new JSONObject();
                    if(old>0)history.put(day,old);
                    item.put("history",history);
                }
                if(!item.has("start"))item.put("start",day);
                if(!item.has("mode"))item.put("mode","daily");
                if(!item.has("id"))item.put("id",UUID.randomUUID().toString());
                int level=item.optInt("priority",2);
                if(level<1 || level>5)item.put("priority",2);
                else if(!item.has("priority"))item.put("priority",2);
            }catch(JSONException ignored){}
        }
        resetWhenNewDay();
        save();
    }

    private void resetWhenNewDay() {
        // Calendar progress must never be reset on date transitions.
        day=today();
    }

    private void save() {
        preferences.edit().putString("day",today())
                .putString("tasks",tasks.toString()).apply();
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
        scroll.setLayoutDirection(Languages.isRtl(selectedLanguage)
                ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

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

        TextView eyebrow = text(tr("eyebrow").toUpperCase(selectedLocale()), 12, TEAL, true);
        eyebrow.setLetterSpacing(0.1f);
        heading.addView(eyebrow);

        TextView title = text("Checklist Orbis ✦", 30, INK, true);
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
        symbol.setContentDescription("Checklist Orbis");
        symbol.setOnClickListener(v -> showLanguagePicker(false));
        LinearLayout.LayoutParams symbolLp = lp(48, 48);
        symbolLp.leftMargin = dp(8);
        header.addView(symbol, symbolLp);
        TextView languageButton = text("🌐", 24, TEAL, true);
        languageButton.setGravity(Gravity.CENTER);
        languageButton.setContentDescription(tr("language"));
        languageButton.setOnClickListener(v -> showLanguagePicker(false));
        LinearLayout.LayoutParams languageLp = lp(44, 48);
        languageLp.leftMargin = dp(5);
        header.addView(languageButton, languageLp);

        LinearLayout.LayoutParams headerLp = lp(-1, -2);
        headerLp.bottomMargin = dp(24);
        content.addView(header, headerLp);

        calendarBody=vertical();
        LinearLayout.LayoutParams calLp=lp(-1,-2);
        calLp.bottomMargin=dp(18);
        content.addView(calendarBody,calLp);
        renderCalendar();

        LinearLayout card = vertical();
        card.setPadding(dp(24), dp(24), dp(24), dp(23));
        card.setBackground(rounded(TEAL, 23, 0));
        card.setElevation(dp(5));
        content.addView(card, lp(-1, -2));

        LinearLayout cardTop = horizontal();
        LinearLayout progressWords = vertical();
        cardTop.addView(progressWords, new LinearLayout.LayoutParams(0, -2, 1f));
        progressWords.addView(text(tr("selected_progress"), 15, WHITE, true));
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
        TextView headingTasks = text(tr("tasks"), 20, INK, true);
        section.addView(headingTasks, new LinearLayout.LayoutParams(0, -2, 1f));
        countLabel = text("", 13, MUTED, false);
        section.addView(countLabel);
        LinearLayout.LayoutParams sectionLp = lp(-1, -2);
        sectionLp.topMargin = dp(29);
        sectionLp.bottomMargin = dp(12);
        content.addView(section, sectionLp);

        // Horizontal priority filter: All, green, blue, yellow, orange, red.
        TextView filtersLabel=text(tr("filter_priority"),13,MUTED,true);
        LinearLayout.LayoutParams filtersLabelLp=lp(-1,-2);
        filtersLabelLp.bottomMargin=dp(8);
        content.addView(filtersLabel,filtersLabelLp);

        HorizontalScrollView filterScroll=new HorizontalScrollView(this);
        filterScroll.setHorizontalScrollBarEnabled(false);
        filterScroll.setFillViewport(false);
        priorityFilterRow=horizontal();
        priorityFilterRow.setPadding(dp(1),dp(1),dp(1),dp(3));
        filterScroll.addView(priorityFilterRow);
        renderPriorityFilters();
        LinearLayout.LayoutParams filterScrollLp=lp(-1,-2);
        filterScrollLp.bottomMargin=dp(14);
        content.addView(filterScroll,filterScrollLp);

        LinearLayout form = horizontal();
        taskInput = new EditText(this);
        taskInput.setSingleLine(true);
        taskInput.setTextColor(INK);
        taskInput.setTextSize(15);
        taskInput.setHintTextColor(Color.rgb(139, 160, 157));
        taskInput.setHint(tr("placeholder"));
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

        TextView add = text("+ " + tr("add"), 14, WHITE, true);
        add.setGravity(Gravity.CENTER);
        add.setBackground(rounded(TEAL, 14, 0));
        add.setContentDescription(tr("add"));
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

        emptyLabel = text(tr("empty"),
                14, MUTED, false);
        emptyLabel.setGravity(Gravity.CENTER);
        emptyLabel.setPadding(dp(14), dp(28), dp(14), dp(28));
        emptyLabel.setBackground(rounded(WHITE, 16, LINE));
        content.addView(emptyLabel, lp(-1, -2));

        TextView tip = text("ⓘ  " + tr("tip"),
                13, MUTED, false);
        tip.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams tipLp = lp(-1, -2);
        tipLp.topMargin = dp(24);
        content.addView(tip, tipLp);

        TextView footer = text(tr("footer"),
                12, Color.rgb(132, 155, 149), false);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footLp = lp(-1, -2);
        footLp.topMargin = dp(36);
        content.addView(footer, footLp);
    }

    private void addTask() {
        String title=taskInput.getText().toString().trim();
        if(title.isEmpty())return;
        showScheduleEditor(task(title),true);
    }

    private String formatDay(String key) {
        return DateFormat.getDateInstance(DateFormat.MEDIUM,selectedLocale())
                .format(Schedule.date(key).getTime());
    }

    private TextView action(String title) {
        TextView button=text(title,15,TEAL,true);
        button.setGravity(Gravity.CENTER);
        button.setBackground(rounded(WHITE,12,LINE));
        button.setPadding(dp(6),dp(7),dp(6),dp(7));
        return button;
    }

    private void renderPriorityFilters() {
        if(priorityFilterRow==null)return;
        priorityFilterRow.removeAllViews();
        for(int level=0;level<=5;level++){
            final int chosen=level;
            boolean active=level==priorityFilter;
            String label=level==0?tr("all"):tr(PRIORITY_KEYS[level-1]);
            TextView chip=text(level==0?label:"● "+label,13,
                    active&&level==0?WHITE:(level==0?INK:priorityColor(level)),true);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(14),0,dp(14),0);
            int fill=active?(level==0?TEAL:PRIORITY_LIGHT[level-1]):WHITE;
            int stroke=level==0?(active?TEAL:LINE):priorityColor(level);
            chip.setBackground(rounded(fill,13,stroke));
            chip.setSelected(active);
            chip.setContentDescription(tr("filter_priority")+": "+label);
            chip.setOnClickListener(v->{
                if(priorityFilter==chosen)return;
                priorityFilter=chosen;
                renderPriorityFilters();
                render();
            });
            LinearLayout.LayoutParams cp=lp(-2,39);
            cp.rightMargin=dp(7);
            priorityFilterRow.addView(chip,cp);
        }
    }

    private int calendarPriorityForDay(String date) {
        int highest=0;
        for(int i=0;i<tasks.length();i++){
            JSONObject task=tasks.optJSONObject(i);
            if(Schedule.occurs(task,date)
                    && (priorityFilter==0 || priority(task)==priorityFilter)){
                highest=Math.max(highest,priority(task));
            }
        }
        return highest;
    }

    private boolean hasTasksForDay(String date) {
        return calendarPriorityForDay(date)>0;
    }

    private void renderCalendar(){
        if(calendarBody==null||shownMonth==null)return;
        calendarBody.removeAllViews();
        LinearLayout frame=vertical();
        frame.setPadding(dp(12),dp(12),dp(12),dp(14));
        frame.setBackground(rounded(WHITE,20,LINE));
        calendarBody.addView(frame,lp(-1,-2));

        LinearLayout toolbar=horizontal();
        TextView prev=action("‹");
        prev.setContentDescription(tr("previous_month"));
        toolbar.addView(prev,lp(42,42));
        prev.setOnClickListener(v->moveMonth(-1));
        String month=new SimpleDateFormat("LLLL yyyy",selectedLocale())
                .format(shownMonth.getTime());
        TextView heading=text(month.substring(0,1).toUpperCase(selectedLocale())+
                month.substring(1),18,INK,true);
        heading.setGravity(Gravity.CENTER);
        toolbar.addView(heading,new LinearLayout.LayoutParams(0,dp(42),1f));
        TextView next=action("›");
        next.setContentDescription(tr("next_month"));
        toolbar.addView(next,lp(42,42));
        next.setOnClickListener(v->moveMonth(1));
        frame.addView(toolbar,lp(-1,-2));

        TextView now=text(tr("today"),13,TEAL,true);
        now.setGravity(Gravity.CENTER);
        frame.addView(now,lp(-1,35));
        now.setOnClickListener(v->{
            selectedDay=today();
            shownMonth=Schedule.date(selectedDay);
            render();
        });

        Calendar cal=(Calendar)shownMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH,1);
        int first=Calendar.getInstance(selectedLocale()).getFirstDayOfWeek();
        String[] names=new DateFormatSymbols(selectedLocale()).getShortWeekdays();
        LinearLayout labels=horizontal();
        for(int i=0;i<7;i++){
            int dow=((first-1+i)%7)+1;
            TextView label=text(names[dow],12,MUTED,true);
            label.setGravity(Gravity.CENTER);
            labels.addView(label,new LinearLayout.LayoutParams(0,dp(28),1f));
        }
        frame.addView(labels,lp(-1,-2));

        int offset=(cal.get(Calendar.DAY_OF_WEEK)-first+7)%7;
        cal.add(Calendar.DAY_OF_MONTH,-offset);
        for(int r=0;r<6;r++){
            LinearLayout week=horizontal();
            for(int c=0;c<7;c++){
                String key=Schedule.key(cal);
                boolean currentMonth=cal.get(Calendar.MONTH)==shownMonth.get(Calendar.MONTH)
                        && cal.get(Calendar.YEAR)==shownMonth.get(Calendar.YEAR);
                boolean selected=key.equals(selectedDay);
                boolean todayCell=key.equals(today());
                int markerPriority=calendarPriorityForDay(key);
                String dateNumber=String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
                String cellLabel=dateNumber+(markerPriority>0?" •":"");
                TextView cell=text(cellLabel,
                        13,selected?WHITE:(currentMonth?(todayCell?TEAL:INK):MUTED),
                        selected||todayCell);
                if(markerPriority>0){
                    SpannableString styled=new SpannableString(cellLabel);
                    styled.setSpan(new ForegroundColorSpan(priorityColor(markerPriority)),
                            dateNumber.length()+1,cellLabel.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    cell.setText(styled);
                }
                cell.setGravity(Gravity.CENTER);
                if(selected)cell.setBackground(rounded(TEAL,11,0));
                else if(todayCell)cell.setBackground(rounded(Color.rgb(230,244,239),11,0));
                cell.setContentDescription(formatDay(key)+(markerPriority>0?" • "+tr("tasks"):""));
                cell.setOnClickListener(v->{
                    selectedDay=key;
                    shownMonth=Schedule.date(key);
                    render();
                });
                week.addView(cell,new LinearLayout.LayoutParams(0,dp(44),1f));
                cal.add(Calendar.DAY_OF_MONTH,1);
            }
            frame.addView(week,lp(-1,-2));
        }
        TextView note=text(tr("calendar_tip"),12,MUTED,false);
        note.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams noteLp=lp(-1,-2);
        noteLp.topMargin=dp(6);
        frame.addView(note,noteLp);
    }

    private void moveMonth(int direction){
        shownMonth.set(Calendar.DAY_OF_MONTH,1);
        shownMonth.add(Calendar.MONTH,direction);
        renderCalendar();
    }

    private interface DateSelection { void accept(String key); }

    private void pickDate(String key,DateSelection callback){
        Calendar cal=Schedule.date(key);
        DatePickerDialog dialog=new DatePickerDialog(this,
                (picker,y,month,day)->{
                    Calendar chosen=new GregorianCalendar(y,month,day);
                    callback.accept(Schedule.key(chosen));
                },cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showScheduleEditor(JSONObject item,boolean creating){
        String[] modes={"once","daily","weekly","monthly","yearly"};
        String[] modeNames={tr("once"),tr("daily"),tr("weekly"),tr("monthly"),tr("yearly")};
        String[] endNames={tr("never"),tr("until"),tr("after_count")};
        String[] start={item.optString("start",selectedDay)};
        String[] until={item.optString("until",start[0])};
        int modePos=Arrays.asList(modes).indexOf(item.optString("mode","once"));
        if(modePos<0)modePos=0;

        ScrollView scroll=new ScrollView(this);
        LinearLayout form=vertical();
        form.setPadding(dp(16),dp(10),dp(16),dp(16));
        scroll.addView(form);

        form.addView(text(tr("task_name"),14,INK,true),lp(-1,-2));
        EditText name=new EditText(this);
        name.setSingleLine(true);
        name.setText(item.optString("title",""));
        name.setFilters(new android.text.InputFilter[]{
                new android.text.InputFilter.LengthFilter(90)});
        form.addView(name,lp(-1,-2));

        TextView priorityLabel=text(tr("priority"),14,INK,true);
        LinearLayout.LayoutParams priorityLabelLp=lp(-1,-2);
        priorityLabelLp.topMargin=dp(12);
        form.addView(priorityLabel,priorityLabelLp);
        LinearLayout priorityChoice=horizontal();
        TextView colorDot=text("●",27,priorityColor(priority(item)),true);
        colorDot.setGravity(Gravity.CENTER);
        priorityChoice.addView(colorDot,lp(32,48));
        String[] priorityOptions=new String[5];
        for(int level=1;level<=5;level++){
            priorityOptions[level-1]=level+" · "+tr(PRIORITY_KEYS[level-1]);
        }
        Spinner prioritySpinner=new Spinner(this);
        prioritySpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,priorityOptions));
        prioritySpinner.setSelection(priority(item)-1);
        priorityChoice.addView(prioritySpinner,new LinearLayout.LayoutParams(0,dp(48),1f));
        form.addView(priorityChoice,lp(-1,-2));
        prioritySpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener(){
                    @Override public void onItemSelected(
                            android.widget.AdapterView<?> parent,View view,int pos,long id){
                        colorDot.setTextColor(priorityColor(pos+1));
                    }
                    @Override public void onNothingSelected(
                            android.widget.AdapterView<?> parent){}
                });

        TextView startButton=action("");
        Runnable updateStart=()->startButton.setText(tr("start_date")+": "+formatDay(start[0]));
        updateStart.run();
        LinearLayout.LayoutParams startLp=lp(-1,52);
        startLp.topMargin=dp(8);
        form.addView(startButton,startLp);
        startButton.setOnClickListener(v->pickDate(start[0],key->{
            start[0]=key;
            updateStart.run();
        }));

        TextView repeatLabel=text(tr("repeat"),14,INK,true);
        LinearLayout.LayoutParams repeatLp=lp(-1,-2);
        repeatLp.topMargin=dp(10);
        form.addView(repeatLabel,repeatLp);
        Spinner repeat=new Spinner(this);
        repeat.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,modeNames));
        repeat.setSelection(modePos);
        form.addView(repeat,lp(-1,48));

        LinearLayout weeklyBox=vertical();
        weeklyBox.addView(text(tr("weekdays"),14,INK,true),lp(-1,-2));
        LinearLayout weekdays=horizontal();
        String[] dayNames=new DateFormatSymbols(selectedLocale()).getShortWeekdays();
        int first=Calendar.getInstance(selectedLocale()).getFirstDayOfWeek();
        int mask=item.optInt("weekdays",0);
        if(mask==0)mask=1<<(Schedule.date(start[0]).get(Calendar.DAY_OF_WEEK)-1);
        CheckBox[] boxes=new CheckBox[7];
        for(int i=0;i<7;i++){
            int dayOfWeek=((first-1+i)%7)+1;
            CheckBox cb=new CheckBox(this);
            String text=dayNames[dayOfWeek];
            cb.setText(text.isEmpty()?"•":text.substring(0,1));
            cb.setContentDescription(text);
            cb.setTextSize(11);
            cb.setPadding(0,0,0,0);
            cb.setTag(dayOfWeek);
            cb.setButtonTintList(ColorStateList.valueOf(TEAL));
            cb.setChecked((mask&(1<<(dayOfWeek-1)))!=0);
            weekdays.addView(cb,new LinearLayout.LayoutParams(0,dp(44),1f));
            boxes[i]=cb;
        }
        weeklyBox.addView(weekdays,lp(-1,-2));
        form.addView(weeklyBox,lp(-1,-2));

        TextView endLabel=text(tr("end_condition"),14,INK,true);
        LinearLayout.LayoutParams endLp=lp(-1,-2);
        endLp.topMargin=dp(10);
        form.addView(endLabel,endLp);
        Spinner endMode=new Spinner(this);
        endMode.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,endNames));
        endMode.setSelection(item.optInt("count",0)>0?2:(item.has("until")?1:0));
        form.addView(endMode,lp(-1,48));

        TextView endButton=action("");
        Runnable updateEnd=()->endButton.setText(tr("end_date")+": "+formatDay(until[0]));
        updateEnd.run();
        form.addView(endButton,lp(-1,50));
        endButton.setOnClickListener(v->pickDate(until[0],key->{
            until[0]=key;
            updateEnd.run();
        }));
        EditText occurrences=new EditText(this);
        occurrences.setSingleLine(true);
        occurrences.setInputType(InputType.TYPE_CLASS_NUMBER);
        occurrences.setHint(tr("occurrences"));
        if(item.optInt("count",0)>0)occurrences.setText(String.valueOf(item.optInt("count")));
        form.addView(occurrences,lp(-1,50));

        Runnable refresh=()->{
            boolean recurring=repeat.getSelectedItemPosition()!=0;
            weeklyBox.setVisibility(recurring && repeat.getSelectedItemPosition()==2?
                    View.VISIBLE:View.GONE);
            endLabel.setVisibility(recurring?View.VISIBLE:View.GONE);
            endMode.setVisibility(recurring?View.VISIBLE:View.GONE);
            endButton.setVisibility(recurring && endMode.getSelectedItemPosition()==1?
                    View.VISIBLE:View.GONE);
            occurrences.setVisibility(recurring && endMode.getSelectedItemPosition()==2?
                    View.VISIBLE:View.GONE);
        };
        android.widget.AdapterView.OnItemSelectedListener listener=
                new android.widget.AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(android.widget.AdapterView<?> parent,
                                                 View view,int position,long id){refresh.run();}
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent){}
        };
        repeat.setOnItemSelectedListener(listener);
        endMode.setOnItemSelectedListener(listener);
        refresh.run();

        AlertDialog dialog=new AlertDialog.Builder(this)
                .setTitle(creating?tr("schedule_new"):tr("edit_schedule"))
                .setView(scroll)
                .setNegativeButton(tr("cancel"),(d,w)->{})
                .setPositiveButton(tr("save"),null).create();
        dialog.setOnShowListener(ignore->dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener(button->{
                String newTitle=name.getText().toString().trim();
                if(newTitle.isEmpty()){name.setError(tr("required"));return;}
                int modeIndex=repeat.getSelectedItemPosition();
                int weekdaysMask=0;
                for(CheckBox cb:boxes)if(cb.isChecked())
                    weekdaysMask|=1<<(((Integer)cb.getTag())-1);
                if(modeIndex==2 && weekdaysMask==0){
                    android.widget.Toast.makeText(this,tr("choose_weekday"),
                            android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                int endIndex=endMode.getSelectedItemPosition();
                if(modeIndex!=0 && endIndex==1 && until[0].compareTo(start[0])<0){
                    android.widget.Toast.makeText(this,tr("bad_end"),
                            android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                int count=0;
                if(modeIndex!=0 && endIndex==2){
                    try{count=Integer.parseInt(occurrences.getText().toString());}
                    catch(NumberFormatException e){count=0;}
                    if(count<1 || count>10000){
                        occurrences.setError(tr("count_range"));return;
                    }
                }
                try {
                    item.put("title",newTitle);
                    item.put("start",start[0]);
                    item.put("mode",modes[modeIndex]);
                    item.put("priority",prioritySpinner.getSelectedItemPosition()+1);
                    item.put("weekdays",weekdaysMask);
                    item.remove("until");
                    item.remove("count");
                    if(modeIndex!=0 && endIndex==1)item.put("until",until[0]);
                    if(modeIndex!=0 && endIndex==2)item.put("count",count);
                    if(creating)tasks.put(item);
                }catch(JSONException ignored){}
                taskInput.setText("");
                InputMethodManager keyboard=(InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                if(keyboard!=null)keyboard.hideSoftInputFromWindow(taskInput.getWindowToken(),0);
                if(creating){
                    selectedDay=start[0];
                    shownMonth=Schedule.date(selectedDay);
                }
                save();dialog.dismiss();render();
            }));
        dialog.show();
    }

    private void deleteOccurrence(JSONObject item){
        if("once".equals(item.optString("mode","once"))){
            removeSeries(item);return;
        }
        new AlertDialog.Builder(this).setTitle(item.optString("title"))
                .setItems(new String[]{tr("delete_this"),tr("delete_all")},
                        (dialog,which)->{
                            if(which==1)removeSeries(item);
                            else{
                                JSONArray excluded=item.optJSONArray("skips");
                                if(excluded==null)excluded=new JSONArray();
                                excluded.put(selectedDay);
                                try{item.put("skips",excluded);}
                                catch(JSONException ignored){}
                                save();render();
                            }
                        })
                .setNegativeButton(tr("cancel"),(dialog,which)->{})
                .show();
    }

    private void removeSeries(JSONObject target){
        JSONArray kept=new JSONArray();
        String id=target.optString("id");
        for(int i=0;i<tasks.length();i++){
            JSONObject item=tasks.optJSONObject(i);
            if(item!=null && !id.equals(item.optString("id")))kept.put(item);
        }
        tasks=kept;save();render();
    }

    /** Aggregate only tasks scheduled for the currently selected day. */
    private void updateSummary(){
        int total=0,completed=0,sum=0;
        for(int i=0;i<tasks.length();i++){
            JSONObject item=tasks.optJSONObject(i);
            if(!visible(item))continue;
            int value=progress(item);
            total++;sum+=value;
            if(value==100)completed++;
        }
        final int percent=total==0?0:Math.round(sum/(float)total);
        progressLabel.setText(String.format(selectedLocale(),tr("progress_count"),
                completed,total));
        percentLabel.setText(percent+"%");
        countLabel.setText(String.format(selectedLocale(),tr("task_count"),total));
        progressTrack.setContentDescription(String.format(selectedLocale(),
                tr("progress_accessible"),percent));
        progressTrack.post(()->{
            FrameLayout.LayoutParams params=(FrameLayout.LayoutParams)progressFill.getLayoutParams();
            params.width=Math.round(progressTrack.getWidth()*percent/100f);
            progressFill.setLayoutParams(params);
        });
    }

    private void render(){
        resetWhenNewDay();
        if(dateLabel==null||tasksBox==null)return;
        dateLabel.setText(formatDay(selectedDay));
        renderPriorityFilters();
        renderCalendar();
        updateSummary();
        tasksBox.removeAllViews();
        int count=0;
        for(int i=0;i<tasks.length();i++){
            JSONObject item=tasks.optJSONObject(i);
            if(visible(item)){addTaskRow(item);count++;}
        }
        emptyLabel.setVisibility(count==0?View.VISIBLE:View.GONE);
    }

    private void addTaskRow(JSONObject job) {
        final int initialProgress = progress(job);
        String title = job.optString("title", "");
        String id = job.optString("id", "");

        LinearLayout card = vertical();
        card.setPadding(dp(15), dp(13), dp(15), dp(13));
        card.setBackground(rounded(WHITE, 16, LINE));
        card.setElevation(dp(2));

        LinearLayout row = horizontal();
        card.addView(row, lp(-1, -2));

        TextView check = text(initialProgress == 100 ? "✓" : "", 20, WHITE, true);
        check.setGravity(Gravity.CENTER);
        check.setBackground(rounded(initialProgress == 100 ? TEAL : WHITE, 9,
                initialProgress == 100 ? TEAL : Color.rgb(181, 207, 197)));
        check.setContentDescription((initialProgress == 100 ? tr("pending_action")
                : tr("done_action")) + title);
        row.addView(check, lp(28, 28));

        TextView taskText = text(title, 15,
                initialProgress == 100 ? Color.rgb(141, 161, 156) : INK, false);
        taskText.setPadding(dp(12), 0, dp(6), 0);
        row.addView(taskText, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView delete = text("×", 26, Color.rgb(155, 177, 170), false);
        delete.setGravity(Gravity.CENTER);
        delete.setContentDescription(tr("delete_action") + title);
        delete.setOnClickListener(v -> deleteOccurrence(job));
        row.addView(delete, lp(33, 33));
        TextView edit=text("▣",21,TEAL,true);
        edit.setGravity(Gravity.CENTER);
        edit.setContentDescription(tr("edit_schedule"));
        edit.setOnClickListener(v->showScheduleEditor(job,false));
        row.addView(edit,lp(33,33));

        TextView rule=text(Schedule.description(job,tr("once"),tr("daily"),
                tr("weekly"),tr("monthly"),tr("yearly")),12,MUTED,false);
        rule.setPadding(dp(7),dp(3),0,0);
        LinearLayout metadata=horizontal();
        metadata.setPadding(dp(40),0,0,0);
        TextView priorityTag=text("● "+tr(PRIORITY_KEYS[priority(job)-1]),
                12,priorityColor(priority(job)),true);
        priorityTag.setContentDescription(tr("priority")+": "+
                priority(job)+" "+tr(PRIORITY_KEYS[priority(job)-1]));
        metadata.addView(priorityTag,lp(-2,-2));
        metadata.addView(rule,lp(-2,-2));
        card.addView(metadata,lp(-1,-2));

        LinearLayout progressLine = horizontal();
        LinearLayout.LayoutParams progressLineLp = lp(-1, -2);
        progressLineLp.topMargin = dp(8);
        card.addView(progressLine, progressLineLp);

        SeekBar slider = new SeekBar(this);
        slider.setMax(100);
        slider.setProgress(initialProgress);
        slider.setProgressTintList(ColorStateList.valueOf(TEAL));
        slider.setThumbTintList(ColorStateList.valueOf(TEAL));
        slider.setProgressBackgroundTintList(ColorStateList.valueOf(LINE));
        slider.setContentDescription(tr("task_progress_label") + ": " + title);
        progressLine.addView(slider, new LinearLayout.LayoutParams(0, dp(44), 1f));

        TextView amount = text("", 13, TEAL, true);
        amount.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        amount.setMinWidth(dp(112));
        amount.setText(String.format(selectedLocale(), tr("task_progress"), initialProgress));
        LinearLayout.LayoutParams amountLp = lp(-2, 44);
        amountLp.leftMargin = dp(5);
        progressLine.addView(amount, amountLp);

        // Muda o visual sem reconstruir o SeekBar durante o arrasto.
        final Runnable refreshTaskView = () -> {
            int value = progress(job);
            boolean completed = value == 100;
            amount.setText(String.format(selectedLocale(), tr("task_progress"), value));
            check.setText(completed ? "✓" : "");
            check.setBackground(rounded(completed ? TEAL : WHITE, 9,
                    completed ? TEAL : Color.rgb(181, 207, 197)));
            check.setContentDescription((completed ? tr("pending_action")
                    : tr("done_action")) + title);
            taskText.setTextColor(completed ? Color.rgb(141, 161, 156) : INK);
            int flags = taskText.getPaintFlags();
            taskText.setPaintFlags(completed
                    ? flags | Paint.STRIKE_THRU_TEXT_FLAG
                    : flags & ~Paint.STRIKE_THRU_TEXT_FLAG);
        };
        refreshTaskView.run();

        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                if (!fromUser) return;
                resetWhenNewDay();
                setProgress(job, value);
                refreshTaskView.run();
                save();
                updateSummary();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // Botão original de marcar concluída continua funcionando.
        check.setOnClickListener(v -> {
            resetWhenNewDay();
            setProgress(job, progress(job) == 100 ? 0 : 100);
            slider.setProgress(progress(job));
            refreshTaskView.run();
            save();
            updateSummary();
        });

        LinearLayout.LayoutParams cardLp = lp(-1, -2);
        cardLp.bottomMargin = dp(10);
        tasksBox.addView(card, cardLp);
    }
}
