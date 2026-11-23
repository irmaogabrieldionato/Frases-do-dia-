"use strict";
/* Teste end-to-end da interface com Playwright e servidor HTTP local. */
const assert = require("node:assert/strict");
const fs = require("node:fs");
const http = require("node:http");
const path = require("node:path");
const { chromium, webkit } = require("playwright");

const root = path.resolve(__dirname, "..");
const types = { ".html": "text/html", ".js": "application/javascript", ".css": "text/css", ".svg": "image/svg+xml", ".webmanifest": "application/manifest+json" };

const server = http.createServer((request, response) => {
  const pathname = decodeURIComponent(new URL(request.url, "http://localhost").pathname);
  const file = path.resolve(root, "." + (pathname === "/" ? "/index.html" : pathname));
  if (!file.startsWith(root + path.sep) && file !== path.join(root, "index.html")) {
    response.writeHead(403).end(); return;
  }
  fs.readFile(file, (err, data) => {
    if (err) { response.writeHead(404).end(); return; }
    response.writeHead(200, { "Content-Type": types[path.extname(file)] || "application/octet-stream", "Cache-Control": "no-cache" });
    response.end(data);
  });
});

function waitMessage(page, phrase, timeout = 12000) {
  return page.waitForFunction((text) => document.getElementById("message").textContent.includes(text),
    phrase, { timeout });
}
function waitLevel(page, level, timeout = 12000) {
  return page.waitForFunction((value) => document.getElementById("level").textContent === String(value),
    level, { timeout });
}
function waitLosses(page, losses, timeout = 12000) {
  return page.waitForFunction((value) => document.getElementById("losses").textContent === String(value),
    losses, { timeout });
}

