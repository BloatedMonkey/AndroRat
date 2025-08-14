#!/bin/bash

# Post-Fix Verification Script
# Comprehensive validation of all project components after fixes

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

# Function to run command and capture output
run_command() {
    local command="$1"
    local description="$2"
    
    print_status "INFO" "Running: $description"
    
    # Execute command and capture exit code
    if eval "$command" >/dev/null 2>&1; then
        print_status "OK" "$description completed successfully"
        return 0
    else
        print_status "ERROR" "$description failed"
        return 1
    fi
}

# Function to verify file exists
verify_file() {
    local file="$1"
    local description="$2"
    
    if [ -f "$file" ]; then
        print_status "OK" "File found: $description"
        return 0
    else
        print_status "ERROR" "File missing: $description"
        return 1
    fi
}

# Function to verify directory exists
verify_directory() {
    local dir="$1"
    local description="$2"
    
    if [ -d "$dir" ]; then
        print_status "OK" "Directory found: $description"
        return 0
    else
        print_status "ERROR" "Directory missing: $description"
        return 1
    fi
}

# Function to verify build system
verify_build_system() {
    print_status "INFO" "Verifying build system..."
    
    local build_success=true
    
    # Check Gradle wrapper
    if [ -f "android/gradlew" ]; then
        if [ -x "android/gradlew" ]; then
            print_status "OK" "Gradle wrapper executable"
        else
            print_status "ERROR" "Gradle wrapper not executable"
            build_success=false
        fi
    else
        print_status "ERROR" "Gradle wrapper not found"
        build_success=false
    fi
    
    # Check Gradle wrapper JAR
    if [ -f "android/gradle/wrapper/gradle-wrapper.jar" ]; then
        print_status "OK" "Gradle wrapper JAR found"
    else
        print_status "ERROR" "Gradle wrapper JAR not found"
        build_success=false
    fi
    
    # Check Gradle wrapper properties
    if [ -f "android/gradle/wrapper/gradle-wrapper.properties" ]; then
        print_status "OK" "Gradle wrapper properties found"
    else
        print_status "ERROR" "Gradle wrapper properties not found"
        build_success=false
    fi
    
    # Check build files
    local build_files=("android/build.gradle" "android/app/build.gradle" "android/settings.gradle")
    for file in "${build_files[@]}"; do
        if [ -f "$file" ]; then
            print_status "OK" "Build file found: $file"
        else
            print_status "ERROR" "Build file missing: $file"
            build_success=false
        fi
    done
    
    # Check version catalog
    if [ -f "android/gradle/libs.versions.toml" ]; then
        print_status "OK" "Version catalog found"
    else
        print_status "ERROR" "Version catalog missing"
        build_success=false
    fi
    
    echo
    
    if [ "$build_success" = true ]; then
        print_status "SUCCESS" "Build system verification completed successfully"
        return 0
    else
        print_status "ERROR" "Build system verification failed"
        return 1
    fi
}

# Function to verify project structure
verify_project_structure() {
    print_status "INFO" "Verifying project structure..."
    
    local structure_success=true
    
    # Check essential directories
    local essential_dirs=("android" "scripts" "docs" "analysis" ".github")
    for dir in "${essential_dirs[@]}"; do
        if [ -d "$dir" ]; then
            print_status "OK" "Directory found: $dir"
        else
            print_status "WARN" "Directory missing: $dir"
            structure_success=false
        fi
    done
    
    # Check essential files
    local essential_files=("README.md" "SAFE_MODE.md" "CONTRIBUTING.md" "SECURITY_NOTICE.md")
    for file in "${essential_files[@]}"; do
        if [ -f "$file" ]; then
            print_status "OK" "File found: $file"
        else
            print_status "WARN" "File missing: $file"
            structure_success=false
        fi
    done
    
    # Check Android source structure
    local android_source_dirs=("android/app/src/main/java" "android/app/src/main/res" "android/app/src/main/AndroidManifest.xml")
    for dir in "${android_source_dirs[@]}"; do
        if [ -e "$dir" ]; then
            print_status "OK" "Android source found: $dir"
        else
            print_status "ERROR" "Android source missing: $dir"
            structure_success=false
        fi
    done
    
    echo
    
    if [ "$structure_success" = true ]; then
        print_status "SUCCESS" "Project structure verification completed successfully"
        return 0
    else
        print_status "WARN" "Project structure verification completed with warnings"
        return 1
    fi
}

