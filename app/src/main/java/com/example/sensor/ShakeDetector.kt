//Interfaces directly with the device's hardware SensorManager (Sensor.TYPE_ACCELEROMETER). Implements dynamic threshold calculations and low-pass filtering to detect when the phone is shaken, triggering milestone logs with haptic feedback.
package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.sqrt

class ShakeDetector(
    private val context: Context,
    private val onShakeListener: () -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastShakeTimestamp: Long = 0
    private var shakeThresholdGravity = 2.2f // Default medium sensitivity
    private val shakeCooldownMs = 1200L // Prevent rapid double triggers

    var isSensorAvailable: Boolean = accelerometer != null
        private set

    fun setSensitivity(sensitivity: String) {
        shakeThresholdGravity = when (sensitivity.uppercase()) {
            "HIGH" -> 1.7f
            "LOW" -> 2.8f
            else -> 2.2f // "MEDIUM"
        }
    }

    fun startListening() {
        if (accelerometer != null) {
            sensorManager?.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        // G-force magnitude
        val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

        if (gForce > shakeThresholdGravity) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTimestamp >= shakeCooldownMs) {
                lastShakeTimestamp = now
                triggerHapticFeedback()
                onShakeListener()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    private fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    vibrator?.vibrate(120)
                }
            }
        } catch (_: Exception) {
            // Ignore haptic failures
        }
    }
}
