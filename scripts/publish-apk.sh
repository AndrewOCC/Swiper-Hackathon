#!/usr/bin/env bash
set -euo pipefail

REPO="${GITHUB_REPOSITORY:-AndrewOCC/Swiper-Hackathon}"
TAG="list-manager-latest"
APK_NAME="list-manager-debug.apk"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

cd "$ROOT"
./gradlew assembleDebug

VERSION="$(grep versionName app/build.gradle | head -1 | sed 's/.*"\(.*\)".*/\1/')"
APK_PATH="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
NOTES="Latest debug APK (v${VERSION}). Re-uploaded on each build — use this URL for device testing."

if gh release view "$TAG" --repo "$REPO" >/dev/null 2>&1; then
  gh release upload "$TAG" "$APK_PATH#${APK_NAME}" --repo "$REPO" --clobber
  gh release edit "$TAG" --repo "$REPO" --title "List Manager (Latest)" --notes "$NOTES" --latest
else
  gh release create "$TAG" "$APK_PATH#${APK_NAME}" --repo "$REPO" \
    --title "List Manager (Latest)" \
    --notes "$NOTES" \
    --latest
fi

echo "Published v${VERSION} to https://github.com/${REPO}/releases/download/${TAG}/${APK_NAME}"
