#!/bin/bash
set -e -u -x


pip="/opt/python/cp36-cp36m/bin/pip"

echo "$pip"


# Compile wheels
echo "building"
for pkg in /build/src/*; do
    "$pip" install "$pkg"
    "$pip" wheel "$pkg" --no-deps -w /build/stage/
done


# Bundle external shared libraries into the wheels
echo "aduiting dependencies"
for whl in /build/stage/*.whl; do
    if ! auditwheel show "$whl"; then
        echo "Skipping non-platform wheel $whl"
        mv "$whl" /build/output/
    else
        auditwheel repair  "$whl" -w /build/output/
    fi
done

