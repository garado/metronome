type EditResult =
    | { field: "bpm"; value: number }
    | { field: "timeSig"; top: number; bottom: number };

let pending: EditResult | null = null;

export function setEditResult(result: EditResult) {
    pending = result;
}

export function consumeEditResult(): EditResult | null {
    const result = pending;
    pending = null;
    return result;
}
