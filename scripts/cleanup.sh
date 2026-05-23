#!/bin/bash

# Project cleanup and maintenance script
# Usage: ./scripts/cleanup.sh [options]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Show help
show_help() {
  echo "Cleanup and maintenance script"
  echo ""
  echo "Usage: ./scripts/cleanup.sh [command]"
  echo ""
  echo "Commands:"
  echo "  all               Clean everything (build, cache, logs)"
  echo "  build             Clean build artifacts"
  echo "  cache             Clean gradle cache"
  echo "  logs              Clean log files"
  echo "  docker            Clean Docker images and volumes"
  echo "  deps              Refresh dependencies"
  echo ""
  echo "Options:"
  echo "  -h, --help        Show this help message"
}

# Clean build artifacts
clean_build() {
  echo -e "${YELLOW}🧹 Cleaning build artifacts...${NC}"
  
  ./gradlew clean
  
  # Remove additional build directories
  find . -type d -name "build" -not -path "*/node_modules/*" -exec rm -rf {} + 2>/dev/null || true
  find . -type d -name "out" -not -path "*/node_modules/*" -exec rm -rf {} + 2>/dev/null || true
  
  echo -e "${GREEN}  ✅ Build artifacts cleaned${NC}"
}

# Clean gradle cache
clean_cache() {
  echo -e "${YELLOW}🗑️  Cleaning Gradle cache...${NC}"
  
  # Clean gradle daemon
  ./gradlew --stop
  
  # Clean gradle caches (be careful with this)
  rm -rf ~/.gradle/caches/modules-2/files-2.1/* 2>/dev/null || true
  
  echo -e "${GREEN}  ✅ Cache cleaned${NC}"
}

# Clean log files
clean_logs() {
  echo -e "${YELLOW}📄 Cleaning log files...${NC}"
  
  find . -type f -name "*.log" -delete 2>/dev/null || true
  find . -type f -name "*.log.*" -delete 2>/dev/null || true
  
  # Clean specific log directories
  rm -rf logs/ 2>/dev/null || true
  
  echo -e "${GREEN}  ✅ Log files cleaned${NC}"
}

# Clean Docker
clean_docker() {
  echo -e "${YELLOW}🐳 Cleaning Docker...${NC}"
  
  if command -v docker &> /dev/null; then
    # Stop containers
    docker-compose down 2>/dev/null || true
    
    # Remove dangling images
    docker image prune -f
    
    # Remove unused volumes
    docker volume prune -f
    
    echo -e "${GREEN}  ✅ Docker cleaned${NC}"
  else
    echo -e "${YELLOW}  ⚠️  Docker not found, skipping${NC}"
  fi
}

# Refresh dependencies
refresh_deps() {
  echo -e "${YELLOW}♻️  Refreshing dependencies...${NC}"
  
  ./gradlew --refresh-dependencies
  
  echo -e "${GREEN}  ✅ Dependencies refreshed${NC}"
}

# Clean everything
clean_all() {
  echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
  echo -e "${BLUE}║       Deep Clean & Maintenance         ║${NC}"
  echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
  echo ""
  
  echo -e "${RED}⚠️  WARNING: This will clean all build artifacts, caches, and logs${NC}"
  read -p "Are you sure? (yes/no): " -r
  echo
  if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
    echo -e "${YELLOW}Cancelled${NC}"
    exit 0
  fi
  
  clean_build
  clean_cache
  clean_logs
  clean_docker
  
  echo ""
  echo -e "${GREEN}✅ Deep clean complete!${NC}"
  echo ""
  echo -e "${BLUE}💡 You may want to:${NC}"
  echo -e "${BLUE}   - Run './dev build' to rebuild the project${NC}"
  echo -e "${BLUE}   - Run './dev test' to verify everything works${NC}"
}

# Main logic
if [ $# -eq 0 ]; then
  show_help
  exit 0
fi

COMMAND=$1
shift

case $COMMAND in
  all)
    clean_all
    ;;
  build)
    echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║       Cleaning Build Artifacts         ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
    echo ""
    clean_build
    echo ""
    echo -e "${GREEN}✅ Done!${NC}"
    ;;
  cache)
    echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║       Cleaning Cache                   ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
    echo ""
    clean_cache
    echo ""
    echo -e "${GREEN}✅ Done!${NC}"
    ;;
  logs)
    echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║       Cleaning Logs                    ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
    echo ""
    clean_logs
    echo ""
    echo -e "${GREEN}✅ Done!${NC}"
    ;;
  docker)
    echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║       Cleaning Docker                  ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
    echo ""
    clean_docker
    echo ""
    echo -e "${GREEN}✅ Done!${NC}"
    ;;
  deps)
    echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║       Refreshing Dependencies          ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
    echo ""
    refresh_deps
    echo ""
    echo -e "${GREEN}✅ Done!${NC}"
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
