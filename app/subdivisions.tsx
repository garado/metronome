import { StyleSheet } from "react-native";
import ContentContainer from "@/components/ContentContainer";
import { StyledButton } from "@/components/StyledButton";
import { n } from "@/utils/scaling";
import { router, useLocalSearchParams } from "expo-router";
import { setEditResult } from "@/utils/editBus";

export enum Subdivision {
  QUARTER = "Quarter",
  EIGHTH = "Eighth",
  TRIPLET = "Triplet",
  SIXTEENTH = "Sixteenth",
  QUINTUPLET = "Quintuplet",
  SEXTUPLET = "Sextuplet",
}

export const subdivisionCount: Record<Subdivision, number> = {
  [Subdivision.QUARTER]: 1,
  [Subdivision.EIGHTH]: 2,
  [Subdivision.TRIPLET]: 3,
  [Subdivision.SIXTEENTH]: 4,
  [Subdivision.QUINTUPLET]: 5,
  [Subdivision.SEXTUPLET]: 6,
};

export const subdivisionLabel: Record<Subdivision, string> = {
  [Subdivision.QUARTER]: "♩",
  [Subdivision.EIGHTH]: "♪",
  [Subdivision.TRIPLET]: "♪³",
  [Subdivision.SIXTEENTH]: "♬",
  [Subdivision.QUINTUPLET]: "♪⁵",
  [Subdivision.SEXTUPLET]: "♪⁶",
};

export default function SubdivisionsScreen() {
  const { subdivision } = useLocalSearchParams<{ subdivision: string }>();

  const handleSelect = (value: Subdivision) => {
    setEditResult({ field: "subdivision", value });
    router.back();
  };

  return (
    <ContentContainer headerTitle="Subdivisions" style={styles.container}>
      {(Object.values(Subdivision) as Subdivision[]).map((value) => (
        <StyledButton
          key={value}
          text={value}
          underline={subdivision === value}
          onPress={() => handleSelect(value)}
        />
      ))}
    </ContentContainer>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: n(20),
  },
});
