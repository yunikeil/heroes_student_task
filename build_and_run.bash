#!/usr/bin/env bash
set -euo pipefail


SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

SRC_DIR="${SCRIPT_DIR}/programs"
HEROES_DIR="${SCRIPT_DIR}/heroes"
GAME_JAR="${HEROES_DIR}/Heroes Battle-1.0.0.jar"
PLUGINS_DIR="${HEROES_DIR}/jars"
TARGET_JAR="${PLUGINS_DIR}/obf.jar"

BUILD_DIR="${SCRIPT_DIR}/.build"
CLASSES_DIR="${BUILD_DIR}/classes"
OUT_JAR="${BUILD_DIR}/obf.jar"


[[ -d "$SRC_DIR" ]] || { echo "ERROR: programs/ not found: $SRC_DIR"; exit 1; }
[[ -f "$GAME_JAR" ]] || { echo "ERROR: game jar not found: $GAME_JAR"; exit 1; }
[[ -d "$PLUGINS_DIR" ]] || { echo "ERROR: heroes/jars not found: $PLUGINS_DIR"; exit 1; }

command -v javac >/dev/null 2>&1 || { echo "ERROR: javac not found. Install a JDK."; exit 1; }
command -v jar   >/dev/null 2>&1 || { echo "ERROR: jar tool not found. Install a JDK."; exit 1; }


mkdir -p "$CLASSES_DIR"

find "$CLASSES_DIR" -type f -delete


CP="$GAME_JAR"
[[ -f "$TARGET_JAR" ]] && CP="$CP:$TARGET_JAR"


echo "==> Compiling Java sources from: $SRC_DIR"


if ! find "$SRC_DIR" -type f -name "*.java" | grep -q .; then
  echo "ERROR: no .java files found in $SRC_DIR"
  exit 1
fi


find "$SRC_DIR" -type f -name "*.java" -print0 | sort -z | \
  xargs -0 javac -encoding UTF-8 -cp "$CP" -d "$CLASSES_DIR"


echo "==> Building jar: $OUT_JAR"
(
  cd "$CLASSES_DIR"
  jar --create --file "$OUT_JAR" .
)


echo "==> Installing as game plugin: $TARGET_JAR"
cp -f "$OUT_JAR" "$TARGET_JAR"


echo "==> Starting game (working dir: $HEROES_DIR)..."
(
  cd "$HEROES_DIR"
  java -jar "Heroes Battle-1.0.0.jar"
)

