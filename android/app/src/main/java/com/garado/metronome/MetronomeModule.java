package com.garado.metronome;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MetronomeModule extends ReactContextBaseJavaModule {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> tickTask = null;
    private SoundPool soundPool;
    private int soundId = -1;
    private int accentSoundId = -1;
    private Vibrator vibrator;
    private boolean hapticsEnabled = true;
    private boolean accentEnabled = true;
    private int beatsPerMeasure = 4;
    private int currentBeat = 0;

    MetronomeModule(ReactApplicationContext context) {
        super(context);
        AudioAttributes attrs = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
        soundPool = new SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(attrs)
            .build();
        try {
            AssetFileDescriptor afd = context.getAssets().openFd("sounds/click.wav");
            soundId = soundPool.load(afd, 1);
            AssetFileDescriptor afdAccent = context.getAssets().openFd("sounds/click-accent.wav");
            accentSoundId = soundPool.load(afdAccent, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vm = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vm != null ? vm.getDefaultVibrator() : null;
        } else {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    @Override
    public String getName() {
        return "MetronomeModule";
    }

    @ReactMethod
    public void start(double bpm, int beats) {
        stop();
        beatsPerMeasure = beats;
        currentBeat = 0;
        long intervalMs = Math.round(60000.0 / bpm);
        tickTask = scheduler.scheduleAtFixedRate(
            this::tick, 0, intervalMs, TimeUnit.MILLISECONDS
        );
    }

    @ReactMethod
    public void stop() {
        if (tickTask != null) {
            tickTask.cancel(false);
            tickTask = null;
        }
    }

    private void tick() {
        boolean isAccent = currentBeat == 0 && accentEnabled;
        currentBeat = (currentBeat + 1) % beatsPerMeasure;
        if (isAccent && accentSoundId != -1) {
            soundPool.play(accentSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
        } else if (soundId != -1) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
        if (hapticsEnabled && vibrator != null && vibrator.hasVibrator()) {
            new Handler(Looper.getMainLooper()).post(() ->
                vibrator.vibrate(VibrationEffect.createWaveform(
                    new long[]{0, 50}, new int[]{0, 30}, -1
                ))
            );
        }
    }

    @ReactMethod
    public void setHapticsEnabled(boolean enabled) {
        hapticsEnabled = enabled;
    }

    @ReactMethod
    public void setAccentEnabled(boolean enabled) {
        accentEnabled = enabled;
    }

    @ReactMethod
    public void addListener(String eventName) {}

    @ReactMethod
    public void removeListeners(double count) {}
}
