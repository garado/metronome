package com.garado.metronome;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.VibrationEffect;
import android.os.Vibrator;

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
    private Vibrator vibrator;

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
        } catch (Exception e) {
            e.printStackTrace();
        }
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public String getName() {
        return "MetronomeModule";
    }

    @ReactMethod
    public void start(double bpm) {
        stop();
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
        if (soundId != -1) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    @ReactMethod
    public void addListener(String eventName) {}

    @ReactMethod
    public void removeListeners(double count) {}
}
