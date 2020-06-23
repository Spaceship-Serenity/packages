#!/bin/bash
set -e -u -x

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

