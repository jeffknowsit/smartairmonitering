package com.smartair.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Core sensor reading data class.
 * Represents a single snapshot from Arduino sensors.
 */
@Entity(tableName = "sensor_readings")
data class SensorReading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val temperature: Float? = null,
    val humidity: Float? = null,
    val dust: Int? = null,
    val gas: Int? = null,
    val fan: Boolean = false,
    val buzzer: Boolean = false,
    val status: AirStatus = AirStatus.NORMAL
)

/**
 * Air quality status levels.
 * Determined by local rule engine, NOT by AI.
 */
enum class AirStatus {
    NORMAL,
    WARNING,
    CRITICAL
}

/**
 * Arduino USB connection state.
 */
enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    PERMISSION_REQUIRED,
    ERROR
}

/**
 * Individual sensor health state.
 */
enum class SensorState {
    AVAILABLE,
    ERROR,
    NO_DATA
}

/**
 * Complete dashboard state for UI consumption.
 */
data class DashboardState(
    val currentReading: SensorReading = SensorReading(),
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val airStatus: AirStatus = AirStatus.NORMAL,
    val statusHeadline: String = "Offline",
    val statusSubtext: String = "Connect Arduino to begin monitoring.",
    val statusKicker: String = "Hardware Pause",
    val aiInsight: String = "Connect your Arduino sensor board to receive AI-powered insights.",
    val lastUpdated: Long = 0L,
    val recentTemps: List<Float> = emptyList(),
    val recentHumidity: List<Float> = emptyList(),
    val recentDust: List<Float> = emptyList(),
    val recentGas: List<Float> = emptyList(),
    val isMockMode: Boolean = true
)

/**
 * AI chat message.
 */
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val sensorContext: SensorReading? = null
)

/**
 * History screen state.
 */
data class HistoryState(
    val selectedMetric: MetricType = MetricType.DUST,
    val selectedRange: TimeRange = TimeRange.HOURS_24,
    val readings: List<SensorReading> = emptyList(),
    val average: Float = 0f,
    val peak: Float = 0f,
    val peakTime: Long = 0L,
    val minimum: Float = 0f,
    val minimumTime: Long = 0L,
    val warningCount: Int = 0,
    val isLoading: Boolean = false
)

enum class MetricType {
    TEMPERATURE, HUMIDITY, DUST, GAS
}

enum class TimeRange(val label: String, val hours: Long) {
    HOURS_24("24H", 24),
    DAYS_7("7D", 168),
    DAYS_30("30D", 720)
}

/**
 * Settings preferences.
 */
data class AppSettings(
    val roomName: String = "Living Room",
    val arduinoAutoConnect: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val notificationPriority: String = "High priority only",
    val buzzerEnabled: Boolean = true,
    val dustWarningThreshold: Int = 270,
    val dustCriticalThreshold: Int = 500,
    val gasWarningThreshold: Int = 400,
    val gasCriticalThreshold: Int = 600,
    val tempWarningThreshold: Float = 35f,
    val tempCriticalThreshold: Float = 40f,
    val humidityWarningThreshold: Float = 75f,
    val humidityCriticalThreshold: Float = 90f,
    val aiSuggestionsEnabled: Boolean = true,
    val aiContextWindowHours: Int = 4,
    val aiApiKey: String = "",
    val smsEnabled: Boolean = false,
    val smsRecipient: String = "",
    val smsCooldownMinutes: Int = 15,
    val keepScreenAwake: Boolean = true,
    val storageIntervalSeconds: Int = 10,
    val appearance: String = "System default",
    val isMockMode: Boolean = false // Developer toggle
)

/**
 * Mock scenario definitions for development without hardware.
 */
enum class MockScenario(val label: String) {
    NORMAL("Normal"),
    WARNING_DUST("Warning (Dust)"),
    WARNING_TEMPERATURE("Warning (Temperature)"),
    WARNING_HUMIDITY("Warning (Humidity)"),
    CRITICAL("Critical"),
    DISCONNECTED("Disconnected"),
    SENSOR_ERROR("Sensor Error"),
    AI_OFFLINE("AI Offline")
}
