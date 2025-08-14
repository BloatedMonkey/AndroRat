#!/bin/bash
# Android Project Build Script
# This script builds the Android project using the Gradle wrapper

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Android Project Build Script${NC}"
echo "=================================="

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

# Check if gradlew exists and is executable
if [ ! -f "$ANDROID_DIR/gradlew" ]; then
    echo -e "${RED}❌ gradlew not found${NC}"
    echo "Please run: bash scripts/repair_wrapper.sh"
    exit 1
fi

if [ ! -x "$ANDROID_DIR/gradlew" ]; then
    echo -e "${YELLOW}⚠️ gradlew not executable, fixing...${NC}"
    chmod +x "$ANDROID_DIR/gradlew"
fi

# Check if gradle-wrapper.jar exists
if [ ! -f "$ANDROID_DIR/gradle/wrapper/gradle-wrapper.jar" ]; then
    echo -e "${RED}❌ gradle-wrapper.jar missing${NC}"
    echo "Please run: bash scripts/repair_wrapper.sh"
    exit 1
fi

cd "$ANDROID_DIR"

echo -e "\n${BLUE}Stopping Gradle daemon...${NC}"
./gradlew --stop || true

echo -e "\n${BLUE}Cleaning previous builds...${NC}"
./gradlew clean

echo -e "\n${BLUE}Building debug APK...${NC}"
./gradlew assembleDebug --stacktrace

if [ $? -eq 0 ]; then
    echo -e "\n${GREEN}✅ Build completed successfully!${NC}"
    
    # Check for output APK
    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        APK_SIZE=$(du -h "app/build/outputs/apk/debug/app-debug.apk" | cut -f1)
        echo -e "${GREEN}APK generated: app/build/outputs/apk/debug/app-debug.apk (${APK_SIZE})${NC}"
    else
        echo -e "${YELLOW}⚠️ APK not found in expected location${NC}"
    fi
else
    echo -e "\n${RED}❌ Build failed${NC}"
    exit 1
fi

echo -e "\n${BLUE}Build Summary:${NC}"
echo "================"
echo "Project: $(basename "$PROJECT_ROOT")"
echo "Build time: $(date)"
echo "Status: Success"

echo -e "\n${GREEN}🎉 Build completed!${NC}"
