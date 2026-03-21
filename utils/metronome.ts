import { NativeModules } from "react-native";

const { MetronomeModule } = NativeModules;

export function startMetronome(bpm: number, beatsPerMeasure: number) {
    MetronomeModule.start(bpm, beatsPerMeasure);
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