# Function to verify code quality tools
verify_code_quality_tools() {
    print_status "INFO" "Verifying code quality tools..."
    
    local quality_success=true
    
    # Check Lint configuration
    if [ -f "android/app/lint.xml" ]; then
        print_status "OK" "Lint configuration found"
    else
        print_status "ERROR" "Lint configuration missing"
        quality_success=false
    fi
    
    # Check Spotless configuration
    if [ -f "android/app/spotless.gradle" ]; then
        print_status "OK" "Spotless configuration found"
    else
        print_status "ERROR" "Spotless configuration missing"
        quality_success=false
    fi
    
    # Check license headers
    local license_files=("android/spotless/license-header.java" "android/spotless/license-header.kt")
    for file in "${license_files[@]}"; do
        if [ -f "$file" ]; then
            print_status "OK" "License header found: $file"
        else
            print_status "ERROR" "License header missing: $file"
            quality_success=false
        fi
    done
    
    echo
    
    if [ "$quality_success" = true ]; then
        print_status "SUCCESS" "Code quality tools verification completed successfully"
        return 0
    else
        print_status "ERROR" "Code quality tools verification failed"
        return 1
    fi
}

# Function to verify safe mode implementation
verify_safe_mode() {
    print_status "INFO" "Verifying safe mode implementation..."
    
    local safe_mode_success=true
    
    # Check safe mode components
    local safe_components=("android/app/src/main/java/com/security/test/rat/safe/SafeModule.java" "android/app/src/main/java/com/security/test/rat/safe/SafeService.java" "android/app/src/main/java/com/security/test/rat/safe/SafeManager.java")
    for component in "${safe_components[@]}"; do
        if [ -f "$component" ]; then
            print_status "OK" "Safe mode component found: $component"
        else
            print_status "ERROR" "Safe mode component missing: $component"
            safe_mode_success=false
        fi
    done
    
    # Check safe mode documentation
    if [ -f "SAFE_MODE.md" ]; then
        print_status "OK" "Safe mode documentation found"
    else
        print_status "ERROR" "Safe mode documentation missing"
        safe_mode_success=false
    fi
    
    echo
    
    if [ "$safe_mode_success" = true ]; then
        print_status "SUCCESS" "Safe mode verification completed successfully"
        return 0
    else
        print_status "ERROR" "Safe mode verification failed"
        return 1
    fi
}

# Function to verify CI/CD configuration
verify_cicd_configuration() {
    print_status "INFO" "Verifying CI/CD configuration..."
    
    local cicd_success=true
    
    # Check GitHub Actions workflow
    if [ -f ".github/workflows/android.yml" ]; then
        print_status "OK" "GitHub Actions workflow found"
    else
        print_status "ERROR" "GitHub Actions workflow missing"
        cicd_success=false
    fi
    
    # Check CI build script
    if [ -f "scripts/ci_build.sh" ]; then
        if [ -x "scripts/ci_build.sh" ]; then
            print_status "OK" "CI build script executable"
        else
            print_status "ERROR" "CI build script not executable"
            cicd_success=false
        fi
    else
        print_status "ERROR" "CI build script missing"
        cicd_success=false
    fi
    
    echo
    
    if [ "$cicd_success" = true ]; then
        print_status "SUCCESS" "CI/CD configuration verification completed successfully"
        return 0
    else
        print_status "ERROR" "CI/CD configuration verification failed"
        return 1
    fi
}

# Function to verify scripts
verify_scripts() {
    print_status "INFO" "Verifying scripts..."
    
    local scripts_success=true
    
    # Check essential scripts
    local essential_scripts=("scripts/doctor.sh" "scripts/build.sh" "scripts/repair_wrapper.sh" "scripts/diag_env.sh")
    for script in "${essential_scripts[@]}"; do
        if [ -f "$script" ]; then
            if [ -x "$script" ]; then
                print_status "OK" "Script executable: $script"
            else
                print_status "ERROR" "Script not executable: $script"
                scripts_success=false
            fi
        else
            print_status "ERROR" "Script missing: $script"
            scripts_success=false
        fi
    done
    
    echo
    
    if [ "$scripts_success" = true ]; then
        print_status "SUCCESS" "Scripts verification completed successfully"
        return 0
    else
        print_status "ERROR" "Scripts verification failed"
        return 1
    fi
}

