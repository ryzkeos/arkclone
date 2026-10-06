package com.arkeos.arkclone.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.arkeos.arkclone.MainActivity
import com.arkeos.arkclone.R
import com.arkeos.arkclone.model.ClonedInstance

class ArkNotificationEngine(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "ark_clone_instances_channel"
        const val CHANNEL_NAME = "Ark Clone Virtual Space Accounts"
        const val EXTRA_TARGET_INSTANCE_ID = "target_instance_id"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = "Account notifications mapped to isolated virtual spaces"
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun postInstanceNotification(
        instance: ClonedInstance,
        title: String,
        body: String
    ): Boolean {
        if (!instance.notificationsEnabled) return false

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_INSTANCE_ID, instance.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            instance.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("[${instance.badgeTag}] ${instance.appName}: $title")
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
                    .setSummaryText("${instance.alias} (${instance.appName} #${instance.cloneNumber})")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            val manager = NotificationManagerCompat.from(context)
            val notificationId = (instance.id + System.currentTimeMillis()).hashCode()
            manager.notify(notificationId, notification)
            return true
        } catch (e: SecurityException) {
            // Android 13+ runtime permission might not be granted yet
            return false
        }
    }
}
