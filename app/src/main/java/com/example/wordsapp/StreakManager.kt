package com.example.wordsapp

import android.content.Context
import android.icu.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

class StreakManager(context: Context) {
    private val prefs = context.getSharedPreferences("streak_prefs", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        // Инициализация при первом запуске
        if (prefs.getString("last_date", null) == null) {
            resetStreak()
        }
    }

    fun checkStreak(): Int {
        val today = dateFormat.format(Date())
        val lastDate = prefs.getString("last_date", "") ?: ""

        // Если даты совпадают - возвращаем текущий стрик
        if (today == lastDate) {
            return prefs.getInt("current_streak", 0)
        }

        // Проверяем разницу в днях
        val daysDiff = getDaysDifference(lastDate, today)

        return when {
            daysDiff == 1 -> incrementStreak(today) // Вчера отметились
            daysDiff == 0 -> prefs.getInt("current_streak", 0) // Сегодня уже отмечали
            else -> resetStreak(today) // Сброс стрика
        }
    }

    private fun getDaysDifference(dateStr1: String, dateStr2: String): Int {
        return try {
            val date1 = dateFormat.parse(dateStr1) ?: return -1
            val date2 = dateFormat.parse(dateStr2) ?: return -1
            val diff = date2.time - date1.time
            (diff / (24 * 60 * 60 * 1000)).toInt()
        } catch (e: Exception) {
            -1
        }
    }

    private fun incrementStreak(today: String): Int {
        val newStreak = prefs.getInt("current_streak", 0) + 1
        prefs.edit().apply {
            putString("last_date", today)
            putInt("current_streak", newStreak)
            if (newStreak > prefs.getInt("max_streak", 0)) {
                putInt("max_streak", newStreak)
            }
            apply()
        }
        return newStreak
    }

    private fun resetStreak(today: String = dateFormat.format(Date())): Int {
        prefs.edit().apply {
            putString("last_date", today)
            putInt("current_streak", 1)
            apply()
        }
        return 1
    }

    fun isStreakUpdatedToday(): Boolean {
        val today = dateFormat.format(Date())
        val lastDate = prefs.getString("last_date", null)

        return lastDate == today
    }

    fun getCurrentStreak(): Int = prefs.getInt("current_streak", 0)
}