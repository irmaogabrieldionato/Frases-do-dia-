/* 7-Note Musical Game — jogo de memória, ouvido, teclado e compositor */
(() => {
  "use strict";

  const core = window.SevenNoteCore;
  if (!core) throw new Error("Motor musical indisponível.");

  const tvMode = new URLSearchParams(location.search).get("tv") === "1";
  if (tvMode) document.documentElement.classList.add("tv-mode");
  const $ = (id) => document.getElementById(id);
  const pads = Array.from(document.querySelectorAll("[data-note]"));
  const modeButtons = Array.from(document.querySelectorAll("[data-mode]"));
  const notes = new Map(core.NOTES.map((note) => [note.id, note]));
  // Nova campanha: começa na fase 1 porque a sequência inicial mudou completamente.
  // A chave antiga permanece intacta para que nenhum dado anterior seja apagado.
  const STORAGE_KEY = "seven-note-musical-game:v2";
  const PREFS_KEY = "seven-note-musical-game:prefs-v1";
  const MELODY_KEY = "seven-note-musical-game:melody-v1";

  function readJSON(key, fallback) {
    try {
      const value = localStorage.getItem(key);
      return value === null ? fallback : JSON.parse(value);
    } catch (_) {
      return fallback;
    }
  }
  function saveJSON(key, value) {
    try { localStorage.setItem(key, JSON.stringify(value)); }
    catch (_) { /* O jogo continua disponível com armazenamento restrito. */ }
  }

  let progress = core.sanitizeProgress(readJSON(STORAGE_KEY, {}));
  let preferences = core.sanitizePreferences(readJSON(PREFS_KEY, {}));
  let melody = core.sanitizeMelody(readJSON(MELODY_KEY, []));
  let mode = "memory";
  let phase = progress.completed ? "finished" : "idle"; // idle | playing | input | won | lost | finished
  let inputIndex = 0;
  let ear = { status: "idle", round: 0, correct: 0, target: null, last: null };
  let adBusy = false;
  let playbackEpoch = 0;
  let audioContext = null;
  let deferredInstall = null;
  let message = progress.completed
    ? "🏆 Parabéns! Você concluiu todas as 1.000 fases do jogo!"
    : "Ouça a sequência e repita as notas na mesma ordem.";
  let messageKind = "default";
  let lastFocused = null;

  // Reutiliza a sequência da fase durante as até 1.000 entradas do jogador.
  // Recalcula somente ao mudar de fase ou reiniciar a campanha.
  let memorySequenceLevel = null;
  let memorySequence = null;
  function currentMemorySequence() {
    if (memorySequenceLevel !== progress.level || !memorySequence) {
      memorySequence = core.sequenceForLevel(progress.level);
      memorySequenceLevel = progress.level;
    }
    return memorySequence;
  }

  function saveProgress() { saveJSON(STORAGE_KEY, progress); }
  function setMessage(text, kind = "default") { message = text; messageKind = kind; }
  function delay(ms) { return new Promise((resolve) => window.setTimeout(resolve, ms)); }
  function cancelPlayback() {
    playbackEpoch++;
    pads.forEach((pad) => pad.classList.remove("active"));
    $("now-playing").textContent = "♫";
    if (phase === "playing") phase = "idle";
    if (ear.status === "playing") ear.status = "ready";
  }

  async function prepareAudio() {
    const Context = window.AudioContext || window.webkitAudioContext;
    if (!Context) {
      setMessage("Seu navegador não oferece áudio. Use uma versão atualizada de Chrome, Safari ou Firefox.", "error");
      render();
      return false;
    }
    try {
      // Um contexto encerrado não pode ser retomado; recrie-o na próxima tentativa.
      if (!audioContext || audioContext.state === "closed") audioContext = new Context();
      if (audioContext.state === "suspended") await audioContext.resume();
      if (audioContext.state !== "running") throw new Error("Áudio suspenso.");
      return true;
    } catch (_) {
      setMessage("Não foi possível iniciar o áudio. Confira a permissão de som e tente novamente.", "error");
      render();
      return false;
    }
  }

  function tone(id, duration = 0.37) {
    if (!audioContext || audioContext.state !== "running") return;
    const note = notes.get(id);
    if (!note || preferences.volume === 0) return;
    const start = audioContext.currentTime + 0.008;
    const master = audioContext.createGain();
    const amplitude = Math.max(0.0002, 0.15 * preferences.volume / 100);
    const shape = preferences.instrument;
    master.gain.setValueAtTime(0.0001, start);
    master.gain.exponentialRampToValueAtTime(amplitude, start + 0.024);
    master.gain.exponentialRampToValueAtTime(shape === "bell" ? amplitude * 0.18 : amplitude * 0.42, start + duration * 0.75);
    master.gain.exponentialRampToValueAtTime(0.0001, start + duration + 0.12);
    master.connect(audioContext.destination);
    const voices = shape === "retro"
      ? [["square", 1, 0.6], ["triangle", 1, 0.18]]
      : shape === "bell"
        ? [["sine", 1, 1], ["sine", 2, 0.27], ["sine", 3, 0.09]]
        : [["triangle", 1, 0.85], ["sine", 2, 0.12]];
    let stopped = 0;
    voices.forEach(([type, multiplier, weight]) => {
      const osc = audioContext.createOscillator();
      const mix = audioContext.createGain();
      osc.type = type;
      osc.frequency.value = note.frequency * multiplier;
      mix.gain.value = weight;
      osc.connect(mix);
      mix.connect(master);
      osc.addEventListener("ended", () => {
        osc.disconnect();
        mix.disconnect();
        stopped += 1;
        if (stopped === voices.length) master.disconnect();
      }, { once: true });
      osc.start(start);
      osc.stop(start + duration + 0.14);
    });
  }

  function flash(id, reveal = true) {
    const pad = pads.find((item) => item.dataset.note === id);
    if (!pad) return;
    pad.classList.remove("active");
    void pad.offsetWidth;
    pad.classList.add("active");
    $("now-playing").textContent = reveal && preferences.showNames ? notes.get(id).name : "♫";
    const token = playbackEpoch;
    window.setTimeout(() => {
      if (token === playbackEpoch) {
        pad.classList.remove("active");
        $("now-playing").textContent = "♫";
      }
    }, 330);
  }

  function playNote(id, reveal = true) {
    tone(id);
    if (reveal) flash(id);
  }

  let achievementRenderKey = null;
  function renderAchievements() {
    // Conquistas mudam somente com o progresso, não a cada nota pressionada.
    const key = [progress.totalWins > 0, progress.best >= 8, progress.bestStreak >= 5,
      progress.earBest >= core.EAR_ROUNDS, progress.best >= 25, progress.completed].join(":");
    if (key === achievementRenderKey) return;
    const all = core.achievements(progress);
    $("achievement-count").textContent = all.filter((item) => item.unlocked).length + "/" + all.length;
    const list = $("achievement-list");
    list.replaceChildren();
    all.forEach((item) => {
      const card = document.createElement("div");
      card.className = "achievement" + (item.unlocked ? " unlocked" : "");
      card.title = item.description;
      const icon = document.createElement("span");
      icon.className = "achievement-icon";
      icon.textContent = item.unlocked ? item.icon : "◌";
      const label = document.createElement("span");
      const heading = document.createElement("strong");
      heading.textContent = item.title;
      const detail = document.createElement("small");
      detail.textContent = item.description;
      label.append(heading, detail);
      card.append(icon, label);
      list.appendChild(card);
    });
    achievementRenderKey = key;
  }

  let melodyRenderKey = null;
  function renderMelody() {
    const holder = $("sequence-chips");
    const key = melody.join(",");
    if (key !== melodyRenderKey) {
      holder.replaceChildren();
      melody.forEach((id, index) => {
      const item = document.createElement("span");
      item.className = "sequence-chip";
      item.textContent = (index + 1) + " · " + notes.get(id).name;
      holder.appendChild(item);
      });
      if (!melody.length) {
      const empty = document.createElement("span");
      empty.className = "sequence-empty";
      empty.textContent = "Toque as teclas para criar sua melodia...";
      holder.appendChild(empty);
      }
      melodyRenderKey = key;
    }
    $("undo-button").disabled = melody.length === 0 || adBusy || phase === "playing";
    $("clear-button").disabled = melody.length === 0 || adBusy || phase === "playing";
  }

  function render() {
    $("level").textContent = String(progress.level);
    $("best").textContent = String(progress.best);
    $("losses").textContent = String(progress.totalLosses);
    $("points").textContent = progress.points.toLocaleString("pt-BR");
    $("total-notes").textContent = String(progress.level);
    $("wins").textContent = String(progress.totalWins);
    $("streak").textContent = String(progress.streak);
    $("ear-best").textContent = progress.earBest + "/" + core.EAR_ROUNDS;
    $("campaign-progress").textContent = Math.min(progress.level,core.MAX_LEVEL) + " / " + core.MAX_LEVEL;
    $("finish-banner").hidden = !(progress.completed && mode === "memory");
    $("message").textContent = message;
    $("message").dataset.kind = messageKind;
    $("ad-countdown").textContent = "Próximo anúncio após " + (2 - progress.totalLosses % 2) + " derrota(s)";
    $("sequence-panel").hidden = mode !== "compose";
    $("keyboard-hint").textContent = "Toque na tela ou use as teclas 1–7";
    modeButtons.forEach((button) => {
      const selected = button.dataset.mode === mode;
      button.classList.toggle("selected", selected);
      button.setAttribute("aria-pressed", String(selected));
      button.disabled = adBusy;
    });

    let mainLabel = "▶ Ouvir sequência";
    let otherLabel = "Ouvir novamente";
    let otherEnabled = false;
    let progressPct = 0;
    const recording = mode === "memory";
    if (recording) {
      $("instructions").textContent = progress.completed
        ? "Você venceu o desafio completo: 1.000 fases e 1.000 notas na sequência final!"
        : "São 1.000 fases. Cada vitória acrescenta uma nota; conclua a fase 1.000 para vencer.";
      $("mode-label").textContent = "DESAFIO DE MEMÓRIA";
      $("step-progress").textContent = progress.completed ? "1.000/1.000 · FIM DO JOGO"
        : phase === "input" ? "Sua vez: " + inputIndex + "/" + progress.level + " notas"
        : "Fase " + progress.level + "/" + core.MAX_LEVEL + " · " + progress.level + " notas";
      mainLabel = progress.completed ? "🏆 Jogo concluído"
        : phase === "playing" ? "♫ Reproduzindo..."
        : phase === "won" ? "▶ Próxima fase"
        : phase === "lost" ? "↺ Tentar novamente"
        : phase === "input" ? "↺ Recomeçar sequência" : "▶ Ouvir sequência";
      otherLabel = "♫ Escutar outra vez";
      otherEnabled = phase === "input";
      progressPct = progress.completed || phase === "won" ? 100 : phase === "input" ? inputIndex / progress.level * 100 : 0;
    } else if (mode === "ear") {
      $("instructions").textContent = "Reconheça as notas somente pelo som. Cada treino tem dez perguntas.";
      $("mode-label").textContent = "TREINO DE OUVIDO";
      $("step-progress").textContent = ear.round
        ? "Pergunta " + ear.round + "/" + core.EAR_ROUNDS + " · Acertos: " + ear.correct
        : "Dez notas para reconhecer";
      mainLabel = ear.status === "idle" || ear.status === "finished" ? "▶ Novo treino"
        : ear.status === "feedback" || ear.status === "ready" ? "▶ Próxima nota"
        : ear.status === "playing" ? "♫ Tocando nota..." : "♫ Ouvir nota outra vez";
      otherLabel = "♫ Repetir som";
      otherEnabled = ear.status === "input";
      progressPct = ear.status === "finished" ? 100 : ear.round
        ? ((ear.round - (ear.status === "input" || ear.status === "playing" ? 1 : 0)) / core.EAR_ROUNDS) * 100 : 0;
    } else if (mode === "free") {
      $("instructions").textContent = "Explore livremente as sete notas, sem derrotas ou anúncios.";
      $("mode-label").textContent = "TECLADO LIVRE";
      $("step-progress").textContent = "Experimente todas as notas";
      mainLabel = "♫ Tocar escala completa";
      otherLabel = "Limpar";
    } else {
      $("instructions").textContent = "Crie uma melodia de até " + core.MAX_MELODY + " notas e escute sua criação.";
      $("mode-label").textContent = "ESTÚDIO DO COMPOSITOR";
      $("step-progress").textContent = "Composição: " + melody.length + "/" + core.MAX_MELODY + " notas";
      mainLabel = phase === "playing" ? "♫ Tocando melodia..." : "▶ Reproduzir melodia";
      otherLabel = "↺ Desfazer última";
      otherEnabled = melody.length > 0;
      progressPct = melody.length / core.MAX_MELODY * 100;
    }

    const main = $("play-button");
    main.textContent = mainLabel;
    main.disabled = adBusy || phase === "playing" || (mode === "memory" && progress.completed) ||
      (mode === "compose" && melody.length === 0) ||
      (mode === "ear" && ear.status === "playing");
    const other = $("secondary-button");
    other.textContent = otherLabel;
    other.hidden = mode === "free";
    other.disabled = adBusy || phase === "playing" || !otherEnabled;

    const bar = $("progress-track");
    bar.setAttribute("aria-valuenow", String(Math.round(progressPct)));
    $("progress-fill").style.width = Math.min(100, Math.max(0, progressPct)) + "%";
    pads.forEach((pad) => {
      pad.disabled = adBusy || phase === "playing" || (mode === "memory" && progress.completed) ||
        (mode === "ear" && ear.status !== "input");
    });
    renderMelody();
    renderAchievements();
  }

  async function playSequence(sequence, owner, hasPreview = true) {
    const epoch = ++playbackEpoch;
    phase = "playing";
    setMessage(owner === "compose" ? "Reproduzindo sua composição..." : "Escute com atenção...", "default");
    render();
    if (!(await prepareAudio())) {
      if (epoch === playbackEpoch) { phase = "idle"; render(); }
      return false;
    }
    await delay(280);
    if (epoch !== playbackEpoch || mode !== owner) return false;
    for (const id of sequence) {
      if (epoch !== playbackEpoch || mode !== owner || document.hidden) {
        if (epoch === playbackEpoch) { phase = "idle"; render(); }
        return false;
      }
      // Se o sistema interromper o áudio durante uma sequência longa, não
      // avance silenciosamente para a fase de resposta como se tivesse tocado.
      if (!audioContext || audioContext.state !== "running") {
        phase = "idle";
        setMessage("O áudio foi interrompido. Toque em Reproduzir para ouvir a sequência novamente.", "error");
        render();
        return false;
      }
      tone(id);
      if (hasPreview) flash(id);
      await delay(core.TEMPOS[preferences.tempo]);
    }
    if (epoch !== playbackEpoch || mode !== owner) return false;
    if (!audioContext || audioContext.state !== "running") {
      phase = "idle";
      setMessage("O áudio foi interrompido. Toque em Reproduzir para ouvir a sequência novamente.", "error");
      render();
      return false;
    }
    phase = owner === "memory" ? "input" : "idle";
    setMessage(owner === "memory"
      ? "Agora repita a sequência! Cada erro conta como uma derrota."
      : "Sua composição terminou. Você pode continuar editando.", "default");
    render();
    return true;
  }

  async function startMemory() {
    if (mode !== "memory" || phase === "playing" || adBusy || progress.completed) return;
    inputIndex = 0;
    await playSequence(currentMemorySequence(), "memory");
  }

  async function startEarQuestion() {
    if (mode !== "ear" || adBusy) return;
    const epoch = ++playbackEpoch;
    ear.status = "playing";
    setMessage("Escute a nota. Qual das sete teclas produziu este som?");
    render();
    if (!(await prepareAudio())) {
      if (epoch === playbackEpoch) { ear.status = "ready"; render(); }
      return;
    }
    if (epoch !== playbackEpoch || mode !== "ear") return;
    tone(ear.target, 0.62); // No treino de ouvido nunca revelamos a tecla correta.
    await delay(540);
    if (epoch !== playbackEpoch || mode !== "ear") return;
    ear.status = "input";
    setMessage("Qual nota você ouviu? Toque a resposta no teclado.");
    render();
  }

  function newEarSession() {
    if (adBusy || mode !== "ear") return;
    cancelPlayback();
    ear = { status: "ready", round: 1, correct: 0, target: null, last: null };
    ear.target = core.chooseEarNote(null);
    ear.last = ear.target;
    void startEarQuestion();
  }

  function nextEar() {
    if (mode !== "ear" || adBusy) return;
    if (ear.status === "idle" || ear.status === "finished") { newEarSession(); return; }
    if (ear.status === "input") {
      void startEarQuestion(); // Ouvir a mesma nota, sem trocar de pergunta.
      return;
    }
    if (ear.status !== "feedback" && ear.status !== "ready") return;
    if (ear.status === "feedback") ear.round += 1;
    ear.target = core.chooseEarNote(ear.last);
    ear.last = ear.target;
    void startEarQuestion();
  }

  function answerEar(id) {
    if (ear.status !== "input" || adBusy) return;
    const correct = id === ear.target;
    ear.status = "feedback";
    if (correct) ear.correct += 1;
    progress = core.recordEarAnswer(progress, correct);
    if (ear.round >= core.EAR_ROUNDS) {
      ear.status = "finished";
      progress = core.recordEarSession(progress, ear.correct);
      setMessage("Treino concluído! Você acertou " + ear.correct + " de " + core.EAR_ROUNDS +
        " notas. " + (ear.correct === core.EAR_ROUNDS ? "Perfeito!" : "Continue praticando."), "success");
    } else {
      setMessage(correct ? "Acertou! Era " + notes.get(ear.target).name + ". Vá para a próxima nota."
        : "Era " + notes.get(ear.target).name + ". Vamos para a próxima nota.", correct ? "success" : "error");
    }
    saveProgress();
    render();
    if (!correct && core.adDue(progress.totalLosses)) void showAdBreak();
  }

  function playScale() {
    if (mode !== "free" || adBusy || phase === "playing") return;
    void playSequence(core.NOTES.map((n) => n.id), "free");
  }

  function storeMelody() { saveJSON(MELODY_KEY, melody); }
  function undoMelody() {
    if (mode !== "compose" || phase === "playing" || !melody.length) return;
    melody.pop(); storeMelody(); setMessage("Última nota removida."); render();
  }
  function clearMelody() {
    if (mode !== "compose" || phase === "playing" || !melody.length) return;
    if (!window.confirm("Apagar a melodia criada?")) return;
    melody = []; storeMelody(); setMessage("Melodia apagada."); render();
  }

  async function pressNote(id) {
    if (adBusy || phase === "playing" || (mode === "memory" && progress.completed) ||
        (mode === "ear" && ear.status !== "input")) return;
    const requestedMode = mode;
    const epoch = playbackEpoch;
    if (!(await prepareAudio())) return;
    if (adBusy || mode !== requestedMode || epoch !== playbackEpoch ||
        phase === "playing" || (mode === "memory" && progress.completed) ||
        (mode === "ear" && ear.status !== "input")) return;
    playNote(id);

    if (mode === "free") {
      setMessage("Você tocou " + notes.get(id).name + ". Experimente outra nota.");
    } else if (mode === "compose") {
      if (melody.length >= core.MAX_MELODY) {
        setMessage("Limite de " + core.MAX_MELODY + " notas atingido. Reproduza ou desfaça uma nota.", "error");
      } else {
        melody.push(id);
        storeMelody();
        setMessage("Nota " + notes.get(id).name + " adicionada! " + melody.length + "/" + core.MAX_MELODY + ".", "success");
      }
    } else if (mode === "ear") {
      answerEar(id);
    } else if (phase === "input") {
      const result = core.evaluateNote(currentMemorySequence(), inputIndex, id);
      if (result.result === "loss") {
        const expected = notes.get(result.expected).name;
        progress = core.registerLoss(progress);
        phase = "lost";
        inputIndex = 0;
        saveProgress();
        setMessage("Você errou! Era " + expected + ". A fase não avança até ser concluída.", "error");
        render();
        if (core.adDue(progress.totalLosses)) void showAdBreak();
        return;
      }
      inputIndex = result.nextIndex;
      if (result.result === "win") {
        const finished = progress.level;
        progress = core.advanceLevel(progress);
        saveProgress();
        if (progress.completed) {
          phase = "finished";
          setMessage("🏆 Parabéns! Você venceu a fase 1.000 e concluiu o 7-Note Musical Game!", "success");
          $("finish-dialog").showModal();
        } else {
          phase = "won";
          setMessage("Parabéns! Fase " + finished + " concluída! A próxima terá " + progress.level + " notas.", "success");
        }
      } else {
        setMessage("Correto! Continue: " + inputIndex + " nota(s) de " + progress.level + ".", "success");
      }
    }
    render();
  }

  function switchMode(next) {
    if (adBusy || !["memory", "ear", "free", "compose"].includes(next) || next === mode) return;
    cancelPlayback();
    mode = next;
    phase = "idle";
    inputIndex = 0;
    if (mode === "ear") ear = { status: "idle", round: 0, correct: 0, target: null, last: null };
    const introductions = {
      memory: progress.completed ? "🏆 Jogo terminado! Você completou todas as 1.000 fases." : "Fase " + progress.level + ": escute e repita a sequência.",
      ear: "Treine sua percepção: identifique dez notas pelo som.",
      free: "Toque como quiser! Este modo não registra derrotas.",
      compose: "Toque para adicionar notas à sua composição."
    };
    setMessage(introductions[mode]);
    render();
  }

  async function showAdBreak() {
    if (adBusy) return;
    adBusy = true;
    render();
    try {
      const adapter = window.SevenNoteAds && window.SevenNoteAds.showInterstitial;
      if (typeof adapter === "function") {
        // O provedor assume o controle da publicidade. true significa anúncio exibido.
        const shown = await Promise.race([
          Promise.resolve().then(() => adapter({
            placement: "after_two_losses",
            losses: progress.totalLosses,
            level: progress.level
          })).catch(() => false),
          delay(12000).then(() => false)
        ]);
        if (shown === true) {
          adBusy = false;
          render();
          return;
        }
      }
    } catch (error) {
      console.warn("Publicidade indisponível:", error);
    }
    lastFocused = document.activeElement;
    $("ad-modal").hidden = false;
    $("close-ad").focus();
  }

  function closeAdBreak() {
    if ($("ad-modal").hidden) return;
    $("ad-modal").hidden = true;
    adBusy = false;
    render();
    const focusBack = lastFocused && lastFocused.isConnected ? lastFocused : $("play-button");
    focusBack.focus();
    lastFocused = null;
  }

  function syncPreferences() {
    preferences = core.sanitizePreferences({
      volume: Number($("volume").value),
      tempo: $("tempo").value,
      instrument: $("instrument").value,
      showNames: $("show-names").checked
    });
    saveJSON(PREFS_KEY, preferences);
    $("volume-value").textContent = preferences.volume + "%";
  }

  function openSettings() {
    if (adBusy) return;
    cancelPlayback();
    render();
    $("settings-dialog").showModal();
  }

  function wipeAll() {
    if (!window.confirm("Apagar fases, recordes, pontos, ajustes e melodia? Esta ação não pode ser desfeita.")) return;
    cancelPlayback();
    progress = core.sanitizeProgress();
    preferences = core.sanitizePreferences();
    melody = [];
    ear = { status: "idle", round: 0, correct: 0, target: null, last: null };
    inputIndex = 0;
    phase = "idle";
    mode = "memory";
    saveProgress();
    storeMelody();
    $("volume").value = String(preferences.volume);
    $("tempo").value = preferences.tempo;
    $("instrument").value = preferences.instrument;
    $("show-names").checked = preferences.showNames;
    syncPreferences();
    $("settings-dialog").close();
    setMessage("Dados apagados. Você está novamente na fase 1.");
    render();
  }

  async function mainAction() {
    if (adBusy) return;
    if (mode === "memory") void startMemory();
    else if (mode === "ear") nextEar();
    else if (mode === "free") playScale();
    else if (mode === "compose" && melody.length) void playSequence([...melody], "compose");
  }

  async function secondaryAction() {
    if (adBusy) return;
    if (mode === "memory" && phase === "input") void startMemory();
    else if (mode === "ear" && ear.status === "input") {
      if (await prepareAudio()) tone(ear.target, 0.62);
    } else if (mode === "compose") undoMelody();
  }

  $("play-button").addEventListener("click", () => void mainAction());
  $("secondary-button").addEventListener("click", () => void secondaryAction());
  $("undo-button").addEventListener("click", undoMelody);
  $("clear-button").addEventListener("click", clearMelody);
  $("close-ad").addEventListener("click", closeAdBreak);
  $("finish-close").addEventListener("click", () => $("finish-dialog").close());
  $("finish-restart").addEventListener("click", () => {
    if (!window.confirm("Recomeçar as 1.000 fases? Seu recorde, pontos e derrotas serão preservados.")) return;
    cancelPlayback();
    $("finish-dialog").close();
    progress = { ...progress, level: 1, streak: 0, completed: false };
    phase = "idle"; mode = "memory"; inputIndex = 0;
    saveProgress();
    setMessage("Nova campanha iniciada: fase 1, nota Dó.");
    render();
  });
  $("help-button").addEventListener("click", () => {
    if (adBusy) return;
    cancelPlayback();
    render();
    $("help-dialog").showModal();
  });
  $("settings-button").addEventListener("click", openSettings);
  $("wipe-button").addEventListener("click", wipeAll);
  $("volume").value = String(preferences.volume);
  $("tempo").value = preferences.tempo;
  $("instrument").value = preferences.instrument;
  $("show-names").checked = preferences.showNames;
  $("volume-value").textContent = preferences.volume + "%";
  ["volume", "tempo", "instrument", "show-names"].forEach((id) => {
    $(id).addEventListener("input", syncPreferences);
    $(id).addEventListener("change", syncPreferences);
  });
  modeButtons.forEach((button) => button.addEventListener("click", () => switchMode(button.dataset.mode)));
  pads.forEach((pad) => pad.addEventListener("click", () => void pressNote(pad.dataset.note)));
  $("reset-button").addEventListener("click", () => {
    if (adBusy || !window.confirm("Voltar à fase 1? Seu recorde e suas derrotas serão preservados.")) return;
    cancelPlayback();
    progress = { ...progress, level: 1, streak: 0, completed: false };
    saveProgress();
    inputIndex = 0;
    phase = "idle";
    mode = "memory";
    setMessage("Você voltou à fase 1. Recordes e derrotas foram preservados.");
    render();
  });
  window.addEventListener("keydown", (event) => {
    if (!$("ad-modal").hidden) {
      if (event.key === "Escape") { event.preventDefault(); closeAdBreak(); }
      return;
    }
    if ($("settings-dialog").open || $("help-dialog").open || event.repeat ||
        event.ctrlKey || event.metaKey || event.altKey ||
        ["INPUT", "SELECT", "TEXTAREA"].includes(document.activeElement.tagName)) return;
    const note = core.NOTES.find((n) => n.keyboard === event.key);
    if (note) {
      event.preventDefault();
      void pressNote(note.id);
    } else if (event.code === "Space" && document.activeElement.tagName !== "BUTTON") {
      event.preventDefault();
      void mainAction();
    }
  });
  document.addEventListener("visibilitychange", () => {
    if (!document.hidden) return;
    cancelPlayback();
    render();
  });
  // pagehide também cobre navegação e entrada no cache de histórico (bfcache).
  // Não mantenha a sequência ativa ao sair da página, mesmo sem visibilitychange.
  window.addEventListener("pagehide", () => {
    cancelPlayback();
    render();
  });
  window.addEventListener("beforeinstallprompt", (event) => {
    event.preventDefault();
    deferredInstall = event;
    $("install-button").hidden = false;
  });
  $("install-button").addEventListener("click", async () => {
    if (!deferredInstall) return;
    const prompt = deferredInstall;
    deferredInstall = null;
    $("install-button").hidden = true;
    await prompt.prompt();
  });
  window.addEventListener("appinstalled", () => { $("install-button").hidden = true; deferredInstall = null; });
  if ("serviceWorker" in navigator && /^https?:$/.test(location.protocol)) {
    window.addEventListener("load", () => navigator.serviceWorker.register("./sw.js").catch(() => {}));
  }

  // Android TV / TV Box: D-pad moves the focus without a mouse or touchscreen.
  // ENTER/DPAD_CENTER activates the focused native HTML button.
  if (tvMode) {
    const focusable = () => {
      const modal = document.querySelector("#ad-modal:not([hidden])");
      const dialog = document.querySelector("dialog[open]");
      const scope = modal || dialog || document;
      return [...scope.querySelectorAll("button:not([disabled]), select:not([disabled]), input:not([disabled])")]
        .filter((element) => element.getClientRects().length > 0 && !element.closest("[hidden]"));
    };
    window.addEventListener("keydown", (event) => {
      const dir = {
        ArrowLeft: [-1, 0], ArrowRight: [1, 0],
        ArrowUp: [0, -1], ArrowDown: [0, 1]
      }[event.key];
      if (!dir) return;
      event.preventDefault();
      event.stopImmediatePropagation();
      const options = focusable();
      if (!options.length) return;
      const current = document.activeElement;
      if (!options.includes(current)) {
        const preferred = options.find((x) => x.id === "play-button") || options[0];
        preferred.focus();
        preferred.scrollIntoView({ block: "nearest" });
        return;
      }
      const origin = current.getBoundingClientRect();
      const cx = origin.left + origin.width / 2;
      const cy = origin.top + origin.height / 2;
      let best = null, score = Infinity;
      options.forEach((element) => {
        if (element === current) return;
        const rect = element.getBoundingClientRect();
        const dx = rect.left + rect.width / 2 - cx;
        const dy = rect.top + rect.height / 2 - cy;
        const along = dx * dir[0] + dy * dir[1];
        const cross = Math.abs(dx * dir[1] - dy * dir[0]);
        if (along <= 3) return;
        const cost = along + cross * 2.7;
        if (cost < score) { score = cost; best = element; }
      });
      if (best) {
        best.focus();
        best.scrollIntoView({ block: "nearest", behavior: "instant" });
      }
    }, true);
    $("play-button").focus();
  }

  render();
})();
