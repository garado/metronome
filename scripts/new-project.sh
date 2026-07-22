#!/usr/bin/env bash
#
# █▄░█ █▀▀ █░█░█   █▀█ █▀█ █▀█ ░░█ █▀▀ █▀▀ ▀█▀
# █░▀█ ██▄ ▀▄▀▄▀   █▀▀ █▀▄ █▄█ █▄█ ██▄ █▄▄ ░█░

# Run once from repo root after cloning.
# Renames tool/'s package + lighttool.toml identity when starting a new project from this template.

set -euo pipefail

TOOL_SRC="tool/src/main/kotlin"
LIGHTTOOL_TOML="tool/lighttool.toml"

if [ ! -f "$LIGHTTOOL_TOML" ]; then
    echo "error: run this from the repo root (can't find $LIGHTTOOL_TOML)" >&2
    exit 1
fi

CURRENT_PACKAGE=$(grep -h '^package ' "$TOOL_SRC"/*/*/*/*.kt 2>/dev/null | head -1 | awk '{print $2}')
if [ -z "$CURRENT_PACKAGE" ]; then
    echo "error: couldn't determine current package under $TOOL_SRC" >&2
    exit 1
fi

CURRENT_LABEL=$(grep -oP '(?<=^label = ")[^"]*' "$LIGHTTOOL_TOML")

echo "Current package: $CURRENT_PACKAGE"
read -rp "New package (e.g. dev.example.mytool): " NEW_PACKAGE

if ! [[ "$NEW_PACKAGE" =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$ ]]; then
    echo "error: package must be a lowercase dotted identifier (e.g. dev.example.mytool)" >&2
    exit 1
fi

read -rp "App label [$CURRENT_LABEL]: " NEW_LABEL
NEW_LABEL=${NEW_LABEL:-$CURRENT_LABEL}

CURRENT_PATH=$(echo "$CURRENT_PACKAGE" | tr '.' '/')
NEW_PATH=$(echo "$NEW_PACKAGE" | tr '.' '/')

echo
echo "Going to:"
echo "  - move  $TOOL_SRC/$CURRENT_PATH  ->  $TOOL_SRC/$NEW_PATH"
echo "  - update 'package $CURRENT_PACKAGE' -> 'package $NEW_PACKAGE' in each .kt file"
echo "  - set tool.id = \"$NEW_PACKAGE\" in $LIGHTTOOL_TOML"
echo "  - set label = \"$NEW_LABEL\" in $LIGHTTOOL_TOML"
echo
read -rp "Proceed? [y/N] " CONFIRM
if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
    echo "Aborted, no changes made."
    exit 0
fi

mkdir -p "$TOOL_SRC/$(dirname "$NEW_PATH")"
git mv "$TOOL_SRC/$CURRENT_PATH" "$TOOL_SRC/$NEW_PATH"
find "$TOOL_SRC" -depth -type d -empty -delete

find "$TOOL_SRC/$NEW_PATH" -name "*.kt" -exec \
    sed -i "s/^package $CURRENT_PACKAGE\$/package $NEW_PACKAGE/" {} +

sed -i "s/^id = \".*\"/id = \"$NEW_PACKAGE\"/" "$LIGHTTOOL_TOML"
sed -i "s/^label = \".*\"/label = \"$NEW_LABEL\"/" "$LIGHTTOOL_TOML"

echo
echo "Finished."

