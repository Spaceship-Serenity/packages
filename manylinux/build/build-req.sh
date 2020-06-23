#!/bin/bash
set -e -u -x


pip="/opt/python/cp36-cp36m/bin/pip"
# pip="/opt/python/cp27-cp27m/bin/pip"

echo "$pip"
"$pip" download -r /build/requirements.txt -d /build/src/

# # Compile wheels
# echo "building"
# for pkg in /build/src/*; do
#     "$pip" install "$pkg"
#     "$pip" wheel "$pkg" --no-deps -w /build/stage/
# done