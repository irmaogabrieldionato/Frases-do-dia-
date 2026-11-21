/* 7-Note Musical Game — regras puras, sem dependências */
(function (root) {
  "use strict";

  const NOTES = Object.freeze([
    Object.freeze({ id: "do", name: "Dó", frequency: 261.63, keyboard: "1" }),
    Object.freeze({ id: "re", name: "Ré", frequency: 293.66, keyboard: "2" }),
    Object.freeze({ id: "mi", name: "Mi", frequency: 329.63, keyboard: "3" }),
    Object.freeze({ id: "fa", name: "Fá", frequency: 349.23, keyboard: "4" }),
    Object.freeze({ id: "sol", name: "Sol", frequency: 392.00, keyboard: "5" }),
    Object.freeze({ id: "la", name: "Lá", frequency: 440.00, keyboard: "6" }),
    Object.freeze({ id: "si", name: "Si", frequency: 493.88, keyboard: "7" })
  ]);

  // As primeiras sete fases são fixas, de acordo com o desenho original.
  const OPENING = Object.freeze(["mi", "do", "sol", "do", "do", "re", "si"]);
  const VALID_NOTES = new Set(NOTES.map((note) => note.id));

  function sequenceForLevel(level) {
    if (!Number.isSafeInteger(level) || level < 1 || level > 10000) {
      throw new RangeError("A fase deve ser um inteiro entre 1 e 10000.");
    }
    const result = OPENING.slice(0, level);
    // Após a fase 7, cada nova fase incorpora mais uma nota. O gerador é
    // determinístico, para o desafio não mudar durante uma tentativa.
    let seed = 20261120;
    while (result.length < level) {
      seed = (Math.imul(seed, 1664525) + 1013904223) >>> 0;
      result.push(NOTES[seed % NOTES.length].id);
    }
    return result;
  }

  function evaluateNote(sequence, index, pressed) {
    if (!Array.isArray(sequence) || !sequence.length ||
        !Number.isSafeInteger(index) || index < 0 || index >= sequence.length ||
        !VALID_NOTES.has(pressed)) {
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

  function sanitizeProgress(raw) {
    const source = raw && typeof raw === "object" ? raw : {};
    const number = (value, fallback, min, max) =>
      Number.isSafeInteger(value) && value >= min && value <= max ? value : fallback;
    const level = number(source.level, 1, 1, 10000);
    const best = Math.max(level, number(source.best, 1, 1, 10000));
    return {
      level,
      best,
      totalWins: number(source.totalWins, 0, 0, Number.MAX_SAFE_INTEGER),
      totalLosses: number(source.totalLosses, 0, 0, Number.MAX_SAFE_INTEGER)
    };
  }

  function advanceLevel(raw) {
    const progress = sanitizeProgress(raw);
    const level = Math.min(progress.level + 1, 10000);
    return {
      ...progress,
      level,
      best: Math.max(progress.best, level),
      totalWins: progress.totalWins + 1
    };
  }

  function registerLoss(raw) {
    const progress = sanitizeProgress(raw);
    return { ...progress, totalLosses: progress.totalLosses + 1 };
  }

  function adDue(totalLosses) {
    return Number.isSafeInteger(totalLosses) &&
      totalLosses > 0 && totalLosses % 2 === 0;
  }

  const api = Object.freeze({
    NOTES, OPENING, sequenceForLevel, evaluateNote,
    sanitizeProgress, advanceLevel, registerLoss, adDue
  });
  if (typeof module !== "undefined" && module.exports) {
    module.exports = api;
  }
  root.SevenNoteCore = api;
})(typeof globalThis !== "undefined" ? globalThis : this);
