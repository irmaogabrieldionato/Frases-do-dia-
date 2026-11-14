package com.orbis.meudia;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Localized interface messages. Order matches the language picker. */
final class Languages {
    static final String[] CODES = {"en", "zh-CN", "hi", "es", "fr", "ar", "bn", "ru", "pt-BR", "id", "ur", "de", "ja", "sw", "mr"};
    static final String[] NAMES = {"English", "中文（普通话）", "हिन्दी", "Español", "Français", "العربية الفصحى", "বাংলা", "Русский", "Português", "Bahasa Indonesia", "اردو", "Deutsch", "日本語", "Kiswahili", "मराठी"};
    private static final Map<String, String[]> MESSAGES = new HashMap<>();
    static {
        put("eyebrow", "YOUR ROUTINE, YOUR PACE", "你的日常，你的节奏", "आपकी दिनचर्या, आपकी रफ़्तार", "TU RUTINA, A TU RITMO", "VOTRE ROUTINE, À VOTRE RYTHME", "روتينك، على وتيرتك", "আপনার রুটিন, আপনার গতিতে", "ВАШ РИТМ, ВАШ ДЕНЬ", "SUA ROTINA, NO SEU RITMO", "RUTINITASMU, SESUAI RITMEMU", "آپ کا معمول، آپ کی رفتار سے", "DEIN ALLTAG, DEIN TEMPO", "自分のペースで、毎日の習慣", "RATIBA YAKO, KWA MWENDO WAKO", "तुमचा दिनक्रम, तुमच्या गतीने");
        put("progress", "Today's progress", "今日进度", "आज की प्रगति", "Progreso de hoy", "Progrès du jour", "تقدم اليوم", "আজকের অগ্রগতি", "Прогресс за сегодня", "Progresso de hoje", "Progres hari ini", "آج کی پیش رفت", "Fortschritt heute", "今日の進捗", "Maendeleo ya leo", "आजची प्रगती");
        put("progress_count", "%1$d of %2$d tasks completed", "已完成 %1$d / %2$d 项任务", "%2$d में से %1$d कार्य पूरे हुए", "%1$d de %2$d tareas completadas", "%1$d tâches terminées sur %2$d", "تم إنجاز %1$d من أصل %2$d مهام", "%2$dটির মধ্যে %1$dটি কাজ সম্পন্ন", "Выполнено %1$d из %2$d задач", "%1$d de %2$d tarefas concluídas", "%1$d dari %2$d tugas selesai", "%2$d میں سے %1$d کام مکمل ہوئے", "%1$d von %2$d Aufgaben erledigt", "%2$d件中%1$d件完了", "Kazi %1$d kati ya %2$d zimekamilika", "%2$d पैकी %1$d कामे पूर्ण");
        put("tasks", "My tasks", "我的任务", "मेरे कार्य", "Mis tareas", "Mes tâches", "مهامي", "আমার কাজ", "Мои задачи", "Minhas tarefas", "Tugas saya", "میرے کام", "Meine Aufgaben", "自分のタスク", "Kazi zangu", "माझी कामे");
        put("task_count", "Tasks: %1$d", "任务：%1$d", "कार्य: %1$d", "Tareas: %1$d", "Tâches : %1$d", "المهام: %1$d", "কাজ: %1$d", "Задачи: %1$d", "Tarefas: %1$d", "Tugas: %1$d", "کام: %1$d", "Aufgaben: %1$d", "タスク：%1$d", "Kazi: %1$d", "कामे: %1$d");
        put("placeholder", "E.g.: Drink 2 liters of water", "例如：喝两升水", "उदा.: 2 लीटर पानी पीना", "Ej.: Beber 2 litros de agua", "Ex. : Boire 2 litres d'eau", "مثال: شرب لترين من الماء", "যেমন: ২ লিটার পানি পান করা", "Например: Выпить 2 литра воды", "Ex.: Beber 2 litros de água", "Contoh: Minum 2 liter air", "مثلاً: 2 لیٹر پانی پینا", "Z. B.: 2 Liter Wasser trinken", "例：水を2リットル飲む", "Mf.: Kunywa lita 2 za maji", "उदा.: २ लिटर पाणी पिणे");
        put("add", "Add", "添加", "जोड़ें", "Añadir", "Ajouter", "إضافة", "যোগ করুন", "Добавить", "Adicionar", "Tambah", "شامل کریں", "Hinzufügen", "追加", "Ongeza", "जोडा");
        put("empty", "Your list is empty. Add your first task above ✨", "列表为空。请在上方添加第一个任务 ✨", "आपकी सूची खाली है। ऊपर अपना पहला कार्य जोड़ें ✨", "Tu lista está vacía. Añade tu primera tarea arriba ✨", "Votre liste est vide. Ajoutez votre première tâche ci-dessus ✨", "قائمتك فارغة. أضف مهمتك الأولى أعلاه ✨", "আপনার তালিকা ফাঁকা। উপরে প্রথম কাজটি যোগ করুন ✨", "Список пуст. Добавьте первую задачу выше ✨", "Sua lista está vazia. Adicione sua primeira tarefa acima ✨", "Daftar masih kosong. Tambahkan tugas pertamamu di atas ✨", "آپ کی فہرست خالی ہے۔ اوپر پہلا کام شامل کریں ✨", "Deine Liste ist leer. Füge oben deine erste Aufgabe hinzu ✨", "リストは空です。上から最初のタスクを追加しましょう ✨", "Orodha yako iko tupu. Ongeza kazi yako ya kwanza hapo juu ✨", "तुमची यादी रिकामी आहे. वर तुमचे पहिले काम जोडा ✨");
        put("tip", "Tasks are saved on this device. Every new day, they become pending again.", "任务保存在此设备上。每天开始时，所有任务会重新变为未完成状态。", "कार्य इस डिवाइस पर सहेजे जाते हैं। हर नए दिन वे फिर से लंबित हो जाते हैं।", "Las tareas se guardan en este dispositivo. Cada nuevo día vuelven a estar pendientes.", "Les tâches sont enregistrées sur cet appareil. Chaque nouveau jour, elles redeviennent à faire.", "تُحفظ المهام على هذا الجهاز. وتعود إلى حالة الانتظار في بداية كل يوم جديد.", "কাজগুলো এই ডিভাইসে সংরক্ষিত থাকে। প্রতিদিন নতুন করে অসম্পূর্ণ হিসেবে দেখাবে।", "Задачи сохраняются на устройстве. Каждый новый день они снова становятся невыполненными.", "As tarefas ficam salvas neste aparelho. A cada novo dia, elas voltam a ficar pendentes.", "Tugas disimpan di perangkat ini. Setiap hari baru, tugas kembali menjadi belum selesai.", "کام اسی ڈیوائس پر محفوظ رہتے ہیں۔ ہر نئے دن دوبارہ نامکمل ہو جاتے ہیں۔", "Die Aufgaben werden auf diesem Gerät gespeichert. Jeden Tag werden sie wieder als offen markiert.", "タスクはこの端末に保存されます。日付が変わると未完了に戻ります。", "Kazi zinahifadhiwa kwenye kifaa hiki. Kila siku mpya zinarudi kuwa hazijakamilika.", "कामे या उपकरणात जतन होतात. प्रत्येक नवीन दिवशी ती पुन्हा अपूर्ण होतात.");
        put("footer", "No account · Your data stays on this device", "无需注册 · 数据仅保存在此设备上", "बिना खाते के · आपका डेटा इसी डिवाइस पर रहता है", "Sin registro · Tus datos permanecen en este dispositivo", "Sans compte · Vos données restent sur cet appareil", "دون حساب · تبقى بياناتك على هذا الجهاز", "কোনো অ্যাকাউন্ট নয় · তথ্য এই ডিভাইসেই থাকে", "Без регистрации · Данные остаются на устройстве", "Sem cadastro · Seus dados ficam neste aparelho", "Tanpa akun · Data tetap di perangkat ini", "بغیر اکاؤنٹ · آپ کا ڈیٹا اسی ڈیوائس پر رہتا ہے", "Ohne Konto · Deine Daten bleiben auf diesem Gerät", "登録不要 · データはこの端末内に保存", "Hakuna akaunti · Data hubaki kwenye kifaa hiki", "खाते आवश्यक नाही · डेटा याच उपकरणात राहतो");
        put("done_action", "Complete: ", "完成：", "पूरा करें: ", "Completar: ", "Terminer : ", "إكمال: ", "সম্পন্ন করুন: ", "Выполнить: ", "Concluir: ", "Selesaikan: ", "مکمل کریں: ", "Erledigen: ", "完了：", "Kamilisha: ", "पूर्ण करा: ");
        put("pending_action", "Mark as pending: ", "标记为未完成：", "लंबित चिह्नित करें: ", "Marcar como pendiente: ", "Marquer comme à faire : ", "وضع علامة غير مكتمل: ", "অসম্পূর্ণ হিসেবে চিহ্নিত করুন: ", "Отметить как невыполненную: ", "Marcar como pendente: ", "Tandai belum selesai: ", "نامکمل نشان زد کریں: ", "Als offen markieren: ", "未完了に戻す：", "Weka kuwa haijakamilika: ", "अपूर्ण म्हणून चिन्हांकित करा: ");
        put("delete_action", "Delete: ", "删除：", "हटाएँ: ", "Eliminar: ", "Supprimer : ", "حذف: ", "মুছে ফেলুন: ", "Удалить: ", "Excluir: ", "Hapus: ", "حذف کریں: ", "Löschen: ", "削除：", "Futa: ", "हटवा: ");
        put("language", "Language", "语言", "भाषा", "Idioma", "Langue", "اللغة", "ভাষা", "Язык", "Idioma", "Bahasa", "زبان", "Sprache", "言語", "Lugha", "भाषा");
        put("progress_accessible", "Daily progress: %1$d%%", "每日进度：%1$d%%", "दैनिक प्रगति: %1$d%%", "Progreso diario: %1$d%%", "Progrès du jour : %1$d%%", "التقدم اليومي: %1$d%%", "দৈনিক অগ্রগতি: %1$d%%", "Прогресс за день: %1$d%%", "Progresso diário: %1$d%%", "Progres harian: %1$d%%", "روزانہ پیش رفت: %1$d%%", "Tagesfortschritt: %1$d%%", "今日の進捗：%1$d%%", "Maendeleo ya siku: %1$d%%", "दैनंदिन प्रगती: %1$d%%");
        put("default1", "Make the bed", "整理床铺", "बिस्तर ठीक करना", "Hacer la cama", "Faire le lit", "ترتيب السرير", "বিছানা গুছানো", "Заправить кровать", "Arrumar a cama", "Merapikan tempat tidur", "بستر ٹھیک کرنا", "Bett machen", "ベッドを整える", "Tandika kitanda", "बिछाना आवरणे");
        put("default2", "Drink water", "喝水", "पानी पीना", "Beber agua", "Boire de l'eau", "شرب الماء", "পানি পান করা", "Пить воду", "Beber água", "Minum air", "پانی پینا", "Wasser trinken", "水を飲む", "Kunywa maji", "पाणी पिणे");
        put("default3", "Exercise", "锻炼身体", "व्यायाम करना", "Hacer ejercicio", "Faire de l'exercice", "ممارسة الرياضة", "ব্যায়াম করা", "Заняться спортом", "Fazer exercícios", "Berolahraga", "ورزش کرنا", "Sport treiben", "運動する", "Fanya mazoezi", "व्यायाम करणे");
        put("default4", "Study for 30 minutes", "学习30分钟", "30 मिनट पढ़ाई करना", "Estudiar 30 minutos", "Étudier pendant 30 minutes", "الدراسة لمدة 30 دقيقة", "৩০ মিনিট পড়াশোনা করা", "Заниматься 30 минут", "Estudar 30 minutos", "Belajar 30 menit", "30 منٹ پڑھنا", "30 Minuten lernen", "30分勉強する", "Soma kwa dakika 30", "३० मिनिटे अभ्यास करणे");
        put("cancel", "Cancel", "取消", "रद्द करें", "Cancelar", "Annuler", "إلغاء", "বাতিল", "Отмена", "Cancelar", "Batal", "منسوخ کریں", "Abbrechen", "キャンセル", "Ghairi", "रद्द करा");
        put("language_screen", "Choose your language", "选择语言", "अपनी भाषा चुनें", "Elige tu idioma", "Choisissez votre langue", "اختر لغتك", "আপনার ভাষা বেছে নিন", "Выберите язык", "Escolha seu idioma", "Pilih bahasa", "اپنی زبان منتخب کریں", "Sprache auswählen", "言語を選択", "Chagua lugha", "तुमची भाषा निवडा");
        put("continue", "Continue", "继续", "जारी रखें", "Continuar", "Continuer", "متابعة", "চালিয়ে যান", "Продолжить", "Continuar", "Lanjutkan", "جاری رکھیں", "Weiter", "続ける", "Endelea", "पुढे चला");
    }

    private static void put(String key, String... values) {
        if (values.length != CODES.length) throw new IllegalArgumentException(key);
        MESSAGES.put(key, values);
    }
    static int index(String code) {
        for (int i = 0; i < CODES.length; i++) if (CODES[i].equals(code)) return i;
        return -1;
    }
    static String text(String code, String key) {
        String[] values = MESSAGES.get(key);
        int index = index(code);
        if (values == null) return key;
        return values[index < 0 ? 8 : index];
    }
    static Locale locale(String code) {
        return Locale.forLanguageTag(index(code) < 0 ? "pt-BR" : code);
    }
    static boolean isRtl(String code) {
        return "ar".equals(code) || "ur".equals(code);
    }
}
