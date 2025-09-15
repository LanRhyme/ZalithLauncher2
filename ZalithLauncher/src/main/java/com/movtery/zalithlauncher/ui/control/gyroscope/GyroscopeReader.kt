package com.movtery.zalithlauncher.ui.control.gyroscope

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs

/**
 * 检查陀螺仪是否可以使用，一些设备并不具备陀螺仪传感器
 */
fun isGyroscopeAvailable(context: Context): Boolean {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val gyroscopeSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    return gyroscopeSensor != null
}

/**
 * 读取陀螺仪传感器数据
 *
 * @param xEvent x轴偏移量回调
 * @param yEvent y轴偏移量回调
 * @param zEvent z轴偏移量回调
 * @param sampleRate 采样率
 * @param smoothing 是否平滑数据
 * @param smoothingWindow 平滑窗口大小，越大越平滑
 * @param threshold 低通阈值，避免微小抖动
 * @param sensitivity 灵敏度倍数，控制放大倍数
 */
@Composable
fun GyroscopeReader(
    xEvent: (Float) -> Unit = {},
    yEvent: (Float) -> Unit = {},
    zEvent: (Float) -> Unit = {},
    sampleRate: Int = SensorManager.SENSOR_DELAY_GAME,
    smoothing: Boolean = true,
    smoothingWindow: Int = 4,
    threshold: Float = 0.02f,
    sensitivity: Float = 1f
) {
    key(
        xEvent, yEvent, zEvent,
        sampleRate, smoothing, smoothingWindow, threshold, sensitivity
    ) {
        GyroscopeReaderMain(
            xEvent = xEvent,
            yEvent = yEvent,
            zEvent = zEvent,
            sampleRate = sampleRate,
            smoothing = smoothing,
            smoothingWindow = smoothingWindow,
            threshold = threshold,
            sensitivity = sensitivity
        )
    }
}

@Composable
private fun GyroscopeReaderMain(
    xEvent: (Float) -> Unit = {},
    yEvent: (Float) -> Unit = {},
    zEvent: (Float) -> Unit = {},
    sampleRate: Int = SensorManager.SENSOR_DELAY_GAME,
    smoothing: Boolean = true,
    smoothingWindow: Int = 4,
    threshold: Float = 0.02f,
    sensitivity: Float = 1f
) {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    //陀螺仪传感器
    val gyroscopeSensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    val angleBuffer = remember { Array(smoothingWindow) { FloatArray(3) } }
    var historyIndex = -1
    var xTotal = 0f
    var yTotal = 0f
    var zTotal = 0f

    fun resetBuffer() {
        historyIndex = -1
        xTotal = 0f
        yTotal = 0f
        zTotal = 0f
        angleBuffer.forEach { it.fill(0f) }
    }

    DisposableEffect(Unit) {
        resetBuffer()
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                var x = event.values[0] * sensitivity
                var y = event.values[1] * sensitivity
                var z = event.values[2] * sensitivity

                if (smoothing) {
                    historyIndex++
                    if (historyIndex >= angleBuffer.size) historyIndex = 0

                    xTotal -= angleBuffer[historyIndex][0]
                    yTotal -= angleBuffer[historyIndex][1]
                    zTotal -= angleBuffer[historyIndex][2]

                    angleBuffer[historyIndex][0] = x
                    angleBuffer[historyIndex][1] = y
                    angleBuffer[historyIndex][2] = z

                    xTotal += x
                    yTotal += y
                    zTotal += z

                    x = xTotal / angleBuffer.size
                    y = yTotal / angleBuffer.size
                    z = zTotal / angleBuffer.size
                }

                //阈值过滤，避免微小抖动
                if (abs(x) > threshold) xEvent(x)
                if (abs(y) > threshold) yEvent(y)
                if (abs(z) > threshold) zEvent(z)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, gyroscopeSensor, sampleRate)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
}
