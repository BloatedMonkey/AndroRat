#!/bin/bash
# Gradle Wrapper Repair Script
# This script safely repairs the Gradle wrapper

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔧 Gradle Wrapper Repair Script${NC}"
echo "====================================="

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

# Check if gradle-wrapper.jar exists
if [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo -e "${GREEN}✅ gradle-wrapper.jar already exists${NC}"
else
    echo -e "${YELLOW}⚠️ gradle-wrapper.jar missing - attempting to regenerate${NC}"
    
    # Try to use gradle wrapper task if gradle is available
    if command -v gradle >/dev/null 2>&1; then
        echo "Using gradle wrapper task to regenerate wrapper..."
        gradle wrapper --gradle-version 8.8
        
        if [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
            echo -e "${GREEN}✅ gradle-wrapper.jar regenerated successfully${NC}"
        else
            echo -e "${RED}❌ Failed to regenerate gradle-wrapper.jar${NC}"
            echo "Please run manually: cd $ANDROID_DIR && gradle wrapper --gradle-version 8.8"
        fi
    else
        echo -e "${RED}❌ Gradle not found in PATH${NC}"
        echo ""
        echo "To fix this manually:"
        echo "1. Install Gradle: sudo apt install gradle"
        echo "2. Or download gradle-wrapper.jar manually:"
        echo "   - Download from: https://github.com/gradle/gradle/releases/tag/v8.8"
        echo "   - Place in: $ANDROID_DIR/gradle/wrapper/gradle-wrapper.jar"
        echo ""
        echo "3. Then run: cd $ANDROID_DIR && gradle wrapper --gradle-version 8.8"
        exit 1
    fi
fi

# Ensure gradlew is executable
if [ -f "gradlew" ]; then
    chmod +x gradlew
    echo -e "${GREEN}✅ gradlew is executable${NC}"
else
    echo -e "${RED}❌ gradlew script not found${NC}"
fi

# Verify wrapper configuration
echo -e "\n${BLUE}Wrapper Configuration:${NC}"
if [ -f "gradle/wrapper/gradle-wrapper.properties" ]; then
    echo "gradle-wrapper.properties:"
    cat gradle/wrapper/gradle-wrapper.properties
else
    echo -e "${RED}❌ gradle-wrapper.properties not found${NC}"
fi

# Test wrapper
echo -e "\n${BLUE}Testing Wrapper:${NC}"
if [ -f "gradlew" ] && [ -x "gradlew" ]; then
    echo "Running: ./gradlew --version"
    if ./gradlew --version >/dev/null 2>&1; then
        echo -e "${GREEN}✅ Gradle wrapper working correctly${NC}"
    else
        echo -e "${RED}❌ Gradle wrapper test failed${NC}"
    fi
else
    echo -e "${RED}❌ Cannot test wrapper - gradlew not executable${NC}"
fi

echo -e "\n${BLUE}Wrapper Repair Complete${NC}"
echo "============================="
