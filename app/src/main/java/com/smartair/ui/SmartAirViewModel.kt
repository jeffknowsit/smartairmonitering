package com.smartair.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartair.ai.AiService
import com.smartair.ai.LocalAiService
import com.smartair.alerts.notification.NotificationService
import com.smartair.alerts.sms.SmsService
import com.smartair.core.status.StatusEngine
import com.smartair.data.database.SmartAirDatabase
import com.smartair.data.mock.MockArduinoService
import com.smartair.data.model.*
import com.smartair.data.repository.SensorRepository
import com.smartair.hardware.usb.ArduinoSerialService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Central ViewModel connecting all services to the UI.
 * Manages sensor data flow, status evaluation, alerts, and AI.
 */
class SmartAirViewModel(application: Application) : AndroidViewModel(application) {

    // Services
    private val database = SmartAirDatabase.getDatabase(application)
    private val arduinoService = ArduinoSerialService(application)
    private val mockService = MockArduinoService()
    private val notificationService = NotificationService(application)
    private val smsService = SmsService(application)
    private val aiService: AiService = LocalAiService()

    val repository = SensorRepository(
        dao = database.sensorReadingDao(),
        arduinoService = arduinoService,
        mockService = mockService
    )

    // State
    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private val _historyState = MutableStateFlow(HistoryState())
    val historyState: StateFlow<HistoryState> = _historyState.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _selectedMockScenario = MutableStateFlow(MockScenario.NORMAL)
    val selectedMockScenario: StateFlow<MockScenario> = _selectedMockScenario.asStateFlow()

    private var previousStatus: AirStatus? = null

    init {
        notificationService.createNotificationChannels()
        startMonitoring()
    }

