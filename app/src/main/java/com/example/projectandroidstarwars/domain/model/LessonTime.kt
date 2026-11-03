package com.example.projectandroidstarwars.domain.model

import java.util.Calendar
import java.util.Locale

object LessonTime {

    private val timePattern =
        Regex("([01][0-9]|2[0-3]):[0-5][0-9]")

    fun validationError(value: String): String? {
        return when {
            value.isBlank() -> {
                "Укажи время любимой пары."
            }

            !timePattern.matches(value) -> {
                "Введи время в формате HH:mm, например 09:05."
            }

            else -> null
        }
    }

    fun format(hour: Int, minute: Int): String {
        return String.format(
            Locale.ROOT,
            "%02d:%02d",
            hour,
            minute
        )
    }

    fun todayTimestamp(
        value: String,
        nowMillis: Long = System.currentTimeMillis()
    ): Long? {
        if (validationError(value) != null) return null

        val hour = value.substring(0, 2).toInt()
        val minute = value.substring(3, 5).toInt()

        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return calendar.timeInMillis
    }
}