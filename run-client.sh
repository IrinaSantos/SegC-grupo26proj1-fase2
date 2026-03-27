#!/usr/bin/env sh

set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
BIN_DIR="$SCRIPT_DIR/bin"

if [ "$#" -ne 3 ]; then
    echo "Uso: $0 <ip:porto> <userID> <password>"
    exit 1
fi

exec java -cp "$BIN_DIR" client.SpertaClient "$1" "$2" "$3"
