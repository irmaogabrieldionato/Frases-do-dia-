"use strict";
const test = require("node:test");
const assert = require("node:assert/strict");
const {
  NOTES, OPENING, sequenceForLevel, evaluateNote, sanitizeProgress,
  advanceLevel, registerLoss, adDue, recordEarAnswer, recordEarSession,
  chooseEarNote, sanitizeMelody, sanitizePreferences, achievements, MAX_MELODY
} = require("../game-core.js");

test("há exatamente sete notas naturais com IDs e frequências exclusivos", () => {
  assert.deepEqual(NOTES.map((n) => n.id), ["do", "re", "mi", "fa", "sol", "la", "si"]);
  assert.equal(new Set(NOTES.map((n) => n.frequency)).size, 7);
  assert.deepEqual(NOTES.map((n) => n.keyboard), ["1", "2", "3", "4", "5", "6", "7"]);
});

test("as sete primeiras fases seguem exatamente a sequência solicitada", () => {
  const esperado = [
    ["mi"], ["mi", "do"], ["mi", "do", "sol"],
    ["mi", "do", "sol", "do"], ["mi", "do", "sol", "do", "do"],
    ["mi", "do", "sol", "do", "do", "re"],
    ["mi", "do", "sol", "do", "do", "re", "si"]
  ];
  esperado.forEach((seq, index) => assert.deepEqual(sequenceForLevel(index + 1), seq));
  assert.deepEqual([...OPENING], esperado[6]);
});

test("toda fase soma uma nota e preserva as anteriores", () => {
  for (let level = 1; level < 100; level += 1) {
    const current = sequenceForLevel(level);
    const next = sequenceForLevel(level + 1);
    assert.equal(current.length, level);
    assert.equal(next.length, level + 1);
    assert.deepEqual(next.slice(0, level), current);
    assert.ok(next.every((note) => NOTES.some((n) => n.id === note)));
    assert.deepEqual(sequenceForLevel(level), current);
  }
});

test("sequências inválidas e fases inválidas são rejeitadas", () => {
  for (const level of [0, -1, 1.2, NaN, 10001, "7"]) {
    assert.throws(() => sequenceForLevel(level), RangeError);
  }
  assert.throws(() => evaluateNote([], 0, "mi"), TypeError);
  assert.throws(() => evaluateNote(["mi"], 2, "mi"), TypeError);
  assert.throws(() => evaluateNote(["mi"], 0, "desconhecida"), TypeError);
});

test("entrada correta intermediária, vitória e erro funcionam", () => {
  const seq = ["mi", "do"];
  assert.deepEqual(evaluateNote(seq, 0, "mi"), { result: "correct", nextIndex: 1 });
  assert.deepEqual(evaluateNote(seq, 1, "do"), { result: "win", nextIndex: 2 });
  assert.deepEqual(evaluateNote(seq, 1, "si"), { result: "loss", expected: "do", index: 1 });
});

test("avançar uma fase preserva recorde, soma vitória e não inventa derrota", () => {
  const first = sanitizeProgress();
  assert.deepEqual([first.level, first.best, first.totalWins, first.totalLosses, first.points], [1, 1, 0, 0, 0]);
  const second = advanceLevel(first);
  assert.deepEqual([second.level, second.best, second.totalWins, second.totalLosses, second.points, second.streak], [2, 2, 1, 0, 25, 1]);
  const third = advanceLevel({ level: 2, best: 12, totalWins: 4, totalLosses: 7 });
  assert.deepEqual([third.level, third.best, third.totalWins, third.totalLosses], [3, 12, 5, 7]);
});

test("derrota mantém a fase, incrementa o total e não perde recorde", () => {
  const progress = { level: 3, best: 9, totalWins: 2, totalLosses: 3 };
  const result = registerLoss(progress);
  assert.deepEqual([result.level, result.best, result.totalWins, result.totalLosses, result.streak], [3, 9, 2, 4, 0]);
});

test("anúncios são acionados somente nas derrotas 2, 4, 6...", () => {
  assert.deepEqual(Array.from({ length: 9 }, (_, i) => adDue(i)), [
    false, false, true, false, true, false, true, false, true
  ]);
  assert.equal(adDue(-2), false);
  assert.equal(adDue(2.5), false);
  assert.equal(adDue("2"), false);
});

test("dados persistidos corrompidos retornam ao estado seguro", () => {
  const result = sanitizeProgress({ level: -3, best: "99", totalLosses: -1, totalWins: NaN });
  assert.deepEqual([result.level, result.best, result.totalWins, result.totalLosses], [1, 1, 0, 0]);
  assert.equal(sanitizeProgress({ level: 4, best: 1 }).best, 4);
});


