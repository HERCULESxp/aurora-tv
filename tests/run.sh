#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
out=$(mktemp -d)
trap 'rm -rf "$out"' EXIT
if command -v javac >/dev/null 2>&1; then
    javac -d "$out" app/src/main/java/dev/aurora/tv/RecentApps.java tests/RecentAppsTest.java
else
    java -m jdk.compiler/com.sun.tools.javac.Main -d "$out" app/src/main/java/dev/aurora/tv/RecentApps.java tests/RecentAppsTest.java
fi
java -cp "$out" dev.aurora.tv.RecentAppsTest
