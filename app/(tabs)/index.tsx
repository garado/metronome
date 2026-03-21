import { useState, useEffect } from "react";
import { View, StyleSheet, Pressable } from "react-native";
import { router, useFocusEffect } from "expo-router";
import { useCallback } from "react";
import ContentContainer from "@/components/ContentContainer";
import { StyledText } from "@/components/StyledText";
import { useInvertColors } from "@/contexts/InvertColorsContext";
import { MaterialIcons } from "@expo/vector-icons";
import { n } from "@/utils/scaling";
import { startMetronome } from "@/utils/metronome";
import { consumeEditResult } from "@/utils/editBus";

export default function MetronomeScreen() {
    const [bpm, setBpm] = useState(120);
    const [beatsPerMeasure, setBeatsPerMeasure] = useState(4);
    const [beatUnit, setBeatUnit] = useState(4);
    const [isPlaying, setIsPlaying] = useState(false);

    const { invertColors } = useInvertColors();
    const textColor = invertColors ? "black" : "white";

    useFocusEffect(useCallback(() => {
        const result = consumeEditResult();
        if (!result) return;
        if (result.field === "bpm") setBpm(result.value);
        else if (result.field === "timeSig") {
            setBeatsPerMeasure(result.top);
            setBeatUnit(result.bottom);
        }
    }, []));

    useEffect(() => {
        if (!isPlaying) return;
        return startMetronome(bpm, beatsPerMeasure);
    }, [isPlaying, bpm, beatsPerMeasure]);

    return (
        <ContentContainer style={{ alignItems: "stretch", paddingHorizontal: 0 }}>
            <View style={styles.center}>
                <Pressable
                    onPress={() => router.push({
                        pathname: "/edit",
                        params: { field: "bpm", bpm: String(bpm) },
                    })}
                    style={styles.bpmContainer}
                >
                    <StyledText style={styles.bpm}>{bpm}</StyledText>
                </Pressable>

                <Pressable
                    onPress={() => router.push({
                        pathname: "/edit",
                        params: { field: "timeSig", beatsPerMeasure: String(beatsPerMeasure), beatUnit: String(beatUnit) },
                    })}
                >
                    <StyledText style={styles.timeSig}>
                        {beatsPerMeasure}/{beatUnit}
                    </StyledText>
                </Pressable>

                <View style={styles.controls}>
                    <Pressable onPress={() => setBpm((b) => Math.max(20, b - 5))}>
                        <MaterialIcons name="remove" size={n(36)} color={textColor} />
                    </Pressable>
                    <Pressable onPress={() => setIsPlaying((p) => !p)}>
                        <MaterialIcons
                            name={isPlaying ? "pause" : "play-arrow"}
                            size={n(52)}
                            color={textColor}
                        />
                    </Pressable>
                    <Pressable onPress={() => setBpm((b) => Math.min(300, b + 5))}>
                        <MaterialIcons name="add" size={n(36)} color={textColor} />
                    </Pressable>
                </View>
            </View>
        </ContentContainer>
    );
}

const styles = StyleSheet.create({
    center: {
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
        gap: n(32),
    },
    bpmContainer: {
        alignItems: "center",
    },
    bpm: {
        fontSize: n(120),
        lineHeight: n(104),
    },
    timeSig: {
        fontSize: n(28),
    },
    controls: {
        flexDirection: "row",
        alignItems: "center",
        gap: n(32),
        marginTop: n(8),
    },
});
