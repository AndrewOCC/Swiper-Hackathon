#!/usr/bin/env bash
set -euo pipefail

REPO="${GITHUB_REPOSITORY:-AndrewOCC/Swiper-Hackathon}"
TAG="list-manager-latest"
APK_NAME="list-manager-debug.apk"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

cd "$ROOT"
./gradlew assembleDebug

VERSION="$(grep versionName app/build.gradle | head -1 | sed 's/.*"\(.*\)".*/\1/')"
STAGING="$ROOT/build/${APK_NAME}"
mkdir -p "$ROOT/build"
cp "$ROOT/app/build/outputs/apk/debug/app-debug.apk" "$STAGING"
NOTES="Latest debug APK (v${VERSION}). Re-uploaded on each build.

Download: https://github.com/${REPO}/releases/download/${TAG}/${APK_NAME}"

if ! gh release view "$TAG" --repo "$REPO" >/dev/null 2>&1; then
  gh release create "$TAG" "$STAGING" --repo "$REPO" \
    --title "List Manager (Latest)" \
    --notes "$NOTES" \
    --latest
else
  # Delete all existing APK assets so the download URL stays stable.
  while IFS= read -r asset; do
    if [[ "$asset" == *.apk ]]; then
      gh release delete-asset "$TAG" "$asset" --repo "$REPO" -y || true
    fi
  done < <(gh release view "$TAG" --repo "$REPO" --json assets -q '.assets[].name')
  gh release upload "$TAG" "$STAGING" --repo "$REPO"
  gh release edit "$TAG" --repo "$REPO" --title "List Manager (Latest)" --notes "$NOTES" --latest
fi

echo "Published v${VERSION} to https://github.com/${REPO}/releases/download/${TAG}/${APK_NAME}"
