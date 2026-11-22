package com.orbis.meudia;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.content.Intent;
import android.net.Uri;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
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
    private static final int TEAL = 0xFF146C63;
    private static final int INK = 0xFF19333A;
    private static final int MUTED = 0xFF657C83;
    private static final int BG = 0xFFF3F7F5;
    private static final int LINE = 0xFFE4EEEA;
    private static final int WHITE = Color.WHITE;
    private static final int EXPORT_REQUEST=7101;
    private static final int IMPORT_REQUEST=7102;
    private static final int MAX_BACKUP_BYTES=5*1024*1024;
    private static final int[] PRIORITY_COLORS = {
        0xFF2E7D32,   // 1: green
        0xFF1565C0,  // 2: blue
        0xFFF9A825,  // 3: yellow
        0xFFEF6C00,   // 4: orange
        0xFFC62828    // 5: red
    };
    private static final String[] PRIORITY_KEYS = {
        "green", "blue", "yellow", "orange", "red"
    };
    private static final int[] PRIORITY_LIGHT = {
        0xFFE8F5E9,
        0xFFE3F2FD,
        0xFFFFF8E1,
        0xFFFFF3E0,
        0xFFFFEBEE
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

    private boolean hasTag(JSONObject item,String name){
        if(item==null)return false;
        JSONArray tags=item.optJSONArray("tags");
        if(tags==null)return false;
        for(int i=0;i<tags.length();i++)if(name.equalsIgnoreCase(tags.optString(i)))return true;
        return false;
    }

    private boolean visible(JSONObject item){
        if(item==null || !Schedule.occurs(item,selectedDay)
                || (priorityFilter!=0 && priority(item)!=priorityFilter))return false;
        if(!projectFilter.isEmpty() && !projectFilter.equals(item.optString("project","")))return false;
        if(!sectionFilter.isEmpty() && !sectionFilter.equals(item.optString("section","")))return false;
        if(!tagFilter.isEmpty() && !hasTag(item,tagFilter))return false;
        if(taskSearch.isEmpty())return true;
        String needle=taskSearch.toLowerCase(Locale.ROOT);
        if(item.optString("title","").toLowerCase(Locale.ROOT).contains(needle)
                || item.optString("description","").toLowerCase(Locale.ROOT).contains(needle))return true;
        JSONArray subs=item.optJSONArray("subtasks");
        if(subs!=null)for(int i=0;i<subs.length();i++){
            JSONObject sub=subs.optJSONObject(i);
            if(sub!=null && sub.optString("title","").toLowerCase(Locale.ROOT).contains(needle))
                return true;
        }
        return false;
    }

    private SharedPreferences preferences;
    private JSONArray tasks = new JSONArray();
    private String day;
    private String selectedDay;
    private Calendar shownMonth;
    private LinearLayout calendarBody;
    private LinearLayout priorityFilterRow;
    private int priorityFilter=0;  // 0 = all, 1–5 = chosen level
    private String taskSearch="";
    private String projectFilter="";
    private String sectionFilter="";
    private String tagFilter="";
    private int sortMode=0; // creation, priority, name, progress
    private Spinner tagFilterSpinner;
    private Spinner sectionFilterSpinner;
    private Spinner projectFilterSpinner;
    private TextView dateLabel;
    private TextView percentLabel;
    private TextView progressLabel;
    private TextView countLabel;
    private TextView emptyLabel;
    private LinearLayout tasksBox;
    private FrameLayout progressTrack;
    private View progressFill;
    private EditText taskInput;

    /** Persist only view preferences, never task data or completion history. */
    private void restoreFilterState() {
        try {
            JSONObject state=new JSONObject(preferences.getString("saved-filter-state-v1","{}"));
            priorityFilter=Math.max(0,Math.min(5,state.optInt("priority",0)));
            projectFilter=state.optString("project","");
            sectionFilter=state.optString("section","");
            tagFilter=state.optString("tag","");
            taskSearch=state.optString("search","");
            if(taskSearch.length()>200)taskSearch=taskSearch.substring(0,200);
            sortMode=Math.max(0,Math.min(3,state.optInt("sort",0)));
        } catch (org.json.JSONException ignored) {
            // Invalid preferences must not prevent opening the task list.
        }
    }

    private void persistFilterState() {
        JSONObject state=new JSONObject();
        try {
            state.put("priority",priorityFilter);
            state.put("project",projectFilter);
            state.put("section",sectionFilter);
            state.put("tag",tagFilter);
            state.put("search",taskSearch);
            state.put("sort",sortMode);
            preferences.edit().putString("saved-filter-state-v1",state.toString()).apply();
        } catch (org.json.JSONException ignored) {
            // Display filtering remains usable even if preferences cannot serialize.
        }
    }


    /** Named local filter presets are separate from task backups and never modify task data. */
    private JSONObject currentFilterSnapshot() throws JSONException {
        JSONObject state=new JSONObject();
        state.put("priority",priorityFilter);
        state.put("project",projectFilter);
        state.put("section",sectionFilter);
        state.put("tag",tagFilter);
        state.put("search",taskSearch);
        state.put("sort",sortMode);
        return state;
    }

    private JSONObject readFilterPresets() {
        try {
            return new JSONObject(preferences.getString("named-filter-presets-v1","{}"));
        } catch (JSONException ignored) {
            return new JSONObject();
        }
    }

    private void saveNamedFilterPreset() {
        EditText name=new EditText(this);
        name.setSingleLine(true);
        name.setHint(tr("preset_name"));
        name.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(40)});
        new AlertDialog.Builder(this).setTitle(tr("preset_save"))
            .setView(name).setNegativeButton(android.R.string.cancel,null)
            .setPositiveButton(android.R.string.ok,(dialog,which)->{
                String key=name.getText().toString().trim();
                if(key.isEmpty())return;
                try {
                    JSONObject presets=readFilterPresets();
                    if(!presets.has(key) && presets.length()>=20){
                        new AlertDialog.Builder(this).setMessage(tr("preset_limit"))
                            .setPositiveButton(android.R.string.ok,null).show();
                        return;
                    }
                    presets.put(key,currentFilterSnapshot());
                    preferences.edit().putString("named-filter-presets-v1",presets.toString()).apply();
                } catch(JSONException ignored){}
            }).show();
    }

    /** Long-press the preset load control to remove a saved filter after confirmation. */
    private void deleteNamedFilterPreset() {
        JSONObject presets=readFilterPresets();
        java.util.ArrayList<String> names=new java.util.ArrayList<>();
        java.util.Iterator<String> keys=presets.keys();
        while(keys.hasNext())names.add(keys.next());
        java.util.Collections.sort(names,String.CASE_INSENSITIVE_ORDER);
        if(names.isEmpty()){
            new AlertDialog.Builder(this).setMessage(tr("preset_empty"))
                .setPositiveButton(android.R.string.ok,null).show();
            return;
        }
        String[] labels=names.toArray(new String[0]);
        new AlertDialog.Builder(this).setTitle(tr("preset_load"))
            .setItems(labels,(dialog,which)->{
                String selected=labels[which];
                new AlertDialog.Builder(this).setTitle(selected)
                    .setMessage(selected + "?")
                    .setNegativeButton(android.R.string.cancel,null)
                    .setPositiveButton(android.R.string.ok,(confirm,button)->{
                        JSONObject latest=readFilterPresets();
                        latest.remove(selected);
                        preferences.edit().putString("named-filter-presets-v1",latest.toString()).apply();
                    }).show();
            }).setNegativeButton(android.R.string.cancel,null).show();
    }

    private void loadNamedFilterPreset() {
        JSONObject presets=readFilterPresets();
        java.util.ArrayList<String> names=new java.util.ArrayList<>();
        java.util.Iterator<String> keys=presets.keys();
        while(keys.hasNext())names.add(keys.next());
        java.util.Collections.sort(names,String.CASE_INSENSITIVE_ORDER);
        if(names.isEmpty()){
            new AlertDialog.Builder(this).setMessage(tr("preset_empty"))
                .setPositiveButton(android.R.string.ok,null).show();
            return;
        }
        String[] labels=names.toArray(new String[0]);
        new AlertDialog.Builder(this).setTitle(tr("preset_load"))
            .setItems(labels,(dialog,which)->{
                JSONObject saved=presets.optJSONObject(labels[which]);
                if(saved==null)return;
                priorityFilter=Math.max(0,Math.min(5,saved.optInt("priority",0)));
                projectFilter=saved.optString("project","");
                sectionFilter=saved.optString("section","");
                tagFilter=saved.optString("tag","");
                taskSearch=saved.optString("search","");
                if(taskSearch.length()>200)taskSearch=taskSearch.substring(0,200);
                sortMode=Math.max(0,Math.min(3,saved.optInt("sort",0)));
                persistFilterState();
                makeInterface();
                render();
            }).setNegativeButton(android.R.string.cancel,null).show();
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        preferences = getSharedPreferences("meu-dia-dados-v1", MODE_PRIVATE);
        selectedLanguage = preferences.getString("app-language", null);
        restoreFilterState();
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

    /** Local Storage Access Framework backup; no network or storage permission. */
    private void exportBackup(){
        int omitted=TaskAttachments.omittedAttachmentCount(tasks);
        if(omitted>0){
            String message=String.format(selectedLocale(),tr("backup_media_warning"),omitted);
            new AlertDialog.Builder(this)
                .setTitle(tr("backup_media_title"))
                .setMessage(message)
                .setNegativeButton(android.R.string.cancel,null)
                .setPositiveButton(tr("export_backup"),(dialog,which)->startBackupDocumentPicker())
                .show();
            return;
        }
        startBackupDocumentPicker();
    }

    /** Export only after the user acknowledges local media are not included. */
    private void startBackupDocumentPicker(){
        Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE,"checklist-orbis-"+today()+".json");
        startActivityForResult(intent,EXPORT_REQUEST);
    }

    private void importBackup(){
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent,IMPORT_REQUEST);
    }

    private String backupJson() throws JSONException {
        JSONObject root=new JSONObject();
        root.put("format","checklist-orbis-backup");
        root.put("version",1);
        root.put("exportedAt",System.currentTimeMillis());
        root.put("tasks",TaskAttachments.portableTasks(tasks));
        return root.toString(2);
    }

    /** Strict ISO calendar date check, independent of the device's timezone. */
    private static boolean validBackupDate(String value) {
        if(value==null || !value.matches("\\d{4}-\\d{2}-\\d{2}"))return false;
        try {
            int year=Integer.parseInt(value.substring(0,4));
            int month=Integer.parseInt(value.substring(5,7));
            int dayOfMonth=Integer.parseInt(value.substring(8,10));
            if(year<1)return false;
            GregorianCalendar date=new GregorianCalendar(java.util.TimeZone.getTimeZone("UTC"));
            // Use the proleptic Gregorian calendar for every ISO year, including pre-1582 dates.
            date.setGregorianChange(new java.util.Date(Long.MIN_VALUE));
            date.setLenient(false);
            date.clear();
            date.set(year,month-1,dayOfMonth);
            date.getTimeInMillis();
            return true;
        }catch(IllegalArgumentException e){return false;}
    }

    private static JSONArray validateBackup(String raw) throws JSONException {
        JSONObject root=new JSONObject(raw);
        if(!"checklist-orbis-backup".equals(root.optString("format"))
                ||!(root.opt("version") instanceof Integer)
                ||((Integer)root.opt("version"))!=1)throw new JSONException("Invalid format/version");
        JSONArray incoming=root.getJSONArray("tasks");
        if(incoming.length()>10000)throw new JSONException("Too many tasks");
        for(int i=0;i<incoming.length();i++){
            JSONObject task=incoming.optJSONObject(i);
            if(task==null || !task.has("title") || !task.has("mode")
                    || !task.has("start"))throw new JSONException("Invalid task");
            if(!(task.opt("title") instanceof String)
                    ||((String)task.opt("title")).length()>1000)
                throw new JSONException("Invalid title");
            if(!validBackupDate(task.optString("start")))
                throw new JSONException("Invalid date");
            String mode=task.optString("mode");
            if(!Arrays.asList("once","daily","weekly","monthly","yearly").contains(mode))
                throw new JSONException("Invalid recurrence");
            if(task.has("history")){
                JSONObject history=task.optJSONObject("history");
                if(history==null || history.length()>36600)
                    throw new JSONException("Invalid history");
                java.util.Iterator<String> keys=history.keys();
                while(keys.hasNext()){
                    String date=keys.next();
                    Object value=history.opt(date);
                    if(!validBackupDate(date) || !(value instanceof Number)
                            || ((Number)value).doubleValue()<0
                            || ((Number)value).doubleValue()>100
                            || ((Number)value).doubleValue()%1!=0)
                        throw new JSONException("Invalid history entry");
                }
            }
        }
        return incoming;
    }

    @Override
    protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(result!=RESULT_OK || data==null || data.getData()==null)return;
        Uri uri=data.getData();
        if(request==EXPORT_REQUEST){
            try(OutputStream output=getContentResolver().openOutputStream(uri)){
                if(output==null)throw new java.io.IOException("No output stream");
                output.write(backupJson().getBytes(StandardCharsets.UTF_8));
                output.flush();
                android.widget.Toast.makeText(this,tr("backup_saved"),
                        android.widget.Toast.LENGTH_LONG).show();
            }catch(Exception e){showBackupError();}
        }else if(request==IMPORT_REQUEST){
            try(InputStream input=getContentResolver().openInputStream(uri)){
                if(input==null)throw new java.io.IOException("No input stream");
                ByteArrayOutputStream bytes=new ByteArrayOutputStream();
                byte[] chunk=new byte[8192];int n;
                while((n=input.read(chunk))!=-1){
                    if(bytes.size()+n>MAX_BACKUP_BYTES)
                        throw new java.io.IOException("Backup too large");
                    bytes.write(chunk,0,n);
                }
                // Backup v1 never carries media bytes or portable URI grants. Strip any
                // injected device-local attachment references before persisting imports.
                JSONArray restored=TaskAttachments.portableTasks(
                        validateBackup(bytes.toString("UTF-8")));
                new AlertDialog.Builder(this)
                    .setTitle(tr("restore_backup"))
                    .setMessage(tr("restore_confirm"))
                    .setNegativeButton(tr("cancel"),(dialog,which)->{})
                    .setPositiveButton(tr("restore_backup"),(dialog,which)->{
                        // Commit the replacement before changing memory or local alarms.
                        // A failed disk write must not report a successful restoration.
                        String previousJson=preferences.getString("tasks",null);
                        boolean persisted=BackupRestoreCommit.restore(
                                (restoreDay,restoreJson)->preferences.edit()
                                        .putString("day",restoreDay)
                                        .putString("tasks",restoreJson)
                                        .commit(),
                                today(),restored.toString(),
                                ()->{
                                    tasks=restored;
                                    // Rebuild reminders only after the restored tasks are durable.
                                    syncOneTimeReminders(previousJson,true);
                                    loadTasks();
                                    render();
                                });
                        if(!persisted){
                            showBackupError();
                            return;
                        }
                        android.widget.Toast.makeText(this,tr("backup_restored"),
                                android.widget.Toast.LENGTH_LONG).show();
                    }).show();
            }catch(Exception e){showBackupError();}
        }
    }

    private void showBackupError(){
        new AlertDialog.Builder(this).setMessage(tr("backup_error"))
                .setPositiveButton("OK",(dialog,which)->{}).show();
    }

    /** Reconcile offline alarms after task edits, deletions and backup restoration. */
    private void syncOneTimeReminders(String previousJson, boolean forceReschedule) {
        JSONArray previous;
        try { previous = new JSONArray(previousJson == null ? "[]" : previousJson); }
        catch (JSONException ignored) { previous = new JSONArray(); }

        java.util.Map<String, JSONObject> oldTasks = reminderTasks(previous);
        java.util.Map<String, JSONObject> newTasks = reminderTasks(tasks);
        boolean migrate = !preferences.getBoolean("reminder_uri_identity_v1", false);
        if (migrate) {
            for (String id : oldTasks.keySet()) LocalReminderScheduler.cancelLegacy(this, id);
            for (String id : newTasks.keySet()) LocalReminderScheduler.cancelLegacy(this, id);
        }
        for (java.util.Map.Entry<String, JSONObject> entry : oldTasks.entrySet()) {
            JSONObject current = newTasks.get(entry.getKey());
            if (forceReschedule || migrate || current == null
                    || !sameReminder(entry.getValue(), current)) {
                LocalReminderScheduler.cancel(this, entry.getKey());
            }
        }
        long now = System.currentTimeMillis();
        for (java.util.Map.Entry<String, JSONObject> entry : newTasks.entrySet()) {
            JSONObject old = oldTasks.get(entry.getKey());
            if (!forceReschedule && !migrate && old != null
                    && sameReminder(old, entry.getValue())) continue;
            JSONObject item = entry.getValue();
            try {
                if ("once".equals(item.optString("mode"))) {
                    LocalReminderScheduler.schedule(this, item, item.optString("start"),
                            entry.getKey(), now);
                } else {
                    LocalReminderScheduler.scheduleNext(this, item, entry.getKey(), now);
                }
            } catch (RuntimeException ignored) {
                // A restricted alarm must not block saving tasks or their histories.
            }
        }
        if (migrate) preferences.edit().putBoolean("reminder_uri_identity_v1", true).apply();
    }

    private static java.util.Map<String, JSONObject> reminderTasks(JSONArray source) {
        java.util.Map<String, JSONObject> result = new java.util.HashMap<>();
        for (int i = 0; i < source.length(); i++) {
            JSONObject item = source.optJSONObject(i);
            if (item == null || item.optString("id").isEmpty()
                    || TaskTime.getTime(item).isEmpty()) continue;
            result.put(item.optString("id"), item);
        }
        return result;
    }

    /** Ignore progress-only changes, but react to all recurrence-rule edits. */
    private static boolean sameReminder(JSONObject a, JSONObject b) {
        for (String field : new String[]{"start", "title", "time", "mode", "until",
                "weekdays", "count"}) {
            if (!String.valueOf(a.opt(field)).equals(String.valueOf(b.opt(field)))) return false;
        }
        // Excluded occurrence dates form a set: their JSON insertion order does
        // not change the reminder schedule. Avoid resetting an existing alarm.
        return excludedDates(a.optJSONArray("skips"))
                .equals(excludedDates(b.optJSONArray("skips")));
    }

    private static java.util.Set<String> excludedDates(JSONArray dates) {
        java.util.Set<String> result = new java.util.HashSet<>();
        if (dates != null) {
            for (int i = 0; i < dates.length(); i++) {
                String date = dates.optString(i, "");
                if (!date.isEmpty()) result.add(date);
            }
        }
        return result;
    }

    private void save() { save(false); }

    private void save(boolean forceReschedule) {
        String previous = preferences.getString("tasks", null);
        preferences.edit().putString("day",today())
                .putString("tasks",tasks.toString()).apply();
        syncOneTimeReminders(previous, forceReschedule);
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

        TextView guideButton = text("❔", 24, TEAL, true);
        guideButton.setGravity(Gravity.CENTER);
        guideButton.setContentDescription("Como usar / User guide");
        guideButton.setOnClickListener(v -> AppGuide.show(this, selectedLanguage));
        LinearLayout.LayoutParams guideLp = lp(44, 48);
        guideLp.leftMargin = dp(5);
        header.addView(guideButton, guideLp);

        LinearLayout.LayoutParams headerLp = lp(-1, -2);
        headerLp.bottomMargin = dp(24);
        content.addView(header, headerLp);

        LinearLayout backupActions=horizontal();
        TextView backupButton=action("↓ "+tr("export_backup"));
        backupButton.setOnClickListener(v->exportBackup());
        backupActions.addView(backupButton,new LinearLayout.LayoutParams(0,dp(43),1f));
        TextView restoreButton=action("↑ "+tr("restore_backup"));
        restoreButton.setOnClickListener(v->importBackup());
        LinearLayout.LayoutParams restoreLp=new LinearLayout.LayoutParams(0,dp(43),1f);
        restoreLp.leftMargin=dp(8);
        backupActions.addView(restoreButton,restoreLp);
        LinearLayout.LayoutParams backupsLp=lp(-1,-2);
        backupsLp.bottomMargin=dp(14);
        content.addView(backupActions,backupsLp);

        TextView suggestionsButton=action(tr("daily_suggestions"));
        suggestionsButton.setOnClickListener(v -> {
            java.util.List<JSONObject> pending=DailySuggestions.pending(tasks,selectedDay);
            StringBuilder body=new StringBuilder();
            for(JSONObject item:pending) {
                if(body.length()>0)body.append("\\n");
                body.append("• ").append(item.optString("title",""));
            }
            new AlertDialog.Builder(this).setTitle(tr("daily_suggestions"))
                    .setMessage(body.length()==0?tr("empty"):body.toString())
                    .setPositiveButton(android.R.string.ok,null).show();
        });
        LinearLayout.LayoutParams suggestionsLp=lp(-1,43);
        suggestionsLp.bottomMargin=dp(14);
        content.addView(suggestionsButton,suggestionsLp);

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

        EditText searchBox=new EditText(this);
        searchBox.setSingleLine(true);
        searchBox.setTextSize(15);
        searchBox.setTextColor(INK);
        searchBox.setHintTextColor(MUTED);
        searchBox.setHint(tr("search_tasks"));
        searchBox.setContentDescription(tr("search_tasks"));
        searchBox.setBackground(rounded(WHITE,14,LINE));
        searchBox.setPadding(dp(14),0,dp(14),0);
        searchBox.setInputType(InputType.TYPE_CLASS_TEXT);
        searchBox.setText(taskSearch);
        searchBox.addTextChangedListener(new android.text.TextWatcher(){
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count){
                taskSearch=s.toString().trim();
                renderTaskResults();
            }
            @Override public void afterTextChanged(android.text.Editable e){}
        });
        LinearLayout.LayoutParams searchLp=lp(-1,49);
        searchLp.bottomMargin=dp(12);
        content.addView(searchBox,searchLp);

        TextView projectFilterLabel=text(tr("filter_project"),13,MUTED,true);
        content.addView(projectFilterLabel,lp(-1,-2));
        projectFilterSpinner=new Spinner(this);
        content.addView(projectFilterSpinner,lp(-1,48));
        refreshProjectFilter();
        TextView sectionFilterLabel=text(tr("filter_section"),13,MUTED,true);
        content.addView(sectionFilterLabel,lp(-1,-2));
        sectionFilterSpinner=new Spinner(this);
        content.addView(sectionFilterSpinner,lp(-1,48));
        refreshSectionFilter();
        content.addView(text(tr("filter_tags"),13,MUTED,true),lp(-1,-2));
        tagFilterSpinner=new Spinner(this);
        content.addView(tagFilterSpinner,lp(-1,48));
        refreshTagFilter();

        content.addView(text(tr("sort_tasks"),13,MUTED,true),lp(-1,-2));
        Spinner sortSpinner=new Spinner(this);
        String[] sortLabels={tr("sort_created"),tr("sort_priority"),tr("sort_name"),tr("sort_pending")};
        sortSpinner.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,sortLabels));
        sortSpinner.setSelection(sortMode);
        sortSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(android.widget.AdapterView<?> parent,View view,int pos,long id){
                if(sortMode!=pos){sortMode=pos;renderTaskResults();}
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent){}
        });
        content.addView(sortSpinner,lp(-1,48));

        LinearLayout presetActions=horizontal();
        TextView savePreset=action(tr("preset_save"));
        savePreset.setOnClickListener(v->saveNamedFilterPreset());
        presetActions.addView(savePreset,new LinearLayout.LayoutParams(0,dp(43),1f));
        TextView loadPreset=action(tr("preset_load"));
        loadPreset.setOnClickListener(v->loadNamedFilterPreset());
        loadPreset.setOnLongClickListener(v->{deleteNamedFilterPreset();return true;});
        LinearLayout.LayoutParams presetLp=new LinearLayout.LayoutParams(0,dp(43),1f);
        presetLp.leftMargin=dp(8);
        presetActions.addView(loadPreset,presetLp);
        content.addView(presetActions,lp(-1,-2));


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
                    && (priorityFilter==0 || priority(task)==priorityFilter)
                    && (projectFilter.isEmpty() || projectFilter.equals(task.optString("project","")))
                    && (sectionFilter.isEmpty() || sectionFilter.equals(task.optString("section","")))
                    && (tagFilter.isEmpty() || hasTag(task,tagFilter))){
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

        TextView projectLabel=text(tr("project"),14,INK,true);
        LinearLayout.LayoutParams projectLabelLp=lp(-1,-2);
        projectLabelLp.topMargin=dp(10);
        form.addView(projectLabel,projectLabelLp);
        EditText projectEditor=new EditText(this);
        projectEditor.setSingleLine(true);
        projectEditor.setHint(tr("project_hint"));
        projectEditor.setText(item.optString("project",""));
        projectEditor.setFilters(new android.text.InputFilter[]{
                new android.text.InputFilter.LengthFilter(60)});
        form.addView(projectEditor,lp(-1,-2));

        TextView sectionLabel=text(tr("section"),14,INK,true);
        LinearLayout.LayoutParams sectionLabelLp=lp(-1,-2);
        sectionLabelLp.topMargin=dp(10);
        form.addView(sectionLabel,sectionLabelLp);
        EditText sectionEditor=new EditText(this);
        sectionEditor.setSingleLine(true);
        sectionEditor.setHint(tr("section_hint"));
        sectionEditor.setText(item.optString("section",""));
        sectionEditor.setFilters(new android.text.InputFilter[]{
                new android.text.InputFilter.LengthFilter(60)});
        form.addView(sectionEditor,lp(-1,-2));
        form.addView(text(tr("tags"),14,INK,true),lp(-1,-2));
        EditText tagsEditor=new EditText(this);
        tagsEditor.setSingleLine(true);
        tagsEditor.setHint(tr("tags_hint"));
        tagsEditor.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(500)});
        JSONArray oldTags=item.optJSONArray("tags");
        if(oldTags!=null){
            StringBuilder oldNames=new StringBuilder();
            for(int i=0;i<oldTags.length();i++){
                if(i>0)oldNames.append(", ");
                oldNames.append(oldTags.optString(i));
            }
            tagsEditor.setText(oldNames.toString());
        }
        form.addView(tagsEditor,lp(-1,-2));

        // Optional free-form details are stored with the task series.
        TextView detailsLabel=text(tr("task_description"),14,INK,true);
        LinearLayout.LayoutParams detailsLabelLp=lp(-1,-2);
        detailsLabelLp.topMargin=dp(10);
        form.addView(detailsLabel,detailsLabelLp);
        EditText details=new EditText(this);
        details.setSingleLine(false);
        details.setMinLines(2);
        details.setMaxLines(6);
        details.setGravity(Gravity.TOP);
        details.setHint(tr("description_hint"));
        details.setText(item.optString("description",""));
        details.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE
                |InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        details.setFilters(new android.text.InputFilter[]{
                new android.text.InputFilter.LengthFilter(2000)});
        form.addView(details,lp(-1,-2));

        TextView subtasksLabel=text(tr("subtasks"),14,INK,true);
        LinearLayout.LayoutParams subLabelLp=lp(-1,-2);
        subLabelLp.topMargin=dp(10);
        form.addView(subtasksLabel,subLabelLp);
        EditText subtasksEditor=new EditText(this);
        subtasksEditor.setSingleLine(false);
        subtasksEditor.setMinLines(2);
        subtasksEditor.setMaxLines(7);
        subtasksEditor.setGravity(Gravity.TOP);
        subtasksEditor.setHint(tr("subtasks_hint"));
        subtasksEditor.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        subtasksEditor.setFilters(new android.text.InputFilter[]{
                new android.text.InputFilter.LengthFilter(4000)});
        JSONArray existingSubtasks=item.optJSONArray("subtasks");
        StringBuilder subtaskLines=new StringBuilder();
        if(existingSubtasks!=null)for(int i=0;i<existingSubtasks.length();i++){
            JSONObject sub=existingSubtasks.optJSONObject(i);
            if(sub==null)continue;
            if(subtaskLines.length()>0)subtaskLines.append("\n");
            subtaskLines.append(sub.optString("title",""));
        }
        subtasksEditor.setText(subtaskLines.toString());
        form.addView(subtasksEditor,lp(-1,-2));

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

        // Optional deadline belongs to the task, not to any occurrence or progress history.
        final String[] deadline={TaskDeadline.get(item)};
        TextView deadlineButton=action("");
        Runnable refreshDeadline=()->deadlineButton.setText(tr("deadline")+": "+
                (deadline[0].isEmpty()?tr("no_deadline"):formatDay(deadline[0])));
        refreshDeadline.run();
        LinearLayout.LayoutParams deadlineLp=lp(-1,52);
        deadlineLp.topMargin=dp(8);
        form.addView(deadlineButton,deadlineLp);
        deadlineButton.setOnClickListener(v->pickDate(
                deadline[0].isEmpty()?start[0]:deadline[0],day->{
                    deadline[0]=day;
                    refreshDeadline.run();
                }));
        TextView clearDeadline=action(tr("clear_deadline"));
        form.addView(clearDeadline,lp(-1,44));
        clearDeadline.setOnClickListener(v->{deadline[0]="";refreshDeadline.run();});

        // Optional wall-clock time is independent of date, deadline and recurrence.
        final String[] localTime={TaskTime.getTime(item)};
        TextView timeButton=action("");
        Runnable refreshTime=()->timeButton.setText(tr("task_time")+": "+
                (localTime[0].isEmpty()?tr("no_task_time"):localTime[0]));
        refreshTime.run();
        form.addView(timeButton,lp(-1,52));
        timeButton.setOnClickListener(v->{
            Calendar now=Calendar.getInstance();
            int hour=now.get(Calendar.HOUR_OF_DAY),minute=now.get(Calendar.MINUTE);
            if(!localTime[0].isEmpty()){
                hour=Integer.parseInt(localTime[0].substring(0,2));
                minute=Integer.parseInt(localTime[0].substring(3,5));
            }
            new android.app.TimePickerDialog(this,(picker,h,m)->{
                localTime[0]=String.format(Locale.ROOT,"%02d:%02d",h,m);
                refreshTime.run();
            },hour,minute,true).show();
        });
        TextView clearTime=action(tr("clear_task_time"));
        form.addView(clearTime,lp(-1,44));
        clearTime.setOnClickListener(v->{localTime[0]="";refreshTime.run();});

        // Explicit user action only: never request notification access on app launch.
        // Scheduling is a separate increment; this control grants permission, not an alarm.
        if (Build.VERSION.SDK_INT >= 33) {
            TextView notificationPermission=action(tr("task_time")+" · 🔔");
            form.addView(notificationPermission,lp(-1,44));
            notificationPermission.setOnClickListener(v->{
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS},7103);
                }
            });
        }

        // Estimated duration is optional and stored separately from progress and recurrence.
        final int[] durationMinutes={TaskTime.getDurationMinutes(item)};
        TextView durationButton=action("");
        Runnable refreshDuration=()->durationButton.setText(tr("task_duration")+": "+
                (durationMinutes[0]==0?tr("no_task_duration"):durationMinutes[0]+" "+tr("duration_minutes")));
        refreshDuration.run();
        form.addView(durationButton,lp(-1,52));
        durationButton.setOnClickListener(v->{
            android.widget.NumberPicker picker=new android.widget.NumberPicker(this);
            picker.setMinValue(1);
            picker.setMaxValue(1440);
            picker.setValue(durationMinutes[0]==0?30:durationMinutes[0]);
            picker.setWrapSelectorWheel(false);
            new AlertDialog.Builder(this).setTitle(tr("task_duration"))
                    .setView(picker)
                    .setPositiveButton(tr("save"),(dialog,which)->{
                        durationMinutes[0]=picker.getValue();
                        refreshDuration.run();
                    })
                    .setNegativeButton(android.R.string.cancel,null).show();
        });
        TextView clearDuration=action(tr("clear_task_duration"));
        form.addView(clearDuration,lp(-1,44));
        clearDuration.setOnClickListener(v->{durationMinutes[0]=0;refreshDuration.run();});

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
                    String newDescription=details.getText().toString().trim();
                    if(newDescription.isEmpty())item.remove("description");
                    else item.put("description",newDescription);
                    JSONArray oldSubs=item.optJSONArray("subtasks");
                    JSONArray updatedSubs=new JSONArray();
                    String[] lines=subtasksEditor.getText().toString().split("\n");
                    boolean[] used=new boolean[oldSubs==null?0:oldSubs.length()];
                    for(String raw:lines){
                        String subTitle=raw.trim();
                        if(subTitle.isEmpty())continue;
                        if(subTitle.length()>90)subTitle=subTitle.substring(0,90);
                        if(updatedSubs.length()>=50)break;
                        JSONObject sub=null;
                        if(oldSubs!=null)for(int k=0;k<oldSubs.length();k++){
                            JSONObject candidate=oldSubs.optJSONObject(k);
                            if(!used[k] && candidate!=null
                                    && subTitle.equals(candidate.optString("title",""))){
                                sub=candidate;used[k]=true;break;
                            }
                        }
                        if(sub==null){
                            sub=new JSONObject();
                            sub.put("id",UUID.randomUUID().toString());
                            sub.put("title",subTitle);
                            sub.put("history",new JSONObject());
                        }
                        updatedSubs.put(sub);
                    }
                    item.put("subtasks",updatedSubs);
                    String project=projectEditor.getText().toString().trim();
                    if(project.isEmpty())item.remove("project");
                    else item.put("project",project);
                    String section=sectionEditor.getText().toString().trim();
                    if(project.isEmpty() || section.isEmpty())item.remove("section");
                    else item.put("section",section);
                    JSONArray newTags=new JSONArray();
                    java.util.LinkedHashSet<String> uniqueTags=new java.util.LinkedHashSet<>();
                    for(String rawTag:tagsEditor.getText().toString().split(",")){
                        String tag=rawTag.trim().replaceAll("\\s+"," ");
                        if(tag.length()>40)tag=tag.substring(0,40).trim();
                        if(tag.isEmpty())continue;
                        boolean duplicate=false;
                        for(String existing:uniqueTags)if(existing.equalsIgnoreCase(tag))duplicate=true;
                        if(!duplicate)uniqueTags.add(tag);
                        if(uniqueTags.size()>=12)break;
                    }
                    for(String tag:uniqueTags)newTags.put(tag);
                    if(newTags.length()==0)item.remove("tags");
                    else item.put("tags",newTags);
                    if(deadline[0].isEmpty())TaskDeadline.clear(item);
                    else TaskDeadline.set(item,deadline[0]);
                    if(localTime[0].isEmpty())TaskTime.clearTime(item);
                    else TaskTime.setTime(item,localTime[0]);
                    if(durationMinutes[0]==0)TaskTime.clearDuration(item);
                    else TaskTime.setDurationMinutes(item,durationMinutes[0]);
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

    /** Duplicate the plan, not its completion history. All IDs must be independent. */
    private void duplicateTask(JSONObject source){
        try{
            JSONObject copy=TaskDuplicator.duplicate(source,selectedDay);
            tasks.put(copy);
            save();
            render();
        }catch(JSONException error){
            new AlertDialog.Builder(this).setMessage(tr("backup_error"))
                    .setPositiveButton(android.R.string.ok,null).show();
        }
    }

    /** Replan a one-time task without rewriting completion history for past dates. */
    private void rescheduleTask(JSONObject item){
        if(!"once".equals(item.optString("mode","once"))){
            showScheduleEditor(item,false);
            return;
        }
        pickDate(item.optString("start",selectedDay),newDay->{
            try{
                if(!TaskRescheduler.moveOnce(item,newDay))return;
                save();
                selectedDay=newDay;
                shownMonth=Schedule.date(newDay);
                render();
            }catch(JSONException ignored){}
        });
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
        refreshProjectFilter();
        refreshSectionFilter();
        refreshTagFilter();
        renderCalendar();
        renderTaskResults();
    }

    private void refreshTagFilter(){
        if(tagFilterSpinner==null)return;
        java.util.TreeSet<String> names=new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for(int i=0;i<tasks.length();i++){
            JSONObject item=tasks.optJSONObject(i);
            if(item==null)continue;
            JSONArray tags=item.optJSONArray("tags");
            if(tags!=null)for(int k=0;k<tags.length();k++){
                String tag=tags.optString(k).trim();
                if(!tag.isEmpty())names.add(tag);
            }
        }
        java.util.ArrayList<String> values=new java.util.ArrayList<>();
        values.add("");values.addAll(names);
        if(!tagFilter.isEmpty()&&!names.contains(tagFilter))tagFilter="";
        String[] labels=new String[values.size()];labels[0]=tr("all_tags");
        for(int i=1;i<labels.length;i++)labels[i]="#"+values.get(i);
        int selected=values.indexOf(tagFilter);
        tagFilterSpinner.setOnItemSelectedListener(null);
        tagFilterSpinner.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels));
        tagFilterSpinner.setSelection(Math.max(0,selected));
        tagFilterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(android.widget.AdapterView<?> parent,View view,int pos,long id){
                String next=values.get(pos);
                if(!next.equals(tagFilter)){tagFilter=next;render();}
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent){}
        });
    }

    /** Preserve selected project across edits; no separate database or network needed. */
    private void refreshProjectFilter(){
        if(projectFilterSpinner==null)return;
        java.util.TreeSet<String> names=new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for(int i=0;i<tasks.length();i++){
            JSONObject item=tasks.optJSONObject(i);
            if(item!=null){
                String name=item.optString("project","").trim();
                if(!name.isEmpty())names.add(name);
            }
        }
        java.util.ArrayList<String> values=new java.util.ArrayList<>();
        values.add("");
        values.addAll(names);
        if(!projectFilter.isEmpty() && !names.contains(projectFilter))projectFilter="";
        String[] labels=new String[values.size()];
        labels[0]=tr("all_projects");
        for(int i=1;i<labels.length;i++)labels[i]=values.get(i);
        int selected=values.indexOf(projectFilter);
        if(selected<0)selected=0;
        projectFilterSpinner.setOnItemSelectedListener(null);
        projectFilterSpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,labels));
        projectFilterSpinner.setSelection(selected);
        projectFilterSpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener(){
                    @Override public void onItemSelected(
                            android.widget.AdapterView<?> parent,View view,int pos,long id){
                        String next=values.get(pos);
                        if(!next.equals(projectFilter)){
                            projectFilter=next;
                            sectionFilter="";
                            refreshSectionFilter();
                            renderCalendar();
                            renderTaskResults();
                        }
                    }
                    @Override public void onNothingSelected(
                            android.widget.AdapterView<?> parent){}
                });
    }

    /** Sections are scoped to the selected project, never globally shared. */
    private void refreshSectionFilter(){
        if(sectionFilterSpinner==null)return;
        java.util.TreeSet<String> names=new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for(int i=0;i<tasks.length();i++){
            JSONObject item=tasks.optJSONObject(i);
            if(item==null)continue;
            String project=item.optString("project","").trim();
            String section=item.optString("section","").trim();
            if(!section.isEmpty() && (projectFilter.isEmpty() || projectFilter.equals(project)))
                names.add(section);
        }
        java.util.ArrayList<String> values=new java.util.ArrayList<>();
        values.add("");
        values.addAll(names);
        if(!sectionFilter.isEmpty() && !names.contains(sectionFilter))sectionFilter="";
        String[] labels=new String[values.size()];
        labels[0]=tr("all_sections");
        for(int i=1;i<labels.length;i++)labels[i]=values.get(i);
        int selected=values.indexOf(sectionFilter);
        if(selected<0)selected=0;
        sectionFilterSpinner.setOnItemSelectedListener(null);
        sectionFilterSpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,labels));
        sectionFilterSpinner.setSelection(selected);
        sectionFilterSpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener(){
                    @Override public void onItemSelected(
                            android.widget.AdapterView<?> parent,View view,int pos,long id){
                        String next=values.get(pos);
                        if(!next.equals(sectionFilter)){
                            sectionFilter=next;
                            renderCalendar();
                            renderTaskResults();
                        }
                    }
                    @Override public void onNothingSelected(
                            android.widget.AdapterView<?> parent){}
                });
    }

    /** Rebuild results only; do not reconstruct the search field while typing. */
    private void renderTaskResults(){
        if(tasksBox==null || emptyLabel==null)return;
        persistFilterState();
        updateSummary();
        tasksBox.removeAllViews();
        java.util.ArrayList<JSONObject> shown=new java.util.ArrayList<>();
        for(int i=0;i<tasks.length();i++){
            JSONObject item=tasks.optJSONObject(i);
            if(visible(item))shown.add(item);
        }
        if(sortMode==1)java.util.Collections.sort(shown,(a,b)->Integer.compare(priority(b),priority(a)));
        else if(sortMode==2)java.util.Collections.sort(shown,(a,b)->a.optString("title","").compareToIgnoreCase(b.optString("title","")));
        else if(sortMode==3)java.util.Collections.sort(shown,(a,b)->Integer.compare(progress(a),progress(b)));
        JSONArray shownForConflicts = new JSONArray();
        for (JSONObject item : shown) shownForConflicts.put(item);
        java.util.Set<Integer> conflictIndices = new java.util.HashSet<>(
                TaskTime.conflictingTaskIndices(shownForConflicts));
        for (int i = 0; i < shown.size(); i++)
            addTaskRow(shown.get(i), conflictIndices.contains(i));
        int count=shown.size();
        emptyLabel.setText(count==0 && !taskSearch.isEmpty()
                ?tr("no_search_results"):tr("empty"));
        emptyLabel.setVisibility(count==0?View.VISIBLE:View.GONE);
    }

    private void addTaskRow(JSONObject job, boolean hasTimeConflict) {
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
        // Visible, non-destructive warning for overlapping timed tasks on this day.
        // The same filtered task set is used for the warning and the displayed rows.
        if (hasTimeConflict) {
            TextView conflict = text("⚠", 19, Color.rgb(176, 91, 0), true);
            conflict.setGravity(Gravity.CENTER);
            conflict.setContentDescription(ConflictAccessibility.label(selectedLanguage, job));
            conflict.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
            row.addView(conflict, lp(48, 48));
        }

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
        TextView duplicate=text("⧉",21,TEAL,true);
        duplicate.setGravity(Gravity.CENTER);
        duplicate.setContentDescription(tr("duplicate_task")+": "+title);
        duplicate.setOnClickListener(v->duplicateTask(job));
        TextView replan=text("↪",21,TEAL,true);
        replan.setGravity(Gravity.CENTER);
        replan.setContentDescription(tr("reschedule_task")+": "+title);
        replan.setOnClickListener(v->rescheduleTask(job));
        row.addView(replan,lp(33,33));
        row.addView(duplicate,lp(33,33));

        String sectionName=job.optString("section","").trim();
        if(!sectionName.isEmpty()){
            TextView sectionTag=text("▸ "+sectionName,12,MUTED,true);
            sectionTag.setPadding(dp(40),dp(4),0,0);
            card.addView(sectionTag,lp(-1,-2));
        }
        JSONArray tags=job.optJSONArray("tags");
        if(tags!=null && tags.length()>0){
            StringBuilder shown=new StringBuilder();
            for(int i=0;i<tags.length();i++){
                if(i>0)shown.append("  ");
                shown.append("#").append(tags.optString(i));
            }
            TextView tagText=text(shown.toString(),12,TEAL,true);
            tagText.setPadding(dp(40),dp(4),0,0);
            card.addView(tagText,lp(-1,-2));
        }
        String projectName=job.optString("project","").trim();
        if(!projectName.isEmpty()){
            TextView projectTag=text("▣ "+projectName,12,TEAL,true);
            projectTag.setPadding(dp(40),dp(4),0,0);
            card.addView(projectTag,lp(-1,-2));
        }
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

        String deadlineDay=TaskDeadline.get(job);
        if(!deadlineDay.isEmpty()){
            TextView deadlineTag=text(tr("deadline")+": "+formatDay(deadlineDay),12,MUTED,true);
            deadlineTag.setPadding(dp(40),dp(4),0,0);
            card.addView(deadlineTag,lp(-1,-2));
        }

        String taskTime=TaskTime.getTime(job);
        if(!taskTime.isEmpty()){
            TextView timeTag=text(tr("task_time")+": "+taskTime,12,MUTED,true);
            timeTag.setPadding(dp(40),dp(4),0,0);
            card.addView(timeTag,lp(-1,-2));
        }

        int duration=TaskTime.getDurationMinutes(job);
        if(duration>0){
            TextView durationTag=text(tr("task_duration")+": "+duration+" "+tr("duration_minutes"),12,MUTED,true);
            durationTag.setPadding(dp(40),dp(4),0,0);
            card.addView(durationTag,lp(-1,-2));
        }

        String detailsText=job.optString("description","").trim();
        if(!detailsText.isEmpty()){
            TextView detailsPreview=text(detailsText,13,MUTED,false);
            detailsPreview.setMaxLines(3);
            detailsPreview.setEllipsize(android.text.TextUtils.TruncateAt.END);
            detailsPreview.setPadding(dp(40),dp(6),dp(5),dp(2));
            detailsPreview.setContentDescription(tr("task_description")+": "+detailsText);
            detailsPreview.setOnClickListener(v->new AlertDialog.Builder(this)
                    .setTitle(job.optString("title",""))
                    .setMessage(detailsText)
                    .setPositiveButton(android.R.string.ok,(dialog,which)->{})
                    .show());
            card.addView(detailsPreview,lp(-1,-2));
        }

        JSONArray subtasks=job.optJSONArray("subtasks");
        if(subtasks!=null && subtasks.length()>0){
            int finished=0;
            for(int i=0;i<subtasks.length();i++){
                JSONObject sub=subtasks.optJSONObject(i);
                if(sub!=null && sub.optJSONObject("history")!=null
                        && sub.optJSONObject("history").optBoolean(selectedDay,false))finished++;
            }
            TextView subHeading=text(tr("subtasks")+" ("+finished+"/"+subtasks.length()+")",
                    12,TEAL,true);
            subHeading.setPadding(dp(40),dp(8),0,dp(2));
            card.addView(subHeading,lp(-1,-2));
            for(int i=0;i<subtasks.length();i++){
                JSONObject sub=subtasks.optJSONObject(i);
                if(sub==null)continue;
                JSONObject history=sub.optJSONObject("history");
                boolean done=history!=null && history.optBoolean(selectedDay,false);
                CheckBox subCheck=new CheckBox(this);
                subCheck.setText(sub.optString("title",""));
                subCheck.setTextColor(INK);
                subCheck.setTextSize(13);
                subCheck.setButtonTintList(ColorStateList.valueOf(TEAL));
                subCheck.setChecked(done);
                subCheck.setPadding(dp(32),0,0,0);
                final JSONObject currentSub=sub;
                subCheck.setOnCheckedChangeListener((buttonView,isChecked)->{
                    JSONObject h=currentSub.optJSONObject("history");
                    if(h==null)h=new JSONObject();
                    try{
                        h.put(selectedDay,isChecked);
                        currentSub.put("history",h);
                    }catch(JSONException ignored){}
                    save();
                    int completed=0;
                    JSONArray all=job.optJSONArray("subtasks");
                    if(all!=null)for(int k=0;k<all.length();k++){
                        JSONObject child=all.optJSONObject(k);
                        if(child!=null && child.optJSONObject("history")!=null
                                && child.optJSONObject("history").optBoolean(selectedDay,false))
                            completed++;
                    }
                    subHeading.setText(tr("subtasks")+" ("+completed+"/"+all.length()+")");
                });
                card.addView(subCheck,lp(-1,-2));
            }
        }

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
