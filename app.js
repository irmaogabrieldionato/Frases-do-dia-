/* 7-Note Musical Game — interface e som via Web Audio */
(() => {
  "use strict";
  const core = window.SevenNoteCore;
  if (!core) throw new Error("game-core.js não foi carregado.");

  const $ = (id) => document.getElementById(id);
  const STORAGE_KEY = "seven-note-musical-game:v1";
  const pads = Array.from(document.querySelectorAll("[data-note]"));
  const noteById = new Map(core.NOTES.map((note) => [note.id, note]));
  let progress = restoreProgress();
  let phaseState = "idle"; // idle, playing, input, won, lost
  let mode = "memory"; // memory ou free
  let inputIndex = 0;
  let playbackEpoch = 0;
  let soundContext = null;
  let adBusy = false;
  let message = "Ouça a sequência e repita as notas na mesma ordem.";
  let messageKind = "default";

  function restoreProgress() {
    try { return core.sanitizeProgress(JSON.parse(localStorage.getItem(STORAGE_KEY))); }
    catch (_) { return core.sanitizeProgress(); }
  }

  function saveProgress() {
    try { localStorage.setItem(STORAGE_KEY, JSON.stringify(progress)); }
    catch (_) { /* O jogo funciona mesmo sem armazenamento local. */ }
  }

  function setMessage(text, kind = "default") {
    message = text;
    messageKind = kind;
  }

  function delay(ms) {
    return new Promise((resolve) => window.setTimeout(resolve, ms));
  }

  async function prepareAudio() {
    const Context = window.AudioContext || window.webkitAudioContext;
    if (!Context) {
      setMessage("Este navegador não oferece áudio Web Audio. Tente um navegador atualizado.", "error");
      render();
      return false;
    }
    try {
      if (!soundContext) soundContext = new Context();
      if (soundContext.state === "suspended") await soundContext.resume();
      return soundContext.state === "running";
    } catch (_) {
      setMessage("Não foi possível iniciar o áudio. Verifique o volume e tente novamente.", "error");
      render();
      return false;
    }
  }

  function playTone(noteId, duration = 0.42) {
    if (!soundContext || soundContext.state !== "running") return;
    const note = noteById.get(noteId);
    if (!note) return;

    const start = soundContext.currentTime + 0.005;
    const amp = soundContext.createGain();
    amp.gain.setValueAtTime(0.0001, start);
    amp.gain.exponentialRampToValueAtTime(0.16, start + 0.025);
    amp.gain.exponentialRampToValueAtTime(0.0001, start + duration);
    amp.connect(soundContext.destination);

    const body = soundContext.createOscillator();
    body.type = "triangle";
    body.frequency.value = note.frequency;
    body.connect(amp);
    body.start(start);
    body.stop(start + duration + 0.02);

    const shimmerGain = soundContext.createGain();
    shimmerGain.gain.value = 0.13;
    const shimmer = soundContext.createOscillator();
    shimmer.type = "sine";
    shimmer.frequency.value = note.frequency * 2;
    shimmer.connect(shimmerGain);
    shimmerGain.connect(amp);
    shimmer.start(start);
    shimmer.stop(start + duration + 0.02);
  }

  function flashPad(noteId) {
    const pad = pads.find((item) => item.dataset.note === noteId);
    if (!pad) return;
    pad.classList.remove("active");
    void pad.offsetWidth;
    pad.classList.add("active");
    window.setTimeout(() => pad.classList.remove("active"), 420);
  }

  function render() {
    const sequence = core.sequenceForLevel(progress.level);
    $("level").textContent = String(progress.level);
    $("best").textContent = String(progress.best);
    $("losses").textContent = String(progress.totalLosses);
    $("total-notes").textContent = String(sequence.length);
    $("message").textContent = message;
    $("message").dataset.kind = messageKind;
    $("step-progress").textContent = mode === "free"
      ? "Toque qualquer nota para improvisar."
      : (phaseState === "input" ? "Sua vez: " + inputIndex + " de " + sequence.length + " notas"
        : "Sequência com " + sequence.length + (sequence.length === 1 ? " nota" : " notas"));
    $("progress-fill").style.width = (phaseState === "won" ? 100
      : phaseState === "input" ? inputIndex / sequence.length * 100 : 0) + "%";

    const replay = $("play-button");
    replay.disabled = phaseState === "playing" || adBusy || mode === "free";
    replay.textContent = phaseState === "playing" ? "Tocando..." :
      phaseState === "won" ? "Começar próxima fase" :
      phaseState === "lost" ? "Tentar novamente" :
      phaseState === "input" ? "Ouvir de novo" : "Ouvir sequência";
    $("free-button").textContent = mode === "free" ? "Voltar ao desafio" : "Modo livre";
    $("mode-label").textContent = mode === "free" ? "Teclado livre" : "Desafio de memória";
    $("instructions").textContent = mode === "free"
      ? "Explore as sete notas sem perder vidas ou ver anúncios."
      : "Cada fase acrescenta uma nota. Ouça com atenção e repita a sequência.";
    pads.forEach((pad) => {
      pad.disabled = adBusy || phaseState === "playing";
      pad.setAttribute("aria-label", "Tocar " + noteById.get(pad.dataset.note).name);
    });
  }

  async function startPhase() {
    if (mode !== "memory" || phaseState === "playing" || adBusy) return;
    if (!(await prepareAudio())) return;

    const epoch = ++playbackEpoch;
    phaseState = "playing";
    inputIndex = 0;
    const sequence = core.sequenceForLevel(progress.level);
    setMessage("Escute com atenção...", "default");
    render();

    await delay(330);
    for (const noteId of sequence) {
      if (epoch !== playbackEpoch || mode !== "memory") return;
      playTone(noteId);
      flashPad(noteId);
      await delay(550);
    }
    if (epoch !== playbackEpoch || mode !== "memory") return;
    phaseState = "input";
    setMessage("Sua vez! Repita as notas na ordem em que foram tocadas.", "default");
    render();
  }

  function closePlaceholder() {
    $("ad-modal").hidden = true;
    adBusy = false;
    render();
    $("play-button").focus();
  }

  async function showAdBreak() {
    // Adaptador público: uma rede de anúncios pode implementar
    // window.SevenNoteAds.showInterstitial({placement, losses, level}).
    // Se não existir, mostramos apenas uma demonstração identificada.
    if (adBusy) return;
    adBusy = true;
    render();
    try {
      const adapter = window.SevenNoteAds && window.SevenNoteAds.showInterstitial;
      if (typeof adapter === "function") {
        const delivered = await adapter({
          placement: "after_two_losses",
          losses: progress.totalLosses,
          level: progress.level
        });
        if (delivered === true) {
          adBusy = false;
          render();
          return;
        }
      }
    } catch (error) {
      console.warn("Anúncio indisponível:", error);
    }
    $("ad-modal").hidden = false;
    $("close-ad").focus();
  }

  async function pressNote(noteId) {
    if (adBusy || phaseState === "playing") return;
    if (!(await prepareAudio())) return;
    playTone(noteId);
    flashPad(noteId);

    if (mode === "free") {
      setMessage("Você tocou " + noteById.get(noteId).name + ".", "default");
      render();
      return;
    }
    if (phaseState !== "input") return;

    const sequence = core.sequenceForLevel(progress.level);
    const verdict = core.evaluateNote(sequence, inputIndex, noteId);
    if (verdict.result === "loss") {
      const expected = noteById.get(verdict.expected).name;
      progress = core.registerLoss(progress);
      saveProgress();
      phaseState = "lost";
      inputIndex = 0;
      setMessage("Você errou! A nota esperada era " + expected + ". Tente a fase novamente.", "error");
      render();
      if (core.adDue(progress.totalLosses)) void showAdBreak();
      return;
    }

    inputIndex = verdict.nextIndex;
    if (verdict.result === "win") {
      const completed = progress.level;
      progress = core.advanceLevel(progress);
      saveProgress();
      phaseState = "won";
      setMessage("Parabéns! Você venceu a fase " + completed + ". A próxima tem " + progress.level + " notas.", "success");
      render();
      return;
    }
    setMessage("Correto! Continue: " + inputIndex + " de " + sequence.length + ".", "success");
    render();
  }

  $("play-button").addEventListener("click", startPhase);
  $("free-button").addEventListener("click", () => {
    playbackEpoch += 1;
    mode = mode === "free" ? "memory" : "free";
    phaseState = "idle";
    inputIndex = 0;
    setMessage(mode === "free"
      ? "Explore as notas livremente. Esse modo não registra derrotas."
      : "Voltou ao desafio. Ouça a sequência para iniciar.");
    render();
  });
  $("reset-button").addEventListener("click", () => {
    if (!window.confirm("Voltar à fase 1? Seu recorde e o total de derrotas serão preservados.")) return;
    playbackEpoch += 1;
    progress = { ...progress, level: 1 };
    phaseState = "idle";
    inputIndex = 0;
    mode = "memory";
    saveProgress();
    setMessage("Progresso reiniciado na fase 1. Seu recorde foi preservado.");
    render();
  });
  $("close-ad").addEventListener("click", closePlaceholder);
  pads.forEach((pad) => pad.addEventListener("click", () => void pressNote(pad.dataset.note)));
  window.addEventListener("keydown", (event) => {
    if (event.repeat || event.altKey || event.ctrlKey || event.metaKey) return;
    if (!$("ad-modal").hidden) {
      if (event.key === "Escape") closePlaceholder();
      return;
    }
    const note = core.NOTES.find((item) => item.keyboard === event.key);
    if (note) {
      event.preventDefault();
      void pressNote(note.id);
    } else if (event.code === "Space" && !["BUTTON", "INPUT"].includes(document.activeElement.tagName)) {
      event.preventDefault();
      void startPhase();
    }
  });
  render();
})();