    private fun startMonitoring() {
        // Connect arduino or mock based on settings
        repository.setMockMode(_settings.value.isMockMode)
        repository.startCollecting(viewModelScope)

        // Collect sensor readings
        viewModelScope.launch {
            repository.currentReading.collect { reading ->
                updateDashboard(reading)
            }
        }

        // Collect connection state
        viewModelScope.launch {
            repository.connectionState.collect { state ->
                _dashboardState.update { it.copy(connectionState = state) }
            }
        }

        // Collect recent data for sparklines
        viewModelScope.launch {
            combine(
                repository.recentTemps,
                repository.recentHumidity,
                repository.recentDust,
                repository.recentGas
            ) { temps, hum, dust, gas ->
                _dashboardState.update {
                    it.copy(
                        recentTemps = temps,
                        recentHumidity = hum,
                        recentDust = dust,
                        recentGas = gas
                    )
                }
            }.collect()
        }

        // Generate initial AI insight
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            refreshAiInsight()
        }
    }

    private fun updateDashboard(reading: SensorReading) {
        val currentSettings = _settings.value
        val status = StatusEngine.evaluate(reading, currentSettings)
        val display = StatusEngine.getStatusDisplay(reading, status, currentSettings)

        // Status transition detection for alerts
        if (previousStatus != null && previousStatus != status) {
            if (status == AirStatus.CRITICAL && currentSettings.smsEnabled) {
                // User requested SMS INSTEAD of standard notification for critical alerts
                smsService.onCriticalAlert(
                    status = status,
                    recipientNumber = currentSettings.smsRecipient,
                    smsEnabled = currentSettings.smsEnabled
                )
            } else {
                // Standard notification for all other state changes
                notificationService.onStatusChange(status, previousStatus)
            }
        }
        previousStatus = status

        _dashboardState.update {
            it.copy(
                currentReading = reading,
                airStatus = status,
                statusHeadline = display.headline,
                statusSubtext = display.subtext,
                statusKicker = display.kicker,
                lastUpdated = System.currentTimeMillis(),
                isMockMode = _settings.value.isMockMode
            )
        }
    }

    fun refreshAiInsight() {
        viewModelScope.launch {
            try {
                val reading = _dashboardState.value.currentReading
                val trends = mapOf(
                    "dust_trend" to StatusEngine.getTrendDescription(_dashboardState.value.recentDust),
                    "temperature_trend" to StatusEngine.getTrendDescription(_dashboardState.value.recentTemps),
                    "humidity_trend" to StatusEngine.getTrendDescription(_dashboardState.value.recentHumidity),
                    "gas_trend" to StatusEngine.getTrendDescription(_dashboardState.value.recentGas)
                )
                val insight = aiService.getInsight(reading, trends)
                _dashboardState.update { it.copy(aiInsight = insight) }
            } catch (e: Exception) {
                _dashboardState.update {
                    it.copy(aiInsight = "AI analysis unavailable.")
                }
            }
        }
    }

    // Chat
    fun sendChatMessage(message: String) {
        val reading = _dashboardState.value.currentReading

        val userMsg = ChatMessage(
            content = message,
            isUser = true,
            sensorContext = reading
        )
        _chatMessages.update { it + userMsg }

        viewModelScope.launch {
            try {
                val trends = mapOf(
                    "dust_trend" to StatusEngine.getTrendDescription(_dashboardState.value.recentDust),
                    "temperature_trend" to StatusEngine.getTrendDescription(_dashboardState.value.recentTemps),
                    "humidity_trend" to StatusEngine.getTrendDescription(_dashboardState.value.recentHumidity),
                    "gas_trend" to StatusEngine.getTrendDescription(_dashboardState.value.recentGas)
                )
                val response = aiService.chat(message, reading, trends)
                val aiMsg = ChatMessage(
                    content = response,
                    isUser = false
                )
                _chatMessages.update { it + aiMsg }
            } catch (e: Exception) {
                val errMsg = ChatMessage(
                    content = "AI analysis unavailable. Sensor monitoring continues normally.",
                    isUser = false
                )
                _chatMessages.update { it + errMsg }
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
    }

    // History
    fun loadHistory(range: TimeRange = _historyState.value.selectedRange) {
        _historyState.update { it.copy(selectedRange = range, isLoading = true) }

        viewModelScope.launch {
            try {
                val readings = repository.getReadingsSince(range.hours)
                val metric = _historyState.value.selectedMetric
                val values = readings.mapNotNull { r ->
                    when (metric) {
                        MetricType.TEMPERATURE -> r.temperature
                        MetricType.HUMIDITY -> r.humidity
                        MetricType.DUST -> r.dust?.toFloat()
                        MetricType.GAS -> r.gas?.toFloat()
                    }
                }

                val warningCount = repository.getWarningCountSince(range.hours)

                _historyState.update {
                    it.copy(
                        readings = readings,
                        average = if (values.isNotEmpty()) values.average().toFloat() else 0f,
                        peak = if (values.isNotEmpty()) values.max() else 0f,
                        peakTime = readings.maxByOrNull { r ->
                            when (metric) {
                                MetricType.TEMPERATURE -> r.temperature ?: 0f
                                MetricType.HUMIDITY -> r.humidity ?: 0f
                                MetricType.DUST -> r.dust?.toFloat() ?: 0f
                                MetricType.GAS -> r.gas?.toFloat() ?: 0f
                            }
                        }?.timestamp ?: 0L,
                        minimum = if (values.isNotEmpty()) values.min() else 0f,
                        minimumTime = readings.minByOrNull { r ->
                            when (metric) {
                                MetricType.TEMPERATURE -> r.temperature ?: Float.MAX_VALUE
                                MetricType.HUMIDITY -> r.humidity ?: Float.MAX_VALUE
                                MetricType.DUST -> r.dust?.toFloat() ?: Float.MAX_VALUE
                                MetricType.GAS -> r.gas?.toFloat() ?: Float.MAX_VALUE
                            }
                        }?.timestamp ?: 0L,
                        warningCount = warningCount,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _historyState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectMetric(metric: MetricType) {
        _historyState.update { it.copy(selectedMetric = metric) }
        loadHistory()
    }

    // Mock control
    fun setMockScenario(scenario: MockScenario) {
        _selectedMockScenario.value = scenario
        repository.setMockScenario(scenario)
        if (scenario == MockScenario.DISCONNECTED) {
            repository.disconnectArduino()
        } else {
            repository.connectArduino(viewModelScope)
        }
    }

    // Settings
    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        repository.setStorageInterval(newSettings.storageIntervalSeconds)
        smsService.setCooldown(newSettings.smsCooldownMinutes)
    }

    // Arduino connection control
    fun connectArduino() {
        val useMock = _settings.value.isMockMode
        repository.setMockMode(useMock)
        repository.connectArduino(viewModelScope)
    }

    fun disconnectArduino() {
        repository.disconnectArduino()
    }

    fun registerUsbReceivers() {
        arduinoService.registerReceivers()
    }

    fun unregisterUsbReceivers() {
        arduinoService.unregisterReceivers()
    }

    override fun onCleared() {
        super.onCleared()
        repository.disconnectArduino()
    }
}
