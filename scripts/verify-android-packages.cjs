#!/usr/bin/env node
"use strict";
// Verifica os quatro binários Android/TV contra o manifesto do mesmo workflow.
const fs = require("node:fs");
const path = require("node:path");
const crypto = require("node:crypto");
const directory = path.resolve(process.argv[2] || ".");
const names = ["app-mobile-debug.apk", "app-tv-debug.apk", "app-mobile-release.aab", "app-tv-release.aab"];
function verify(dir) {
  const manifestPath = path.join(dir, "SHA256SUMS.txt");
  const manifestStat = fs.lstatSync(manifestPath);
  if (!manifestStat.isFile() || manifestStat.size > 512 || manifestStat.size === 0) {
    throw new Error("Manifesto SHA256 inválido");
  }
  const manifest = fs.readFileSync(manifestPath, "utf8");
  const lines = manifest.trim().split(/\r?\n/);
  if (lines.length !== names.length) throw new Error("Manifesto deve conter exatamente quatro hashes");
  const entries = new Map();
  for (const line of lines) {
    const match = /^([a-f0-9]{64})  ([A-Za-z0-9.-]+)$/.exec(line);
    if (!match || !names.includes(match[2]) || entries.has(match[2])) {
      throw new Error("Entrada inválida ou duplicada no manifesto");
    }
    entries.set(match[2], match[1]);
  }
  for (const name of names) {
    const file = path.join(dir, name);
    const stat = fs.lstatSync(file);
    if (!stat.isFile() || stat.size === 0) throw new Error("Pacote inválido: " + name);
    const actual = crypto.createHash("sha256").update(fs.readFileSync(file)).digest("hex");
    if (actual !== entries.get(name)) throw new Error("Hash divergente: " + name);
  }
  // Só anunciar sucesso depois que TODOS os quatro pacotes forem validados.
  for (const name of names) process.stdout.write("OK " + name + "\n");
}
if (require.main === module) {
  try { verify(directory); } catch (error) { console.error(error.message); process.exitCode = 1; }
}
module.exports = { verify };
