package com.royalenfield.ff.dashprototype

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

// Live compass heading from the phone's rotation sensor (F-11).
// MainActivity calls start() in onResume and stop() in onPause.
object Heading {
    private val _degrees = MutableStateFlow(0)
    val degrees: StateFlow<Int> = _degrees.asStateFlow()

    private var manager: SensorManager? = null

    private val listener = object : SensorEventListener {
        private val rotation = FloatArray(9)
        private val orientation = FloatArray(3)

        override fun onSensorChanged(event: SensorEvent) {
            SensorManager.getRotationMatrixFromVector(rotation, event.values)
            SensorManager.getOrientation(rotation, orientation)
            val degrees = (Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0
            _degrees.value = degrees.roundToInt() % 360
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun start(context: Context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) ?: return
        manager = sensorManager
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        manager?.unregisterListener(listener)
        manager = null
    }
}

// Compass point for a heading in degrees, e.g. 312 becomes "NW".
fun cardinal(degrees: Int): String {
    val points = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    return points[((degrees + 22) / 45) % 8]
}
