package com.example.ui

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CameraSettings
import com.example.data.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.sqrt

class CameraViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val repository: SettingsRepository
    private val sensorManager: SensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Room Persistent Settings State
    private val _settings = MutableStateFlow(CameraSettings())
    val settings: StateFlow<CameraSettings> = _settings.asStateFlow()

    // Runtime Interactive UI States
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0) // seconds
    val recordingDuration: StateFlow<Int> = _recordingDuration.asStateFlow()

    private val _zoomRatio = MutableStateFlow(1.0f) // 1.0x to 8.0x
    val zoomRatio: StateFlow<Float> = _zoomRatio.asStateFlow()

    private val _uiLocked = MutableStateFlow(false)
    val uiLocked: StateFlow<Boolean> = _uiLocked.asStateFlow()

    private val _lockHoldProgress = MutableStateFlow(0.0f) // 0.0 to 1.0 (unlocking)
    val lockHoldProgress: StateFlow<Float> = _lockHoldProgress.asStateFlow()

    // Horizon angle in degrees calculated via Accelerometer
    private val _horizonTiltAngle = MutableStateFlow(0.0f)
    val horizonTiltAngle: StateFlow<Float> = _horizonTiltAngle.asStateFlow()

    // Live Simulated Metrics
    private val _batteryLevel = MutableStateFlow(98)
    private val _storageMinutesLeft = MutableStateFlow(185)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()
    val storageMinutesLeft: StateFlow<Int> = _storageMinutesLeft.asStateFlow()

    // Dynamic fake histogram data trigger (updates with small fluctuations)
    private val _histogramData = MutableStateFlow(floatArrayOf())
    val histogramData: StateFlow<FloatArray> = _histogramData.asStateFlow()

    private var recordingJob: Job? = null
    private var histogramJob: Job? = null
    private var unlockJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SettingsRepository(database.cameraSettingsDao())

        // Load settings from Room and observe updates reactive-style
        viewModelScope.launch {
            repository.settingsFlow.collectLatest { dbSettings ->
                _settings.value = dbSettings
            }
        }

        // Register tilt sensor
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Run light noise for histogram rendering
        startHistogramDynamics()
        startBatteryMonitoring()
    }

    private fun startHistogramDynamics() {
        histogramJob = viewModelScope.launch {
            while (true) {
                // Generate simulated video frame histogram curves (64 points)
                val baseData = FloatArray(64) { index ->
                    val progress = index / 63f
                    val curve = (Math.sin(progress * Math.PI).toFloat() * 0.6f + 
                                Math.sin(progress * 3.0 * Math.PI).toFloat() * 0.2f + 
                                (Math.random().toFloat() * 0.15f))
                    (curve.coerceAtLeast(0.05f) * 80f)
                }
                _histogramData.value = baseData
                delay(300) // 3 fps update is visual candy
            }
        }
    }

    private fun startBatteryMonitoring() {
        viewModelScope.launch {
            while (true) {
                delay(20000)
                if (_batteryLevel.value > 10) {
                    _batteryLevel.value -= 1
                }
            }
        }
    }

    // Capture volume zoom commands
    fun onVolumeUpPressed() {
        if (_uiLocked.value) return
        adjustZoom(0.2f)
    }

    fun onVolumeDownPressed() {
        if (_uiLocked.value) return
        adjustZoom(-0.2f)
    }

    private fun adjustZoom(delta: Float) {
        val current = _zoomRatio.value
        val next = (current + delta).coerceIn(1.0f, 8.0f)
        _zoomRatio.value = next
    }

    fun updateFps(newFps: Int) {
        viewModelScope.launch {
            val updated = _settings.value.copy(fps = newFps)
            repository.updateSettings(updated)
            // Auto update duration estimates slightly based on frames saved
            _storageMinutesLeft.value = when (newFps) {
                24 -> 210
                30 -> 185
                60 -> 110
                120 -> 55
                else -> 150
            }
        }
    }

    fun updateResolution(newRes: String) {
        viewModelScope.launch {
            val updated = _settings.value.copy(resolutionValue = newRes)
            repository.updateSettings(updated)
        }
    }

    fun updateColorProfile(newProfile: String) {
        viewModelScope.launch {
            val updated = _settings.value.copy(colorProfile = newProfile)
            repository.updateSettings(updated)
        }
    }

    fun toggleGrid() {
        viewModelScope.launch {
            val updated = _settings.value.copy(gridVisible = !_settings.value.gridVisible)
            repository.updateSettings(updated)
        }
    }

    fun toggleZebra() {
        viewModelScope.launch {
            val updated = _settings.value.copy(zebraPatternEnabled = !_settings.value.zebraPatternEnabled)
            repository.updateSettings(updated)
        }
    }

    fun toggleFocusPeaking() {
        viewModelScope.launch {
            val updated = _settings.value.copy(focusPeakingEnabled = !_settings.value.focusPeakingEnabled)
            repository.updateSettings(updated)
        }
    }

    fun toggleLeveler() {
        viewModelScope.launch {
            val updated = _settings.value.copy(levelerEnabled = !_settings.value.levelerEnabled)
            repository.updateSettings(updated)
        }
    }

    fun updateIso(newIso: Int) {
        viewModelScope.launch {
            val updated = _settings.value.copy(iso = newIso)
            repository.updateSettings(updated)
        }
    }

    fun updateEv(newEv: Float) {
        viewModelScope.launch {
            val updated = _settings.value.copy(ev = newEv)
            repository.updateSettings(updated)
        }
    }

    fun updateManualFocus(newFocus: Float) {
        viewModelScope.launch {
            val updated = _settings.value.copy(manualFocus = newFocus)
            repository.updateSettings(updated)
        }
    }

    fun toggleAeAf() {
        viewModelScope.launch {
            val updated = _settings.value.copy(aeAfLocked = !_settings.value.aeAfLocked)
            repository.updateSettings(updated)
        }
    }

    fun toggleRecording() {
        if (_uiLocked.value) return
        val currentlyRecording = _isRecording.value
        _isRecording.value = !currentlyRecording

        if (!currentlyRecording) {
            // Start duration timer
            _recordingDuration.value = 0
            recordingJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    _recordingDuration.value += 1
                }
            }
        } else {
            // Cancel timer
            recordingJob?.cancel()
            recordingJob = null
        }
    }

    // Safety Screen Lock Logic
    fun toggleLock() {
        if (!_uiLocked.value) {
            // Instantly Lock
            _uiLocked.value = true
            _lockHoldProgress.value = 0f
        }
    }

    fun startUnlockingHold() {
        if (!_uiLocked.value) return
        unlockJob?.cancel()
        unlockJob = viewModelScope.launch {
            val stepTime = 50L
            val duration = 2000L // 2 seconds delay hold
            val steps = duration / stepTime
            for (i in 1..steps) {
                delay(stepTime)
                _lockHoldProgress.value = i.toFloat() / steps
            }
            // Unlock success!
            _uiLocked.value = false
            _lockHoldProgress.value = 0f
        }
    }

    fun cancelUnlockingHold() {
        unlockJob?.cancel()
        unlockJob = null
        _lockHoldProgress.value = 0f
    }

    // Accelerometer changes
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val ax = event.values[0]
            val ay = event.values[1]
            val az = event.values[2]

            // Calculate device roll tilt angle
            // Perfect horizontal state is when ax ~ 0 (or ay ~ 9.8 depending on portrait/landscape)
            // Assuming default camera landscape orientation holding:
            val angleRad = atan2(ax.toDouble(), ay.toDouble())
            var angleDeg = (angleRad * 180.0 / Math.PI).toFloat()

            // Normalize angle for portrait / landscape
            if (angleDeg > 90f) {
                angleDeg = 180f - angleDeg
            } else if (angleDeg < -90f) {
                angleDeg = -180f - angleDeg
            }

            // High filter to reduce accelerometer noise
            val previous = _horizonTiltAngle.value
            _horizonTiltAngle.value = previous * 0.8f + angleDeg * 0.2f
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
        histogramJob?.cancel()
        recordingJob?.cancel()
        unlockJob?.cancel()
    }
}
