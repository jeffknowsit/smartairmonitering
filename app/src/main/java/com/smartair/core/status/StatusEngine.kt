package com.smartair.core.status

import com.smartair.data.model.AirStatus
import com.smartair.data.model.AppSettings
import com.smartair.data.model.SensorReading

/**
 * Local rule-based air status engine.
 * Operates independently of AI — this is the safety-critical path.
 * Determines NORMAL/WARNING/CRITICAL based on configurable thresholds.
 */
object StatusEngine {

    /**
     * Evaluate sensor reading against thresholds and return appropriate status.
     * Uses raw sensor values with configurable thresholds.
     * Does NOT claim exact pollutant concentrations.
     */
    fun evaluate(reading: SensorReading, settings: AppSettings): AirStatus {
        var hasCritical = false
        var hasWarning = false

        // Temperature check
        reading.temperature?.let { temp ->
            when {
                temp >= settings.tempCriticalThreshold -> hasCritical = true
                temp >= settings.tempWarningThreshold -> hasWarning = true
            }
        }

        // Humidity check
        reading.humidity?.let { hum ->
            when {
                hum >= settings.humidityCriticalThreshold -> hasCritical = true
                hum >= settings.humidityWarningThreshold -> hasWarning = true
            }
        }

        // Dust check (raw sensor value)
        reading.dust?.let { dust ->
            when {
                dust >= settings.dustCriticalThreshold -> hasCritical = true
                dust >= settings.dustWarningThreshold -> hasWarning = true
            }
        }

        // Gas / MQ-5 check (raw sensor value)
        reading.gas?.let { gas ->
            when {
                gas >= settings.gasCriticalThreshold -> hasCritical = true
                gas >= settings.gasWarningThreshold -> hasWarning = true
            }
        }

        return when {
            hasCritical -> AirStatus.CRITICAL
            hasWarning -> AirStatus.WARNING
            else -> AirStatus.NORMAL
        }
    }

    /**
     * Generate headline, subtext, and kicker for the hero card based on status and readings.
     */
    fun getStatusDisplay(reading: SensorReading, status: AirStatus, settings: AppSettings): StatusDisplay {
        return when (status) {
            AirStatus.NORMAL -> StatusDisplay(
                headline = "Good air",
                subtext = "Everything looks normal and fresh.",
                kicker = "Atmosphere Balanced",
                scoreBadge = "Optimal"
            )
            AirStatus.WARNING -> {
                val elevated = getElevatedSensors(reading, settings)
                StatusDisplay(
                    headline = when {
                        elevated.contains("dust") -> "Dust Elevated"
                        elevated.contains("temperature") -> "Warm Room"
                        elevated.contains("humidity") -> "High Humidity"
                        elevated.contains("gas") -> "Gas Detected"
                        else -> "Elevated Readings"
                    },
                    subtext = "One or more readings exceed comfort levels.",
                    kicker = "Airflow Recommended",
                    scoreBadge = "Elevated"
                )
            }
            AirStatus.CRITICAL -> StatusDisplay(
                headline = "Air Alert",
                subtext = "Rapid environmental change detected.",
                kicker = "Action Required",
                scoreBadge = "Critical"
            )
        }
    }

    /**
     * Get trend descriptions for recent data.
     */
    fun getTrendDescription(recent: List<Float>): String {
        if (recent.size < 3) return "→ Insufficient data"
        val last5 = recent.takeLast(5)
        val first = last5.first()
        val last = last5.last()
        val change = last - first
        val pct = if (first != 0f) (change / first * 100) else 0f

        return when {
            pct > 5 -> "↑ Rising"
            pct < -5 -> "↓ Falling"
            else -> "→ Stable"
        }
    }

    private fun getElevatedSensors(reading: SensorReading, settings: AppSettings): List<String> {
        val elevated = mutableListOf<String>()
        reading.temperature?.let { if (it >= settings.tempWarningThreshold) elevated.add("temperature") }
        reading.humidity?.let { if (it >= settings.humidityWarningThreshold) elevated.add("humidity") }
        reading.dust?.let { if (it >= settings.dustWarningThreshold) elevated.add("dust") }
        reading.gas?.let { if (it >= settings.gasWarningThreshold) elevated.add("gas") }
        return elevated
    }
}

data class StatusDisplay(
    val headline: String,
    val subtext: String,
    val kicker: String,
    val scoreBadge: String
)
