import * as MetronomeModule from "@/modules/metronome-module";

export function startMetronome(bpm: number, beatsPerMeasure: number, subdivisions: number = 1) {
  MetronomeModule.start(bpm, beatsPerMeasure, subdivisions);
  return () => MetronomeModule.stop();
}

export function stopMetronome() {
  MetronomeModule.stop();
}

export function setHapticsEnabled(enabled: boolean) {
  MetronomeModule.setHapticsEnabled(enabled);
}

export function setAccentEnabled(enabled: boolean) {
  MetronomeModule.setAccentEnabled(enabled);
}
