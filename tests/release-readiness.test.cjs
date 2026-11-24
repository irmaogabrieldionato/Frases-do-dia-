"use strict";
const test=require("node:test");
const assert=require("node:assert/strict");
const fs=require("node:fs");
const path=require("node:path");
const root=path.resolve(__dirname,"..");
const content=(p)=>fs.readFileSync(path.join(root,p),"utf8");
test("Google Play: Android mobile API36 e TV API35",()=>{
 const app=content("native/android/app/build.gradle.kts");
 const build=content("native/android/build.gradle.kts");
 const ci=content(".github/workflows/packages-7-note.yml");
 assert.match(app,/compileSdk\s*=\s*36/);
 assert.match(app,/create\("mobile"\)\s*\{[\s\S]*?targetSdk\s*=\s*36/);
 assert.match(app,/create\("tv"\)\s*\{[\s\S]*?targetSdk\s*=\s*35/);
 assert.match(build,/8\.13\./);
 assert.match(ci,/android-36/);
 assert.match(ci,/gradle-version: '8\.13'/);
});
test("política de privacidade via interface, offline e em todas as plataformas",()=>{
 assert.match(content("index.html"),/href="privacy\.html"/);
 assert.match(content("sw.js"),/privacy\.html/);
 assert.match(content("scripts/sync-native.mjs"),/privacy\.html/);
 assert.match(content("privacy.html"),/Dados armazenados/);
});
test("Windows: Electron suportado, sandbox e permissões bloqueadas",()=>{
 const pkg=JSON.parse(content("native/windows/package.json"));
 const main=content("native/windows/main.cjs");
 assert.match(pkg.devDependencies.electron,/^45\./);
 for(const name of ["nodeIntegration:false","contextIsolation:true","sandbox:true","setPermissionRequestHandler","setPermissionCheckHandler","setWindowOpenHandler","isAllowedLocalUrl"]){
   assert.ok(main.includes(name),name);
 }
});
test("Android: nenhum pedido de permissão sensível",()=>{
 const manifest=content("native/android/app/src/main/AndroidManifest.xml");
 const main=content("native/android/app/src/main/java/com/orbis/sevennote/MainActivity.java");
 assert.doesNotMatch(manifest,/uses-permission/);
 assert.match(main,/setSafeBrowsingEnabled\(true\)/);
 for(const setting of ["setAllowFileAccess(false)","setAllowContentAccess(false)","setAllowFileAccessFromFileURLs(false)","setAllowUniversalAccessFromFileURLs(false)","setSupportMultipleWindows(false)","setJavaScriptCanOpenWindowsAutomatically(false)","setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW)"]){
   assert.ok(main.includes(setting),`Android WebView: ${setting}`);
 }
 assert.match(main,/getOnBackPressedDispatcher/);
 assert.match(main,/setOnApplyWindowInsetsListener/);
});
test("assinaturas de publicação ainda não estão configuradas",()=>{
 const ci=content(".github/workflows/packages-7-note.yml");
 assert.match(ci,/CODE_SIGNING_ALLOWED=NO/);
 assert.match(content("PACKAGES.md"),/assinatura/i);
});

test("contador inicial de conquistas corresponde às conquistas do motor",()=>{
 const core=require("../game-core.js");
 const total=core.achievements({}).length;
 const html=content("index.html");
 const count=html.match(/id="achievement-count">0\/(\d+)</);
 assert.ok(count,"contador inicial presente");
 assert.equal(Number(count[1]),total);
 assert.equal(total,6);
});

test("instruções Android locais usam SDK, Gradle e lint do CI",()=>{
 const docs=content("PACKAGES.md");
 const workflow=content(".github/workflows/packages-7-note.yml");
 assert.match(docs,/Android SDK API 36/);
 assert.match(docs,/Gradle 8\.13/);
 assert.match(docs,/:app:bundleMobileRelease/);
 assert.match(docs,/:app:bundleTvRelease/);
 assert.match(docs,/:app:lintMobileRelease/);
 assert.match(docs,/:app:lintTvRelease/);
 for(const task of ["bundleMobileRelease","bundleTvRelease","lintMobileRelease","lintTvRelease"]){
   assert.ok(workflow.includes(task),task);
 }
});

// A reprodução e as respostas devem compartilhar uma única sequência por fase.
test("sequência da memória é reutilizada durante a fase 1000",()=>{
 const app=content("app.js");
 assert.match(app,/function currentMemorySequence\(\)/);
 assert.match(app,/memorySequenceLevel !== progress\.level/);
 assert.match(app,/playSequence\(currentMemorySequence\(\), "memory"\)/);
 assert.match(app,/core\.evaluateNote\(currentMemorySequence\(\), inputIndex, id\)/);
 assert.doesNotMatch(app,/core\.evaluateNote\(core\.sequenceForLevel\(/);
});

// Evita milhares de reconstruções da lista de conquistas na fase final.
test("conquistas só são redesenhadas quando a condição de desbloqueio muda",()=>{
 const app=content("app.js");
 const core=content("game-core.js");
 assert.match(app,/let achievementRenderKey = null/);
 assert.match(app,/if \(key === achievementRenderKey\) return/);
 assert.match(app,/achievementRenderKey = key/);
 for(const expression of ["progress.totalWins > 0","progress.best >= 8","progress.bestStreak >= 5",
   "progress.earBest >= core.EAR_ROUNDS","progress.best >= 25","progress.completed"]){
   assert.ok(app.includes(expression),expression);
 }
 for(const field of ["totalWins","best","bestStreak","earBest","completed"]){
   assert.ok(core.includes("p."+field),field);
 }
});

// O painel do compositor não deve reconstruir o DOM durante 1.000 entradas no modo Livre.
test("compositor mantém os chips quando a melodia não muda",()=>{
 const app=content("app.js");
 assert.match(app,/let melodyRenderKey = null/);
 assert.match(app,/if \(key !== melodyRenderKey\)/);
 assert.match(app,/melodyRenderKey = key/);
 assert.match(app,/const key = melody\.join\(","\)/);
});

test("reprodução interrompida não habilita respostas silenciosas", () => {
 const app=content("app.js");
 const section=app.slice(app.indexOf("async function playSequence("),app.indexOf("async function startMemory("));
 assert.match(section,/for \(const id of sequence\)/);
 assert.equal((section.match(/audioContext\.state !== "running"/g)||[]).length,2,
   "verificar interrupção durante e ao concluir a reprodução");
 assert.match(section,/phase = "idle";[\s\S]*?setMessage\("O áudio foi interrompido/);
 assert.match(section,/phase = owner === "memory" \? "input" : "idle"/);
});

test("manifesto SHA256 portátil rejeita alteração, duplicata e arquivo ausente",()=>{
 const os=require("node:os");
 const crypto=require("node:crypto");
 const {verify}=require("../scripts/verify-android-packages.cjs");
 const dir=fs.mkdtempSync(path.join(os.tmpdir(),"sevennote-sha-"));
 const names=["app-mobile-debug.apk","app-tv-debug.apk","app-mobile-release.aab","app-tv-release.aab"];
 try {
   const lines=names.map((name,i)=>{
     const data=Buffer.from("fixture-"+i);
     fs.writeFileSync(path.join(dir,name),data);
     return crypto.createHash("sha256").update(data).digest("hex")+"  "+name;
   });
   fs.writeFileSync(path.join(dir,"SHA256SUMS.txt"),lines.join("\n")+"\n");
   assert.doesNotThrow(()=>verify(dir));
   // Um hash inválido no último pacote não pode produzir mensagens parciais "OK".
   const originalWrite=process.stdout.write;
   let output="";
   process.stdout.write=function(chunk){output+=String(chunk);return true;};
   try {
     fs.appendFileSync(path.join(dir,names[3]),"corrompido");
     assert.throws(()=>verify(dir),/Hash divergente/);
     assert.equal(output,"","nenhum pacote deve ser anunciado como OK antes da validação completa");
     fs.writeFileSync(path.join(dir,names[3]),"fixture-3");
     verify(dir);
     assert.equal((output.match(/^OK /gm)||[]).length,4);
   } finally {process.stdout.write=originalWrite;}
   fs.appendFileSync(path.join(dir,names[0]),"alterado");
   assert.throws(()=>verify(dir),/Hash divergente/);
   fs.writeFileSync(path.join(dir,names[0]),"fixture-0");
   fs.writeFileSync(path.join(dir,"SHA256SUMS.txt"),[lines[0],lines[0],lines[2],lines[3]].join("\n"));
   assert.throws(()=>verify(dir),/duplicada/);
   fs.writeFileSync(path.join(dir,"SHA256SUMS.txt"),lines.join("\n"));
   fs.unlinkSync(path.join(dir,names[3]));
   assert.throws(()=>verify(dir),/ENOENT/);
   fs.writeFileSync(path.join(dir,names[3]),"fixture-3");
   fs.writeFileSync(path.join(dir,"SHA256SUMS.txt"),"x".repeat(513));
   assert.throws(()=>verify(dir),/Manifesto SHA256 inválido/);
   fs.writeFileSync(path.join(dir,"SHA256SUMS.txt"),lines.join("\n")+"\n");
   const original=path.join(dir,"original-manifest.txt");
   fs.renameSync(path.join(dir,"SHA256SUMS.txt"),original);
   fs.symlinkSync(original,path.join(dir,"SHA256SUMS.txt"));
   assert.throws(()=>verify(dir),/Manifesto SHA256 inválido/);
 } finally {fs.rmSync(dir,{recursive:true,force:true});}
});

test("verificador de APK/AAB calcula SHA256 em blocos sem carregar pacote inteiro",()=>{
 const verifier=content("scripts/verify-android-packages.cjs");
 assert.match(verifier,/Buffer\.allocUnsafe\(1024 \* 1024\)/);
 assert.match(verifier,/fs\.readSync\(fd, buffer, 0, buffer\.length, null\)/);
 assert.match(verifier,/hash\.update\(buffer\.subarray\(0, count\)\)/);
 assert.doesNotMatch(verifier,/fs\.readFileSync\(file\)/);
});


test("verificador valida identidade e tipo do arquivo após abrir o descritor",()=>{
 const verifier=content("scripts/verify-android-packages.cjs");
 assert.match(verifier,/fs\.fstatSync\(fd\)/);
 assert.match(verifier,/opened\.isFile\(\)/);
 assert.match(verifier,/opened\.dev !== stat\.dev/);
 assert.match(verifier,/opened\.ino !== stat\.ino/);
 assert.match(verifier,/finally \{\s*fs\.closeSync\(fd\)/);
});

test("treino de ouvido bloqueia resposta se áudio interrompido e repete a mesma nota",()=>{
 const app=content("app.js");
 const question=app.split("async function startEarQuestion()")[1].split("function newEarSession()")[0];
 assert.equal((question.match(/audioContext.state !== "running"/g)||[]).length,2);
 assert.equal((question.match(/O áudio foi interrompido/g)||[]).length,2);
 assert.match(question,/await delay\(640\);[\s\S]*?audioContext.state !== "running"[\s\S]*?ear.status = "input"/);
 const next=app.split("function nextEar()")[1].split("function answerEar")[0];
 assert.match(next,/if \(ear.status === "feedback"\) \{\s*ear.round \+= 1;\s*ear.target = core.chooseEarNote\(ear.last\);\s*ear.last = ear.target;\s*\}/);
 const secondary=app.split("async function secondaryAction()")[1].split('$("play-button")')[0];
 assert.match(secondary,/void startEarQuestion\(\)/);
});
