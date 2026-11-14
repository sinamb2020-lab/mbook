package com.sina.mbook

import android.annotation.SuppressLint
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {
    private lateinit var web: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webViewClient = WebViewClient()
        web.webChromeClient = WebChromeClient()
        web.addJavascriptInterface(Bridge(this), "Android")
        setContentView(web)
        val hash = if (intent?.getBooleanExtra("add", false) == true) "#add" else ""
        web.loadUrl("file:///android_asset/index.html$hash")
    }

    override fun onNewIntent(i: Intent?) {
        super.onNewIntent(i)
        if (i?.getBooleanExtra("add", false) == true) web.evaluateJavascript("openSheet()", null)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}

class Bridge(private val c: Context) {
    @JavascriptInterface
    fun sync(today: String, month: String) {
        c.getSharedPreferences("m", 0).edit().putString("t", today).putString("m", month).apply()
        val mgr = AppWidgetManager.getInstance(c)
        val ids = mgr.getAppWidgetIds(ComponentName(c, Widget::class.java))
        Widget.update(c, mgr, ids)
    }
}
