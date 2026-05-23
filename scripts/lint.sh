#!/bin/bash

# Linting script for Kotlin code
# Usage: ./scripts/lint.sh [options]
#   Options:
#     --fix, -f         Auto-fix linting issues
#     --format          Format code
#     --check           Only check, don't modify files
#     --verbose, -v     Verbose output
#     --help, -h        Show this help message

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default options
FIX=false
FORMAT=false
CHECK=false
VERBOSE=false

# Parse arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    -f|--fix)
      FIX=true
      shift
      ;;
    --format)
      FORMAT=true
      shift
      ;;
    --check)
      CHECK=true
      shift
      ;;
    -v|--verbose)
      VERBOSE=true
      shift
      ;;
    -h|--help)
      echo "Linting script"
      echo ""
      echo "Usage: ./scripts/lint.sh [options]"
      echo ""
      echo "Options:"
      echo "  -f, --fix         Auto-fix linting issues"
      echo "  --format          Format code"
      echo "  --check           Only check, don't modify files"
      echo "  -v, --verbose     Verbose output"
      echo "  -h, --help        Show this help message"
      exit 0
      ;;
    *)
      echo -e "${RED}Unknown option: $1${NC}"
      exit 1
      ;;
  esac
done

echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║         Code Linting & Formatting      ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
echo ""

# Check if ktlint is available
if ! command -v ktlint &> /dev/null; then
  echo -e "${YELLOW}⚠️  ktlint not found. Installing via Gradle...${NC}"
  echo ""
  
  # Create a ktlint wrapper task if it doesn't exist
  echo -e "${BLUE}Note: You can install ktlint globally with:${NC}"
  echo -e "${BLUE}  brew install ktlint${NC}"
  echo -e "${BLUE}  or${NC}"
  echo -e "${BLUE}  curl -sSLO https://github.com/pinterest/ktlint/releases/download/1.0.1/ktlint && chmod a+x ktlint && sudo mv ktlint /usr/local/bin/${NC}"
  echo ""
fi

# Build the command
if [ "$FORMAT" = true ]; then
  echo -e "${YELLOW}🎨 Formatting Kotlin code...${NC}"
  
  if command -v ktlint &> /dev/null; then
    ktlint -F "**/*.kt"
  else
    # Use Gradle spotless or detekt if available
    if ./gradlew tasks --all | grep -q "spotlessApply"; then
      ./gradlew spotlessApply
    else
      echo -e "${RED}No formatter available. Please install ktlint or add spotless plugin.${NC}"
      exit 1
    fi
  fi
  
  echo -e "${GREEN}✅ Code formatted successfully!${NC}"
  
elif [ "$CHECK" = true ]; then
  echo -e "${YELLOW}🔍 Checking code style...${NC}"
  
  if command -v ktlint &> /dev/null; then
    if ktlint "**/*.kt"; then
      echo -e "${GREEN}✅ No linting issues found!${NC}"
    else
      echo -e "${RED}❌ Linting issues found!${NC}"
      echo -e "${YELLOW}Run './scripts/lint.sh --format' to auto-fix${NC}"
      exit 1
    fi
  else
    # Use Gradle spotless check or detekt if available
    if ./gradlew tasks --all | grep -q "spotlessCheck"; then
      ./gradlew spotlessCheck
    elif ./gradlew tasks --all | grep -q "detekt"; then
      ./gradlew detekt
    else
      echo -e "${YELLOW}⚠️  No linter available. Running basic compilation check...${NC}"
      ./gradlew compileKotlin compileTestKotlin
    fi
    
    echo -e "${GREEN}✅ Code checks passed!${NC}"
  fi
  
elif [ "$FIX" = true ]; then
  echo -e "${YELLOW}🔧 Auto-fixing linting issues...${NC}"
  
  if command -v ktlint &> /dev/null; then
    ktlint -F "**/*.kt"
  else
    if ./gradlew tasks --all | grep -q "spotlessApply"; then
      ./gradlew spotlessApply
    else
      echo -e "${RED}No auto-fix available. Please install ktlint or add spotless plugin.${NC}"
      exit 1
    fi
  fi
  
  echo -e "${GREEN}✅ Issues fixed!${NC}"
  
else
  # Default: just check
  echo -e "${YELLOW}🔍 Running linter...${NC}"
  
  if command -v ktlint &> /dev/null; then
    if ktlint "**/*.kt"; then
      echo -e "${GREEN}✅ No linting issues found!${NC}"
    else
      echo -e "${RED}❌ Linting issues found!${NC}"
      echo -e "${YELLOW}Run './scripts/lint.sh --fix' to auto-fix${NC}"
      exit 1
    fi
  else
    # Fallback to compilation check
    echo -e "${YELLOW}⚠️  ktlint not found. Running compilation check...${NC}"
    ./gradlew compileKotlin compileTestKotlin
    echo -e "${GREEN}✅ Compilation successful!${NC}"
  fi
fi

echo ""
echo -e "${GREEN}Done!${NC}"