async function run() {
  await new Promise((resolve) => server.listen(0, "127.0.0.1", resolve));
  const address = server.address();
  const url = "http://127.0.0.1:" + address.port + "/";
  let browser;
  try {
    const safari = process.env.TEST_BROWSER === "webkit";
    browser = await (safari ? webkit : chromium).launch({
      headless: true,
      args: safari ? [] : ["--autoplay-policy=no-user-gesture-required"]
    });
    const context = await browser.newContext({ viewport: { width: 390, height: 844 } });
    const page = await context.newPage();
    const errors = [];
    page.on("pageerror", (error) => errors.push(error.message));

    await page.goto(url, { waitUntil: "domcontentloaded" });
    assert.match(await page.title(), /7-Note Musical Game/);
    assert.equal(await page.locator("[data-note]").count(), 7);
    assert.equal(await page.locator("[data-mode]").count(), 4);
    console.log("✓ Tela inicial, sete teclas e quatro modos");
    await page.goto(url + "?tv=1", { waitUntil: "domcontentloaded" });
    assert.equal(await page.locator("html.tv-mode").count(), 1);
    const before = await page.evaluate(() => document.activeElement.id);
    assert.equal(before, "play-button");
    await page.keyboard.press("ArrowRight");
    const after = await page.evaluate(() => document.activeElement.id);
    assert.notEqual(after, before, "D-pad deve mover o foco no TV Box");
    await page.goto(url, { waitUntil: "domcontentloaded" });
    console.log("✓ TV Box ativado e controle remoto navega entre botões");

    // Navegadores sem Web Audio API não podem deixar a fase em estado "playing".
    // Página isolada evita modificar o áudio dos demais cenários do teste.
    const noAudioPage = await context.newPage();
    await noAudioPage.addInitScript(() => {
      Object.defineProperty(window, "AudioContext", { configurable: true, value: undefined });
      Object.defineProperty(window, "webkitAudioContext", { configurable: true, value: undefined });
    });
    await noAudioPage.goto(url, { waitUntil: "domcontentloaded" });
    await noAudioPage.locator("#play-button").click();
    await noAudioPage.waitForFunction(() =>
      document.getElementById("message").textContent.includes("não oferece áudio"));
    assert.equal(await noAudioPage.locator("#play-button").isEnabled(), true,
      "Falha de áudio deve permitir nova tentativa sem bloquear o jogo");
    assert.equal(await noAudioPage.locator("#level").textContent(), "1");
    assert.equal(await noAudioPage.locator("#wins").textContent(), "0");
    assert.equal(await noAudioPage.locator("#losses").textContent(), "0");
    await noAudioPage.close();
    console.log("✓ Falta de Web Audio API informa erro sem bloquear ou alterar progresso");



    // Sair da página durante uma sequência não pode deixar a fase bloqueada.
    await page.locator("#play-button").click();
    await page.evaluate(() => window.dispatchEvent(new Event("pagehide")));
    assert.equal(await page.locator("#play-button").isEnabled(), true,
      "pagehide deve cancelar reprodução e liberar reinício da fase");
    await page.waitForTimeout(350);
    assert.equal(await page.locator("#play-button").isEnabled(), true,
      "reprodução cancelada não pode reativar o bloqueio após timer pendente");
    console.log("✓ Navegação/pagehide cancela sequência e permite reinício");

    // Navegação real: a página de privacidade deve ser acessível e o retorno
    // não pode conservar um estado de reprodução bloqueado (inclusive bfcache).
    await page.locator("#play-button").click();
    await page.goto(url + "privacy.html", { waitUntil: "domcontentloaded" });
    assert.match(await page.locator("body").innerText(), /privacidade/i);
    await page.goBack({ waitUntil: "domcontentloaded" });
    assert.equal(await page.locator("#play-button").isEnabled(), true,
      "Voltar da política de privacidade deve permitir reproduzir novamente");
    await page.waitForTimeout(350);
    assert.equal(await page.locator("#play-button").isEnabled(), true,
      "Timer da página anterior não deve bloquear o retorno do histórico");
    console.log("✓ Navegação real e retorno do histórico liberam reprodução");

    await page.locator("#play-button").click();
    await waitMessage(page, "Agora repita");
    await page.locator('[data-note="do"]').click();
    await waitLevel(page, 2);
    await page.locator("#play-button").click();
    await waitMessage(page, "Agora repita");
    await page.locator('[data-note="do"]').click();
    await page.locator('[data-note="si"]').click();
    await waitLevel(page, 3);
    console.log("✓ Fases 1 e 2 e progressão da sequência");

    for (let losses = 1; losses <= 2; losses++) {
      await page.locator("#play-button").click();
      await waitMessage(page, "Agora repita");
      await page.locator('[data-note="fa"]').click();
      await waitLosses(page, losses);
      if (losses === 1) assert.equal(await page.locator("#ad-modal").isVisible(), false);
    }
    assert.equal(await page.locator("#ad-modal").isVisible(), true);
    await page.locator("#close-ad").click();
    assert.equal(await page.locator("#ad-modal").isVisible(), false);
    console.log("✓ Anúncio demonstrativo só após a segunda derrota");

    await page.locator('[data-mode="ear"]').click();
    await page.locator("#play-button").click();
    await waitMessage(page, "Qual nota você ouviu");
    await page.locator('[data-note="do"]').click();
    assert.match(await page.locator("#message").textContent(), /próxima nota/i);
    console.log("✓ Desafio auditivo recebe respostas");

    await page.locator('[data-mode="compose"]').click();
    await page.locator('[data-note="mi"]').click();
    await page.locator('[data-note="do"]').click();
    assert.equal(await page.locator(".sequence-chip").count(), 2);
    await page.reload({ waitUntil: "domcontentloaded" });
    await page.locator('[data-mode="compose"]').click();
    assert.equal(await page.locator(".sequence-chip").count(), 2);
    await page.locator("#undo-button").click();
    assert.equal(await page.locator(".sequence-chip").count(), 1);
    await page.locator("#play-button").click();
    await waitMessage(page, "terminou");
    console.log("✓ Compositor grava, recupera, desfaz e reproduz");

    await page.locator("#settings-button").click();
    await page.locator("#volume").evaluate((input) => { input.value = "40"; input.dispatchEvent(new Event("input", { bubbles: true })); });
    await page.locator("#tempo").selectOption("fast");
    await page.locator('#settings-dialog button[type="submit"]').click();
    await page.reload({ waitUntil: "domcontentloaded" });
    await page.locator("#settings-button").click();
    assert.equal(await page.locator("#volume").inputValue(), "40");
    assert.equal(await page.locator("#tempo").inputValue(), "fast");
    await page.locator('#settings-dialog button[type="submit"]').click();
    console.log("✓ Ajustes e progresso persistem após atualizar a página");

    // Exercita 1.000 entradas na fase final sem reproduzir a sequência inteira.
    // O modo Livre evita alterar a campanha e permite isolar o custo da interface.
    await page.evaluate(() => {
      localStorage.setItem("seven-note-musical-game:v2",
        JSON.stringify({ level: 1000, best: 1000, completed: false,
          totalWins: 999, points: 500000, totalLosses: 2 }));
      localStorage.setItem("seven-note-musical-game:prefs-v1",
        JSON.stringify({ volume: 0, tempo: "fast", instrument: "soft", showNames: true }));
    });
    await page.reload({ waitUntil: "domcontentloaded" });
    // Valida a campanha completa no mesmo motor carregado pela interface.
    const campaignAudit = await page.evaluate(() => {
      const core = window.SevenNoteCore;
      const opening = ["do", "si", "re", "mi", "do", "do", "re", "sol", "fa"];
      let previous = [];
      for (let level = 1; level <= 1000; level++) {
        const sequence = core.sequenceForLevel(level);
        if (sequence.length !== level ||
            sequence.slice(0, previous.length).some((note, index) => note !== previous[index]) ||
            (level <= opening.length && sequence[level - 1] !== opening[level - 1])) {
          return { valid: false, level };
        }
        previous = sequence;
      }
      let rejects1001 = false;
      try { core.sequenceForLevel(1001); } catch (error) { rejects1001 = error instanceof RangeError; }
      return { valid: rejects1001, level: 1000, finalLength: previous.length };
    });
    assert.deepEqual(campaignAudit, { valid: true, level: 1000, finalLength: 1000 },
      "Campanha deve manter os prefixos e terminar exatamente na fase 1.000");
    console.log("✓ Campanha completa: 1.000 prefixos estáveis e fase 1.001 proibida");
    // A fase final precisa iniciar sem percorrer as mil notas sincronicamente.
    // Interromper uma reprodução longa não pode deixar a UI travada ou premiar o jogador.
    const finalPlaybackStarted = Date.now();
    await page.locator("#play-button").click();
    assert.match(await page.locator("#message").textContent(), /Escute com atenção/);
    assert.equal(await page.locator("#play-button").isDisabled(), true,
      "A fase 1.000 deve bloquear novas reproduções enquanto toca");
    await page.evaluate(() => window.dispatchEvent(new Event("pagehide")));
    assert.equal(await page.locator("#play-button").isEnabled(), true,
      "Interromper a fase 1.000 deve liberar o botão imediatamente");
    await page.waitForTimeout(400);
    assert.equal(await page.locator("#play-button").isEnabled(), true,
      "Temporizadores pendentes da fase 1.000 não podem rebloquear o botão");
    assert.equal(await page.locator("#level").textContent(), "1000");
    assert.equal(await page.locator("#wins").textContent(), "999",
      "Interromper a reprodução não deve registrar vitória");
    console.log("✓ Reprodução da fase 1.000 inicia e cancela sem bloquear a UI (" +
      (Date.now() - finalPlaybackStarted) + " ms, incluindo espera de cancelamento)");

    // Testa a transição real do motor no último acerto, sem falsificar o estado final no storage.
    const finalTransition = await page.evaluate(() => {
      const core = window.SevenNoteCore;
      const sequence = core.sequenceForLevel(1000);
      const before = core.sanitizeProgress({
        level: 1000, best: 1000, completed: false, totalWins: 999, points: 500000
      });
      const penultimate = core.evaluateNote(sequence, 998, sequence[998]);
      const last = core.evaluateNote(sequence, 999, sequence[999]);
      const finished = core.advanceLevel(before);
      const repeated = core.advanceLevel(finished);
      const persisted = core.sanitizeProgress(JSON.parse(JSON.stringify(finished)));
      return {
        penultimate: penultimate.result,
        last: last.result,
        level: finished.level,
        completed: finished.completed,
        wins: finished.totalWins,
        stable: JSON.stringify(repeated) === JSON.stringify(finished),
        persisted: JSON.stringify(persisted) === JSON.stringify(finished),
        finalAchievement: core.achievements(finished).find((item) => item.id === "final").unlocked
      };
    });
    assert.deepEqual(finalTransition, {
      penultimate: "correct", last: "win", level: 1000,
      completed: true, wins: 1000, stable: true, persisted: true, finalAchievement: true
    }, "Última nota deve concluir a fase 1.000 uma única vez e persistir sem fase 1.001");
    console.log("✓ Último acerto conclui a fase 1.000, persiste e não duplica a vitória");

    await page.locator('[data-mode="free"]').click();
    const performanceSample = await page.evaluate(() => {
      const firstAchievement = document.querySelector("#achievement-list .achievement");
      const firstMelodyChip = document.querySelector("#sequence-chips .sequence-chip");
      const key = document.querySelector('[data-note="do"]');
      const started = performance.now();
      for (let i = 0; i < 1000; i++) key.click();
      return {
        elapsedMs: performance.now() - started,
        stableAchievement: document.querySelector("#achievement-list .achievement") === firstAchievement,
        stableMelody: document.querySelector("#sequence-chips .sequence-chip") === firstMelodyChip,
        level: document.getElementById("level").textContent,
        count: document.querySelectorAll("#achievement-list .achievement").length
      };
    });
    assert.equal(performanceSample.level, "1000");
    assert.equal(performanceSample.count, 6);
    assert.equal(performanceSample.stableAchievement, true,
      "Entradas sem desbloqueio não devem reconstruir o painel de conquistas");
    assert.equal(performanceSample.stableMelody, true,
      "Entradas no modo Livre não devem reconstruir os chips do compositor");
    assert.ok(Number.isFinite(performanceSample.elapsedMs));
    console.log("✓ 1.000 entradas na fase 1.000 (modo Livre, áudio mudo): " +
      performanceSample.elapsedMs.toFixed(1) + " ms; conquistas sem reconstrução");

    await page.evaluate(() => {
      localStorage.setItem("seven-note-musical-game:v2",
        JSON.stringify({ level: 1000, best: 1000, completed: true,
          totalWins: 1000, points: 500000, totalLosses: 2 }));
    });
    await page.reload({ waitUntil: "domcontentloaded" });
    assert.equal(await page.locator("#level").textContent(), "1000");
    assert.equal(await page.locator("#finish-banner").isVisible(), true);
    assert.equal(await page.locator("#play-button").isDisabled(), true);
    assert.match(await page.locator("#message").textContent(), /concluiu todas as 1.000 fases/);
    assert.match(await page.locator("#step-progress").textContent(), /FIM DO JOGO/);
    assert.equal(await page.locator("#achievement-count").textContent(), "4/6");
    console.log("✓ Tela de fim do jogo mostra a fase 1000 e bloqueia a 1001");

    const ready = await page.evaluate(async () => {
      if (!("serviceWorker" in navigator)) return false;
      await navigator.serviceWorker.ready;
      return true;
    });
    assert.equal(ready, true);
    await page.reload({ waitUntil: "domcontentloaded" });
    if (safari) {
      // Playwright WebKit no Linux apresenta erro interno ao recarregar offline.
      // Verificamos que o conteúdo crítico está realmente cacheado;
      // a navegação offline Safari nativa exige teste em dispositivo físico.
      const cached = await page.evaluate(async () => {
        const response = await caches.match(new URL("index.html", location.href).href);
        const script = await caches.match(new URL("game-core.js", location.href).href);
        return Boolean(response && script);
      });
      assert.equal(cached, true);
      console.log("✓ WebKit mantém arquivos essenciais no cache offline");
    } else {
      await context.setOffline(true);
      await page.reload({ waitUntil: "domcontentloaded" });
      assert.equal(await page.locator("[data-note]").count(), 7);
      await context.setOffline(false);
      console.log("✓ Service worker permite reabrir o jogo offline");
    }

    assert.deepEqual(errors, [], "Erros de JavaScript na interface: " + errors.join("; "));
    console.log("✓ Sem erros de execução JavaScript");
    await context.close();
  } finally {
    if (browser) await browser.close();
    await new Promise((resolve) => server.close(resolve));
  }
}

run().catch((error) => { console.error(error); process.exitCode = 1; });
