"use strict";
const test = require("node:test");
const assert = require("node:assert/strict");
const {
  NOTES, OPENING, sequenceForLevel, evaluateNote, sanitizeProgress,
  advanceLevel, registerLoss, adDue
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
  assert.deepEqual(first, { level: 1, best: 1, totalWins: 0, totalLosses: 0 });
  const second = advanceLevel(first);
  assert.deepEqual(second, { level: 2, best: 2, totalWins: 1, totalLosses: 0 });
  const third = advanceLevel({ level: 2, best: 12, totalWins: 4, totalLosses: 7 });
  assert.deepEqual(third, { level: 3, best: 12, totalWins: 5, totalLosses: 7 });
});

test("derrota mantém a fase, incrementa o total e não perde recorde", () => {
  const progress = { level: 3, best: 9, totalWins: 2, totalLosses: 3 };
  assert.deepEqual(registerLoss(progress), { ...progress, totalLosses: 4 });
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
  assert.deepEqual(sanitizeProgress({ level: -3, best: "99", totalLosses: -1, totalWins: NaN }), {
    level: 1, best: 1, totalWins: 0, totalLosses: 0
  });
  assert.equal(sanitizeProgress({ level: 4, best: 1 }).best, 4);
});
