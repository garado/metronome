package expo.modules.metronomemodule

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTimestamp
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.locks.LockSupport

private const val SAMPLE_RATE = 44100

class MetronomeModule : Module() {

    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r).apply { priority = Thread.MAX_PRIORITY }
    }
    private val hapticsExecutor = Executors.newSingleThreadExecutor()
    private var tickTask: Future<*>? = null
    private var audioTrack: AudioTrack? = null

    private val clickSamples by lazy { loadWavPcm("sounds/click.wav") }
    private val accentSamples by lazy { loadWavPcm("sounds/click-accent.wav") }
    private val subClickSamples by lazy { loadWavPcm("sounds/click-sub.wav") }

    private val vibrator: Vibrator? by lazy {
        val context = appContext.reactContext ?: return@lazy null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private var hapticsEnabled = true
    private var accentEnabled = true

    override fun definition() = ModuleDefinition {
        Name("MetronomeModule")

        Function("start") { bpm: Double, beats: Int, subdivisions: Int ->
            start(bpm, beats, subdivisions)
        }

        Function("stop") {
            stop()
        }

        Function("setHapticsEnabled") { enabled: Boolean ->
            hapticsEnabled = enabled
        }

        Function("setAccentEnabled") { enabled: Boolean ->
            accentEnabled = enabled
        }
    }

    private fun start(bpm: Double, beats: Int, subdivisions: Int) {
        stop()

        val beatSamples = ((60.0 / bpm) * SAMPLE_RATE).toInt()
        val subSamples = beatSamples / subdivisions

        // low-level dither to keep BT codec active between clicks
        val silence = ShortArray(beatSamples) { (Math.random() * 6 - 1).toInt().toShort() }

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val format = AudioFormat.Builder()
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(SAMPLE_RATE)
            .build()
        val minBuf = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val track = AudioTrack(attrs, format, minBuf, AudioTrack.MODE_STREAM, 0)
        audioTrack = track
        track.play()
        track.write(ShortArray(minBuf / 2), 0, minBuf / 2) // prime pipeline
        var currentBeat = 0
        var writePosition = (minBuf / 2).toLong()
        val timestamp = AudioTimestamp()
        val nsPerFrame = 1_000_000_000L / SAMPLE_RATE

        tickTask = executor.submit {
            while (!Thread.interrupted()) {
                val isAccent = currentBeat == 0 && accentEnabled
                currentBeat = (currentBeat + 1) % beats

                val beatClick = if (isAccent) accentSamples else clickSamples
                val clickPosition = writePosition
                if (hapticsEnabled && vibrator?.hasVibrator() == true && track.getTimestamp(timestamp)) {
                    val clickPresentationNs = timestamp.nanoTime + (clickPosition - timestamp.framePosition) * nsPerFrame
                    hapticsExecutor.submit {
                        val sleepNs = clickPresentationNs - System.nanoTime()
                        if (sleepNs > 0) LockSupport.parkNanos(sleepNs)
                        try {
                            vibrator!!.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                        } catch (e: Exception) { }
                    }
                }

                val mainLen = minOf(beatClick.size, subSamples)
                track.write(beatClick, 0, mainLen)
                track.write(silence, 0, subSamples - mainLen)
                writePosition += subSamples.toLong()

                // subdivision clicks (no haptics)
                for (i in 1 until subdivisions) {
                    val subLen = minOf(subClickSamples.size, subSamples)
                    track.write(subClickSamples, 0, subLen)
                    track.write(silence, 0, subSamples - subLen)
                    writePosition += subSamples.toLong()
                }
            }
        }
    }

    private fun stop() {
        tickTask?.cancel(true)
        tickTask = null
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }

    private fun loadWavPcm(assetPath: String): ShortArray {
        return try {
            val context = appContext.reactContext ?: return ShortArray(0)
            val bytes = context.assets.open(assetPath).readBytes()
            // skip 44-byte WAV header, read remaining PCM data as 16-bit little-endian shorts
            val pcmBytes = bytes.copyOfRange(44, bytes.size)
            val shorts = ShortArray(pcmBytes.size / 2)
            ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
            shorts
        } catch (e: Exception) {
            ShortArray(0)
        }
    }
}
