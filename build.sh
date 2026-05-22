#!/bin/bash
# Quick setup script for building the Reels Editing Android APK

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}ReelsEditing Android APK Builder${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check for Android SDK
if [ -z "$ANDROID_HOME" ]; then
    echo -e "${YELLOW}⚠️  ANDROID_HOME not set${NC}"
    echo ""
    echo -e "${BLUE}Quick Setup:${NC}"
    echo "1. Install Android Studio from: https://developer.android.com/studio"
    echo "2. Set environment variable:"
    echo "   export ANDROID_HOME=~/Android/Sdk"
    echo ""
    echo -e "${BLUE}Or install SDK tools manually:${NC}"
    echo "   - Download: https://developer.android.com/studio#command-tools"
    echo "   - Extract to: ~/android-sdk"
    echo "   - Set: export ANDROID_HOME=~/android-sdk"
    echo ""
fi

# Check for Gradle
if command -v gradle &> /dev/null; then
    echo -e "${GREEN}✓ Gradle found${NC}: $(gradle --version | head -1)"
else
    echo -e "${RED}✗ Gradle not found${NC}"
    echo "Install from: https://gradle.org/install"
    exit 1
fi

# Check for Java
if command -v java &> /dev/null; then
    echo -e "${GREEN}✓ Java found${NC}: $(java -version 2>&1 | head -1)"
else
    echo -e "${RED}✗ Java not found${NC}"
    echo "Install Java Development Kit (JDK 11 or higher)"
    exit 1
fi

echo ""
echo -e "${BLUE}Project Information:${NC}"
echo "Package: com.app.clipsteronline.upload.reelsediting"
echo "Min SDK: Android 7.0 (API 24)"
echo "Target SDK: Android 14 (API 34)"
echo "Build Type: Debug"
echo ""

# Attempt to build
if [ -n "$ANDROID_HOME" ]; then
    echo -e "${BLUE}Starting build...${NC}"
    echo ""
    cd "$(dirname "$0")"
    
    if gradle build; then
        echo ""
        echo -e "${GREEN}✓ Build successful!${NC}"
        echo ""
        echo -e "${BLUE}APK Location:${NC}"
        echo "app/build/outputs/apk/debug/app-debug.apk"
        echo ""
        echo -e "${BLUE}Install on phone:${NC}"
        echo "adb install app/build/outputs/apk/debug/app-debug.apk"
    else
        echo ""
        echo -e "${RED}✗ Build failed${NC}"
        echo "Check BUILD_INSTRUCTIONS.md for troubleshooting"
        exit 1
    fi
else
    echo -e "${YELLOW}⚠️  Skipping build - Android SDK not configured${NC}"
    echo ""
    echo -e "${BLUE}See BUILD_INSTRUCTIONS.md for complete setup guide${NC}"
fi
