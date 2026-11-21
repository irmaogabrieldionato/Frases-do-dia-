"use strict";
const {app, BrowserWindow, shell} = require("electron");
const {join} = require("node:path");

app.commandLine.appendSwitch("autoplay-policy", "no-user-gesture-required");
function start() {
  const win=new BrowserWindow({
    width:1200,height:850,minWidth:700,minHeight:590,
    backgroundColor:"#080d1d",autoHideMenuBar:true,
    title:"7-Note Musical Game",
    webPreferences:{
      nodeIntegration:false,contextIsolation:true,sandbox:true,
      webSecurity:true,webviewTag:false
    }
  });
  win.webContents.setWindowOpenHandler(() => ({action:"deny"}));
  win.webContents.on("will-navigate",(event,url) => {
    if(!url.startsWith("file://")) event.preventDefault();
  });
  win.loadFile(join(__dirname,"web","index.html"));
}
app.whenReady().then(() => {
  start();
  app.on("activate",() => { if(BrowserWindow.getAllWindows().length===0) start(); });
});
app.on("window-all-closed",() => { if(process.platform!=="darwin") app.quit(); });
