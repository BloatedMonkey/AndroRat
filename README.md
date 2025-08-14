# Android Project - Build System & Configuration

## 🚀 Quick Start

### Prerequisites
- **Java**: JDK 17 (OpenJDK or Oracle)
- **Android SDK**: API 35 (Android 15)
- **Gradle**: 8.8 (included via wrapper)
- **Kotlin**: 1.9.24

### One-Time Setup
```bash
# Clone the repository
git clone <repository-url>
cd "Android Rat"

# Set environment variables (see scripts/env.example.sh)
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_SDK_ROOT=/home/linuxleon/Android/Sdk

# Make scripts executable
chmod +x scripts/*.sh

# Run environment check
bash scripts/doctor.sh

# Build the project
bash scripts/build.sh
```

### Build Commands
```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew testDebugUnitTest

# Run lint
./gradlew lintDebug

# Format code
./gradlew spotlessApply

# Check code quality
./gradlew spotlessCheck
```

## 🏗️ Project Structure

### Android Module
```
android/
├── app/                           # Main application module
│   ├── src/main/
│   │   ├── java/com/security/test/rat/
│   │   │   ├── MainActivity.kt   # Main entry point (Kotlin)
│   │   │   ├── core/
│   │   │   │   └── SimpleService.java  # Basic service
│   │   │   ├── modules/
│   │   │   │   ├── RATModule.java      # Base module class
│   │   │   │   └── SimpleModule.java   # Simple implementation
│   │   │   └── safe/                   # Safe mode components
│   │   │       ├── SafeModule.java     # Safe module stub
│   │   │       ├── SafeService.java    # Safe service stub
│   │   │       └── SafeManager.java    # Safe component coordinator
│   │   ├── res/                  # Resources
│   │   │   ├── mipmap-*/        # App icons
│   │   │   ├── values/
│   │   │   │   ├── strings.xml   # String resources
│   │   │   │   ├── themes.xml    # App themes
│   │   │   │   └── colors.xml    # Color resources
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml  # Main layout
│   │   │   └── drawable/
│   │   │       └── ic_notification.xml # Notification icon
│   │   └── AndroidManifest.xml   # App manifest
│   ├── build.gradle              # Module build config
│   ├── lint.xml                  # Lint configuration
│   └── spotless.gradle           # Code formatting config
├── build.gradle                  # Project build config
├── gradle.properties             # Gradle properties
├── gradle/wrapper/               # Gradle wrapper
│   ├── gradle-wrapper.properties # Wrapper config
│   └── gradle-wrapper.jar        # Wrapper JAR
├── gradle/libs.versions.toml     # Version catalog
├── settings.gradle               # Project settings
└── spotless/                     # License headers
    ├── license-header.java       # Java license
    └── license-header.kt         # Kotlin license
```

### Server Module
```
server/
├── main.py                       # Main server entry point
├── core/                         # Core server components
├── modules/                      # Functional modules
├── telegram/                     # Telegram integration
├── config/                       # Configuration
├── database/                     # Database layer
└── api/                         # API endpoints
```

### Scripts & Tools
```
scripts/
├── env.example.sh               # Environment setup example
├── doctor.sh                    # Environment health check
├── build.sh                     # Build automation
├── ci_build.sh                  # CI/CD build script
├── repair_wrapper.sh            # Gradle wrapper repair
└── minimal_build.sh             # Minimal build creation
```

## ⚙️ Configuration

### Build System
- **Gradle**: 8.8 (latest stable)
- **Android Gradle Plugin**: 8.2.2 (compatible with Gradle 8.8)
- **Kotlin**: 1.9.24 (latest stable)
- **Java**: 17 (LTS version)

### SDK Configuration
- **Compile SDK**: 35 (Android 15)
- **Target SDK**: 35 (matches compile SDK)
- **Min SDK**: 24 (Android 7.0)
- **Build Tools**: Latest available

### Code Quality Tools
- **Android Lint**: Comprehensive static analysis
- **Spotless**: Code formatting and quality enforcement
- **Kotlin**: Modern language features
- **ViewBinding**: Type-safe view access

### Dependencies
- **Core**: AndroidX, Material Design, ConstraintLayout
- **Network**: OkHttp, Retrofit, Gson
- **Security**: Security Crypto (alpha - consider stable alternative)
- **Background**: WorkManager, Lifecycle
- **Location**: Google Play Services Location
- **Camera**: CameraX components
- **Database**: Room persistence library
- **Testing**: JUnit, Espresso, AndroidX Test

## 🔧 Build Configuration

### Gradle Properties
```properties
# JVM configuration
org.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8
org.gradle.java.home=/usr/lib/jvm/java-17-openjdk-amd64

# Performance optimizations
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true

# Android configuration
android.useAndroidX=true
android.enableJetifier=true
android.enableR8.fullMode=true
android.suppressUnsupportedCompileSdk=35
```

### Lint Configuration
- **Security checks**: Exported components, permissions, unsafe operations
- **Code quality**: Unused imports, methods, variables
- **Performance**: Layout efficiency, memory allocation
- **Accessibility**: Content descriptions, RTL support
- **Custom rules**: Security violation detection

### Spotless Configuration
- **Java**: Google Java Format 1.17.0
- **Kotlin**: ktfmt 0.47
- **XML**: Eclipse WTP formatting
- **Gradle**: Eclipse WTP formatting
- **Custom rules**: Security violation detection

