import { createContext, useContext, useState, useEffect, ReactNode } from "react";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { activateKeepAwakeAsync, deactivateKeepAwake } from "expo-keep-awake";

interface KeepAwakeContextType {
  keepAwake: boolean;
  setKeepAwake: (value: boolean) => void;
}

const KeepAwakeContext = createContext<KeepAwakeContextType>({
  keepAwake: false,
  setKeepAwake: () => { },
});

export const useKeepAwake = () => useContext(KeepAwakeContext);

export function KeepAwakeProvider({ children }: { children: ReactNode }) {
  const [keepAwake, setKeepAwakeState] = useState(false);

  useEffect(() => {
    AsyncStorage.getItem("keepAwake").then((value) => {
      const enabled = value === "true";
      setKeepAwakeState(enabled);
      if (enabled) activateKeepAwakeAsync();
    });
  }, []);

  const setKeepAwake = (value: boolean) => {
    setKeepAwakeState(value);
    AsyncStorage.setItem("keepAwake", value.toString());
    if (value) {
      activateKeepAwakeAsync();
    } else {
      deactivateKeepAwake();
    }
  };

  return (
    <KeepAwakeContext.Provider value={{ keepAwake, setKeepAwake }}>
      {children}
    </KeepAwakeContext.Provider>
  );
}
