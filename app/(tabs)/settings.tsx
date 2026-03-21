import { View, StyleSheet } from "react-native";
import ContentContainer from "@/components/ContentContainer";
import { ToggleSwitch } from "@/components/ToggleSwitch";
import { useMetronomeHaptics } from "@/contexts/MetronomeHapticsContext";
import { n } from "@/utils/scaling";

export default function SettingsScreen() {
    const { hapticsEnabled, setHapticsEnabled } = useMetronomeHaptics();

    return (
        <ContentContainer headerTitle="Settings" hideBackButton>
            <View style={styles.section}>
                <ToggleSwitch
                    label="Haptics"
                    value={hapticsEnabled}
                    onValueChange={setHapticsEnabled}
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
