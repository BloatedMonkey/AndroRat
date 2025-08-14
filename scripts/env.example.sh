#!/bin/bash
# Android Project Environment Setup
# Copy this to env.sh and customize for your system

# Java 17 Configuration
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Android SDK Configuration
export ANDROID_SDK_ROOT=$HOME/Android/Sdk

# Add to PATH
export PATH="$JAVA_HOME/bin:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"

# Verify Java version
echo "Java version: $(java -version 2>&1 | head -1)"
echo "JAVA_HOME: $JAVA_HOME"
echo "Android SDK: $ANDROID_SDK_ROOT"

# Source this file: source scripts/env.example.sh
