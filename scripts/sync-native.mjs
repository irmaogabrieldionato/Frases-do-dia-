import {copyFileSync,mkdirSync} from "node:fs";
import {resolve,dirname} from "node:path";
import {fileURLToPath} from "node:url";
const root=resolve(dirname(fileURLToPath(import.meta.url)),"..");
const assets=["index.html","styles.css","game-core.js","app.js","icon.svg","manifest.webmanifest","sw.js","tv.css"];
for(const dir of ["native/android/app/src/main/assets","native/ios/SevenNote/Resources/Web","native/windows/web"]){
 for(const asset of assets){
  const target=resolve(root,dir,asset);
  mkdirSync(dirname(target),{recursive:true});
  copyFileSync(resolve(root,asset),target);
 }
 console.log("Synced",dir);
}
