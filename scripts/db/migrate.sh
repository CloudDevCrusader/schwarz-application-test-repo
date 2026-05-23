#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}Database Migration Script${NC}"
echo -e "${BLUE}======================================${NC}"

# Load environment variables if .env exists
if [ -f .env ]; then
    echo -e "${YELLOW}Loading environment from .env file...${NC}"
    export $(cat .env | grep -v '^#' | xargs)
else
    echo -e "${YELLOW}Warning: .env file not found, using defaults${NC}"
fi

# Database configuration with defaults
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-library}
DB_USER=${DB_USER:-postgres}
DB_PASSWORD=${DB_PASSWORD:-postgres}

echo ""
echo -e "${GREEN}Database Configuration:${NC}"
echo -e "  Host: ${BLUE}${DB_HOST}${NC}"
echo -e "  Port: ${BLUE}${DB_PORT}${NC}"
echo -e "  Database: ${BLUE}${DB_NAME}${NC}"
echo -e "  User: ${BLUE}${DB_USER}${NC}"
echo ""

# Check if PostgreSQL is available
echo -e "${YELLOW}Checking database connection...${NC}"
export PGPASSWORD=$DB_PASSWORD

if ! psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d postgres -c '\q' 2>/dev/null; then
    echo -e "${RED}ERROR: Cannot connect to PostgreSQL server${NC}"
    echo -e "${YELLOW}Make sure PostgreSQL is running and credentials are correct${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Database connection successful${NC}"
echo ""

# Create database if it doesn't exist
echo -e "${YELLOW}Checking if database '${DB_NAME}' exists...${NC}"
if ! psql -h $DB_HOST -p $DB_PORT -U $DB_USER -lqt | cut -d \| -f 1 | grep -qw $DB_NAME; then
    echo -e "${YELLOW}Database '${DB_NAME}' does not exist. Creating...${NC}"
    psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d postgres -c "CREATE DATABASE $DB_NAME;"
    echo -e "${GREEN}✓ Database created${NC}"
else
    echo -e "${GREEN}✓ Database '${DB_NAME}' already exists${NC}"
fi

echo ""

# Run migrations using Flyway (via Gradle)
echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}Running Flyway Migrations${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""

# Check if we should use Gradle or JAR
if [ -f ./gradlew ]; then
    echo -e "${YELLOW}Using Gradle to run migrations...${NC}"
    
    # Create temporary application.yaml for migration
    TEMP_CONFIG=$(mktemp)
    cat > $TEMP_CONFIG << EOF
database:
  embedded: false
  url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
  user: ${DB_USER}
  password: ${DB_PASSWORD}
EOF
    
    # Run the application in migration-only mode
    echo -e "${YELLOW}Building and running migrations...${NC}"
    ./gradlew :server:build -x test --no-daemon
    
    # Run migrations using a Kotlin script
    echo -e "${YELLOW}Executing Flyway migrations...${NC}"
    java -cp "server/build/libs/*:server/build/resources/main" \
         -Ddatabase.embedded=false \
         -Ddatabase.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME} \
         -Ddatabase.user=${DB_USER} \
         -Ddatabase.password=${DB_PASSWORD} \
         -jar server/build/libs/server-*.jar migrate 2>/dev/null || {
        echo -e "${YELLOW}Note: Using direct SQL migration approach...${NC}"
        
        # Fallback to direct SQL execution
        for migration in server/src/main/resources/db/migration/*.sql; do
            if [ -f "$migration" ]; then
                echo -e "${YELLOW}Applying migration: $(basename $migration)${NC}"
                psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f "$migration"
            fi
        done
    }
    
    rm -f $TEMP_CONFIG
else
    echo -e "${YELLOW}Running SQL migrations directly...${NC}"
    
    # Run migrations from SQL files
    MIGRATION_DIR="./server/src/main/resources/db/migration"
    
    if [ ! -d "$MIGRATION_DIR" ]; then
        echo -e "${RED}ERROR: Migration directory not found: $MIGRATION_DIR${NC}"
        exit 1
    fi
    
    # Get all migration files sorted
    for migration in $(ls -1 $MIGRATION_DIR/V*.sql | sort -V); do
        echo -e "${YELLOW}Applying: $(basename $migration)${NC}"
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f "$migration"
        echo -e "${GREEN}✓ Applied successfully${NC}"
    done
fi

echo ""
echo -e "${BLUE}======================================${NC}"
echo -e "${GREEN}✓ Migration completed successfully!${NC}"
echo -e "${BLUE}======================================${NC}"

# Show migration status
echo ""
echo -e "${YELLOW}Current database schema:${NC}"
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "\dt"

echo ""
echo -e "${YELLOW}Database tables created:${NC}"
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
    SELECT 
        schemaname as schema,
        tablename as table,
        pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
    FROM pg_tables
    WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
    ORDER BY tablename;
"

# Clean up
unset PGPASSWORD
