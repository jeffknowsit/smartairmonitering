package com.smartair.data.repository

import com.smartair.data.database.SensorReadingDao
import com.smartair.data.mock.MockArduinoService
import com.smartair.data.model.*
import com.smartair.hardware.usb.ArduinoSerialService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Central repository bridging hardware/mock data sources with the UI layer.
 * Handles data routing, storage throttling, and recent-data buffering.
 */
class SensorRepository(
    private val dao: SensorReadingDao,
    private val arduinoService: ArduinoSerialService,
    private val mockService: MockArduinoService
) {
    private var isMockMode = true
    private var lastStoredTimestamp = 0L
    private var storageIntervalMs = 10_000L // 10 seconds default

    // Expose readings based on current mode
    val currentReading: Flow<SensorReading>
        get() = if (isMockMode) mockService.readings else arduinoService.readings

    val connectionState: Flow<ConnectionState>
        get() = if (isMockMode) mockService.connectionState else arduinoService.connectionState

    // Recent data buffers for sparkline trends (last ~50 readings)
    private val _recentTemps = MutableStateFlow<List<Float>>(emptyList())
    val recentTemps: StateFlow<List<Float>> = _recentTemps.asStateFlow()

    private val _recentHumidity = MutableStateFlow<List<Float>>(emptyList())
    val recentHumidity: StateFlow<List<Float>> = _recentHumidity.asStateFlow()

    private val _recentDust = MutableStateFlow<List<Float>>(emptyList())
    val recentDust: StateFlow<List<Float>> = _recentDust.asStateFlow()

    private val _recentGas = MutableStateFlow<List<Float>>(emptyList())
    val recentGas: StateFlow<List<Float>> = _recentGas.asStateFlow()

    fun setMockMode(mock: Boolean) {
        isMockMode = mock
    }

    fun setStorageInterval(seconds: Int) {
        storageIntervalMs = seconds * 1000L
    }

    /**
     * Start collecting readings from active source and storing to DB at the configured interval.
     */
    fun startCollecting(scope: CoroutineScope) {
        scope.launch {
            currentReading.collect { reading ->
                // Update recent buffers
                updateRecentBuffers(reading)

                // Throttled database storage
                val now = System.currentTimeMillis()
                if (now - lastStoredTimestamp >= storageIntervalMs) {
                    lastStoredTimestamp = now
                    try {
                        dao.insert(reading.copy(timestamp = now))
                    } catch (e: Exception) {
                        // Database failure should not crash the app
                    }
                }
            }
        }
    }

    private fun updateRecentBuffers(reading: SensorReading) {
        val maxPoints = 50

        reading.temperature?.let { temp ->
            _recentTemps.value = (_recentTemps.value + temp).takeLast(maxPoints)
        }
        reading.humidity?.let { hum ->
            _recentHumidity.value = (_recentHumidity.value + hum).takeLast(maxPoints)
        }
        reading.dust?.let { dust ->
            _recentDust.value = (_recentDust.value + dust.toFloat()).takeLast(maxPoints)
        }
        reading.gas?.let { gas ->
            _recentGas.value = (_recentGas.value + gas.toFloat()).takeLast(maxPoints)
        }
    }

    // History queries
    suspend fun getReadingsSince(hours: Long): List<SensorReading> {
        val since = System.currentTimeMillis() - (hours * 3600_000)
        return dao.getReadingsSinceSnapshot(since)
    }

    suspend fun getWarningCountSince(hours: Long): Int {
        val since = System.currentTimeMillis() - (hours * 3600_000)
        return dao.getWarningCountSince(since)
    }

    suspend fun getRecentReadings(limit: Int): List<SensorReading> {
        return dao.getRecentReadings(limit)
    }

    // Cleanup old data (keep last 30 days)
    suspend fun cleanupOldData() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 3600_000)
        dao.deleteOlderThan(thirtyDaysAgo)
    }

    // Arduino control
    fun connectArduino(scope: CoroutineScope) {
        if (isMockMode) {
            mockService.connect(scope)
        } else {
            arduinoService.connect(scope)
        }
    }

    fun disconnectArduino() {
        if (isMockMode) {
            mockService.disconnect()
        } else {
            arduinoService.disconnect()
        }
    }

    fun setMockScenario(scenario: MockScenario) {
        mockService.setScenario(scenario)
    }
}
