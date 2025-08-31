package com.example.wordsapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.widget.RemoteViews
import java.util.Date
import java.util.Locale

class MyAppWidgetProvider : AppWidgetProvider() {

    private lateinit var streakManager: StreakManager
    // Этот метод вызывается с периодичностью, которую вы задали в metadata,
    // а также при добавлении каждого виджета.
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // appWidgetIds - массив ID всех экземпляров вашего виджета, добавленных на главный экран.
        // Нужно обновить каждый из них.
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    // Вспомогательная функция для обновления виджета
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        streakManager = StreakManager(context)
        // Здесь мы готовим данные, которые хотим показать в виджете
        val widgetText = "${streakManager.getCurrentStreak()}"

        // Создаем Intent для открытия MainActivity при нажатии на виджет
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Создаем RemoteViews, который управляет макетом, находящимся в другом процессе
        val views = RemoteViews(context.packageName, R.layout.my_widget_layout)

        // Устанавливаем текст для TextView с id @+id/widget_text
        views.setTextViewText(R.id.widget_text, widgetText)
        if(!streakManager.isStreakUpdatedToday()) {
            views.setTextColor(R.id.widget_text, Color.GRAY)
        }

        // Вешаем обработчик нажатия на весь виджет (или на отдельную кнопку)
        views.setOnClickPendingIntent(R.id.widget_layout_root, pendingIntent)

        // Говорим AppWidgetManager обновить виджет
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}