# Android Project Troubleshooting Guide

This guide covers common issues and their solutions for the Android project build system.

## 🚨 Critical Issues

### Java Version Mismatch

**Error:** `Android Gradle plugin requires Java 17`

**Solution:**
```bash
# Check current Java version
java -version

# Install Java 17
sudo apt update
sudo apt install openjdk-17-jdk

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"

# Verify
java -version
echo $JAVA_HOME
```

**Alternative Java versions:**
```bash
# List available Java versions
update-java-alternatives --list

# Switch to Java 17
sudo update-java-alternatives --set java-17-openjdk-amd64
```

### Missing Gradle Wrapper

**Error:** `GradleWrapperMain class not found` or `gradle-wrapper.jar missing`

**Solution:**
```bash
# Run repair script
bash scripts/repair_wrapper.sh

# Or manually regenerate
cd android
gradle wrapper --gradle-version 8.8

# If gradle not available
sudo apt install gradle
```

**Manual download:**
```bash
# Download from Gradle releases
wget https://github.com/gradle/gradle/releases/download/v8.8/gradle-8.8-bin.zip
unzip gradle-8.8-bin.zip

# Copy wrapper jar
cp gradle-8.8/gradle/wrapper/gradle-wrapper.jar android/gradle/wrapper/
```

### AGP/Gradle Incompatibility

**Error:** `Plugin [id: 'com.android.application'] was not found`

**Solution:**
```bash
# Check Gradle version
cd android
./gradlew --version

# Update AGP version in android/build.gradle
# Use AGP 8.2.x with Gradle 8.8
```

**Compatibility Matrix:**
- **Gradle 8.8** → **AGP 8.2.x**
- **Gradle 8.4** → **AGP 8.1.x**
- **Gradle 8.0** → **AGP 8.0.x**

## 🔧 Build Issues

### Build Failures

**Error:** `Build failed with an exception`

**Solution:**
```bash
# Clean and rebuild
cd android
./gradlew clean
./gradlew assembleDebug --stacktrace

# Check for specific errors
./gradlew assembleDebug --info
```

**Common causes:**
- Insufficient memory: Increase `org.gradle.jvmargs` in `gradle.properties`
- Network issues: Check internet connection for dependency downloads
- Permission issues: Ensure write access to project directory

### Dependency Issues

**Error:** `Could not resolve dependencies`

**Solution:**
```bash
# Refresh dependencies
cd android
./gradlew --refresh-dependencies

# Check network connectivity
ping google.com

# Clear Gradle cache
rm -rf ~/.gradle/caches/
```

### Memory Issues

**Error:** `Java heap space` or `OutOfMemoryError`

**Solution:**
```bash
# Increase memory in gradle.properties
echo 'org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8' >> android/gradle.properties

# Or set environment variable
export GRADLE_OPTS="-Xmx4g"
```

## 📁 File and Path Issues

### Spaces in Project Path

**Issue:** Project folder contains spaces (e.g., "Android Rat")

**Solutions:**

**Option 1: Quote paths in scripts**
```bash
bash "scripts/build.sh"
cd "Android Rat/android"
```

**Option 2: Rename project folder**
```bash
# From parent directory
mv "Android Rat" Android_Rat

# Update any hardcoded paths in scripts
```

**Option 3: Create symlink**
```bash
# Create symlink without spaces
ln -s "Android Rat" Android_Rat
cd Android_Rat
```

### Permission Issues

**Error:** `Permission denied` or `Cannot create directory`

**Solution:**
```bash
# Check current permissions
ls -la android/

# Fix permissions
chmod -R 755 android/
chmod +x android/gradlew

# Check ownership
ls -la android/ | head -5
```

## 🌐 Network and SDK Issues

### Android SDK Problems

**Error:** `SDK location not found` or `Android SDK not found`

**Solution:**
```bash
# Set ANDROID_SDK_ROOT
export ANDROID_SDK_ROOT=$HOME/Android/Sdk

# Verify SDK installation
ls $ANDROID_SDK_ROOT

# Install missing SDK components
$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-34"
```

