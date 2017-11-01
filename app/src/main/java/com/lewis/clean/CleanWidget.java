package com.lewis.clean;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class CleanWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Intent intent = new Intent(context, CleanService.class);
        PendingIntent serviceIntent = PendingIntent.getService(context, 0, intent, 0);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        remoteViews.setOnClickPendingIntent(R.id.imageButton_widget, serviceIntent);
        if (appWidgetIds.length > 0) {
            for (int appWidgetId : appWidgetIds) {
                appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            }
        }

    }
}