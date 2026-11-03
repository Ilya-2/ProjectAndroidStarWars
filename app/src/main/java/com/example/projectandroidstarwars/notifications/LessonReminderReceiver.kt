package com.example.projectandroidstarwars.notifications

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.projectandroidstarwars.MainActivity
import com.example.projectandroidstarwars.R

class LessonReminderReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action != LessonReminderScheduler.ACTION_REMIND) {
            return
        }

        val scheduler = LessonReminderScheduler(context)
        scheduler.createNotificationChannel()

        if (!scheduler.canPostNotifications()) return

        val fullName = intent.getStringExtra(
            LessonReminderScheduler.EXTRA_FULL_NAME
        ).orEmpty().ifBlank {
            "Пользователь"
        }

        val lessonTime = intent.getStringExtra(
            LessonReminderScheduler.EXTRA_LESSON_TIME
        ).orEmpty()

        val openAppIntent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            LessonReminderScheduler.NOTIFICATION_ID,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val message =
            "$fullName, твоя любимая пара начинается в $lessonTime!"

        val notification = NotificationCompat.Builder(
            context,
            LessonReminderScheduler.CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_lesson_notification)
            .setContentTitle("Время любимой пары")
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                LessonReminderScheduler.NOTIFICATION_ID,
                notification
            )
        } catch (error: SecurityException) {
            Log.w(
                "LessonReminder",
                "Нет разрешения на показ уведомления.",
                error
            )
        }
    }
}