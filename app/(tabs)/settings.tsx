import { View, StyleSheet } from "react-native";
import ContentContainer from "@/components/ContentContainer";
import { ToggleSwitch } from "@/components/ToggleSwitch";
import { useMetronomeHaptics } from "@/contexts/MetronomeHapticsContext";

export default function SettingsScreen() {
  const { hapticsEnabled, setHapticsEnabled, accentEnabled, setAccentEnabled } = useMetronomeHaptics();

  return (
    <ContentContainer headerTitle="Settings" hideBackButton>
      <View style={styles.section}>
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
      </View>
    </ContentContainer>
  );
}

const styles = StyleSheet.create({
  section: {
    width: "100%",
  },
});
