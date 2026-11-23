"use strict";
const test=require("node:test");
const assert=require("node:assert/strict");
const {readFileSync,existsSync}=require("node:fs");
const {join}=require("node:path");
const root=join(__dirname,"..");
const file=(p)=>readFileSync(join(root,p),"utf8");
test("Android and TV Box use separate package IDs and launchers",()=>{
 const gradle=file("native/android/app/build.gradle.kts");
 const manifest=file("native/android/app/src/main/AndroidManifest.xml");
 const tv=file("native/android/app/src/tv/AndroidManifest.xml");
 assert.match(gradle,/create\("mobile"\)/);
 assert.match(gradle,/create\("tv"\)/);
 assert.match(gradle,/applicationIdSuffix\s*=\s*"\.tv"/);
 assert.match(manifest,/\$\{launcherCategory\}/);
 assert.match(tv,/LEANBACK|leanback/i);
 assert.match(tv,/android\.intent\.category\.LEANBACK_LAUNCHER/);
 assert.match(tv,/android\.hardware\.touchscreen" android:required="false"/);
 assert.match(tv,/android:exported="true"/);
 assert.match(gradle,/manifestPlaceholders\["launcherCategory"\]\s*=\s*"android.intent.category.LAUNCHER"/);
});
test("iOS XcodeGen project includes offline WKWebView and bundle icon",()=>{
 const ios=file("native/ios/project.yml");
 const view=file("native/ios/SevenNote/GameView.swift");
 const appIcon=JSON.parse(file("native/ios/SevenNote/Assets.xcassets/AppIcon.appiconset/Contents.json"));
 assert.match(ios,/platform: iOS/);
 assert.match(view,/loadFileURL/);
 assert.match(view,/WKWebView/);
 assert.match(view,/address\.isFileURL/);
 assert.match(view,/standardizedFileURL\.path/);
 assert.match(view,/targetPath\.hasPrefix\(rootPath \+ "\/"\)/);
 assert.doesNotMatch(view,/decisionHandler\(address\.isFileURL \? \.allow : \.cancel\)/);
 assert.equal(appIcon.images.length,1);
});
test("Windows installer and portable executable are configured safely",()=>{
 const pkg=JSON.parse(file("native/windows/package.json"));
 const app=file("native/windows/main.cjs");
 assert.match(pkg.scripts.build,/--win nsis portable --x64/);
 assert.equal(pkg.build.asar,true);
 assert.match(app,/nodeIntegration:false/);
 assert.match(app,/contextIsolation:true/);
 assert.match(app,/sandbox:true/);
 assert.match(app,/ses\.webRequest\.onBeforeRequest/);
 assert.match(app,/cancel:!isAllowedLocalUrl\(details\.url\)/);
 assert.match(app,/setPermissionRequestHandler/);
});
test("native assets include all the same files as the web game",()=>{
 const sync=file("scripts/sync-native.mjs");
 for(const name of ["index.html","game-core.js","app.js","styles.css","tv.css","sw.js","privacy.html"]){
   assert.ok(sync.includes(name),name);
   assert.ok(existsSync(join(root,name)),name);
 }
 const artwork=file("scripts/artwork.py");
 for(const name of ["tv_banner.png","icon-1024.png","icon.ico"]){
   assert.ok(artwork.includes(name),name);
 }
});

test("Android/TV checksums are portable and validated before upload",()=>{
 const workflow=file(".github/workflows/packages-7-note.yml");
 assert.match(workflow,/sha256sum --check --strict SHA256SUMS\.txt/);
 assert.match(workflow,/sha256sum app-mobile-debug\.apk app-tv-debug\.apk app-mobile-release\.aab app-tv-release\.aab > SHA256SUMS\.txt/);
 assert.match(workflow,/7-note-Android-TV-SHA256/);
 assert.doesNotMatch(workflow,/sha256sum native\/android\/app\/build\/outputs\/apk\/\{mobile,tv\}\/debug\/\*\.apk/);
});
