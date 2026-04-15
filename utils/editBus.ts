import { Subdivision } from "@/app/subdivisions";

export type EditResult =
  | { field: "bpm"; value: number }
  | { field: "timeSig"; top: number; bottom: number }
  | { field: "subdivision"; value: Subdivision };

let pending: EditResult | null = null;

export function setEditResult(result: EditResult) {
  pending = result;
}

export function consumeEditResult(): EditResult | null {
  const result = pending;
  pending = null;
  return result;
}