**SDK installation:**
```bash
# Download command line tools
wget https://dl.google.com/android/repository/commandlinetools-linux-8512546_latest.zip
unzip commandlinetools-linux-8512546_latest.zip

# Setup directory structure
mkdir -p $HOME/Android/Sdk/cmdline-tools/latest
mv cmdline-tools/* $HOME/Android/Sdk/cmdline-tools/latest/

# Accept licenses
yes | $ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager --licenses
```

### Network Connectivity

**Error:** `Connection timeout` or `Failed to download`

**Solution:**
```bash
# Check internet connection
ping google.com
curl -I https://services.gradle.org

# Configure proxy if needed
export HTTP_PROXY=http://proxy.company.com:8080
export HTTPS_PROXY=http://proxy.company.com:8080

# Use mirror repositories
# Add to android/build.gradle repositories section
```

## 🧪 Testing Issues

### Unit Test Failures

**Error:** `Tests failed` or `Test execution failed`

**Solution:**
```bash
# Run tests with verbose output
cd android
./gradlew testDebugUnitTest --info

# Run specific test
./gradlew testDebugUnitTest --tests "com.example.TestClass.testMethod"

# Check test reports
open app/build/reports/tests/testDebugUnitTest/index.html
```

### Instrumentation Test Issues

**Error:** `No devices/emulators found`

**Solution:**
```bash
# Check available devices
adb devices

# Start emulator
emulator -avd Pixel_4_API_34

# Or use physical device with USB debugging enabled
```

## 🔍 Debugging Commands

### System Health Check
```bash
# Run comprehensive health check
bash scripts/doctor.sh

# Check specific components
java -version
echo $JAVA_HOME
echo $ANDROID_SDK_ROOT
which adb
which gradle
```

### Gradle Debug
```bash
cd android

# Verbose build
./gradlew assembleDebug --debug

# Dependency insight
./gradlew dependencyInsight --dependency androidx.appcompat:appcompat

# Task execution
./gradlew tasks --all
```

### File System Check
```bash
# Check disk space
df -h .

# Check file permissions
ls -la android/
ls -la android/gradle/wrapper/

# Check for hidden files
ls -la android/.gradle/
```

## 📋 Common Error Messages

| Error | Cause | Solution |
|-------|-------|----------|
| `Java version X is not supported` | Wrong Java version | Install Java 17 |
| `Gradle wrapper not found` | Missing wrapper files | Run `repair_wrapper.sh` |
| `SDK location not found` | ANDROID_SDK_ROOT not set | Set environment variable |
| `Build failed` | Various build issues | Clean and rebuild |
| `Permission denied` | File permission issues | Fix permissions |
| `Connection timeout` | Network issues | Check connectivity |

## 🆘 Getting Help

### Before Asking for Help

1. **Run health check:** `bash scripts/doctor.sh`
2. **Check logs:** Look at build output and error messages
3. **Search issues:** Check if problem is already documented
4. **Minimal reproduction:** Create minimal test case

### Information to Include

- **Error message:** Exact error text
- **System info:** OS, Java version, Android SDK version
- **Project path:** Full path to project directory
- **Steps to reproduce:** Exact commands run
- **Health check output:** Result of `scripts/doctor.sh`

### Support Channels

- **GitHub Issues:** For bug reports and feature requests
- **Documentation:** Check this guide and README first
- **Community:** Android development forums and Stack Overflow

## 🔄 Recovery Procedures

### Complete Reset
```bash
# Stop Gradle daemon
cd android
./gradlew --stop

# Clean everything
./gradlew clean
rm -rf .gradle/
rm -rf app/build/

# Repair wrapper
bash ../scripts/repair_wrapper.sh

# Rebuild
./gradlew assembleDebug
```

### Environment Reset
```bash
# Clear environment variables
unset JAVA_HOME
unset ANDROID_SDK_ROOT
unset GRADLE_OPTS

# Reload environment
source scripts/env.example.sh

# Verify
bash scripts/doctor.sh
```

### Dependency Reset
```bash
# Clear Gradle caches
rm -rf ~/.gradle/caches/
rm -rf ~/.gradle/wrapper/

# Rebuild wrapper
cd android
gradle wrapper --gradle-version 8.8
```

---

**Remember:** Most issues can be resolved by running `bash scripts/doctor.sh` and following the recommendations. If problems persist, check the error messages carefully and ensure all prerequisites are met.
