import { useState, useRef } from "react";
import { View, StyleSheet, TextInput } from "react-native";
import { router, useLocalSearchParams } from "expo-router";
import ContentContainer from "@/components/ContentContainer";
import { StyledText } from "@/components/StyledText";
import { useInvertColors } from "@/contexts/InvertColorsContext";
import { setEditResult } from "@/utils/editBus";
import { n } from "@/utils/scaling";

export default function EditScreen() {
    const { field, bpm, beatsPerMeasure, beatUnit } = useLocalSearchParams<{
        field: "bpm" | "timeSig";
        bpm: string;
        beatsPerMeasure: string;
        beatUnit: string;
    }>();

    const { invertColors } = useInvertColors();
    const textColor = invertColors ? "black" : "white";

    const [bpmInput, setBpmInput] = useState(bpm ?? "120");
    const [topInput, setTopInput] = useState(beatsPerMeasure ?? "4");
    const [bottomInput, setBottomInput] = useState(beatUnit ?? "4");
    const bottomRef = useRef<TextInput>(null);

    const commit = () => {
        if (field === "bpm") {
            const val = parseInt(bpmInput);
            if (!isNaN(val) && val >= 20 && val <= 300) {
                setEditResult({ field: "bpm", value: val });
            }
        } else {
            const top = parseInt(topInput);
            const bottom = parseInt(bottomInput);
            setEditResult({
                field: "timeSig",
                top: !isNaN(top) && top > 0 ? top : parseInt(beatsPerMeasure),
                bottom: !isNaN(bottom) && bottom > 0 ? bottom : parseInt(beatUnit),
            });
        }
        router.back();
    };

    return (
        <ContentContainer hideBackButton style={{ alignItems: "stretch" }}>
            <View style={styles.container}>
                <StyledText style={[styles.title, { color: textColor }]}>
                    {field === "bpm" ? "Edit BPM" : "Edit time signature"}
                </StyledText>
                {field === "bpm" ? (
                    <TextInput
                        autoFocus
                        keyboardType="number-pad"
                        value={bpmInput}
                        onChangeText={(t) => setBpmInput(t.replace(/[^0-9]/g, ""))}
                        onSubmitEditing={commit}
                        style={[styles.input, { color: textColor }]}
                        selectionColor={textColor}
                    />
                ) : (
                    <View style={styles.timeSigRow}>
                        <TextInput
                            autoFocus
                            keyboardType="number-pad"
                            value={topInput}
                            onChangeText={(t) => setTopInput(t.replace(/[^0-9]/g, ""))}
                            onSubmitEditing={() => bottomRef.current?.focus()}
                            style={[styles.input, { color: textColor }]}
                            selectionColor={textColor}
                        />
                        <StyledText style={[styles.slash, { color: textColor }]}>/</StyledText>
                        <TextInput
                            ref={bottomRef}
                            keyboardType="number-pad"
                            value={bottomInput}
                            onChangeText={(t) => setBottomInput(t.replace(/[^0-9]/g, ""))}
                            onSubmitEditing={commit}
                            style={[styles.input, { color: textColor }]}
                            selectionColor={textColor}
                        />
                    </View>
                )}
            </View>
        </ContentContainer>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: "flex-start",
        alignItems: "center",
        paddingTop: n(48),
        gap: n(24),
    },
    title: {
        fontSize: n(30),
        textAlign: "center",
    },
    timeSigRow: {
        flexDirection: "row",
        alignItems: "center",
        gap: n(8),
    },
    input: {
        fontSize: n(64),
        fontFamily: "PublicSans-Regular",
        width: n(120),
        textAlign: "center",
    },
    slash: {
        fontSize: n(64),
    },
});
