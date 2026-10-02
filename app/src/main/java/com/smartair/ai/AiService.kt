package com.smartair.ai

import com.smartair.data.model.SensorReading
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import com.google.ai.client.generativeai.GenerativeModel

/**
 * AI service interface - replaceable for production.
 * The AI is ONLY for explanation and suggestion.
 * It NEVER controls hardware or makes safety decisions.
 */
interface AiService {
    suspend fun getInsight(reading: SensorReading, trends: Map<String, String>): String
    suspend fun chat(userMessage: String, reading: SensorReading, trends: Map<String, String>): String
    fun isAvailable(): Boolean
}

/**
 * Development AI service that generates contextual responses locally.
 * Does not require an API key or internet connection.
 * For production, replace with a secure proxy implementation.
 */
class LocalAiService : AiService {

    override suspend fun getInsight(reading: SensorReading, trends: Map<String, String>): String {
        return withContext(Dispatchers.Default) {
            generateInsight(reading, trends)
        }
    }

    override suspend fun chat(
        userMessage: String,
        reading: SensorReading,
        trends: Map<String, String>
    ): String {
        return withContext(Dispatchers.Default) {
            generateChatResponse(userMessage, reading, trends)
        }
    }

    override fun isAvailable(): Boolean = true

    private fun generateInsight(reading: SensorReading, trends: Map<String, String>): String {
        val parts = mutableListOf<String>()

        // Temperature insight
        reading.temperature?.let { temp ->
            when {
                temp > 35 -> parts.add("Room temperature is quite high at ${temp}°C. Consider improving air circulation or turning on cooling.")
                temp > 30 -> parts.add("Temperature is warm at ${temp}°C.")
                temp < 18 -> parts.add("Room temperature is cool at ${temp}°C.")
                else -> parts.add("Temperature is comfortable at ${temp}°C.")
            }
        }

        // Dust insight
        reading.dust?.let { dust ->
            val trend = trends["dust_trend"] ?: "stable"
            when {
                dust > 400 -> parts.add("Dust reading is significantly elevated at $dust. " +
                        if (reading.fan) "Ventilation is active and working to reduce levels." 
                        else "Consider activating ventilation.")
                dust > 250 -> parts.add("Dust has increased slightly${if (trend == "increasing") " and appears to be rising" else ""}. " +
                        if (reading.fan) "Ventilation is helping bring it back down." else "")
                else -> parts.add("Dust levels look normal at $dust.")
            }
        }

        // Gas insight
        reading.gas?.let { gas ->
            when {
                gas > 500 -> parts.add("MQ-5 gas sensor reading is elevated at $gas. Recommend checking for gas sources and ensuring ventilation.")
                gas > 350 -> parts.add("MQ-5 reading is slightly above baseline at $gas.")
                else -> parts.add("MQ-5 reading is normal at $gas.")
            }
        }

        // Fan status
        if (reading.fan) {
            parts.add("Ventilation is currently active, improving room circulation.")
        }

        return if (parts.isEmpty()) {
            "Room conditions appear stable. All sensor readings are within expected ranges."
        } else {
            parts.joinToString(" ")
        }
    }

    private fun generateChatResponse(
        userMessage: String,
        reading: SensorReading,
        trends: Map<String, String>
    ): String {
        val msg = userMessage.lowercase()

        return when {
            msg.contains("why") && (msg.contains("warning") || msg.contains("status")) ->
                buildWarningExplanation(reading)

            msg.contains("improving") || msg.contains("better") ->
                buildImprovementAnalysis(reading, trends)

            msg.contains("fan") || msg.contains("ventilation") ->
                buildFanAdvice(reading)

            msg.contains("explain") || msg.contains("reading") || msg.contains("today") ->
                buildReadingSummary(reading, trends)

            msg.contains("dust") ->
                "The dust sensor is currently reading ${reading.dust ?: "unavailable"}. " +
                        "This is a raw sensor value from the GP2Y1010 dust sensor. " +
                        if ((reading.dust ?: 0) > 250) "The reading is above the typical comfort threshold."
                        else "The reading appears within normal range."

            msg.contains("temperature") || msg.contains("temp") ->
                "Current temperature is ${reading.temperature ?: "unavailable"}°C. " +
                        "The DHT11 sensor provides this reading. " +
                        if ((reading.temperature ?: 0f) > 30) "The room is warmer than typical comfort levels."
                        else "Temperature appears comfortable."

            msg.contains("humidity") ->
                "Humidity is at ${reading.humidity ?: "unavailable"}%. " +
                        if ((reading.humidity ?: 0f) > 70) "This is relatively high. Opening a window or using ventilation can help reduce moisture."
                        else "This is within a comfortable range."

            msg.contains("gas") || msg.contains("mq") ->
                "The MQ-5 gas sensor shows a raw reading of ${reading.gas ?: "unavailable"}. " +
                        "This sensor responds to LPG, natural gas, and coal gas. " +
                        "Note: The raw reading requires calibration for specific gas concentration measurements."

            else ->
                "Based on current readings — Temperature: ${reading.temperature ?: "--"}°C, " +
                        "Humidity: ${reading.humidity ?: "--"}%, Dust: ${reading.dust ?: "--"}, " +
                        "Gas: ${reading.gas ?: "--"} — the room is currently " +
                        when (reading.status) {
                            com.smartair.data.model.AirStatus.NORMAL -> "in good condition."
                            com.smartair.data.model.AirStatus.WARNING -> "showing elevated readings that may need attention."
                            com.smartair.data.model.AirStatus.CRITICAL -> "in a critical state that requires immediate attention."
                        }
        }
    }

