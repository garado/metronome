package com.garado.metronome

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTimestamp
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.locks.LockSupport

private const val SAMPLE_RATE = 44100

class MetronomeModule(context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {

    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r).apply { priority = Thread.MAX_PRIORITY }
    }
    private val hapticsExecutor = Executors.newSingleThreadExecutor()
    private var tickTask: Future<*>? = null
    private var audioTrack: AudioTrack? = null

    private val clickSamples = loadWavPcm(context, "sounds/click.wav")
    private val accentSamples = loadWavPcm(context, "sounds/click-accent.wav")

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var hapticsEnabled = true
    private var accentEnabled = true

    override fun getName() = "MetronomeModule"

    @ReactMethod
    fun start(bpm: Double, beats: Int) {
        stop()

        val intervalSamples = ((60.0 / bpm) * SAMPLE_RATE).toInt()

        // low-level dither to keep bluetooth codec hot between clicks
        // prevents auto gain reduction by bt when it detects silence (makes clicks sound weird)
        val silence = ShortArray(intervalSamples) { (Math.random() * 6 - 1).toInt().toShort() }

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
                val samples = if (currentBeat == 0 && accentEnabled) accentSamples else clickSamples
                currentBeat = (currentBeat + 1) % beats

                // schedule haptics to fire when the click actually plays
                val clickPosition = writePosition
                if (hapticsEnabled && vibrator?.hasVibrator() == true && track.getTimestamp(timestamp)) {
                    val clickPresentationNs = timestamp.nanoTime + (clickPosition - timestamp.framePosition) * nsPerFrame
                    hapticsExecutor.submit {
                        val sleepNs = clickPresentationNs - System.nanoTime()
                        if (sleepNs > 0) LockSupport.parkNanos(sleepNs)
                        try {
                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                        } catch (e: Exception) { }
                    }
                }

                val clickLen = minOf(samples.size, intervalSamples)
                track.write(samples, 0, clickLen)
                val silenceLen = intervalSamples - clickLen
                if (silenceLen > 0) track.write(silence, 0, silenceLen)
                writePosition += intervalSamples.toLong()
            }
        }
    }

    @ReactMethod
    fun stop() {
        tickTask?.cancel(true)
        tickTask = null
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }

    @ReactMethod
    fun setHapticsEnabled(enabled: Boolean) { hapticsEnabled = enabled }

    @ReactMethod
    fun setAccentEnabled(enabled: Boolean) { accentEnabled = enabled }

    @ReactMethod
    fun addListener(eventName: String) {}

    @ReactMethod
    fun removeListeners(count: Double) {}

    private fun loadWavPcm(context: ReactApplicationContext, assetPath: String): ShortArray {
        return try {
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
