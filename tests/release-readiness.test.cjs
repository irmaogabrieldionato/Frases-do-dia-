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
 assert.match(main,/OnBackInvokedDispatcher/);
 assert.match(main,/setOnApplyWindowInsetsListener/);
});
test("assinaturas de publicação ainda não estão configuradas",()=>{
 const ci=content(".github/workflows/packages-7-note.yml");
 assert.match(ci,/CODE_SIGNING_ALLOWED=NO/);
 assert.match(content("PACKAGES.md"),/assinatura/i);
});
