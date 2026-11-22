/* 7-Note Musical Game — regras determinísticas e sem bibliotecas */
(function (root) {
  "use strict";

  const NOTES = Object.freeze([
    Object.freeze({ id: "do", name: "Dó", frequency: 261.63, keyboard: "1", color: "#fa8b86" }),
    Object.freeze({ id: "re", name: "Ré", frequency: 293.66, keyboard: "2", color: "#ffbd73" }),
    Object.freeze({ id: "mi", name: "Mi", frequency: 329.63, keyboard: "3", color: "#f8d977" }),
    Object.freeze({ id: "fa", name: "Fá", frequency: 349.23, keyboard: "4", color: "#9eddb1" }),
    Object.freeze({ id: "sol", name: "Sol", frequency: 392.00, keyboard: "5", color: "#86d1f5" }),
    Object.freeze({ id: "la", name: "Lá", frequency: 440.00, keyboard: "6", color: "#bea6f5" }),
    Object.freeze({ id: "si", name: "Si", frequency: 493.88, keyboard: "7", color: "#eea7d7" })
  ]);
  const OPENING = Object.freeze(["do", "si", "re", "mi", "do", "do", "re", "sol", "fa"]);
  const VALID_NOTES = new Set(NOTES.map((n) => n.id));
  const MAX_LEVEL = 1000;
  const MAX_MELODY = 64;
  const EAR_ROUNDS = 10;
  const PREFS = Object.freeze({
    tempo: "normal",
    instrument: "soft",
    volume: 70,
    showNames: false
  });
  const TEMPOS = Object.freeze({ slow: 800, normal: 580, fast: 400 });
  const INSTRUMENTS = Object.freeze(["soft", "bell", "retro"]);

  function sequenceForLevel(level) {
    if (!Number.isSafeInteger(level) || level < 1 || level > MAX_LEVEL) {
      throw new RangeError("A fase deve ser um inteiro entre 1 e " + MAX_LEVEL + ".");
    }
    const result = OPENING.slice(0, level);
    let seed = 20261120;
    while (result.length < level) {
      seed = (Math.imul(seed, 1664525) + 1013904223) >>> 0;
      result.push(NOTES[seed % NOTES.length].id);
    }
    return result;
  }

  function evaluateNote(sequence, index, pressed) {
    if (!Array.isArray(sequence) || sequence.length === 0 ||
        !Number.isSafeInteger(index) || index < 0 || index >= sequence.length ||
        !VALID_NOTES.has(pressed) || !sequence.every((n) => VALID_NOTES.has(n))) {
      throw new TypeError("Tentativa ou sequência inválida.");
    }
    if (sequence[index] !== pressed) {
      return { result: "loss", expected: sequence[index], index };
    }
    if (index + 1 === sequence.length) {
      return { result: "win", nextIndex: index + 1 };
    }
    return { result: "correct", nextIndex: index + 1 };
  }

  function safeInt(value, defaultValue, min, max) {
    return Number.isSafeInteger(value) && value >= min && value <= max
      ? value : defaultValue;
  }

  function sanitizeProgress(raw) {
    const input = raw && typeof raw === "object" ? raw : {};
    const level = safeInt(input.level, 1, 1, MAX_LEVEL);
    const best = Math.max(level, safeInt(input.best, 1, 1, MAX_LEVEL));
    const streak = safeInt(input.streak, 0, 0, MAX_LEVEL);
    return {
      level,
      best,
      completed: input.completed === true && level === MAX_LEVEL,
      totalWins: safeInt(input.totalWins, 0, 0, Number.MAX_SAFE_INTEGER - 1),
      totalLosses: safeInt(input.totalLosses, 0, 0, Number.MAX_SAFE_INTEGER - 1),
      points: safeInt(input.points, 0, 0, Number.MAX_SAFE_INTEGER - 1),
      streak,
      bestStreak: Math.max(streak, safeInt(input.bestStreak, 0, 0, MAX_LEVEL)),
      earBest: safeInt(input.earBest, 0, 0, EAR_ROUNDS),
      earPlayed: safeInt(input.earPlayed, 0, 0, Number.MAX_SAFE_INTEGER - 1),
      earCorrect: safeInt(input.earCorrect, 0, 0, Number.MAX_SAFE_INTEGER - 1)
    };
  }

  function advanceLevel(raw) {
    const progress = sanitizeProgress(raw);
    if (progress.completed) return progress; // impede pontuar novamente após o fim
    const next = Math.min(MAX_LEVEL, progress.level + 1);
    const streak = Math.min(MAX_LEVEL, progress.streak + 1);
    return {
      ...progress,
      level: next,
      best: Math.max(progress.best, next),
      completed: progress.level === MAX_LEVEL,
      totalWins: Math.min(Number.MAX_SAFE_INTEGER - 1, progress.totalWins + 1),
      points: Math.min(Number.MAX_SAFE_INTEGER - 1, progress.points + progress.level * 20 + streak * 5),
      streak,
      bestStreak: Math.max(progress.bestStreak, streak)
    };
  }

  function registerLoss(raw) {
    const progress = sanitizeProgress(raw);
    return { ...progress, totalLosses: Math.min(Number.MAX_SAFE_INTEGER - 1, progress.totalLosses + 1), streak: 0 };
  }

  function recordEarAnswer(raw, isCorrect) {
    if (typeof isCorrect !== "boolean") throw new TypeError("Resposta inválida.");
    const progress = sanitizeProgress(raw);
    const next = {
      ...progress,
      earPlayed: Math.min(Number.MAX_SAFE_INTEGER - 1, progress.earPlayed + 1),
      earCorrect: Math.min(Number.MAX_SAFE_INTEGER - 1, progress.earCorrect + (isCorrect ? 1 : 0)),
      points: Math.min(Number.MAX_SAFE_INTEGER - 1, progress.points + (isCorrect ? 10 : 0))
    };
    return isCorrect ? next : registerLoss(next);
  }

  function recordEarSession(raw, correctCount) {
    if (!Number.isSafeInteger(correctCount) || correctCount < 0 || correctCount > EAR_ROUNDS) {
      throw new RangeError("Pontuação auditiva inválida.");
    }
    const progress = sanitizeProgress(raw);
    return { ...progress, earBest: Math.max(progress.earBest, correctCount) };
  }

  function chooseEarNote(previous, rng) {
    const rand = typeof rng === "function" ? rng : Math.random;
    const candidates = NOTES.filter((n) => n.id !== previous);
    const value = rand();
    if (typeof value !== "number" || !Number.isFinite(value) || value < 0 || value >= 1) {
      throw new RangeError("Gerador aleatório inválido.");
    }
    return candidates[Math.floor(value * candidates.length)].id;
  }

  function sanitizeMelody(raw) {
    if (!Array.isArray(raw)) return [];
    return raw.filter((note) => VALID_NOTES.has(note)).slice(0, MAX_MELODY);
  }

  function sanitizePreferences(raw) {
    const prefs = raw && typeof raw === "object" ? raw : {};
    return {
      tempo: Object.hasOwn(TEMPOS, prefs.tempo) ? prefs.tempo : PREFS.tempo,
      instrument: INSTRUMENTS.includes(prefs.instrument) ? prefs.instrument : PREFS.instrument,
      volume: safeInt(prefs.volume, PREFS.volume, 0, 100),
      showNames: typeof prefs.showNames === "boolean" ? prefs.showNames : PREFS.showNames
    };
  }

  function adDue(totalLosses) {
    return Number.isSafeInteger(totalLosses) && totalLosses > 0 && totalLosses % 2 === 0;
  }

  function achievements(raw) {
    const p = sanitizeProgress(raw);
    return [
      { id: "first", icon: "♪", title: "Primeira vitória", description: "Conclua uma fase.", unlocked: p.totalWins >= 1 },
      { id: "seven", icon: "♫", title: "Sete notas", description: "Chegue à fase 8.", unlocked: p.best >= 8 },
      { id: "streak", icon: "✦", title: "Em harmonia", description: "Vença 5 fases consecutivas.", unlocked: p.bestStreak >= 5 },
      { id: "ear", icon: "◉", title: "Ouvido absoluto?", description: "Acerte 10 notas em um treino.", unlocked: p.earBest >= EAR_ROUNDS },
      { id: "master", icon: "★", title: "Maestro", description: "Alcance a fase 25.", unlocked: p.best >= 25 },
      { id: "final", icon: "🏆", title: "Mil sequências", description: "Conclua a fase 1.000 e termine o jogo.", unlocked: p.completed }
    ];
  }

  const api = Object.freeze({
    NOTES, OPENING, MAX_LEVEL, MAX_MELODY, EAR_ROUNDS, PREFS, TEMPOS,
    INSTRUMENTS, sequenceForLevel, evaluateNote, sanitizeProgress,
    advanceLevel, registerLoss, recordEarAnswer, recordEarSession,
    chooseEarNote, sanitizeMelody, sanitizePreferences, adDue, achievements
  });
  if (typeof module !== "undefined" && module.exports) module.exports = api;
  root.SevenNoteCore = api;
})(typeof globalThis !== "undefined" ? globalThis : this);
