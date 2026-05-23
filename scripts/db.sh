#!/bin/bash

# Database management script
# Usage: ./scripts/db.sh [command] [options]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Show help
show_help() {
  echo "Database management script"
  echo ""
  echo "Usage: ./scripts/db.sh [command]"
  echo ""
  echo "Commands:"
  echo "  start             Start database container"
  echo "  stop              Stop database container"
  echo "  restart           Restart database container"
  echo "  reset             Reset database (drop and recreate)"
  echo "  migrate           Run database migrations"
  echo "  seed              Seed database with test data"
  echo "  backup            Backup database"
  echo "  restore           Restore database from backup"
  echo "  shell             Open database shell"
  echo "  logs              Show database logs"
  echo ""
}

# Check if docker-compose is available
check_docker_compose() {
  if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ docker-compose not found${NC}"
    echo -e "${YELLOW}Please install docker-compose to use database commands${NC}"
    exit 1
  fi
  
  if [ ! -f "docker-compose.yml" ]; then
    echo -e "${RED}❌ docker-compose.yml not found${NC}"
    exit 1
  fi
}

# Start database
start_db() {
  echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
  echo -e "${BLUE}║       Starting Database                ║${NC}"
  echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
  echo ""
  
  check_docker_compose
  
  echo -e "${YELLOW}🐘 Starting PostgreSQL...${NC}"
  docker-compose up -d postgres
  
  echo -e "${YELLOW}⏳ Waiting for database to be ready...${NC}"
  sleep 3
  
  # Wait for database to be ready
  for i in {1..30}; do
    if docker-compose exec -T postgres pg_isready -U postgres > /dev/null 2>&1; then
      echo -e "${GREEN}✅ Database is ready!${NC}"
      echo ""
      echo -e "${BLUE}Connection details:${NC}"
      echo -e "${BLUE}  Host: localhost${NC}"
      echo -e "${BLUE}  Port: 5432${NC}"
      echo -e "${BLUE}  Database: library${NC}"
      echo -e "${BLUE}  User: postgres${NC}"
      return 0
    fi
    sleep 1
  done
  
  echo -e "${RED}❌ Database failed to start${NC}"
  exit 1
}

# Stop database
stop_db() {
  echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
  echo -e "${BLUE}║       Stopping Database                ║${NC}"
  echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
  echo ""
  
  check_docker_compose
  
  echo -e "${YELLOW}🛑 Stopping PostgreSQL...${NC}"
  docker-compose stop postgres
  
  echo -e "${GREEN}✅ Database stopped${NC}"
}

# Reset database
reset_db() {
  echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
  echo -e "${BLUE}║       Resetting Database               ║${NC}"
  echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
  echo ""
  
  check_docker_compose
  
  echo -e "${RED}⚠️  WARNING: This will delete all data!${NC}"
  read -p "Are you sure? (yes/no): " -r
  echo
  if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
    echo -e "${YELLOW}Cancelled${NC}"
    exit 0
  fi
  
  echo -e "${YELLOW}♻️  Resetting database...${NC}"
  docker-compose down -v postgres
  docker-compose up -d postgres
  
  echo -e "${YELLOW}⏳ Waiting for database to be ready...${NC}"
  sleep 3
  
  for i in {1..30}; do
    if docker-compose exec -T postgres pg_isready -U postgres > /dev/null 2>&1; then
      echo -e "${GREEN}✅ Database reset complete!${NC}"
      return 0
    fi
    sleep 1
  done
  
  echo -e "${RED}❌ Database failed to start after reset${NC}"
  exit 1
}

# Run migrations
migrate_db() {
  echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
  echo -e "${BLUE}║       Running Migrations               ║${NC}"
  echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
  echo ""
  
  echo -e "${YELLOW}🔄 Running Flyway migrations...${NC}"
  
  if ./gradlew tasks --all | grep -q "flywayMigrate"; then
    ./gradlew :server:flywayMigrate
    echo -e "${GREEN}✅ Migrations complete!${NC}"
  else
    echo -e "${BLUE}ℹ️  Migrations run automatically on server start${NC}"
  fi
}

# Backup database
backup_db() {
  echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
  echo -e "${BLUE}║       Backing Up Database              ║${NC}"
  echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
  echo ""
  
  check_docker_compose
  
  mkdir -p backups
  BACKUP_FILE="backups/db_backup_$(date +%Y%m%d_%H%M%S).sql"
  
  echo -e "${YELLOW}💾 Creating backup...${NC}"
  docker-compose exec -T postgres pg_dump -U postgres library > "$BACKUP_FILE"
  
  echo -e "${GREEN}✅ Backup created: $BACKUP_FILE${NC}"
}

# Open database shell
db_shell() {
  echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
  echo -e "${BLUE}║       Database Shell                   ║${NC}"
  echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
  echo ""
  
  check_docker_compose
  
  echo -e "${YELLOW}🐘 Opening PostgreSQL shell...${NC}"
  echo -e "${BLUE}   Database: library${NC}"
  echo ""
  docker-compose exec postgres psql -U postgres library
}

# Show logs
show_logs() {
  echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
  echo -e "${BLUE}║       Database Logs                    ║${NC}"
  echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
  echo ""
  
  check_docker_compose
  docker-compose logs -f postgres
}

# Main logic
if [ $# -eq 0 ]; then
  show_help
  exit 0
fi

COMMAND=$1
shift

case $COMMAND in
  start)
    start_db
    ;;
  stop)
    stop_db
    ;;
  restart)
    stop_db
    sleep 2
    start_db
    ;;
  reset)
    reset_db
    ;;
  migrate)
    migrate_db
    ;;
  backup)
    backup_db
    ;;
  shell)
    db_shell
    ;;
  logs)
    show_logs
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
