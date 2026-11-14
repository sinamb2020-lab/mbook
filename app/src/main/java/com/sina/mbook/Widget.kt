package com.sina.mbook

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class Widget : AppWidgetProvider() {
    override fun onUpdate(c: Context, m: AppWidgetManager, ids: IntArray) = update(c, m, ids)

    companion object {
        fun update(c: Context, m: AppWidgetManager, ids: IntArray) {
            val p = c.getSharedPreferences("m", 0)
            val i = Intent(c, MainActivity::class.java).putExtra("add", true)
            val pi = PendingIntent.getActivity(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            for (id in ids) {
                val v = RemoteViews(c.packageName, R.layout.widget)
                v.setTextViewText(R.id.today, p.getString("t", "۰") ?: "۰")
                v.setTextViewText(R.id.month, "این ماه: " + (p.getString("m", "۰") ?: "۰"))
                v.setOnClickPendingIntent(R.id.root, pi)
                m.updateAppWidget(id, v)
            }
        }
    }
}