# Function to verify documentation
verify_documentation() {
    print_status "INFO" "Verifying documentation..."
    
    local docs_success=true
    
    # Check essential documentation
    local essential_docs=("README.md" "PROJECT_STRUCTURE.md" "SAFE_MODE.md" "CONTRIBUTING.md" "SECURITY_NOTICE.md" "TROUBLESHOOTING.md")
    for doc in "${essential_docs[@]}"; do
        if [ -f "$doc" ]; then
            print_status "OK" "Documentation found: $doc"
        else
            print_status "WARN" "Documentation missing: $doc"
            docs_success=false
        fi
    done
    
    # Check analysis documents
    local analysis_docs=("analysis/ARCHITECTURE.md" "analysis/DEPENDENCIES.md" "analysis/RISK_REGISTER.md" "analysis/BUILD_HEALTH.md" "analysis/DEPENDENCY_UPGRADES.md")
    for doc in "${analysis_docs[@]}"; do
        if [ -f "$doc" ]; then
            print_status "OK" "Analysis document found: $doc"
        else
            print_status "WARN" "Analysis document missing: $doc"
            docs_success=false
        fi
    done
    
    echo
    
    if [ "$docs_success" = true ]; then
        print_status "SUCCESS" "Documentation verification completed successfully"
        return 0
    else
        print_status "WARN" "Documentation verification completed with warnings"
        return 1
    fi
}

# Function to run basic tests
run_basic_tests() {
    print_status "INFO" "Running basic tests..."
    
    local tests_success=true
    
    # Test Gradle wrapper
    print_status "INFO" "Testing Gradle wrapper version..."
    if cd android && ./gradlew --version >/dev/null 2>&1; then
        print_status "OK" "Gradle wrapper functional"
        cd ..
    else
        print_status "ERROR" "Gradle wrapper not functional"
        cd ..
        tests_success=false
    fi
    
    # Test clean build
    print_status "INFO" "Testing Gradle clean task..."
    if cd android && ./gradlew clean >/dev/null 2>&1; then
        print_status "OK" "Gradle clean successful"
        cd ..
    else
        print_status "ERROR" "Gradle clean failed"
        cd ..
        tests_success=false
    fi
    
    # Test doctor script
    print_status "INFO" "Testing doctor script execution..."
    if bash scripts/doctor.sh >/dev/null 2>&1; then
        print_status "OK" "Doctor script successful"
    else
        print_status "ERROR" "Doctor script failed"
        tests_success=false
    fi
    
    echo
    
    if [ "$tests_success" = true ]; then
        print_status "SUCCESS" "Basic tests completed successfully"
        return 0
    else
        print_status "ERROR" "Basic tests failed"
        return 1
    fi
}

