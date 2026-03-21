import { createContext, useContext, useState, useEffect, ReactNode } from "react";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { setHapticsEnabled } from "@/utils/metronome";

interface MetronomeHapticsContextType {
    hapticsEnabled: boolean;
    setHapticsEnabled: (value: boolean) => void;
}

const MetronomeHapticsContext = createContext<MetronomeHapticsContextType>({
    hapticsEnabled: true,
    setHapticsEnabled: () => {},
});

export const useMetronomeHaptics = () => useContext(MetronomeHapticsContext);

export function MetronomeHapticsProvider({ children }: { children: ReactNode }) {
    const [hapticsEnabled, setHapticsEnabledState] = useState(true);

    useEffect(() => {
        AsyncStorage.getItem("metronomeHaptics").then((value) => {
            const enabled = value !== "false";
            setHapticsEnabledState(enabled);
            setHapticsEnabled(enabled);
        });
    }, []);

    const handleSet = (value: boolean) => {
        setHapticsEnabledState(value);
        setHapticsEnabled(value);
        AsyncStorage.setItem("metronomeHaptics", value.toString());
    };

    return (
        <MetronomeHapticsContext.Provider value={{ hapticsEnabled, setHapticsEnabled: handleSet }}>
            {children}
        </MetronomeHapticsContext.Provider>
    );
}
