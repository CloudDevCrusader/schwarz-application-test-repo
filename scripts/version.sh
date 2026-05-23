#!/bin/bash

# Version management script
# Usage: ./scripts/version.sh [command] [options]
#   Commands:
#     show              Show current version
#     bump [type]       Bump version (major, minor, patch)
#     set <version>     Set specific version
#     
#   Options:
#     --help, -h        Show this help message

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Version file location
VERSION_FILE="build.gradle.kts"
SETTINGS_FILE="settings.gradle.kts"

# Extract current version from build.gradle.kts
get_current_version() {
  if [ -f "$VERSION_FILE" ]; then
    grep -E "version.*=.*\".*\"" "$VERSION_FILE" | head -1 | sed 's/.*"\(.*\)".*/\1/'
  else
    echo "0.0.0"
  fi
}

# Parse semantic version
parse_version() {
  local version=$1
  local major minor patch suffix
  
  # Remove -SNAPSHOT or other suffixes
  if [[ $version =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)(-.*)?$ ]]; then
    major="${BASH_REMATCH[1]}"
    minor="${BASH_REMATCH[2]}"
    patch="${BASH_REMATCH[3]}"
    suffix="${BASH_REMATCH[4]}"
    
    echo "$major $minor $patch $suffix"
  else
    echo "0 0 0"
  fi
}

# Bump version
bump_version() {
  local type=$1
  local current_version=$(get_current_version)
  local parsed=($(parse_version "$current_version"))
  local major=${parsed[0]}
  local minor=${parsed[1]}
  local patch=${parsed[2]}
  local suffix=${parsed[3]}
  
  case $type in
    major)
      major=$((major + 1))
      minor=0
      patch=0
      ;;
    minor)
      minor=$((minor + 1))
      patch=0
      ;;
    patch)
      patch=$((patch + 1))
      ;;
    *)
      echo -e "${RED}Invalid bump type: $type${NC}"
      echo -e "${YELLOW}Valid types: major, minor, patch${NC}"
      exit 1
      ;;
  esac
  
  local new_version="$major.$minor.$patch$suffix"
  echo "$new_version"
}

# Set version in files
set_version() {
  local new_version=$1
  
  echo -e "${YELLOW}📝 Updating version to $new_version...${NC}"
  
  # Update version in build.gradle.kts subprojects
  if [ -f "$VERSION_FILE" ]; then
    # Update the version in the subprojects block
    sed -i.bak "s/version = \".*\"/version = \"$new_version\"/" "$VERSION_FILE"
    rm -f "$VERSION_FILE.bak"
    echo -e "${GREEN}  ✅ Updated $VERSION_FILE${NC}"
  fi
  
  # Update version in all module build.gradle.kts files
  for module in server client core; do
    if [ -f "$module/build.gradle.kts" ]; then
      if grep -q "version = " "$module/build.gradle.kts"; then
        sed -i.bak "s/version = \".*\"/version = \"$new_version\"/" "$module/build.gradle.kts"
        rm -f "$module/build.gradle.kts.bak"
        echo -e "${GREEN}  ✅ Updated $module/build.gradle.kts${NC}"
      fi
    fi
  done
  
  echo -e "${GREEN}✅ Version updated to $new_version${NC}"
}

# Show help
show_help() {
  echo "Version management script"
  echo ""
  echo "Usage: ./scripts/version.sh [command] [options]"
  echo ""
  echo "Commands:"
  echo "  show              Show current version"
  echo "  bump <type>       Bump version (major, minor, patch)"
  echo "  set <version>     Set specific version"
  echo ""
  echo "Options:"
  echo "  -h, --help        Show this help message"
  echo ""
  echo "Examples:"
  echo "  ./scripts/version.sh show"
  echo "  ./scripts/version.sh bump patch"
  echo "  ./scripts/version.sh bump minor"
  echo "  ./scripts/version.sh set 2.0.0-SNAPSHOT"
}

# Main logic
if [ $# -eq 0 ]; then
  show_help
  exit 0
fi

COMMAND=$1
shift

case $COMMAND in
  show)
    echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║         Current Version                ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
    echo ""
    current_version=$(get_current_version)
    echo -e "${GREEN}Version: $current_version${NC}"
    ;;
    
  bump)
    if [ $# -eq 0 ]; then
      echo -e "${RED}Error: Bump type required${NC}"
      echo -e "${YELLOW}Usage: ./scripts/version.sh bump <major|minor|patch>${NC}"
      exit 1
    fi
    
    TYPE=$1
    echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║         Bumping Version                ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
    echo ""
    
    current_version=$(get_current_version)
    echo -e "${YELLOW}Current version: $current_version${NC}"
    
    new_version=$(bump_version "$TYPE")
    echo -e "${GREEN}New version:     $new_version${NC}"
    echo ""
    
    set_version "$new_version"
    ;;
    
  set)
    if [ $# -eq 0 ]; then
      echo -e "${RED}Error: Version required${NC}"
      echo -e "${YELLOW}Usage: ./scripts/version.sh set <version>${NC}"
      exit 1
    fi
    
    NEW_VERSION=$1
    echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║         Setting Version                ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
    echo ""
    
    set_version "$NEW_VERSION"
    ;;
    
  -h|--help)
    show_help
    ;;
    
  *)
    echo -e "${RED}Unknown command: $COMMAND${NC}"
    echo ""
    show_help
    exit 1
    ;;
esac
