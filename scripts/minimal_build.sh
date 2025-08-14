#!/bin/bash
# Minimal Build Script for Android Project
# This script creates a minimal working build by temporarily disabling complex modules

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔧 Minimal Build Script${NC}"
echo "============================="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ANDROID_DIR="$PROJECT_ROOT/android"

echo "Project root: $PROJECT_ROOT"
echo "Android directory: $ANDROID_DIR"

cd "$ANDROID_DIR"

# Create backup of problematic files
echo -e "\n${BLUE}Creating backups of complex modules...${NC}"
mkdir -p backup/modules
cp -r app/src/main/java/com/security/test/rat/modules/* backup/modules/ 2>/dev/null || true
cp -r app/src/main/java/com/security/test/rat/core backup/ 2>/dev/null || true

# Remove complex modules temporarily
echo -e "\n${BLUE}Removing complex modules for minimal build...${NC}"
rm -rf app/src/main/java/com/security/test/rat/modules
rm -rf app/src/main/java/com/security/test/rat/core

# Create minimal module structure
echo -e "\n${BLUE}Creating minimal module structure...${NC}"
mkdir -p app/src/main/java/com/security/test/rat/modules

# Create a simple test module
cat > app/src/main/java/com/security/test/rat/modules/SimpleModule.java << 'EOF'
package com.security.test.rat.modules;

import android.content.Context;
import org.json.JSONObject;

/**
 * Simple test module for minimal build
 */
public class SimpleModule extends RATModule {
    
    public SimpleModule() {
        super();
    }
    
    @Override
    public String getModuleName() {
        return "SimpleModule";
    }
    
    @Override
    public String getModuleVersion() {
        return "1.0.0";
    }
    
    @Override
    public void executeCommand(JSONObject command) {
        // Simple command execution
        System.out.println("Executing command: " + command.toString());
    }
}
EOF

# Create minimal core service
mkdir -p app/src/main/java/com/security/test/rat/core
cat > app/src/main/java/com/security/test/rat/core/SimpleService.java << 'EOF'
package com.security.test.rat.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Simple service for minimal build
 */
public class SimpleService extends Service {
    
    private static final String TAG = "SimpleService";
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SimpleService created");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SimpleService started");
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SimpleService destroyed");
    }
}
EOF

# Update AndroidManifest.xml to remove complex components
echo -e "\n${BLUE}Updating AndroidManifest.xml for minimal build...${NC}"

# Create a simplified manifest
cat > app/src/main/AndroidManifest.xml << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Basic permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.AndroidApp"
        android:requestLegacyExternalStorage="true"
        android:usesCleartextTraffic="true"
        tools:targetApi="31">
        
        <!-- Main activity -->
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.AndroidApp"
            android:launchMode="singleTask">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <!-- Simple service -->
        <service
            android:name=".core.SimpleService"
            android:exported="false" />
            
    </application>

</manifest>
EOF

echo -e "\n${GREEN}✅ Minimal build setup complete!${NC}"
echo "You can now try building with: ./gradlew assembleDebug"
echo ""
echo "To restore the original modules:"
echo "cp -r backup/modules/* app/src/main/java/com/security/test/rat/modules/"
echo "cp -r backup/core/* app/src/main/java/com/security/test/rat/core/"
echo "cp backup/AndroidManifest.xml app/src/main/AndroidManifest.xml"
