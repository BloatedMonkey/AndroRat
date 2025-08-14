#!/bin/bash

# Environment Diagnostics Script
# Provides comprehensive analysis of the Android build environment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    local status=$1
    local message=$2
    
    case $status in
        "OK")
            echo -e "${GREEN}✅ $message${NC}"
            ;;
        "WARN")
            echo -e "${YELLOW}⚠️  $message${NC}"
            ;;
        "ERROR")
            echo -e "${RED}❌ $message${NC}"
            ;;
        "INFO")
            echo -e "${BLUE}ℹ️  $message${NC}"
            ;;
        "SUCCESS")
            echo -e "${GREEN}🎉 $message${NC}"
            ;;
        *)
            echo -e "${CYAN}$message${NC}"
            ;;
    esac
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to get file size in human readable format
get_file_size() {
    local file=$1
    if [ -f "$file" ]; then
        du -h "$file" | cut -f1
    else
        echo "N/A"
    fi
}

# Function to check Java installation
check_java() {
    print_status "INFO" "Checking Java installation..."
    
    if command_exists java; then
        local java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        local java_home=${JAVA_HOME:-"Not set"}
        
        print_status "OK" "Java found: $java_version"
        print_status "INFO" "JAVA_HOME: $java_home"
        
        if [ "$java_home" != "Not set" ]; then
            if [ -d "$java_home" ]; then
                print_status "OK" "JAVA_HOME directory exists"
            else
                print_status "ERROR" "JAVA_HOME directory does not exist: $java_home"
            fi
        else
            print_status "WARN" "JAVA_HOME environment variable not set"
        fi
        
        # Check Java version compatibility
        if [[ "$java_version" == 17* ]]; then
            print_status "SUCCESS" "Java 17 detected - compatible with project requirements"
        else
            print_status "WARN" "Java version $java_version - project requires Java 17"
        fi
    else
        print_status "ERROR" "Java not found in PATH"
    fi
    
    echo
}

# Function to check Android SDK
check_android_sdk() {
    print_status "INFO" "Checking Android SDK installation..."
    
    local sdk_root=${ANDROID_SDK_ROOT:-"Not set"}
    local android_home=${ANDROID_HOME:-"Not set"}
    
    print_status "INFO" "ANDROID_SDK_ROOT: $sdk_root"
    print_status "INFO" "ANDROID_HOME: $android_home"
    
    if [ "$sdk_root" != "Not set" ] && [ -d "$sdk_root" ]; then
        print_status "OK" "Android SDK directory exists: $sdk_root"
        
        # Check SDK components
        local platform_tools="$sdk_root/platform-tools"
        local cmdline_tools="$sdk_root/cmdline-tools"
        local platforms="$sdk_root/platforms"
        
        if [ -d "$platform_tools" ]; then
            print_status "OK" "Platform tools found"
            if command_exists adb; then
                local adb_version=$(adb version | head -n 1)
                print_status "OK" "ADB: $adb_version"
            else
                print_status "WARN" "ADB not found in PATH"
            fi
        else
            print_status "WARN" "Platform tools not found"
        fi
        
        if [ -d "$cmdline_tools" ]; then
            print_status "OK" "Command line tools found"
            if [ -f "$cmdline_tools/latest/bin/sdkmanager" ]; then
                print_status "OK" "SDK Manager found"
            else
                print_status "WARN" "SDK Manager not found"
            fi
        else
            print_status "WARN" "Command line tools not found"
        fi
        
        if [ -d "$platforms" ]; then
            local platform_count=$(find "$platforms" -maxdepth 1 -type d | wc -l)
            print_status "OK" "Android platforms found: $((platform_count - 1))"
        else
            print_status "WARN" "Android platforms not found"
        fi
    else
        print_status "ERROR" "Android SDK directory not found or ANDROID_SDK_ROOT not set"
    fi
    
    echo
}

# Function to check Gradle
check_gradle() {
    print_status "INFO" "Checking Gradle installation..."
    
    if command_exists gradle; then
        local gradle_version=$(gradle --version | grep "Gradle" | head -n 1)
        print_status "OK" "Gradle found: $gradle_version"
    else
        print_status "WARN" "Gradle not found in PATH (using wrapper)"
    fi
    
    # Check Gradle wrapper
    if [ -f "android/gradlew" ]; then
        if [ -x "android/gradlew" ]; then
            print_status "OK" "Gradle wrapper executable"
            
            # Check wrapper version
            local wrapper_version=$(cd android && ./gradlew --version | grep "Gradle" | head -n 1)
            print_status "OK" "Gradle wrapper: $wrapper_version"
            
            # Check wrapper JAR
            if [ -f "android/gradle/wrapper/gradle-wrapper.jar" ]; then
                local jar_size=$(get_file_size "android/gradle/wrapper/gradle-wrapper.jar")
                print_status "OK" "Wrapper JAR exists (size: $jar_size)"
            else
                print_status "ERROR" "Wrapper JAR not found"
            fi
            
            # Check wrapper properties
            if [ -f "android/gradle/wrapper/gradle-wrapper.properties" ]; then
                local distribution_url=$(grep "distributionUrl" android/gradle/wrapper/gradle-wrapper.properties | cut -d'=' -f2)
                print_status "OK" "Distribution URL: $distribution_url"
            else
                print_status "ERROR" "Wrapper properties not found"
            fi
        else
            print_status "ERROR" "Gradle wrapper not executable"
        fi
    else
        print_status "ERROR" "Gradle wrapper not found"
    fi
    
    echo
}

