package com.movtery.zalithlauncher.utils.microphone

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sqrt

class MicMeter {
    private val sampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    
    private var audioRecord: AudioRecord? = null
    private var job: Job? = null

    /**
     * 开始录音，实时返回相对音量值
     * @param onPermissionRequest 没有麦克风权限时，向用户申请权限
     */
    fun start(
        context: Context,
        onLevelUpdate: (Double) -> Unit,
        onPermissionRequest: () -> Unit
    ) {
        if (
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onPermissionRequest()
            return
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        audioRecord?.startRecording()

        job = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ShortArray(bufferSize)
            withContext(Dispatchers.IO) {
                while (true) {
                    try {
                        ensureActive()
                        val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                        if (read > 0) {
                            var sum = 0.0
                            for (i in 0 until read) {
                                val v = buffer[i].toDouble()
                                sum += v * v
                            }
                            val rms = sqrt(sum / read)

                            val safeRms = max(rms, 1.0)

                            //计算相对分贝值，0 对应完全静音
                            val db = 20 * log10(safeRms)
                            val level = max(db, 0.0)

                            onLevelUpdate(level)
                        }
                        delay(50)
                    } catch (_: CancellationException) {
                        break
                    }
                }
            }
        }
    }

    /**
     * 停止麦克风检查
     */
    fun stop() {
        job?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}