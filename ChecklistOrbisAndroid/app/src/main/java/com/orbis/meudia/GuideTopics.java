package com.orbis.meudia;

/** Topic headings for the offline visual guide. No external translations or services. */
final class GuideTopics {
    private GuideTopics() {}
    // Each row follows Languages.CODES and contains five guide topics.
    private static final String[][] HEADINGS = {
        {"Calendar and tasks","Priorities and progress","Conflicts and filters","Backup and restore","Account and sync"},
        {"日历与任务","优先级与进度","冲突与筛选","备份与恢复","账户与同步"},
        {"कैलेंडर और कार्य","प्राथमिकता और प्रगति","समय टकराव और फ़िल्टर","बैकअप और पुनर्स्थापना","खाता और सिंक"},
        {"Calendario y tareas","Prioridades y progreso","Conflictos y filtros","Copia de seguridad","Cuenta y sincronización"},
        {"Calendrier et tâches","Priorités et progression","Conflits et filtres","Sauvegarde et restauration","Compte et synchronisation"},
        {"التقويم والمهام","الأولويات والتقدم","التعارضات والمرشحات","النسخ الاحتياطي والاستعادة","الحساب والمزامنة"},
        {"ক্যালেন্ডার ও কাজ","অগ্রাধিকার ও অগ্রগতি","সময় সংঘাত ও ফিল্টার","ব্যাকআপ ও পুনরুদ্ধার","অ্যাকাউন্ট ও সিঙ্ক"},
        {"Календарь и задачи","Приоритеты и прогресс","Конфликты и фильтры","Резервные копии","Учётная запись и синхронизация"},
        {"Agenda e tarefas","Prioridades e progresso","Conflitos e filtros","Backup e restauração","Conta e sincronização"},
        {"Kalender dan tugas","Prioritas dan kemajuan","Bentrok dan filter","Cadangan dan pemulihan","Akun dan sinkronisasi"},
        {"کیلنڈر اور کام","ترجیحات اور پیش رفت","وقت کا ٹکراؤ اور فلٹر","بیک اپ اور بحالی","اکاؤنٹ اور سنک"},
        {"Kalender und Aufgaben","Prioritäten und Fortschritt","Konflikte und Filter","Sicherung und Wiederherstellung","Konto und Synchronisierung"},
        {"カレンダーとタスク","優先度と進捗","時間の重複とフィルター","バックアップと復元","アカウントと同期"},
        {"Kalenda na kazi","Vipaumbele na maendeleo","Migongano na vichujio","Nakala na urejeshaji","Akaunti na usawazishaji"},
        {"दिनदर्शिका व कामे","प्राधान्य व प्रगती","वेळेचे संघर्ष व फिल्टर","बॅकअप व पुनर्संचयित","खाते व समक्रमण"}
    };
    static String[] headings(String language) {
        int index = Languages.index(language);
        return HEADINGS[index < 0 ? 8 : index].clone();
    }
}
