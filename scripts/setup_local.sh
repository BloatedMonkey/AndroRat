#!/bin/bash

# Local Development Setup Script
# Sets up the local environment for development with Java 17

set -e

echo "🔧 Setting up local development environment..."

# Check if we're in the project root
if [ ! -f "android/gradlew" ]; then
    echo "❌ Error: Please run this script from the project root directory"
    exit 1
fi

# Check if Java 17 is available
if [ -d "/usr/lib/jvm/java-17-openjdk-amd64" ]; then
    echo "✅ Java 17 found at /usr/lib/jvm/java-17-openjdk-amd64"
    
    # Copy local gradle properties
    if [ -f "android/gradle.properties.local" ]; then
        echo "📋 Setting up local Gradle properties..."
        cp android/gradle.properties.local android/gradle.properties
        echo "✅ Local Gradle properties configured"
    else
        echo "⚠️  Warning: gradle.properties.local not found"
    fi
    
    # Set JAVA_HOME for this session
    export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
    echo "✅ JAVA_HOME set to: $JAVA_HOME"
    
    # Verify Java version
    echo "🔍 Verifying Java version..."
    java -version
    
    # Test Gradle
    echo "🔍 Testing Gradle..."
    cd android && ./gradlew --version && cd ..
    
    echo "🎉 Local development environment setup complete!"
    echo "💡 You can now run: bash scripts/build.sh"
    
else
    echo "❌ Java 17 not found at /usr/lib/jvm/java-17-openjdk-amd64"
    echo "💡 Please install Java 17 or update the path in gradle.properties.local"
    exit 1
fi
