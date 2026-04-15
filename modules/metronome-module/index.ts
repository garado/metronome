import { requireNativeModule } from "expo-modules-core";

const MetronomeNative = requireNativeModule("MetronomeModule");

export function start(bpm: number, beats: number, subdivisions: number): void {
  MetronomeNative.start(bpm, beats, subdivisions);
}

export function stop(): void {
  MetronomeNative.stop();
}

export function setHapticsEnabled(enabled: boolean): void {
  MetronomeNative.setHapticsEnabled(enabled);
}

export function setAccentEnabled(enabled: boolean): void {
  MetronomeNative.setAccentEnabled(enabled);
}
