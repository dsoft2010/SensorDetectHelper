package kr.ds.helper.util

import android.hardware.Sensor
import android.hardware.SensorManager

class ShakeDetectHelper(private val sensorManager: SensorManager): ShakeDetector.OnShakeListener {
    private val acceleometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val shakeDetector = ShakeDetector()

    var onShake: ((Int) -> Unit) = {}

    fun start() {
        shakeDetector.setOnShakeListener(this)
        sensorManager.registerListener(shakeDetector, acceleometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        sensorManager.unregisterListener(shakeDetector)
    }

    override fun onShake(count: Int) {
        onShake.invoke(count)
    }
}