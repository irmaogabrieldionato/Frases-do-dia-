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
