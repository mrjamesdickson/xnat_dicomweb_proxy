#!/bin/bash

# Build the XNAT DICOMweb Proxy Plugin

echo "Building XNAT DICOMweb Proxy Plugin..."

./gradlew clean build

if [ $? -eq 0 ]; then
    echo ""
    echo "Build successful!"
    echo "Plugin JAR location: build/libs/xnat-dicomweb-proxy-1.1.1.jar"
    echo ""
    echo "To install:"
    echo "1. Copy the JAR to your XNAT plugins directory"
    echo "2. Restart XNAT"
else
    echo ""
    echo "Build failed!"
    exit 1
fi
