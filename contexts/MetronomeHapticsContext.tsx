import { createContext, useContext, useState, useEffect, ReactNode } from "react";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { setHapticsEnabled, setAccentEnabled } from "@/utils/metronome";

interface MetronomeHapticsContextType {
    hapticsEnabled: boolean;
    setHapticsEnabled: (value: boolean) => void;
    accentEnabled: boolean;
    setAccentEnabled: (value: boolean) => void;
}

const MetronomeHapticsContext = createContext<MetronomeHapticsContextType>({
    hapticsEnabled: true,
    setHapticsEnabled: () => {},
    accentEnabled: true,
    setAccentEnabled: () => {},
});

export const useMetronomeHaptics = () => useContext(MetronomeHapticsContext);

export function MetronomeHapticsProvider({ children }: { children: ReactNode }) {
    const [hapticsEnabled, setHapticsEnabledState] = useState(true);
    const [accentEnabled, setAccentEnabledState] = useState(true);

    useEffect(() => {
        AsyncStorage.getItem("metronomeHaptics").then((value) => {
            const enabled = value !== "false";
            setHapticsEnabledState(enabled);
            setHapticsEnabled(enabled);
        });
        AsyncStorage.getItem("metronomeAccent").then((value) => {
            const enabled = value !== "false";
            setAccentEnabledState(enabled);
            setAccentEnabled(enabled);
        });
    }, []);

    const handleSetHaptics = (value: boolean) => {
        setHapticsEnabledState(value);
        setHapticsEnabled(value);
        AsyncStorage.setItem("metronomeHaptics", value.toString());
    };

    const handleSetAccent = (value: boolean) => {
        setAccentEnabledState(value);
        setAccentEnabled(value);
        AsyncStorage.setItem("metronomeAccent", value.toString());
    };

    return (
        <MetronomeHapticsContext.Provider value={{
            hapticsEnabled,
            setHapticsEnabled: handleSetHaptics,
            accentEnabled,
            setAccentEnabled: handleSetAccent,
        }}>
            {children}
        </MetronomeHapticsContext.Provider>
    );
}
