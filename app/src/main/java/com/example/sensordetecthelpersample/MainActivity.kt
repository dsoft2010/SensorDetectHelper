package com.example.sensordetecthelpersample

import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.checkdebugtoolsample.BuildConfig
import com.example.checkdebugtoolsample.databinding.ActivityMainBinding
import kr.ds.helper.util.MoveDetectHelper
import kr.ds.helper.util.ShakeDetectHelper
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val sensorManager by lazy {
        Timber.plant(Timber.DebugTree())
        getSystemService(SENSOR_SERVICE) as SensorManager
    }

    private val moveDetectHelper by lazy {
        MoveDetectHelper(sensorManager)
    }

    private val shakeDetectHelper by lazy {
        ShakeDetectHelper(sensorManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        moveDetectHelper.apply {
            callback = { isMoving ->
                val state = if (isMoving) "움직임" else "멈춤"
                binding.textState.text = state
                val message = "상태 변경 : $state"
                Timber.d(message)
                if (BuildConfig.DEBUG) {
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
            start()
        }

        shakeDetectHelper.onShake = {
            Toast.makeText(this, "흔들기 감지됨", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()

        shakeDetectHelper.start()
    }

    override fun onPause() {
        super.onPause()

        shakeDetectHelper.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        moveDetectHelper.stop()
        Timber.tag("SensorDetectHelper").d("onDestroy()")
    }
}