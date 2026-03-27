#!/usr/bin/env sh

set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
SRC_DIR="$SCRIPT_DIR/src"
BIN_DIR="$SCRIPT_DIR/bin"

mkdir -p "$BIN_DIR"
find "$BIN_DIR" -type f -name '*.class' -delete

javac -d "$BIN_DIR" $(find "$SRC_DIR" -type f -name '*.java')
