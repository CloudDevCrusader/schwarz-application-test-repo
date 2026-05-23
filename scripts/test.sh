#!/bin/bash

# Test runner script with coverage support
# Usage: ./scripts/test.sh [options]
#   Options:
#     --coverage, -c    Run tests with coverage report
#     --watch, -w       Run tests in watch mode
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
COVERAGE=false
WATCH=false
VERBOSE=false

# Parse arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    -c|--coverage)
      COVERAGE=true
      shift
      ;;
    -w|--watch)
      WATCH=true
      shift
      ;;
    -v|--verbose)
      VERBOSE=true
      shift
      ;;
    -h|--help)
      echo "Test runner script"
      echo ""
      echo "Usage: ./scripts/test.sh [options]"
      echo ""
      echo "Options:"
      echo "  -c, --coverage    Run tests with coverage report"
      echo "  -w, --watch       Run tests in watch mode (continuous)"
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
echo -e "${BLUE}║         Running Tests                  ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
echo ""

# Build the gradle command
GRADLE_CMD="./gradlew"
GRADLE_ARGS="test"

if [ "$VERBOSE" = true ]; then
  GRADLE_ARGS="$GRADLE_ARGS --info"
fi

if [ "$COVERAGE" = true ]; then
  echo -e "${YELLOW}📊 Running tests with coverage...${NC}"
  GRADLE_ARGS="$GRADLE_ARGS koverHtmlReport koverXmlReport"
else
  echo -e "${YELLOW}🧪 Running tests...${NC}"
fi

if [ "$WATCH" = true ]; then
  echo -e "${YELLOW}👀 Watch mode enabled (continuous testing)${NC}"
  GRADLE_ARGS="$GRADLE_ARGS --continuous"
fi

# Run the tests
if $GRADLE_CMD $GRADLE_ARGS; then
  echo ""
  echo -e "${GREEN}✅ Tests passed successfully!${NC}"
  
  if [ "$COVERAGE" = true ]; then
    echo ""
    echo -e "${GREEN}📈 Coverage reports generated:${NC}"
    echo -e "${BLUE}   HTML: ${NC}server/build/reports/kover/html/index.html"
    echo -e "${BLUE}   XML:  ${NC}server/build/reports/kover/report.xml"
    
    # Try to open the HTML report
    if command -v open &> /dev/null; then
      echo ""
      echo -e "${YELLOW}Opening coverage report in browser...${NC}"
      open server/build/reports/kover/html/index.html
    fi
  fi
  
  echo ""
  echo -e "${GREEN}Test report: ${NC}server/build/reports/tests/test/index.html"
else
  echo ""
  echo -e "${RED}❌ Tests failed!${NC}"
  echo -e "${YELLOW}Check the test report for details:${NC}"
  echo -e "${BLUE}   ${NC}server/build/reports/tests/test/index.html"
  exit 1
fi
