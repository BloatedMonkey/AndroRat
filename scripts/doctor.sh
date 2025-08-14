#!/bin/bash
# Android Project Health Check Script
# This script diagnoses common issues without making changes

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔍 Android Project Health Check${NC}"
echo "=================================="

# Check Java version
echo -e "\n${BLUE}Java Environment:${NC}"
if command -v java >/dev/null 2>&1; then
    JAVA_VERSION=$(java -version 2>&1 | head -1)
    echo "Java: $JAVA_VERSION"
    
    if echo "$JAVA_VERSION" | grep -q "17"; then
        echo -e "${GREEN}✅ Java 17 detected${NC}"
    else
        echo -e "${RED}❌ Java 17 required, found: $JAVA_VERSION${NC}"
    fi
else
    echo -e "${RED}❌ Java not found in PATH${NC}"
fi

# Check JAVA_HOME
if [ -n "${JAVA_HOME:-}" ]; then
    echo "JAVA_HOME: $JAVA_HOME"
    if [ -d "$JAVA_HOME" ]; then
        echo -e "${GREEN}✅ JAVA_HOME directory exists${NC}"
    else
        echo -e "${RED}❌ JAVA_HOME directory not found${NC}"
    fi
else
    echo -e "${YELLOW}⚠️ JAVA_HOME not set${NC}"
fi

# Check Android SDK
echo -e "\n${BLUE}Android SDK:${NC}"
if [ -n "${ANDROID_SDK_ROOT:-}" ]; then
    echo "ANDROID_SDK_ROOT: $ANDROID_SDK_ROOT"
    if [ -d "$ANDROID_SDK_ROOT" ]; then
        echo -e "${GREEN}✅ Android SDK directory exists${NC}"
        
        # Check key packages
        if [ -d "$ANDROID_SDK_ROOT/platform-tools" ]; then
            echo -e "${GREEN}✅ platform-tools found${NC}"
        else
            echo -e "${RED}❌ platform-tools missing${NC}"
        fi
        
        if [ -d "$ANDROID_SDK_ROOT/cmdline-tools" ]; then
            echo -e "${GREEN}✅ cmdline-tools found${NC}"
        else
            echo -e "${RED}❌ cmdline-tools missing${NC}"
        fi
    else
        echo -e "${RED}❌ Android SDK directory not found${NC}"
    fi
else
    echo -e "${YELLOW}⚠️ ANDROID_SDK_ROOT not set${NC}"
fi

# Check tools
echo -e "\n${BLUE}Essential Tools:${NC}"
if command -v adb >/dev/null 2>&1; then
    echo -e "${GREEN}✅ adb found: $(which adb)${NC}"
else
    echo -e "${RED}❌ adb not found in PATH${NC}"
fi

if command -v sdkmanager >/dev/null 2>&1; then
    echo -e "${GREEN}✅ sdkmanager found: $(which sdkmanager)${NC}"
else
    echo -e "${RED}❌ sdkmanager not found in PATH${NC}"
fi

# Check project structure
echo -e "\n${BLUE}Project Structure:${NC}"
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
echo "Project root: $PROJECT_ROOT"

# Check for spaces in path
if [[ "$PROJECT_ROOT" == *" "* ]]; then
    echo -e "${YELLOW}⚠️ Project path contains spaces - may cause issues${NC}"
    echo "Consider renaming to: $(echo "$PROJECT_ROOT" | sed 's/ /_/g')"
fi

# Check Android directory
ANDROID_DIR="$PROJECT_ROOT/android"
if [ -d "$ANDROID_DIR" ]; then
    echo -e "${GREEN}✅ android/ directory found${NC}"
    
    # Check Gradle wrapper
    if [ -f "$ANDROID_DIR/gradlew" ]; then
        echo -e "${GREEN}✅ gradlew script found${NC}"
        if [ -x "$ANDROID_DIR/gradlew" ]; then
            echo -e "${GREEN}✅ gradlew is executable${NC}"
        else
            echo -e "${RED}❌ gradlew not executable${NC}"
        fi
    else
        echo -e "${RED}❌ gradlew script missing${NC}"
    fi
    
    if [ -f "$ANDROID_DIR/gradle/wrapper/gradle-wrapper.jar" ]; then
        echo -e "${GREEN}✅ gradle-wrapper.jar found${NC}"
    else
        echo -e "${RED}❌ gradle-wrapper.jar missing${NC}"
    fi
    
    if [ -f "$ANDROID_DIR/gradle/wrapper/gradle-wrapper.properties" ]; then
        echo -e "${GREEN}✅ gradle-wrapper.properties found${NC}"
        GRADLE_VERSION=$(grep "distributionUrl" "$ANDROID_DIR/gradle/wrapper/gradle-wrapper.properties" | sed 's/.*gradle-\([0-9.]*\)-.*/\1/')
        echo "Gradle version: $GRADLE_VERSION"
    else
        echo -e "${RED}❌ gradle-wrapper.properties missing${NC}"
    fi
else
    echo -e "${RED}❌ android/ directory not found${NC}"
fi

# Check disk space
echo -e "\n${BLUE}System Resources:${NC}"
DISK_SPACE=$(df "$PROJECT_ROOT" | tail -1 | awk '{print $4}')
DISK_SPACE_GB=$((DISK_SPACE / 1024 / 1024))
echo "Available disk space: ${DISK_SPACE_GB}GB"

if [ $DISK_SPACE_GB -gt 5 ]; then
    echo -e "${GREEN}✅ Sufficient disk space${NC}"
else
    echo -e "${RED}❌ Low disk space - may cause build issues${NC}"
fi

# Check file permissions
echo -e "\n${BLUE}File Permissions:${NC}"
if [ -r "$ANDROID_DIR" ] && [ -w "$ANDROID_DIR" ]; then
    echo -e "${GREEN}✅ android/ directory is readable and writable${NC}"
else
    echo -e "${RED}❌ android/ directory permission issues${NC}"
fi

if [ -d "$ANDROID_DIR/.gradle" ]; then
    if [ -r "$ANDROID_DIR/.gradle" ] && [ -w "$ANDROID_DIR/.gradle" ]; then
        echo -e "${GREEN}✅ .gradle directory permissions OK${NC}"
    else
        echo -e "${RED}❌ .gradle directory permission issues${NC}"
    fi
fi

echo -e "\n${BLUE}Health Check Complete${NC}"
echo "=================================="
