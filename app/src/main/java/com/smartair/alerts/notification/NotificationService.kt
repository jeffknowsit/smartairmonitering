package com.smartair.alerts.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.smartair.MainActivity
import com.smartair.data.model.AirStatus

/**
 * Manages Android notifications for SmartAir alerts.
 * Implements cooldown/debouncing to avoid notification spam.
 */
class NotificationService(private val context: Context) {

    companion object {
        const val CHANNEL_ALERTS = "smartair_alerts"
        const val CHANNEL_RECOVERY = "smartair_recovery"
        private const val NOTIFICATION_ID_ALERT = 1001
        private const val NOTIFICATION_ID_RECOVERY = 1002
        private const val COOLDOWN_MS = 60_000L // 1 minute cooldown
    }

    private var lastAlertTime = 0L
    private var lastAlertStatus: AirStatus? = null

    fun createNotificationChannels() {
        val alertChannel = NotificationChannel(
            CHANNEL_ALERTS,
            "SmartAir Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Environmental warnings and critical alerts"
            enableVibration(true)
        }

        val recoveryChannel = NotificationChannel(
            CHANNEL_RECOVERY,
            "SmartAir Recovery",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Recovery notifications when readings normalize"
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(alertChannel)
        manager.createNotificationChannel(recoveryChannel)
    }

    /**
     * Notify on status transitions with debouncing.
     * Only fires on state CHANGE, not continuously during same condition.
     */
    fun onStatusChange(newStatus: AirStatus, previousStatus: AirStatus?) {
        if (!hasNotificationPermission()) return

        val now = System.currentTimeMillis()

        when {
            // Transition to WARNING
            newStatus == AirStatus.WARNING && previousStatus != AirStatus.WARNING -> {
                if (now - lastAlertTime > COOLDOWN_MS) {
                    sendAlert(
                        title = "SmartAir Warning",
                        message = "Elevated environmental reading detected.",
                        channel = CHANNEL_ALERTS,
                        notificationId = NOTIFICATION_ID_ALERT
                    )
                    lastAlertTime = now
                    lastAlertStatus = newStatus
                }
            }
            // Transition to CRITICAL
            newStatus == AirStatus.CRITICAL && previousStatus != AirStatus.CRITICAL -> {
                sendAlert(
                    title = "SmartAir Critical",
                    message = "Critical environmental reading detected. Check the room.",
                    channel = CHANNEL_ALERTS,
                    notificationId = NOTIFICATION_ID_ALERT
                )
                lastAlertTime = now
                lastAlertStatus = newStatus
            }
            // Recovery to NORMAL from WARNING/CRITICAL
            newStatus == AirStatus.NORMAL &&
                    (previousStatus == AirStatus.WARNING || previousStatus == AirStatus.CRITICAL) -> {
                sendAlert(
                    title = "SmartAir",
                    message = "Room readings have returned toward normal.",
                    channel = CHANNEL_RECOVERY,
                    notificationId = NOTIFICATION_ID_RECOVERY
                )
                lastAlertStatus = null
            }
        }
    }

    private fun sendAlert(title: String, message: String, channel: String, notificationId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(
                if (channel == CHANNEL_ALERTS) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
