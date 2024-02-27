package kr.ds.helper.util

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.sqrt

class MoveDetectHelper(private var sensorManager: SensorManager) : SensorEventListener {

    private val mAccelerometer: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var isMoving = false
        private set

    var callback: ((Boolean) -> Unit)? = null

    fun start() {
        isMoving = false
        hitCount = 0
        index = 0
        accArray = Array(ACC_EVENT_SIZE) { 0.0 }
        gData = Array(3) { 0.0f }
        isFirstCheck = true
        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        callback = null
    }

    private var mAccelVal = 0f
    private var mAccelLast = 0f
    private var index = 0
    private var hitCount = 0
    private var accArray = Array(ACC_EVENT_SIZE) { 0.0 }
    private var isFirstCheck: Boolean = true
    private var prevMillis = 0L
    private var gData = Array(3) { 0.0f }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val curMillis = System.currentTimeMillis()
            gData[0] = alpha * gData[0] + (1 - alpha) * event.values[0]
            gData[1] = alpha * gData[1] + (1 - alpha) * event.values[1]
            gData[2] = alpha * gData[2] + (1 - alpha) * event.values[2]
            val x = event.values[0] - gData[0]
            val y = event.values[1] - gData[1]
            val z = event.values[2] - gData[2]
            val accelCurr = sqrt((/*x * x + y * y +*/ z * z).toDouble()).toFloat()
            if (isFirstCheck) {
                mAccelLast = accelCurr
                isFirstCheck = false
                prevMillis = curMillis
            }
            val delta = accelCurr - mAccelLast
            mAccelLast = accelCurr
            mAccelVal = mAccelVal * 0.9f + delta
            val force = abs(mAccelVal).toDouble()
            accArray[index] = force
            index = ++index % ACC_EVENT_SIZE
            val avgAcc = accArray.sumOf { it } / ACC_EVENT_SIZE
            if (avgAcc >= SHAKE_THRESHOLD_GRAVITY) {
                ++hitCount
                if (hitCount >= MOVE_HIT_COUNT && !isMoving) {
                    val diff = curMillis - prevMillis
                    Timber.d("val: ${"%.3f".format(force)}, avgAcc: ${"%.3f".format(avgAcc)}, diff: ${(diff / 1000).toInt()}(${diff}ms)")
                    isMoving = true
                    callback?.invoke(isMoving)
                }
            } else {
                if (avgAcc < INACTIVITY_THRESHOLD_GRAVITY) {
                    hitCount = 0
                    prevMillis = curMillis
                    if (isMoving) {
                        Timber.d("val: ${"%.3f".format(force)}, avgAcc: ${"%.3f".format(avgAcc)}")
                        isMoving = false
                        callback?.invoke(isMoving)
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Timber.d("${sensor.name}(${sensor.type}) accuracy: $accuracy")
    }

    companion object {
        private const val ACC_EVENT_SIZE = 10
        private const val MOVE_HIT_COUNT = 10 * 20 // 1초 마다 20 개씩 10 초
        private const val SHAKE_THRESHOLD_GRAVITY = 0.75
        private const val INACTIVITY_THRESHOLD_GRAVITY = 0.05
        private const val alpha = 0.8f
    }
}