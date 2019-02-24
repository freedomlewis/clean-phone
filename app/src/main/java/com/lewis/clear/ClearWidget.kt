package com.lewis.clear

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class ClearWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val intent = Intent(context, ClearActivity::class.java)
        val activityIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout)
        remoteViews.setOnClickPendingIntent(R.id.imageButton_widget, activityIntent)
        if (appWidgetIds.size > 0) {
            for (appWidgetId in appWidgetIds) {
                appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
            }
        }

    }
}