package com.smartair.data.mock

import com.smartair.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.*
import kotlin.random.Random

/**
 * Mock Arduino service for development without physical hardware.
 * Produces realistic sensor data streams for all scenarios.
 */
class MockArduinoService {

    private val _readings = MutableStateFlow(SensorReading())
    val readings: StateFlow<SensorReading> = _readings.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var job: Job? = null
    private var currentScenario = MockScenario.NORMAL
    private var elapsedSeconds = 0

    fun setScenario(scenario: MockScenario) {
        currentScenario = scenario
        elapsedSeconds = 0
        if (scenario == MockScenario.DISCONNECTED) {
            _connectionState.value = ConnectionState.DISCONNECTED
            job?.cancel()
        }
    }

    fun connect(scope: CoroutineScope) {
        if (currentScenario == MockScenario.DISCONNECTED) return

        _connectionState.value = ConnectionState.CONNECTING
        job?.cancel()
        job = scope.launch {
            delay(800) // Simulate connection delay
            _connectionState.value = ConnectionState.CONNECTED

            while (isActive) {
                val reading = generateReading()
                _readings.value = reading
                elapsedSeconds += 2
                delay(2000) // Arduino sends every ~2 seconds
            }
        }
    }

    fun disconnect() {
        job?.cancel()
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    private fun generateReading(): SensorReading {
        val t = elapsedSeconds.toFloat()
        val noise = { amplitude: Float -> (Random.nextFloat() - 0.5f) * amplitude }

        return when (currentScenario) {
            MockScenario.NORMAL -> SensorReading(
                temperature = 27.5f + sin(t / 60.0).toFloat() * 1.5f + noise(0.3f),
                humidity = 58f + sin(t / 90.0).toFloat() * 5f + noise(1f),
                dust = (200 + (sin(t / 45.0) * 30).toInt() + Random.nextInt(-10, 10)),
                gas = (280 + (sin(t / 120.0) * 20).toInt() + Random.nextInt(-8, 8)),
                fan = true,
                buzzer = false,
                status = AirStatus.NORMAL
            )

            MockScenario.WARNING_DUST -> SensorReading(
                temperature = 29f + noise(0.4f),
                humidity = 64f + noise(1.5f),
                dust = (380 + (sin(t / 30.0) * 60).toInt() + Random.nextInt(-15, 15)),
                gas = (320 + Random.nextInt(-10, 10)),
                fan = true,
                buzzer = false,
                status = AirStatus.WARNING
            )

            MockScenario.WARNING_TEMPERATURE -> SensorReading(
                temperature = 36f + sin(t / 40.0).toFloat() * 1f + noise(0.3f),
                humidity = 70f + noise(2f),
                dust = (220 + Random.nextInt(-10, 10)),
                gas = (300 + Random.nextInt(-8, 8)),
                fan = true,
                buzzer = false,
                status = AirStatus.WARNING
            )

            MockScenario.WARNING_HUMIDITY -> SensorReading(
                temperature = 30f + noise(0.5f),
                humidity = 82f + sin(t / 50.0).toFloat() * 4f + noise(1f),
                dust = (230 + Random.nextInt(-10, 10)),
                gas = (290 + Random.nextInt(-8, 8)),
                fan = true,
                buzzer = false,
                status = AirStatus.WARNING
            )

            MockScenario.CRITICAL -> SensorReading(
                temperature = 33f + noise(0.5f),
                humidity = 78f + noise(2f),
                dust = (650 + (sin(t / 20.0) * 80).toInt() + Random.nextInt(-20, 20)),
                gas = (680 + (sin(t / 25.0) * 50).toInt() + Random.nextInt(-15, 15)),
                fan = true,
                buzzer = true,
                status = AirStatus.CRITICAL
            )

            MockScenario.SENSOR_ERROR -> SensorReading(
                temperature = if (Random.nextFloat() > 0.3f) 28f + noise(0.5f) else null,
                humidity = if (Random.nextFloat() > 0.3f) 60f + noise(1f) else null,
                dust = if (Random.nextFloat() > 0.5f) 200 + Random.nextInt(-10, 10) else null,
                gas = 300 + Random.nextInt(-8, 8),
                fan = false,
                buzzer = false,
                status = AirStatus.NORMAL
            )

            MockScenario.AI_OFFLINE -> SensorReading(
                temperature = 28f + noise(0.3f),
                humidity = 60f + noise(1f),
                dust = (210 + Random.nextInt(-10, 10)),
                gas = (290 + Random.nextInt(-8, 8)),
                fan = true,
                buzzer = false,
                status = AirStatus.NORMAL
            )

            MockScenario.DISCONNECTED -> SensorReading() // Won't be called
        }
    }
}