test("a primeira derrota não mostra anúncio; a segunda mostra", () => {
  let progress = sanitizeProgress();
  progress = registerLoss(progress);
  assert.equal(progress.totalLosses, 1);
  assert.equal(adDue(progress.totalLosses), false);
  progress = registerLoss(progress);
  assert.equal(progress.totalLosses, 2);
  assert.equal(adDue(progress.totalLosses), true);
  progress = advanceLevel(progress);
  assert.equal(adDue(progress.totalLosses), true);
  progress = registerLoss(progress);
  assert.equal(adDue(progress.totalLosses), false);
});

test("pontuação e sequência de vitórias são acumuladas e interrompidas pela derrota", () => {
  let progress = sanitizeProgress();
  progress = advanceLevel(progress);
  progress = advanceLevel(progress);
  assert.equal(progress.streak, 2);
  assert.equal(progress.bestStreak, 2);
  assert.equal(progress.points, 25 + 50);
  progress = registerLoss(progress);
  assert.equal(progress.streak, 0);
  assert.equal(progress.bestStreak, 2);
  progress = advanceLevel(progress);
  assert.equal(progress.streak, 1);
  assert.equal(progress.bestStreak, 2);
});

test("treino auditivo registra acertos, erros e recordes sem avançar fases", () => {
  let progress = sanitizeProgress();
  progress = recordEarAnswer(progress, true);
  assert.equal(progress.earPlayed, 1);
  assert.equal(progress.earCorrect, 1);
  assert.equal(progress.points, 10);
  assert.equal(progress.level, 1);
  progress = recordEarAnswer(progress, false);
  assert.equal(progress.earPlayed, 2);
  assert.equal(progress.earCorrect, 1);
  assert.equal(progress.totalLosses, 1);
  progress = recordEarSession(progress, 8);
  assert.equal(progress.earBest, 8);
  progress = recordEarSession(progress, 5);
  assert.equal(progress.earBest, 8);
  assert.throws(() => recordEarSession(progress, 11), RangeError);
  assert.throws(() => recordEarAnswer(progress, "sim"), TypeError);
});

test("cada nota do teste de ouvido tem alternativa diferente da anterior", () => {
  const first = chooseEarNote(null, () => 0);
  assert.equal(first, "do");
  const second = chooseEarNote("do", () => 0);
  assert.equal(second, "re");
  for (const previous of NOTES.map((n) => n.id)) {
    const current = chooseEarNote(previous, () => 0.999);
    assert.notEqual(current, previous);
    assert.ok(NOTES.some((n) => n.id === current));
  }
  assert.throws(() => chooseEarNote(null, () => 1), RangeError);
  assert.throws(() => chooseEarNote(null, () => -0.1), RangeError);
});

test("o compositor armazena apenas notas válidas e limita a 64", () => {
  assert.deepEqual(sanitizeMelody(["mi", "do", "outro", "sol"]), ["mi", "do", "sol"]);
  assert.deepEqual(sanitizeMelody(null), []);
  assert.equal(sanitizeMelody(Array(100).fill("do")).length, MAX_MELODY);
});

test("ajustes incorretos são substituídos por padrões seguros", () => {
  assert.deepEqual(sanitizePreferences(), { tempo: "normal", instrument: "soft", volume: 70, showNames: false });
  assert.deepEqual(sanitizePreferences({ tempo: "fast", instrument: "bell", volume: 0, showNames: true }),
    { tempo: "fast", instrument: "bell", volume: 0, showNames: true });
  assert.deepEqual(sanitizePreferences({ tempo: "extreme", instrument: "noise", volume: 400, showNames: "yes" }),
    { tempo: "normal", instrument: "soft", volume: 70, showNames: false });
});

test("conquistas acompanham estatísticas reais", () => {
  const none = achievements({});
  assert.equal(none.filter((a) => a.unlocked).length, 0);
  const all = achievements({
    level: 25, best: 25, totalWins: 24, streak: 5, bestStreak: 5,
    earBest: 10
  });
  assert.equal(all.filter((a) => a.unlocked).length, 5);
  assert.ok(all.every((a) => a.title && a.description));
});

test("arquivos da PWA estão presentes e o HTML conecta os quatro modos", () => {
  const fs = require("node:fs");
  const path = require("node:path");
  const root = path.join(__dirname, "..");
  const file = (name) => fs.readFileSync(path.join(root, name), "utf8");
  const html = file("index.html");
  const manifest = JSON.parse(file("manifest.webmanifest"));
  const worker = file("sw.js");
  const app = file("app.js");
  for (const mode of ["memory", "ear", "free", "compose"]) {
    assert.ok(html.includes('data-mode="' + mode + '"'), mode);
  }
  for (const asset of ["game-core.js", "app.js", "styles.css", "manifest.webmanifest", "icon.svg"]) {
    assert.ok(file(asset).length > 10, asset);
    assert.ok(worker.includes(asset), asset);
  }
  assert.equal(manifest.display, "standalone");
  assert.match(html, /id="ad-modal"/);
  assert.match(app, /core\.adDue\(progress\.totalLosses\)/);
});
