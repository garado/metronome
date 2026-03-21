import { useState, useEffect } from "react";
import { View, StyleSheet, Modal, TextInput, Pressable } from "react-native";
import ContentContainer from "@/components/ContentContainer";
import { StyledText } from "@/components/StyledText";
import { useInvertColors } from "@/contexts/InvertColorsContext";
import { MaterialIcons } from "@expo/vector-icons";
import { n } from "@/utils/scaling";
import { startMetronome } from "@/utils/metronome";

export default function MetronomeScreen() {
  const [bpm, setBpm] = useState(120);
  const [beatsPerMeasure, setBeatsPerMeasure] = useState(4);
  const [beatUnit, setBeatUnit] = useState(4);
  const [isPlaying, setIsPlaying] = useState(false);
  const [editingBpm, setEditingBpm] = useState(false);
  const [editingTimeSig, setEditingTimeSig] = useState(false);
  const [bpmInput, setBpmInput] = useState("120");
  const [timeSigTop, setTimeSigTop] = useState("4");
  const [timeSigBottom, setTimeSigBottom] = useState("4");

  const { invertColors } = useInvertColors();

  const textColor = invertColors ? "black" : "white";
  const dimColor = invertColors ? "#555" : "#888";

  useEffect(() => {
    if (!isPlaying) return;
    return startMetronome(bpm);
  }, [isPlaying, bpm]);

  const commitBpm = () => {
    const val = parseInt(bpmInput);
    if (!isNaN(val) && val >= 20 && val <= 300) {
      setBpm(val);
    } else {
      setBpmInput(String(bpm));
    }
    setEditingBpm(false);
  };

  const commitTimeSig = () => {
    const top = parseInt(timeSigTop);
    const bottom = parseInt(timeSigBottom);
    if (!isNaN(top) && top > 0) setBeatsPerMeasure(top);
    else setTimeSigTop(String(beatsPerMeasure));
    if (!isNaN(bottom) && [1, 2, 4, 8, 16].includes(bottom)) setBeatUnit(bottom);
    else setTimeSigBottom(String(beatUnit));
    setEditingTimeSig(false);
  };

  return (
    <ContentContainer style={{ alignItems: "stretch", paddingHorizontal: 0 }}>
      <View style={styles.center}>
        <Pressable
          onPress={() => {
            setBpmInput(String(bpm));
            setEditingBpm(true);
          }}
          style={styles.bpmContainer}
        >
          <StyledText style={styles.bpm}>{bpm}</StyledText>
        </Pressable>

        <Pressable
          onPress={() => {
            setTimeSigTop(String(beatsPerMeasure));
            setTimeSigBottom(String(beatUnit));
            setEditingTimeSig(true);
          }}
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

      <Modal visible={editingBpm} transparent animationType="fade">
        <Pressable style={styles.overlay} onPress={commitBpm}>
          <View
            style={[
              styles.modalBox,
              { backgroundColor: invertColors ? "white" : "#111" },
            ]}
          >
            <TextInput
              autoFocus
              keyboardType="number-pad"
              value={bpmInput}
              onChangeText={setBpmInput}
              onSubmitEditing={commitBpm}
              style={[styles.modalInput, { color: textColor }]}
              selectionColor={textColor}
            />
          </View>
        </Pressable>
      </Modal>

      <Modal visible={editingTimeSig} transparent animationType="fade">
        <Pressable style={styles.overlay} onPress={commitTimeSig}>
          <View
            style={[
              styles.modalBox,
              { backgroundColor: invertColors ? "white" : "#111" },
            ]}
          >
            <TextInput
              autoFocus
              keyboardType="number-pad"
              value={timeSigTop}
              onChangeText={setTimeSigTop}
              onSubmitEditing={commitTimeSig}
              style={[styles.modalInput, { color: textColor }]}
              selectionColor={textColor}
            />
            <StyledText style={[styles.modalSlash, { color: textColor }]}>/</StyledText>
            <TextInput
              keyboardType="number-pad"
              value={timeSigBottom}
              onChangeText={setTimeSigBottom}
              onSubmitEditing={commitTimeSig}
              style={[styles.modalInput, { color: textColor }]}
              selectionColor={textColor}
            />
          </View>
        </Pressable>
      </Modal>
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
    gap: n(4),
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
  overlay: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "rgba(0,0,0,0.6)",
  },
  modalBox: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: n(32),
    paddingVertical: n(24),
    gap: n(8),
    borderRadius: n(4),
  },
  modalInput: {
    fontSize: n(52),
    fontFamily: "PublicSans-Regular",
    width: n(90),
    textAlign: "center",
  },
  modalSlash: {
    fontSize: n(52),
  },
});
