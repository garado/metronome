import { StyleSheet } from "react-native";
import ContentContainer from "@/components/ContentContainer";
import { ToggleSwitch } from "@/components/ToggleSwitch";
import { useMetronomeHaptics } from "@/contexts/MetronomeHapticsContext";
import { n } from "@/utils/scaling";
import * as Application from "expo-application";

export default function SettingsScreen() {
  const { hapticsEnabled, setHapticsEnabled, accentEnabled, setAccentEnabled } = useMetronomeHaptics();
  const version = Application.nativeApplicationVersion;

  return (
    <ContentContainer
      headerTitle={`Settings (v${version})`}
      hideBackButton
      style={styles.container}
    >
      <ToggleSwitch
        label="Haptic Feedback"
        value={hapticsEnabled}
        onValueChange={setHapticsEnabled}
      />
      <ToggleSwitch
        label="Downbeat Accent"
        value={accentEnabled}
        onValueChange={setAccentEnabled}
      />
    </ContentContainer>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: n(20),
  },
});
