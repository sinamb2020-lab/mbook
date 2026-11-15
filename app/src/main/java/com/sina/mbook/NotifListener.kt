package com.sina.mbook

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import org.json.JSONArray
import org.json.JSONObject

class NotifListener : NotificationListenerService() {
    companion object {
        val LOCK = Any()
    }

    private fun label(pkg: String): String = try {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
    } catch (e: Exception) {
        pkg
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val pkg = sbn.packageName
            if (pkg == packageName) return
            val p = getSharedPreferences("m", 0)
            val ex = sbn.notification.extras
            val title = ex.getCharSequence("android.title")?.toString() ?: ""
            val text = (ex.getCharSequence("android.bigText") ?: ex.getCharSequence("android.text"))?.toString() ?: ""
            synchronized(LOCK) {
                val seen = JSONObject(p.getString("seen", "{}") ?: "{}")
                if (!seen.has(pkg)) {
                    if (seen.length() > 40) seen.remove(seen.keys().next())
                    seen.put(pkg, label(pkg))
                }
                val ed = p.edit().putString("seen", seen.toString())
                val allowed = p.getStringSet("allowed", emptySet()) ?: emptySet()
                if (pkg in allowed && text.isNotBlank()) {
                    val id = sbn.postTime.toString() + pkg
                    val arr = JSONArray(p.getString("pending", "[]") ?: "[]")
                    var dup = false
                    for (i in 0 until arr.length()) if (arr.getJSONObject(i).getString("id") == id) dup = true
                    if (!dup) {
                        arr.put(JSONObject().put("id", id).put("pkg", pkg).put("title", title).put("text", text).put("t", sbn.postTime))
                        val out = JSONArray()
                        val from = if (arr.length() > 100) arr.length() - 100 else 0
                        for (i in from until arr.length()) out.put(arr.get(i))
                        ed.putString("pending", out.toString())
                    }
                }
                ed.apply()
            }
        } catch (e: Exception) {
        }
    }
}
