"use strict";
const test = require("node:test");
const assert = require("node:assert/strict");
const {
  NOTES, OPENING, sequenceForLevel, evaluateNote, sanitizeProgress,
  advanceLevel, registerLoss, adDue, recordEarAnswer, recordEarSession,
  chooseEarNote, sanitizeMelody, sanitizePreferences, achievements, MAX_MELODY, MAX_LEVEL
} = require("../game-core.js");

test("há exatamente sete notas naturais com IDs e frequências exclusivos", () => {
  assert.deepEqual(NOTES.map((n) => n.id), ["do", "re", "mi", "fa", "sol", "la", "si"]);
  assert.equal(new Set(NOTES.map((n) => n.frequency)).size, 7);
  assert.deepEqual(NOTES.map((n) => n.keyboard), ["1", "2", "3", "4", "5", "6", "7"]);
});

test("as nove primeiras fases seguem exatamente as notas definidas", () => {
  const esperado = [
    ["do"],
    ["do", "si"],
    ["do", "si", "re"],
    ["do", "si", "re", "mi"],
    ["do", "si", "re", "mi", "do"],
    ["do", "si", "re", "mi", "do", "do"],
    ["do", "si", "re", "mi", "do", "do", "re"],
    ["do", "si", "re", "mi", "do", "do", "re", "sol"],
    ["do", "si", "re", "mi", "do", "do", "re", "sol", "fa"]
  ];
  esperado.forEach((seq, index) => assert.deepEqual(sequenceForLevel(index + 1), seq));
  assert.deepEqual([...OPENING], esperado[8]);
});

test("toda fase soma uma nota e preserva as anteriores", () => {
  for (let level = 1; level < MAX_LEVEL; level += 1) {
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
  for (const level of [0, -1, 1.2, NaN, 1001, 10001, "7"]) {
    assert.throws(() => sequenceForLevel(level), RangeError);
  }
  assert.throws(() => evaluateNote([], 0, "mi"), TypeError);
  assert.throws(() => evaluateNote(["mi"], 2, "mi"), TypeError);
  assert.throws(() => evaluateNote(["mi"], 0, "desconhecida"), TypeError);
});

test("entrada correta intermediária, vitória e erro funcionam", () => {
  const seq = ["do", "si"];
  assert.deepEqual(evaluateNote(seq, 0, "do"), { result: "correct", nextIndex: 1 });
  assert.deepEqual(evaluateNote(seq, 1, "si"), { result: "win", nextIndex: 2 });
  assert.deepEqual(evaluateNote(seq, 1, "re"), { result: "loss", expected: "si", index: 1 });
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
  assert.equal(achievements({ level: MAX_LEVEL, best: MAX_LEVEL, completed: true }).filter((a) => a.id === "final" && a.unlocked).length, 1);
  assert.equal(achievements({ level: MAX_LEVEL, best: MAX_LEVEL, completed: false }).find((a) => a.id === "final").unlocked, false);
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

test("exatamente 1000 sequências e nenhuma fase 1001", () => {
  assert.equal(MAX_LEVEL, 1000);
  assert.equal(sequenceForLevel(1).length, 1);
  const last = sequenceForLevel(MAX_LEVEL);
  assert.equal(last.length, MAX_LEVEL);
  assert.deepEqual(last.slice(0, OPENING.length), [...OPENING]);
  assert.deepEqual(last.slice(0, MAX_LEVEL - 1), sequenceForLevel(MAX_LEVEL - 1));
  assert.throws(() => sequenceForLevel(MAX_LEVEL + 1), RangeError);
});

test("a milésima vitória conclui definitivamente a campanha", () => {
  const before = sanitizeProgress({
    level: 1000, best: 1000, completed: false, totalWins: 999, totalLosses: 10,
    points: 85000, bestStreak: 80, streak: 3
  });
  assert.equal(before.completed, false);
  const finished = advanceLevel(before);
  assert.equal(finished.level, 1000);
  assert.equal(finished.best, 1000);
  assert.equal(finished.completed, true);
  assert.equal(finished.totalWins, 1000);
  assert.equal(finished.totalLosses, 10);
  assert.deepEqual(advanceLevel(finished), finished, "não pode haver vitória duplicada");
  assert.deepEqual(sanitizeProgress(finished), finished, "vitória final continua salva");
});

test("conclusão inválida é corrigida, mas novo início ainda pode preservar o recorde", () => {
  assert.equal(sanitizeProgress({level: 999, completed: true}).completed, false);
  assert.equal(sanitizeProgress({level: 1001, completed: true}).completed, false);
  const restarted = sanitizeProgress({level: 1, best: 1000, completed: false, totalWins: 1000});
  assert.equal(restarted.level, 1);
  assert.equal(restarted.best, 1000);
  assert.equal(restarted.completed, false);
});

test("service worker limita o cache a arquivos próprios e não captura outras rotas", () => {
  const fs = require("node:fs");
  const path = require("node:path");
  const sw = fs.readFileSync(path.join(__dirname, "..", "sw.js"), "utf8");
  assert.match(sw, /const APP_URLS = new Set\(/);
  assert.match(sw, /if \(url\.search \|\| !APP_URLS\.has\(url\.href\)\) return;/);
  assert.match(sw, /response\.type === "basic"/);
  assert.doesNotMatch(sw, /caches\.match\("\.\/index\.html"\)/);
});

test("contadores próximos do limite seguro não transbordam nem apagam progresso salvo", () => {
  const cap = Number.MAX_SAFE_INTEGER - 1;
  const near = sanitizeProgress({
    level: 1000, best: 1000, totalWins: cap, totalLosses: cap,
    earPlayed: cap, earCorrect: cap
  });
  const won = advanceLevel(near);
  assert.equal(won.totalWins, cap);
  assert.equal(won.completed, true);
  const lost = registerLoss(near);
  assert.equal(lost.totalLosses, cap);
  assert.equal(sanitizeProgress(lost).totalLosses, cap);
  const heard = recordEarAnswer(near, true);
  assert.equal(heard.earPlayed, cap);
  assert.equal(heard.earCorrect, cap);
  assert.equal(sanitizeProgress(heard).earPlayed, cap);
  assert.equal(sanitizeProgress(heard).earCorrect, cap);
  const missed = recordEarAnswer(near, false);
  assert.equal(missed.totalLosses, cap);
  assert.equal(missed.earPlayed, cap);
});
