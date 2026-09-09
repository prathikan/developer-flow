#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="$ROOT/build/test-classes"
rm -rf "$BUILD"
mkdir -p "$BUILD"

find "$ROOT/src/main/java" "$ROOT/src/test/java" -name '*.java' -print0 \
  | xargs -0 javac --release 17 -d "$BUILD"
java -ea -cp "$BUILD" com.example.developerflow.DeveloperFlowAnalyzerTest