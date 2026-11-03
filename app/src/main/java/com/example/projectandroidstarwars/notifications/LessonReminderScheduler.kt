package com.example.projectandroidstarwars.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.projectandroidstarwars.domain.model.UserProfile
import com.example.projectandroidstarwars.domain.repository.ProfileException

class LessonReminderScheduler(
    context: Context
) {

    private val appContext = context.applicationContext

    private val alarmManager = requireNotNull(
        appContext.getSystemService(AlarmManager::class.java)
    )

    private val notificationManager = requireNotNull(
        appContext.getSystemService(NotificationManager::class.java)
    )

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Любимая пара",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Напоминание о начале любимой пары"
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    fun canPostNotifications(): Boolean {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        if (
            !NotificationManagerCompat.from(appContext)
                .areNotificationsEnabled()
        ) {
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager
                .getNotificationChannel(CHANNEL_ID)

            if (
                channel != null &&
                channel.importance == NotificationManager.IMPORTANCE_NONE
            ) {
                return false
            }
        }

        return true
    }

    fun canScheduleExactAlarms(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()
    }

    fun checkPermissions() {
        createNotificationChannel()

        if (!canPostNotifications()) {
            throw ProfileException(
                "Разреши уведомления для приложения и канала «Любимая пара»."
            )
        }

        if (!canScheduleExactAlarms()) {
            throw ProfileException(
                "Разреши приложению устанавливать точные напоминания."
            )
        }
    }

    fun schedule(
        profile: UserProfile,
        triggerAtMillis: Long
    ) {
        checkPermissions()

        if (triggerAtMillis <= System.currentTimeMillis()) {
            throw ProfileException(
                "Выбранное время уже прошло. Укажи более позднее время сегодня."
            )
        }

        val intent = Intent(
            appContext,
            LessonReminderReceiver::class.java
        ).apply {
            action = ACTION_REMIND
            putExtra(EXTRA_FULL_NAME, profile.fullName)
            putExtra(EXTRA_LESSON_TIME, profile.favoriteLessonTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (_: SecurityException) {
            throw ProfileException(
                "Не удалось установить напоминание. " +
                        "Проверь доступ к точным будильникам."
            )
        }
    }

    companion object {
        const val CHANNEL_ID = "favorite_lesson"
        const val NOTIFICATION_ID = 7001

        const val ACTION_REMIND =
            "com.example.projectandroidstarwars.REMIND_LESSON"

        const val EXTRA_FULL_NAME = "full_name"
        const val EXTRA_LESSON_TIME = "lesson_time"

        private const val REQUEST_CODE = 7001
    }
}