#!/bin/bash

# Place this file and AppImageBuilder.yml in an empty folder.
# Copy Simulator.zip to this folder.
# Run this file

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

if [ ! -f "${DIR}/Simulator.zip" ]
then
  echo "Simulator.zip not found"
  return
fi

if [ ! -f "${DIR}/AppImageBuilder.yml" ]
then
  echo "AppImageBuilder.yml not found"
  return
fi

if [ ! -f "${DIR}/pkg2appimage-1807-x86_64.AppImage" ]
then
  wget https://github.com/AppImage/pkg2appimage/releases/download/continuous/pkg2appimage-1807-x86_64.AppImage
fi
chmod u+x pkg2appimage-1807-x86_64.AppImage

rm -rf "${DIR}/Warteschlangensimulator"
rm -rf "${DIR}/out"

./pkg2appimage-1807-x86_64.AppImage AppImageBuilder.yml

mv ${DIR}/out/Warteschlangensimulator-.*.AppImage "${DIR}/Warteschlangensimulator-x86_64.AppImage"

rm -rf "${DIR}/Warteschlangensimulator"
rm -rf "${DIR}/out"