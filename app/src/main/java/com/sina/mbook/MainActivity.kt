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
import android.provider.Settings
import org.json.JSONArray
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

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
        web.settings.allowFileAccess = true
        val hash = if (intent?.getBooleanExtra("add", false) == true) "#add" else ""
        val cache = File(filesDir, "index.html")
        val asset = assets.open("index.html").bufferedReader().use { it.readText() }
        val cached = if (cache.exists()) cache.readText() else ""
        val useCache = ver(cached) > ver(asset)
        web.loadUrl((if (useCache) "file://" + cache.absolutePath else "file:///android_asset/index.html") + hash)
        val cur = if (useCache) ver(cached) else ver(asset)
        Thread { update(cache, cur) }.start()
    }

    private fun ver(t: String): Int =
        Regex("APP_VER=(\\d+)").find(t)?.groupValues?.get(1)?.toInt() ?: 0

    private fun update(cache: File, cur: Int) {
        try {
            if (BuildConfig.REPO.isEmpty()) return
            val u = URL("https://raw.githubusercontent.com/${BuildConfig.REPO}/${BuildConfig.BRANCH}/app/src/main/assets/index.html")
            val c = u.openConnection() as HttpURLConnection
            c.connectTimeout = 8000
            c.readTimeout = 10000
            val t = c.inputStream.bufferedReader().use { it.readText() }
            if (ver(t) > cur) {
                cache.writeText(t)
                runOnUiThread { web.evaluateJavascript("toast('نسخهٔ جدید آماده شد. برنامه را ببند و دوباره باز کن')", null) }
            }
        } catch (e: Exception) {
        }
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
    private val p get() = c.getSharedPreferences("m", 0)

    @JavascriptInterface
    fun sync(today: String, month: String) {
        p.edit().putString("t", today).putString("m", month).apply()
        val mgr = AppWidgetManager.getInstance(c)
        Widget.update(c, mgr, mgr.getAppWidgetIds(ComponentName(c, Widget::class.java)))
        SmallWidget.update(c, mgr, mgr.getAppWidgetIds(ComponentName(c, SmallWidget::class.java)))
    }

    @JavascriptInterface
    fun hasAccess(): Boolean =
        (Settings.Secure.getString(c.contentResolver, "enabled_notification_listeners") ?: "").contains(c.packageName)

    @JavascriptInterface
    fun openAccess() {
        c.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    @JavascriptInterface
    fun seen(): String = p.getString("seen", "{}") ?: "{}"

    @JavascriptInterface
    fun allowed(): String = JSONArray((p.getStringSet("allowed", emptySet()) ?: emptySet()).toList()).toString()

    @JavascriptInterface
    fun setAllowed(j: String) {
        val a = JSONArray(j)
        val s = HashSet<String>()
        for (i in 0 until a.length()) s.add(a.getString(i))
        p.edit().putStringSet("allowed", s).apply()
    }

    @JavascriptInterface
    fun pending(): String = synchronized(NotifListener.LOCK) { p.getString("pending", "[]") ?: "[]" }

    @JavascriptInterface
    fun removePending(id: String) {
        synchronized(NotifListener.LOCK) {
            val a = JSONArray(p.getString("pending", "[]") ?: "[]")
            val o = JSONArray()
            for (i in 0 until a.length()) if (a.getJSONObject(i).getString("id") != id) o.put(a.getJSONObject(i))
            p.edit().putString("pending", o.toString()).apply()
        }
    }
}
