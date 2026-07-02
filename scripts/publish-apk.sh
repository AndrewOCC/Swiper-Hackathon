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
STAGING="$(mktemp /tmp/list-manager-debug.XXXXXX.apk)"
cp "$APK_PATH" "$STAGING"
NOTES="Latest debug APK (v${VERSION}). Re-uploaded on each build.

Download: https://github.com/${REPO}/releases/download/${TAG}/${APK_NAME}"

if gh release view "$TAG" --repo "$REPO" >/dev/null 2>&1; then
  gh release upload "$TAG" "${STAGING}#${APK_NAME}" --repo "$REPO" --clobber
  gh release edit "$TAG" --repo "$REPO" --title "List Manager (Latest)" --notes "$NOTES" --latest
else
  gh release create "$TAG" "${STAGING}#${APK_NAME}" --repo "$REPO" \
    --title "List Manager (Latest)" \
    --notes "$NOTES" \
    --latest
fi

# Remove legacy asset name if a previous upload used the Gradle default.
if gh release view "$TAG" --repo "$REPO" --json assets -q '.assets[].name' 2>/dev/null | grep -qx 'app-debug.apk'; then
  gh release delete-asset "$TAG" "app-debug.apk" --repo "$REPO" -y || true
fi

rm -f "$STAGING"
echo "Published v${VERSION} to https://github.com/${REPO}/releases/download/${TAG}/${APK_NAME}"
