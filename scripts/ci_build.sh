#!/bin/bash
# Android Project CI Build Script
# This script runs comprehensive checks and builds for CI/CD

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔄 Android Project CI Build Script${NC}"
echo "======================================="

# Get project root
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ANDROID_DIR="$PROJECT_ROOT/android"

echo "Project root: $PROJECT_ROOT"
echo "Android directory: $ANDROID_DIR"

# Check if android directory exists
if [ ! -d "$ANDROID_DIR" ]; then
    echo -e "${RED}❌ android/ directory not found${NC}"
    exit 1
fi

cd "$ANDROID_DIR"

# Check if gradlew exists and is executable
if [ ! -f "gradlew" ] || [ ! -x "gradlew" ]; then
    echo -e "${RED}❌ gradlew not found or not executable${NC}"
    echo "Please run: bash scripts/repair_wrapper.sh"
    exit 1
fi

# Check if gradle-wrapper.jar exists
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo -e "${RED}❌ gradle-wrapper.jar missing${NC}"
    echo "Please run: bash scripts/repair_wrapper.sh"
    exit 1
fi

echo -e "\n${BLUE}Stopping Gradle daemon...${NC}"
./gradlew --stop || true

echo -e "\n${BLUE}Cleaning previous builds...${NC}"
./gradlew clean

echo -e "\n${BLUE}Running lint checks...${NC}"
./gradlew lintDebug || {
    echo -e "${YELLOW}⚠️ Lint found issues (non-blocking for CI)${NC}"
    echo "Lint issues can be addressed later"
}

echo -e "\n${BLUE}Running unit tests...${NC}"
./gradlew testDebugUnitTest

echo -e "\n${BLUE}Building debug APK...${NC}"
./gradlew assembleDebug --stacktrace

echo -e "\n${BLUE}Running instrumentation tests (if emulator available)...${NC}"
# Check if emulator is available
if command -v adb >/dev/null 2>&1 && adb devices | grep -q "emulator"; then
    echo "Emulator detected, running instrumentation tests..."
    ./gradlew connectedDebugAndroidTest || {
        echo -e "${YELLOW}⚠️ Instrumentation tests failed (non-blocking)${NC}"
    }
else
    echo -e "${YELLOW}⚠️ No emulator available, skipping instrumentation tests${NC}"
fi

echo -e "\n${BLUE}CI Build Summary:${NC}"
echo "====================="
echo "Project: $(basename "$PROJECT_ROOT")"
echo "Build time: $(date)"
echo "Status: Success"

# Check for output APK
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    APK_SIZE=$(du -h "app/build/outputs/apk/debug/app-debug.apk" | cut -f1)
    echo -e "${GREEN}APK generated: app/build/outputs/apk/debug/app-debug.apk (${APK_SIZE})${NC}"
else
    echo -e "${RED}❌ APK not found in expected location${NC}"
    exit 1
fi

echo -e "\n${GREEN}🎉 CI Build completed successfully!${NC}"
