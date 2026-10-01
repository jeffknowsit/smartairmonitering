package com.smartair.alerts.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.smartair.data.model.AirStatus

/**
 * Optional SMS alert service.
 * Only sends when explicitly enabled by user and a recipient is configured.
 * Implements cooldown protection to prevent SMS spam.
 */
class SmsService(private val context: Context) {

    companion object {
        private const val TAG = "SmartAirSMS"
    }

    private var lastSmsSentTime = 0L
    private var cooldownMs = 15 * 60_000L // 15 minutes default

    fun setCooldown(minutes: Int) {
        cooldownMs = minutes * 60_000L
    }

    /**
     * Send SMS alert if conditions are met:
     * - CRITICAL status
     * - SMS is enabled
     * - Recipient is configured
     * - Cooldown has elapsed
     * - SMS permission is granted
     */
    fun onCriticalAlert(
        status: AirStatus,
        recipientNumber: String,
        smsEnabled: Boolean
    ): SmsResult {
        if (!smsEnabled) return SmsResult.DISABLED
        if (recipientNumber.isBlank()) return SmsResult.NO_RECIPIENT
        if (status != AirStatus.CRITICAL) return SmsResult.NOT_CRITICAL

        val now = System.currentTimeMillis()
        if (now - lastSmsSentTime < cooldownMs) {
            return SmsResult.COOLDOWN_ACTIVE
        }

        if (!hasSmsPermission()) {
            return SmsResult.NO_PERMISSION
        }

        return try {
            val smsManager = context.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(
                recipientNumber,
                null,
                "SmartAir CRITICAL Alert: Environmental readings have reached critical levels. " +
                        "Please check the room immediately.",
                null,
                null
            )
            lastSmsSentTime = now
            Log.d(TAG, "SMS alert sent to $recipientNumber")
            SmsResult.SENT
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS: ${e.message}")
            SmsResult.SEND_FAILED
        }
    }

    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
}

enum class SmsResult {
    SENT,
    DISABLED,
    NO_RECIPIENT,
    NOT_CRITICAL,
    COOLDOWN_ACTIVE,
    NO_PERMISSION,
    SEND_FAILED
}