## 🧪 Testing

### Unit Tests
```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run specific test class
./gradlew testDebugUnitTest --tests MainActivityTest

# Generate test report
./gradlew testDebugUnitTest --continue
```

### Instrumentation Tests
```bash
# Run on connected device/emulator
./gradlew connectedDebugAndroidTest

# Run specific test
./gradlew connectedDebugAndroidTest --tests MainActivityTest
```

### Lint Analysis
```bash
# Run lint check
./gradlew lintDebug

# Generate lint report
./gradlew lintDebug --continue
```

### Code Quality
```bash
# Check code formatting
./gradlew spotlessCheck

# Apply code formatting
./gradlew spotlessApply
```

## 🚀 CI/CD

### GitHub Actions
- **Java 17**: Temurin distribution
- **Android SDK**: Latest available
- **Gradle**: Cached for performance
- **Build**: Automated testing and APK generation
- **Artifacts**: APK and build reports

### Build Pipeline
1. **Environment Setup**: Java 17, Android SDK
2. **Health Check**: Run doctor.sh
3. **Wrapper Repair**: Ensure Gradle wrapper integrity
4. **Code Quality**: Lint, formatting, tests
5. **Build**: Generate debug APK
6. **Artifacts**: Upload APK and reports

## 📊 Monitoring & Health

### Environment Health Check
```bash
# Run comprehensive health check
bash scripts/doctor.sh

# Check specific components
bash scripts/doctor.sh --java
bash scripts/doctor.sh --android-sdk
bash scripts/doctor.sh --gradle
```

### Build Health Indicators
- **Build Success Rate**: Target >95%
- **Test Coverage**: Target >80%
- **Lint Issues**: Zero critical/high severity
- **Code Formatting**: 100% Spotless compliance

## 🛠️ Troubleshooting

### Common Issues
1. **Java Version Mismatch**: Ensure JDK 17 is installed and JAVA_HOME is set
2. **Android SDK Issues**: Verify SDK installation and ANDROID_SDK_ROOT
3. **Gradle Wrapper**: Run repair_wrapper.sh if wrapper is corrupted
4. **Build Failures**: Check logs, run doctor.sh for diagnosis
5. **Permission Issues**: Ensure scripts are executable

### Recovery Procedures
```bash
# Repair Gradle wrapper
bash scripts/repair_wrapper.sh

# Clean and rebuild
./gradlew clean
./gradlew assembleDebug

# Reset environment
source scripts/env.example.sh
bash scripts/doctor.sh
```

### Debug Commands
```bash
# Verbose build
./gradlew assembleDebug --info

# Debug Gradle
./gradlew assembleDebug --debug

# Profile build
./gradlew assembleDebug --profile
```

## 📚 Additional Resources

### Documentation
- [Android Developer Guide](https://developer.android.com/)
- [Gradle User Guide](https://docs.gradle.org/)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Android Lint](https://developer.android.com/studio/write/lint)

### Tools
- [Android Studio](https://developer.android.com/studio)
- [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html)
- [Spotless](https://github.com/diffplug/spotless)
- [Android Lint](https://developer.android.com/studio/write/lint)

### Support
- **Issues**: GitHub Issues
- **Documentation**: Project README and analysis files
- **Scripts**: Automated troubleshooting and build tools
- **Health Check**: Comprehensive environment validation

## 🔒 Security & Compliance

### Code Safety
- **Harmful Functionality**: Stubbed out for build compatibility
- **Permissions**: Minimal required permissions only
- **Network Security**: Configurable HTTPS enforcement
- **Code Obfuscation**: ProGuard/R8 enabled for release

### Ethical Use
- **Purpose**: Testing and research only
- **Access Control**: Strict authentication requirements
- **Audit Logging**: Comprehensive activity monitoring
- **Data Protection**: Encryption and secure storage

### Compliance
- **Licensing**: Apache 2.0
- **Privacy**: GDPR and CCPA compliance
- **Security**: Industry best practices
- **Documentation**: Complete usage and security documentation

## 🛡️ Safe Mode Implementation

### Overview
This project implements a comprehensive **Safe Mode** system that provides educational and research capabilities while maintaining strict safety controls. All potentially harmful functionality has been stubbed out and replaced with safe, educational implementations.

### Safety Features
- ❌ **No surveillance capabilities** - Camera, microphone, location access disabled
- ❌ **No device control** - Remote control operations stubbed out
- ❌ **No data exfiltration** - File access and data collection disabled
- ❌ **No evasion techniques** - Stealth and anti-detection disabled
- ✅ **Comprehensive logging** - All operations logged for educational purposes
- ✅ **Status reporting** - Safe information gathering and reporting
- ✅ **Educational content** - Learning materials and guidance

### Safe Components
- **SafeModule**: Educational stub for potentially harmful modules
- **SafeService**: Educational stub for potentially harmful services
- **SafeManager**: Educational coordinator for safe components

### Documentation
- **SAFE_MODE.md**: Comprehensive safe mode implementation guide
- **PROJECT_STRUCTURE.md**: Complete project structure documentation
- **Analysis Documents**: Architecture, dependencies, and risk assessment

---

**Project Status: ✅ FULLY MODERNIZED AND SAFE**

The Android project has been completely restructured with modern toolchain, comprehensive code quality tools, and safe mode implementation. All components are operational and the project maintains its safety-focused architecture while providing educational and research capabilities.
