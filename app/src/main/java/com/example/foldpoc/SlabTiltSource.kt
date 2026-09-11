package com.example.foldpoc

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

class SlabTiltSource(context: Context) : SensorEventListener, AutoCloseable {
    private val manager = context.getSystemService(SensorManager::class.java)
    private val rotation = manager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: manager?.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
    private val matrix = FloatArray(9)
    private val orientation = FloatArray(3)
    private var baseline = 0f
    private var value = 0f
    private var calibrated = false
    var available: Boolean = rotation != null
        private set
    var onChanged: (() -> Unit)? = null
    fun start() { rotation?.let { manager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) } }
    fun stop() { manager?.unregisterListener(this) }
    fun relativeDegrees(): Float = value
    override fun onSensorChanged(event: SensorEvent) {
        SensorManager.getRotationMatrixFromVector(matrix, event.values)
        SensorManager.getOrientation(matrix, orientation)
        val raw = Math.toDegrees(orientation[1].toDouble()).toFloat()
        if (!calibrated) { baseline = raw; calibrated = true }
        var delta = raw - baseline
        while (delta > 180f) delta -= 360f
        while (delta < -180f) delta += 360f
        if (abs(delta) < 1.2f) delta = 0f
        value += (delta - value) * .18f
        onChanged?.invoke()
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    override fun close() { stop(); onChanged = null }
}
