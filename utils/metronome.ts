import { NativeModules } from "react-native";

const { MetronomeModule } = NativeModules;

export function startMetronome(bpm: number) {
    MetronomeModule.start(bpm);
    return () => MetronomeModule.stop();
}

export function stopMetronome() {
    MetronomeModule.stop();
}