# Function to generate verification report
generate_verification_report() {
    print_status "INFO" "Generating verification report..."
    
    local report_file="analysis/VERIFICATION_REPORT.md"
    mkdir -p analysis
    
    cat > "$report_file" << EOF
# Post-Fix Verification Report

Generated on: $(date)

## Verification Summary

This report contains the results of comprehensive post-fix verification for the Android project.

## Verification Results

### Build System
- **Status**: $(if verify_build_system >/dev/null 2>&1; then echo "✅ PASSED"; else echo "❌ FAILED"; fi)
- **Details**: Gradle wrapper, build files, and version catalog verification

### Project Structure
- **Status**: $(if verify_project_structure >/dev/null 2>&1; then echo "✅ PASSED"; else echo "⚠️  WARNINGS"; fi)
- **Details**: Essential directories, files, and Android source structure

### Code Quality Tools
- **Status**: $(if verify_code_quality_tools >/dev/null 2>&1; then echo "✅ PASSED"; else echo "❌ FAILED"; fi)
- **Details**: Lint, Spotless, and license header configuration

### Safe Mode Implementation
- **Status**: $(if verify_safe_mode >/dev/null 2>&1; then echo "✅ PASSED"; else echo "❌ FAILED"; fi)
- **Details**: Safe mode components and documentation

### CI/CD Configuration
- **Status**: $(if verify_cicd_configuration >/dev/null 2>&1; then echo "✅ PASSED"; else echo "❌ FAILED"; fi)
- **Details**: GitHub Actions workflow and CI scripts

### Scripts
- **Status**: $(if verify_scripts >/dev/null 2>&1; then echo "✅ PASSED"; else echo "❌ FAILED"; fi)
- **Details**: Essential scripts and executability

### Documentation
- **Status**: $(if verify_documentation >/dev/null 2>&1; then echo "✅ PASSED"; else echo "⚠️  WARNINGS"; fi)
- **Details**: Project documentation and analysis documents

### Basic Tests
- **Status**: $(if run_basic_tests >/dev/null 2>&1; then echo "✅ PASSED"; else echo "❌ FAILED"; fi)
- **Details**: Gradle functionality and script execution

## Recommendations

Based on the verification results:

1. **Address Failures**: Fix any verification failures immediately
2. **Address Warnings**: Resolve warnings to improve project quality
3. **Run Full Build**: Execute complete build process to verify functionality
4. **Test Functionality**: Verify all project features work as expected
5. **Update Documentation**: Keep documentation current with changes

## Next Steps

1. **Fix Issues**: Address any verification failures
2. **Re-verify**: Run verification again after fixes
3. **Full Testing**: Execute comprehensive testing
4. **Deployment**: Proceed with deployment if all checks pass

## Verification Environment

- **Project Root**: $(pwd)
- **Verification Date**: $(date)
- **Script Version**: 1.0.0

EOF
    
    print_status "SUCCESS" "Verification report generated: $report_file"
}

# Main execution
main() {
    echo -e "${PURPLE}🔍 Post-Fix Verification${NC}"
    echo "=================================="
    echo
    
    local overall_success=true
    local verification_results=()
    
    # Run all verifications
    print_status "INFO" "Starting comprehensive verification..."
    echo
    
    # Build system verification
    if verify_build_system; then
        verification_results+=("Build System: ✅ PASSED")
    else
        verification_results+=("Build System: ❌ FAILED")
        overall_success=false
    fi
    
    # Project structure verification
    if verify_project_structure; then
        verification_results+=("Project Structure: ✅ PASSED")
    else
        verification_results+=("Project Structure: ⚠️  WARNINGS")
    fi
    
    # Code quality tools verification
    if verify_code_quality_tools; then
        verification_results+=("Code Quality Tools: ✅ PASSED")
    else
        verification_results+=("Code Quality Tools: ❌ FAILED")
        overall_success=false
    fi
    
    # Safe mode verification
    if verify_safe_mode; then
        verification_results+=("Safe Mode: ✅ PASSED")
    else
        verification_results+=("Safe Mode: ❌ FAILED")
        overall_success=false
    fi
    
    # CI/CD configuration verification
    if verify_cicd_configuration; then
        verification_results+=("CI/CD Configuration: ✅ PASSED")
    else
        verification_results+=("CI/CD Configuration: ❌ FAILED")
        overall_success=false
    fi
    
    # Scripts verification
    if verify_scripts; then
        verification_results+=("Scripts: ✅ PASSED")
    else
        verification_results+=("Scripts: ❌ FAILED")
        overall_success=false
    fi
    
    # Documentation verification
    if verify_documentation; then
        verification_results+=("Documentation: ✅ PASSED")
    else
        verification_results+=("Documentation: ⚠️  WARNINGS")
    fi
    
    # Basic tests
    if run_basic_tests; then
        verification_results+=("Basic Tests: ✅ PASSED")
    else
        verification_results+=("Basic Tests: ❌ FAILED")
        overall_success=false
    fi
    
    # Generate report
    generate_verification_report
    
    # Display summary
    echo -e "${PURPLE}==================================${NC}"
    echo -e "${PURPLE}📊 Verification Summary${NC}"
    echo "=================================="
    
    for result in "${verification_results[@]}"; do
        echo "$result"
    done
    
    echo
    if [ "$overall_success" = true ]; then
        print_status "SUCCESS" "All critical verifications passed! Project is ready for use."
    else
        print_status "ERROR" "Some verifications failed. Please address issues before proceeding."
    fi
    
    echo
    print_status "INFO" "Verification report generated: analysis/VERIFICATION_REPORT.md"
}

# Run main function
main "$@"
