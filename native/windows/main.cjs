"use strict";
const {app,BrowserWindow,session}=require("electron");
const {join,resolve,sep}=require("node:path");
const {fileURLToPath}=require("node:url");
app.commandLine.appendSwitch("autoplay-policy","no-user-gesture-required");
function isAllowedLocalUrl(value){
  try{
    const url=new URL(value);
    if(url.protocol!=="file:")return false;
    const path=resolve(fileURLToPath(url));
    const root=resolve(__dirname,"web");
    return path===root||path.startsWith(root+sep);
  }catch{return false;}
}
function start(){
  const win=new BrowserWindow({
    width:1200,height:850,minWidth:700,minHeight:590,
    backgroundColor:"#080d1d",autoHideMenuBar:true,
    title:"7-Note Musical Game",
    webPreferences:{
      nodeIntegration:false,contextIsolation:true,sandbox:true,
      webSecurity:true,webviewTag:false,partition:"persist:seven-note"
    }
  });
  win.webContents.setWindowOpenHandler(()=>({action:"deny"}));
  win.webContents.on("will-navigate",(event,url)=>{
    if(!isAllowedLocalUrl(url))event.preventDefault();
  });
  win.webContents.on("will-redirect",(event,url)=>{
    if(!isAllowedLocalUrl(url))event.preventDefault();
  });
  win.loadFile(join(__dirname,"web","index.html"));
}
app.whenReady().then(()=>{
  const ses=session.fromPartition("persist:seven-note");
  ses.setPermissionRequestHandler((webContents,permission,callback)=>callback(false));
  ses.setPermissionCheckHandler(()=>false);
  start();
  app.on("activate",()=>{if(BrowserWindow.getAllWindows().length===0)start();});
});
app.on("window-all-closed",()=>{if(process.platform!=="darwin")app.quit();});
