package com.garado.metronome

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class MetronomeModule(context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {

    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private var tickTask: ScheduledFuture<*>? = null
    private val soundPool: SoundPool
    private var soundId = -1
    private var accentSoundId = -1
    private val vibrator: Vibrator?
    private var hapticsEnabled = true
    private var accentEnabled = true
    private var beatsPerMeasure = 4
    private var currentBeat = 0

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(attrs)
            .build()
        try {
            soundId = soundPool.load(context.assets.openFd("sounds/click.wav"), 1)
            accentSoundId = soundPool.load(context.assets.openFd("sounds/click-accent.wav"), 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun getName() = "MetronomeModule"

    @ReactMethod
    fun start(bpm: Double, beats: Int) {
        stop()
        beatsPerMeasure = beats
        currentBeat = 0
        val intervalNs = Math.round(60_000_000_000.0 / bpm)
        tickTask = scheduler.scheduleAtFixedRate(::tick, 0, intervalNs, TimeUnit.NANOSECONDS)
    }

    @ReactMethod
    fun stop() {
        tickTask?.cancel(false)
        tickTask = null
    }

    private fun tick() {
        val isAccent = currentBeat == 0 && accentEnabled
        currentBeat = (currentBeat + 1) % beatsPerMeasure

        if (isAccent && accentSoundId != -1) {
            soundPool.play(accentSoundId, 1f, 1f, 1, 0, 1f)
        } else if (soundId != -1) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }

        if (hapticsEnabled && vibrator?.hasVibrator() == true) {
            try {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50), intArrayOf(0, 30), -1))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @ReactMethod
    fun setHapticsEnabled(enabled: Boolean) { hapticsEnabled = enabled }

    @ReactMethod
    fun setAccentEnabled(enabled: Boolean) { accentEnabled = enabled }

    @ReactMethod
    fun addListener(eventName: String) {}

    @ReactMethod
    fun removeListeners(count: Double) {}
}
