package com.orbis.sevennote;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import android.os.Bundle;
import android.os.Build;
import android.view.WindowInsets;
import android.graphics.Color;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.webkit.WebViewAssetLoader;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class MainActivity extends ComponentActivity {
 private WebView game;
 private static final String HOST="appassets.androidplatform.net";
 private static final String BASE="https://"+HOST+"/assets/";

 @Override public void onCreate(Bundle state) {
   super.onCreate(state);
   game=new WebView(this);
   game.setBackgroundColor(Color.rgb(8,13,29));
   game.setFocusable(true);
   game.setFocusableInTouchMode(true);
   setContentView(game);
   // Respeitar as barras do sistema no Android 16 (edge-to-edge).
   if (Build.VERSION.SDK_INT >= 30) {
      game.setOnApplyWindowInsetsListener((view, insets) -> {
         android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars());
         view.setPadding(bars.left,bars.top,bars.right,bars.bottom);
         return insets;
      });
   } else {
      game.setFitsSystemWindows(true);
   }
   getOnBackPressedDispatcher().addCallback(this,new OnBackPressedCallback(true) {
      @Override public void handleOnBackPressed() { handleBack(); }
   });
   WebSettings s=game.getSettings();
   if (Build.VERSION.SDK_INT >= 26) s.setSafeBrowsingEnabled(true);
   s.setJavaScriptEnabled(true);
   s.setDomStorageEnabled(true);
   s.setMediaPlaybackRequiresUserGesture(false);
   s.setAllowFileAccess(false);
   s.setAllowContentAccess(false);
   s.setJavaScriptCanOpenWindowsAutomatically(false);
   s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
   WebViewAssetLoader loader=new WebViewAssetLoader.Builder()
     .setDomain(HOST)
     .addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();
   game.setWebChromeClient(new WebChromeClient());
   game.setWebViewClient(new WebViewClient(){
      @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest req){
        if("https".equals(req.getUrl().getScheme()) && HOST.equals(req.getUrl().getHost()) &&
           req.getUrl().getPath()!=null && req.getUrl().getPath().startsWith("/assets/"))
             return loader.shouldInterceptRequest(req.getUrl());
        return new WebResourceResponse("text/plain","UTF-8",403,"Forbidden",
          Collections.emptyMap(),new ByteArrayInputStream("Blocked".getBytes(StandardCharsets.UTF_8)));
      }
      @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest req) {
         return !("https".equals(req.getUrl().getScheme()) && HOST.equals(req.getUrl().getHost()) &&
                  req.getUrl().getPath()!=null && req.getUrl().getPath().startsWith("/assets/"));
      }
   });
   game.loadUrl(BASE+"index.html"+("tv".equals(BuildConfig.FLAVOR)?"?tv=1":""));
   game.requestFocus();
 }
 private void handleBack(){
   if(game==null){finish();return;}
   game.evaluateJavascript(
     "(!document.getElementById('ad-modal').hidden || !!document.querySelector('dialog[open]'))",
     value->{
       if("true".equals(value)){
          game.evaluateJavascript(
            "if(document.querySelector('dialog[open]')) document.querySelector('dialog[open]').close();"+
            "else window.dispatchEvent(new KeyboardEvent('keydown',{key:'Escape'}));",null);
       }else finish();
     });
 }
 @Override protected void onDestroy(){
   if(game!=null){game.loadUrl("about:blank");game.destroy();game=null;}
   super.onDestroy();
 }
}