# Function to check project structure
check_project_structure() {
    print_status "INFO" "Checking project structure..."
    
    local project_root=$(pwd)
    print_status "INFO" "Project root: $project_root"
    
    # Check for spaces in path
    if [[ "$project_root" == *" "* ]]; then
        print_status "WARN" "Project path contains spaces - may cause issues"
        print_status "INFO" "Consider renaming to: $(echo "$project_root" | sed 's/ /_/g')"
    else
        print_status "OK" "Project path is clean"
    fi
    
    # Check essential directories
    local essential_dirs=("android" "scripts" "docs" "analysis")
    for dir in "${essential_dirs[@]}"; do
        if [ -d "$dir" ]; then
            print_status "OK" "Directory found: $dir"
        else
            print_status "WARN" "Directory missing: $dir"
        fi
    done
    
    # Check essential files
    local essential_files=("android/app/build.gradle" "android/build.gradle" "android/settings.gradle" "README.md")
    for file in "${essential_files[@]}"; do
        if [ -f "$file" ]; then
            print_status "OK" "File found: $file"
        else
            print_status "WARN" "File missing: $file"
        fi
    done
    
    echo
}

# Function to check system resources
check_system_resources() {
    print_status "INFO" "Checking system resources..."
    
    # Check disk space
    local disk_space=$(df -h . | awk 'NR==2 {print $4}')
    print_status "INFO" "Available disk space: $disk_space"
    
    # Check memory
    local total_mem=$(free -h | awk 'NR==2 {print $2}')
    local available_mem=$(free -h | awk 'NR==2 {print $7}')
    print_status "INFO" "Total memory: $total_mem, Available: $available_mem"
    
    # Check CPU
    local cpu_count=$(nproc)
    print_status "INFO" "CPU cores: $cpu_count"
    
    # Check file permissions
    if [ -r "android" ] && [ -w "android" ]; then
        print_status "OK" "Android directory permissions OK"
    else
        print_status "ERROR" "Android directory permission issues"
    fi
    
    echo
}

# Function to check build tools
check_build_tools() {
    print_status "INFO" "Checking build tools..."
    
    # Check Android build tools
    if [ -n "$ANDROID_SDK_ROOT" ] && [ -d "$ANDROID_SDK_ROOT" ]; then
        local build_tools_dir="$ANDROID_SDK_ROOT/build-tools"
        if [ -d "$build_tools_dir" ]; then
            local build_tools_count=$(find "$build_tools_dir" -maxdepth 1 -type d | wc -l)
            print_status "OK" "Build tools found: $((build_tools_count - 1))"
        else
            print_status "WARN" "Build tools directory not found"
        fi
    fi
    
    # Check essential tools
    local essential_tools=("git" "make" "gcc" "unzip" "wget")
    for tool in "${essential_tools[@]}"; do
        if command_exists "$tool"; then
            print_status "OK" "Tool found: $tool"
        else
            print_status "WARN" "Tool missing: $tool"
        fi
    done
    
    echo
}

# Function to check environment variables
check_environment_variables() {
    print_status "INFO" "Checking environment variables..."
    
    local env_vars=("JAVA_HOME" "ANDROID_SDK_ROOT" "ANDROID_HOME" "PATH" "GRADLE_HOME")
    for var in "${env_vars[@]}"; do
        local value="${!var}"
        if [ -n "$value" ]; then
            print_status "OK" "$var: $value"
        else
            print_status "WARN" "$var: Not set"
        fi
    done
    
    echo
}

# Function to generate summary report
generate_summary() {
    print_status "INFO" "Generating summary report..."
    
    local report_file="analysis/ENVIRONMENT_DIAGNOSTICS.md"
    mkdir -p analysis
    
    cat > "$report_file" << EOF
# Environment Diagnostics Report

Generated on: $(date)

## Summary

This report contains a comprehensive analysis of the Android build environment.

## Environment Details

- **Project Root**: $(pwd)
- **Java Version**: $(java -version 2>&1 | head -n 1 | cut -d'"' -f2 2>/dev/null || echo "Not available")
- **Gradle Version**: $(cd android && ./gradlew --version | grep "Gradle" | head -n 1 2>/dev/null || echo "Not available")
- **Android SDK**: ${ANDROID_SDK_ROOT:-"Not set"}

## Recommendations

Based on the diagnostics, consider the following:

1. **Environment Setup**: Ensure all required tools are properly installed
2. **Path Configuration**: Verify PATH includes all necessary tools
3. **Permissions**: Check file and directory permissions
4. **Resources**: Monitor system resources during builds

## Next Steps

1. Address any ERROR or WARN status items
2. Run build tests to verify environment
3. Update environment configuration if needed
4. Re-run diagnostics after changes

EOF
    
    print_status "SUCCESS" "Diagnostics report generated: $report_file"
}

# Main execution
main() {
    echo -e "${PURPLE}🔍 Environment Diagnostics${NC}"
    echo "=================================="
    echo
    
    check_java
    check_android_sdk
    check_gradle
    check_project_structure
    check_system_resources
    check_build_tools
    check_environment_variables
    
    echo -e "${PURPLE}==================================${NC}"
    generate_summary
    print_status "SUCCESS" "Environment diagnostics completed!"
}

# Run main function
main "$@"
