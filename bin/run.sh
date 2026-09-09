#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="$ROOT/build/classes"
mkdir -p "$BUILD"

find "$ROOT/src/main/java" -name '*.java' -print0 | xargs -0 javac --release 17 -d "$BUILD"
exec java -cp "$BUILD" com.example.developerflow.Main "$@"