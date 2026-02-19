#!/bin/bash
# build.sh — Compile and run the Sprint 1 skeleton

set -e

echo "=== Compiling Sprint 1 ==="
find src -name "*.java" > sources.txt
mkdir -p out
javac -d out @sources.txt
echo "Compilation successful."

echo ""
echo "=== Running Sprint 1 ==="
java -cp out com.shareanalysis.Main