    private fun buildWarningExplanation(reading: SensorReading): String {
        val issues = mutableListOf<String>()
        reading.dust?.let { if (it > 250) issues.add("Dust is elevated at $it") }
        reading.gas?.let { if (it > 350) issues.add("MQ-5 gas reading is above baseline at $it") }
        reading.temperature?.let { if (it > 32) issues.add("Temperature is warm at ${it}°C") }
        reading.humidity?.let { if (it > 70) issues.add("Humidity is high at ${it}%") }

        return if (issues.isEmpty()) {
            "The current readings are actually within normal ranges. The status may have been set by the Arduino based on its own threshold logic."
        } else {
            "The warning status is triggered because: ${issues.joinToString("; ")}. " +
                    if (reading.fan) "Ventilation is active and should help improve conditions." 
                    else "Consider activating ventilation to improve air circulation."
        }
    }

    private fun buildImprovementAnalysis(reading: SensorReading, trends: Map<String, String>): String {
        val dustTrend = trends["dust_trend"] ?: "stable"
        val tempTrend = trends["temperature_trend"] ?: "stable"

        return "Based on recent trends: " +
                "Dust is $dustTrend, Temperature is $tempTrend. " +
                if (dustTrend == "decreasing" || tempTrend == "decreasing") {
                    "Yes, conditions appear to be improving. "
                } else if (dustTrend == "stable" && tempTrend == "stable") {
                    "Conditions are holding steady. "
                } else {
                    "Some readings are still trending upward. "
                } +
                if (reading.fan) "Active ventilation is contributing to air quality improvement."
                else ""
    }

    private fun buildFanAdvice(reading: SensorReading): String {
        return if (reading.fan) {
            "The ventilation fan is currently active. " +
                    when {
                        (reading.dust ?: 0) > 300 -> "Given the elevated dust reading of ${reading.dust}, it's recommended to keep it running until levels drop below 250."
                        (reading.temperature ?: 0f) > 30 -> "With temperature at ${reading.temperature}°C, continued ventilation will help improve comfort."
                        else -> "Current readings suggest the room is in good condition. The fan can be turned off when convenient."
                    }
        } else {
            "The ventilation fan is currently off. " +
                    when {
                        (reading.dust ?: 0) > 250 -> "Consider turning it on — dust levels are at ${reading.dust}."
                        (reading.temperature ?: 0f) > 32 -> "It may help with the warm temperature of ${reading.temperature}°C."
                        else -> "Current conditions don't urgently require ventilation."
                    }
        }
    }

    private fun buildReadingSummary(reading: SensorReading, trends: Map<String, String>): String {
        return "Current sensor summary:\n" +
                "• Temperature: ${reading.temperature ?: "--"}°C (${trends["temperature_trend"] ?: "no data"})\n" +
                "• Humidity: ${reading.humidity ?: "--"}% (${trends["humidity_trend"] ?: "no data"})\n" +
                "• Dust: ${reading.dust ?: "--"} raw (${trends["dust_trend"] ?: "no data"})\n" +
                "• MQ-5 Gas: ${reading.gas ?: "--"} raw (${trends["gas_trend"] ?: "no data"})\n" +
                "• Ventilation: ${if (reading.fan) "Active" else "Off"}\n" +
                "• Status: ${reading.status.name}"
    }
}

class GeminiAiService(private val apiKey: String) : AiService {
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-3.1-flash-lite",
        apiKey = apiKey
    )

    override suspend fun getInsight(reading: SensorReading, trends: Map<String, String>): String {
        val prompt = "Generate a very brief, crisp 1-2 sentence insight for these sensor readings: Temperature ${reading.temperature}°C (${trends["temperature_trend"]}), Humidity ${reading.humidity}% (${trends["humidity_trend"]}), Dust ${reading.dust} (${trends["dust_trend"]}), Gas ${reading.gas} (${trends["gas_trend"]}). Fan is ${if(reading.fan) "on" else "off"}. Keep it extremely minimal and avoid large paragraphs."
        return callGemini(prompt) ?: LocalAiService().getInsight(reading, trends)
    }

    override suspend fun chat(
        userMessage: String,
        reading: SensorReading,
        trends: Map<String, String>
    ): String {
        val prompt = "Context: Temperature ${reading.temperature}°C, Humidity ${reading.humidity}%, Dust ${reading.dust}, Gas ${reading.gas}. Fan is ${if(reading.fan) "on" else "off"}.\nUser says: $userMessage\nSystem Instructions: You are a smart room assistant. You MUST provide very crisp, minimal, and direct responses. Do NOT use large paragraphs. Answer strictly in a brief, concise manner."
        return callGemini(prompt) ?: LocalAiService().chat(userMessage, reading, trends)
    }

    override fun isAvailable(): Boolean = true

    private suspend fun callGemini(prompt: String): String? {
        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "AI returned an empty response"
        } catch (e: Exception) {
            e.printStackTrace()
            "AI API Error: ${e.message}"
        }
    }
}
