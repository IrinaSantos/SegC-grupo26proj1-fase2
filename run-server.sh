#!/usr/bin/env sh

set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
BIN_DIR="$SCRIPT_DIR/bin"

PORT="${1:-22345}"

exec java -cp "$BIN_DIR" server.SpertaServer "$PORT"
