import { Text, View, StyleSheet } from "react-native";
import { Subdivision } from "@/app/subdivisions";
import { n } from "@/utils/scaling";
import { StyledText } from "./StyledText";

const NOTE_QUARTER = "\uE1D5";
const NOTE_EIGHTH = "\uE1D7";
const NOTE_SIXTEENTH = "\uE1D9";

type Props = { subdivision: Subdivision; color: string; size?: number };

export function SubdivisionIcon({ subdivision, color, size = n(12) }: Props) {
  const baseStyle = {
    fontFamily: "Bravura",
    color,
    includeFontPadding: true,
    textAlign: "center" as const,
  };

  if ([Subdivision.TRIPLET, Subdivision.QUINTUPLET, Subdivision.SEXTUPLET].includes(subdivision)) {
    const number = subdivision === Subdivision.TRIPLET ? "3"
      : subdivision === Subdivision.QUINTUPLET ? "5"
        : "6";

    return (
      <View style={{ flexDirection: "row", alignItems: "flex-start", backgroundColor: "transparent" }}>
        <Text style={[baseStyle, { fontSize: n(30), lineHeight: n(30), paddingTop: n(28) }]}>{NOTE_EIGHTH}</Text>
        <StyledText style={{ fontFamily: "PublicSans-Regular", color, fontSize: n(12), lineHeight: size * 0.8 }}>
          {number}
        </StyledText>
      </View>
    );
  }

  const glyph = subdivision === Subdivision.QUARTER ? NOTE_QUARTER
    : subdivision === Subdivision.EIGHTH ? NOTE_EIGHTH
      : NOTE_SIXTEENTH;

  return (
    <View style={{ height: size * 2, alignItems: "center", justifyContent: "center", backgroundColor: "transparent" }}>
      <Text style={[baseStyle, { fontSize: n(30), lineHeight: n(30), paddingTop: n(26) }]}>{glyph}</Text>
    </View>
  );
}
