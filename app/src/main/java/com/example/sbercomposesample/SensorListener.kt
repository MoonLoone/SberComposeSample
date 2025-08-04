package com.example.sbercomposesample

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

fun createSensorListener(
    onShake: () -> Unit,
) = object : SensorEventListener {
    private var lastUpdate: Long = 0
    private var lastX: Float = 0.0f
    private var lastY: Float = 0.0f
    private var lastZ: Float = 0.0f
    private val SHAKE_THRESHOLD = 600
    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val curTime = System.currentTimeMillis()
        if ((curTime - lastUpdate) > 100) {
            val diffTime = curTime - lastUpdate
            val speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000

            if (speed > SHAKE_THRESHOLD) {
                onShake()
            }
            lastX = x
            lastY = y
            lastZ = z
            lastUpdate = curTime
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}