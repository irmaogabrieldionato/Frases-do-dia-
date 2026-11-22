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
    // Five concise instructions per language, matched to HEADINGS order.
    // These are offline, instructional text; never assume account sync is available.
    private static final String[][] DETAILS = {
        {"Select a day and create tasks with times and recurrence.", "Choose one of five priority colors and set progress from 0 to 100% for each day.", "Tap the warning to inspect overlapping visible tasks; filters never delete tasks.", "Export a JSON backup before restoring. Local photos and videos are not included.", "Cross-device sync and account registration are not yet available."},
        {"选择日期并创建任务，设置时间和重复规则。","选择五种优先级颜色之一，并按日期设置0至100%的进度。","点击警告查看可见任务的时间冲突；筛选不会删除任务。","恢复前导出JSON备份；不包含本地照片和视频。","账户注册和跨设备同步尚不可用。"},
        {"तारीख चुनें और समय व दोहराव के साथ कार्य बनाएँ।","पाँच प्राथमिकता रंगों में से चुनें और हर दिन 0 से 100% प्रगति दर्ज करें।","दिखाई देने वाले कार्यों के समय टकराव के लिए चेतावनी छुएँ; फ़िल्टर कार्य नहीं मिटाते।","बहाली से पहले JSON बैकअप लें; स्थानीय फ़ोटो और वीडियो शामिल नहीं हैं।","खाता पंजीकरण और उपकरणों के बीच सिंक अभी उपलब्ध नहीं हैं।"},
        {"Selecciona un día y crea tareas con horarios y repeticiones.","Elige uno de cinco colores de prioridad y registra el progreso diario de 0 a 100 %.","Toca la advertencia para ver solapamientos visibles; los filtros no borran tareas.","Exporta un respaldo JSON antes de restaurar; no incluye fotos ni vídeos locales.","El registro de cuentas y la sincronización aún no están disponibles."},
        {"Choisissez un jour et créez des tâches avec horaires et récurrences.","Choisissez parmi cinq couleurs de priorité et notez la progression quotidienne de 0 à 100 %.","Touchez l’avertissement pour voir les chevauchements visibles ; les filtres ne suppriment rien.","Exportez une sauvegarde JSON avant restauration ; photos et vidéos locales exclues.","L’inscription et la synchronisation entre appareils ne sont pas disponibles."},
        {"اختر يومًا وأنشئ المهام مع الأوقات والتكرار.","اختر أحد ألوان الأولوية الخمسة وسجّل تقدم كل يوم من 0 إلى 100٪.","اضغط التحذير لمعرفة تداخل أوقات المهام الظاهرة؛ المرشحات لا تحذف المهام.","صدّر نسخة JSON قبل الاستعادة؛ الصور والفيديوهات المحلية غير مشمولة.","تسجيل الحساب والمزامنة بين الأجهزة غير متاحين بعد."},
        {"দিন বেছে সময় ও পুনরাবৃত্তিসহ কাজ তৈরি করুন।","পাঁচটি অগ্রাধিকার রঙ থেকে বেছে প্রতিদিনের অগ্রগতি ০ থেকে ১০০% দিন।","দেখানো কাজের সময়ের সংঘাত দেখতে সতর্কতায় চাপুন; ফিল্টার কাজ মুছে না।","পুনরুদ্ধারের আগে JSON ব্যাকআপ নিন; স্থানীয় ছবি ও ভিডিও অন্তর্ভুক্ত নয়।","অ্যাকাউন্ট নিবন্ধন ও ডিভাইস সিঙ্ক এখনো উপলব্ধ নয়।"},
        {"Выберите день и создайте задачи со временем и повторениями.","Выберите один из пяти цветов приоритета и укажите прогресс 0–100 % за день.","Нажмите предупреждение для просмотра пересечений видимых задач; фильтры ничего не удаляют.","Перед восстановлением экспортируйте JSON; локальные фото и видео не входят.","Регистрация аккаунта и синхронизация устройств пока недоступны."},
        {"Escolha um dia e crie tarefas com horários e recorrências.","Selecione uma das cinco cores de prioridade e registre o progresso diário de 0 a 100%.","Toque no aviso para ver sobreposições das tarefas visíveis; filtros não apagam tarefas.","Exporte um backup JSON antes de restaurar; fotos e vídeos locais não são incluídos.","Cadastro e sincronização entre aparelhos ainda não estão disponíveis."},
        {"Pilih hari dan buat tugas dengan waktu serta pengulangan.","Pilih satu dari lima warna prioritas dan catat kemajuan harian 0–100%.","Ketuk peringatan untuk melihat bentrok tugas yang tampil; filter tidak menghapus tugas.","Ekspor cadangan JSON sebelum memulihkan; foto dan video lokal tidak disertakan.","Pendaftaran akun dan sinkronisasi antarperangkat belum tersedia."},
        {"دن منتخب کریں اور وقت اور تکرار کے ساتھ کام بنائیں۔","پانچ ترجیحی رنگوں میں سے منتخب کریں اور روزانہ 0 سے 100٪ پیش رفت درج کریں۔","نظر آنے والے کاموں کا وقت ٹکرانے پر انتباہ دبائیں؛ فلٹر کام حذف نہیں کرتے۔","بحالی سے پہلے JSON بیک اپ بنائیں؛ مقامی تصاویر اور ویڈیوز شامل نہیں۔","اکاؤنٹ رجسٹریشن اور آلات کے درمیان سنک ابھی دستیاب نہیں۔"},
        {"Wählen Sie einen Tag und erstellen Sie Aufgaben mit Zeiten und Wiederholungen.","Wählen Sie eine von fünf Prioritätsfarben und erfassen Sie den Tagesfortschritt von 0 bis 100 %.","Tippen Sie auf die Warnung für Überschneidungen sichtbarer Aufgaben; Filter löschen nichts.","Exportieren Sie vor der Wiederherstellung ein JSON-Backup; lokale Medien fehlen.","Registrierung und geräteübergreifende Synchronisierung sind noch nicht verfügbar."},
        {"日付を選び、時刻や繰り返しを設定してタスクを作成します。","5色の優先度から選び、日ごとに0～100%の進捗を記録します。","警告をタップして表示中のタスクの時間重複を確認します。フィルターで削除はされません。","復元前にJSONバックアップを保存します。端末内の写真や動画は含まれません。","アカウント登録と端末間同期はまだ利用できません。"},
        {"Chagua siku na uunde kazi zenye muda na marudio.","Chagua moja ya rangi tano za kipaumbele na rekodi maendeleo ya kila siku 0–100%.","Gusa onyo kuona muda unaogongana kwa kazi zinazoonekana; vichujio havifuti kazi.","Hamisha nakala ya JSON kabla ya kurejesha; picha na video za ndani hazijumuishwi.","Usajili wa akaunti na usawazishaji wa vifaa bado haupatikani."},
        {"दिवस निवडा आणि वेळ व पुनरावृत्तीसह कामे तयार करा.","पाच प्राधान्य रंगांपैकी निवडा आणि दररोज ० ते १००% प्रगती नोंदवा.","दिसणाऱ्या कामांतील वेळेचे संघर्ष पाहण्यासाठी इशारा टॅप करा; फिल्टर कामे हटवत नाहीत.","पुनर्संचयित करण्यापूर्वी JSON बॅकअप घ्या; स्थानिक फोटो व व्हिडिओ समाविष्ट नाहीत.","खाते नोंदणी व उपकरणांतील समक्रमण अद्याप उपलब्ध नाही."}
    };
    static String[] details(String language) {
        int index = Languages.index(language);
        return DETAILS[index < 0 ? 8 : index].clone();
    }
    static String[] headings(String language) {
        int index = Languages.index(language);
        return HEADINGS[index < 0 ? 8 : index].clone();
    }
    // Accessible, localized color names; indexed by Languages.CODES.
    private static final String[][] PRIORITY_COLORS = {
        {"Green", "Blue", "Yellow", "Orange", "Red"},
        {"绿色", "蓝色", "黄色", "橙色", "红色"},
        {"हरा", "नीला", "पीला", "नारंगी", "लाल"},
        {"Verde", "Azul", "Amarillo", "Naranja", "Rojo"},
        {"Vert", "Bleu", "Jaune", "Orange", "Rouge"},
        {"أخضر", "أزرق", "أصفر", "برتقالي", "أحمر"},
        {"সবুজ", "নীল", "হলুদ", "কমলা", "লাল"},
        {"Зелёный", "Синий", "Жёлтый", "Оранжевый", "Красный"},
        {"Verde", "Azul", "Amarelo", "Laranja", "Vermelho"},
        {"Hijau", "Biru", "Kuning", "Oranye", "Merah"},
        {"سبز", "نیلا", "پیلا", "نارنجی", "سرخ"},
        {"Grün", "Blau", "Gelb", "Orange", "Rot"},
        {"緑", "青", "黄", "オレンジ", "赤"},
        {"Kijani", "Bluu", "Njano", "Chungwa", "Nyekundu"},
        {"हिरवा", "निळा", "पिवळा", "नारिंगी", "लाल"}
    };
    static String[] priorityColors(String language) {
        int index = Languages.index(language);
        return PRIORITY_COLORS[index < 0 ? 8 : index].clone();
    }
}
