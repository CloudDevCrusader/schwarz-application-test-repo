#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${RED}======================================${NC}"
echo -e "${RED}Database Reset Script${NC}"
echo -e "${RED}======================================${NC}"
echo -e "${RED}WARNING: This will DELETE ALL DATA!${NC}"
echo ""

# Load environment variables if .env exists
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

# Database configuration with defaults
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-library}
DB_USER=${DB_USER:-postgres}
DB_PASSWORD=${DB_PASSWORD:-postgres}

echo -e "${YELLOW}Database Configuration:${NC}"
echo -e "  Host: ${BLUE}${DB_HOST}${NC}"
echo -e "  Port: ${BLUE}${DB_PORT}${NC}"
echo -e "  Database: ${BLUE}${DB_NAME}${NC}"
echo ""

# Confirmation prompt
echo -e "${RED}This will:${NC}"
echo -e "  1. Drop the database '${DB_NAME}'"
echo -e "  2. Recreate the database"
echo -e "  3. Run all migrations"
echo -e "  4. Seed with sample data"
echo ""
read -p "Are you absolutely sure? Type 'yes' to continue: " -r
echo
if [[ ! $REPLY == "yes" ]]; then
    echo -e "${YELLOW}Reset cancelled${NC}"
    exit 0
fi

export PGPASSWORD=$DB_PASSWORD

echo ""
echo -e "${YELLOW}Dropping database...${NC}"
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d postgres -c "DROP DATABASE IF EXISTS $DB_NAME;"
echo -e "${GREEN}✓ Database dropped${NC}"

echo ""
echo -e "${YELLOW}Creating database...${NC}"
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d postgres -c "CREATE DATABASE $DB_NAME;"
echo -e "${GREEN}✓ Database created${NC}"

unset PGPASSWORD

echo ""
echo -e "${YELLOW}Running migrations...${NC}"
./scripts/db/migrate.sh

echo ""
echo -e "${YELLOW}Seeding database...${NC}"
./scripts/db/seed.sh

echo ""
echo -e "${GREEN}======================================${NC}"
echo -e "${GREEN}✓ Database reset complete!${NC}"
echo -e "${GREEN}======================================${NC}"
