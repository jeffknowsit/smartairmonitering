package com.smartair.hardware.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import com.smartair.data.model.AirStatus
import com.smartair.data.model.ConnectionState
import com.smartair.data.model.SensorReading
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException

/**
 * Manages USB serial communication with Arduino Uno.
 * Handles connection lifecycle, permission requests, data parsing,
 * and reconnection logic.
 */
class ArduinoSerialService(private val context: Context) {

    companion object {
        private const val TAG = "ArduinoSerial"
        const val ACTION_USB_PERMISSION = "com.smartair.USB_PERMISSION"
        private const val BAUD_RATE = 9600
        private const val READ_TIMEOUT = 1000
    }

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private val gson = Gson()

    private var port: UsbSerialPort? = null
    private var connection: UsbDeviceConnection? = null
    private var ioManager: SerialInputOutputManager? = null
    private var readJob: Job? = null

    private val _readings = MutableStateFlow(SensorReading())
    val readings: StateFlow<SensorReading> = _readings.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val lineBuffer = StringBuilder()

    // USB permission receiver
    private val usbPermissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_USB_PERMISSION) {
                val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                if (granted) {
                    Log.d(TAG, "USB permission granted")
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    }
                    device?.let { d ->
                        var drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
                        if (drivers.isEmpty()) {
                            val customTable = com.hoho.android.usbserial.driver.ProbeTable()
                            customTable.addProduct(d.vendorId, d.productId, com.hoho.android.usbserial.driver.CdcAcmSerialDriver::class.java)
                            drivers = UsbSerialProber(customTable).findAllDrivers(usbManager)
                        }
                        val driver = drivers.find { it.device == d }
                        if (driver != null) {
                            openConnection(driver)
                        } else {
                            _connectionState.value = ConnectionState.ERROR
                        }
                    }
                } else {
                    Log.w(TAG, "USB permission denied")
                    _connectionState.value = ConnectionState.PERMISSION_REQUIRED
                }
            }
        }
    }

    // USB disconnect receiver
    private val usbDetachReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == UsbManager.ACTION_USB_DEVICE_DETACHED) {
                Log.d(TAG, "USB device detached")
                disconnect()
            }
        }
    }

    fun registerReceivers() {
        val permFilter = IntentFilter(ACTION_USB_PERMISSION)
        val detachFilter = IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(usbPermissionReceiver, permFilter, Context.RECEIVER_NOT_EXPORTED)
            context.registerReceiver(usbDetachReceiver, detachFilter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(usbPermissionReceiver, permFilter)
            context.registerReceiver(usbDetachReceiver, detachFilter)
        }
    }

    fun unregisterReceivers() {
        try {
            context.unregisterReceiver(usbPermissionReceiver)
            context.unregisterReceiver(usbDetachReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "Receiver not registered: ${e.message}")
        }
    }

    /**
     * Attempt to connect to an Arduino device.
     * Probes available USB serial devices and requests permission if needed.
     */
    fun connect(scope: CoroutineScope) {
        _connectionState.value = ConnectionState.CONNECTING

        var availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        
        // Fallback for unrecognised clones
        if (availableDrivers.isEmpty()) {
            val allDevices = usbManager.deviceList.values
            if (allDevices.isNotEmpty()) {
                Log.d(TAG, "Default prober found no drivers, but ${allDevices.size} USB devices are connected. Trying fallback.")
                val customTable = com.hoho.android.usbserial.driver.ProbeTable()
                for (device in allDevices) {
                    customTable.addProduct(device.vendorId, device.productId, com.hoho.android.usbserial.driver.CdcAcmSerialDriver::class.java)
                }
                val customProber = UsbSerialProber(customTable)
                availableDrivers = customProber.findAllDrivers(usbManager)
            }
        }

        if (availableDrivers.isEmpty()) {
            Log.w(TAG, "No USB serial devices found")
            _connectionState.value = ConnectionState.DISCONNECTED
            return
        }

        val driver = availableDrivers[0]
        val device = driver.device

        if (!usbManager.hasPermission(device)) {
            Log.d(TAG, "Requesting USB permission")
            _connectionState.value = ConnectionState.PERMISSION_REQUIRED
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val intent = Intent(ACTION_USB_PERMISSION).apply {
                setPackage(context.packageName)
            }
            val permIntent = PendingIntent.getBroadcast(context, 0, intent, flags)
            usbManager.requestPermission(device, permIntent)
            return
        }

        openConnection(driver)
    }

    private fun openConnection(driver: UsbSerialDriver) {
        try {
            connection = usbManager.openDevice(driver.device)
            if (connection == null) {
                _connectionState.value = ConnectionState.ERROR
                return
            }

            port = driver.ports[0]
            port?.open(connection)
            port?.setParameters(BAUD_RATE, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

            _connectionState.value = ConnectionState.CONNECTED
            Log.d(TAG, "Connected to Arduino: ${driver.device.deviceName}")

            startReading()
        } catch (e: IOException) {
            Log.e(TAG, "Connection error: ${e.message}")
            _connectionState.value = ConnectionState.ERROR
            disconnect()
        }
    }

    private fun startReading() {
        val currentPort = port ?: return

        ioManager = SerialInputOutputManager(currentPort, object : SerialInputOutputManager.Listener {
            override fun onNewData(data: ByteArray) {
                processIncomingData(String(data))
            }

            override fun onRunError(e: Exception) {
                Log.e(TAG, "Serial read error: ${e.message}")
                _connectionState.value = ConnectionState.ERROR
            }
        })
        ioManager?.start()
    }

    /**
     * Buffer incoming bytes and split by newline to parse complete JSON messages.
     */
    private fun processIncomingData(data: String) {
        lineBuffer.append(data)

        while (lineBuffer.contains("\n")) {
            val nlIndex = lineBuffer.indexOf("\n")
            val line = lineBuffer.substring(0, nlIndex).trim()
            lineBuffer.delete(0, nlIndex + 1)

            if (line.isNotEmpty()) {
                parseLine(line)
            }
        }
    }

    /**
     * Parse a single JSON line from Arduino.
     * Tolerant of missing fields - does not crash on malformed data.
     */
    private fun parseLine(line: String) {
        try {
            val json = gson.fromJson(line, Map::class.java) ?: return

            val reading = SensorReading(
                timestamp = System.currentTimeMillis(),
                temperature = (json["temperature"] as? Number)?.toFloat(),
                humidity = (json["humidity"] as? Number)?.toFloat(),
                dust = (json["dust"] as? Number)?.toInt(),
                gas = (json["gas"] as? Number)?.toInt(),
                fan = when (val f = json["fan"]) {
                    is Number -> f.toInt() != 0
                    is Boolean -> f
                    else -> false
                },
                buzzer = when (val b = json["buzzer"]) {
                    is Number -> b.toInt() != 0
                    is Boolean -> b
                    else -> false
                },
                status = try {
                    AirStatus.valueOf((json["status"] as? String)?.uppercase() ?: "NORMAL")
                } catch (e: Exception) {
                    AirStatus.NORMAL
                }
            )

            _readings.value = reading
        } catch (e: JsonSyntaxException) {
            Log.w(TAG, "Malformed JSON from Arduino: $line")
            // Ignore malformed data, continue listening
        } catch (e: Exception) {
            Log.w(TAG, "Parse error: ${e.message}")
        }
    }

    /**
     * Send a command to Arduino (for future command channel).
     */
    fun sendCommand(command: String) {
        try {
            port?.write("$command\n".toByteArray(), READ_TIMEOUT)
        } catch (e: IOException) {
            Log.e(TAG, "Write error: ${e.message}")
        }
    }

    fun disconnect() {
        ioManager?.stop()
        ioManager = null
        readJob?.cancel()

        try { port?.close() } catch (e: Exception) { /* ignore */ }
        try { connection?.close() } catch (e: Exception) { /* ignore */ }

        port = null
        connection = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}
