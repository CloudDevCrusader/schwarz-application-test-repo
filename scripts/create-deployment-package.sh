#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}======================================${NC}"
echo -e "${GREEN}Creating Deployment Package${NC}"
echo -e "${GREEN}======================================${NC}"

# Configuration
VERSION=${VERSION:-"1.0.0-SNAPSHOT"}
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
PACKAGE_NAME="deployment-${VERSION}-${TIMESTAMP}"
TEMP_DIR="./build/deployment"
OUTPUT_DIR="."

# Clean and create temp directory
echo -e "${YELLOW}Cleaning build directory...${NC}"
rm -rf "$TEMP_DIR"
mkdir -p "$TEMP_DIR/$PACKAGE_NAME"

# Create directory structure
echo -e "${YELLOW}Creating directory structure...${NC}"
mkdir -p "$TEMP_DIR/$PACKAGE_NAME/bin"
mkdir -p "$TEMP_DIR/$PACKAGE_NAME/config"
mkdir -p "$TEMP_DIR/$PACKAGE_NAME/logs"
mkdir -p "$TEMP_DIR/$PACKAGE_NAME/init-db"
mkdir -p "$TEMP_DIR/$PACKAGE_NAME/scripts/db"
mkdir -p "$TEMP_DIR/$PACKAGE_NAME/migrations"

# Copy application JAR
echo -e "${YELLOW}Copying application files...${NC}"
if [ -f "./server/build/libs/server-all.jar" ]; then
    cp ./server/build/libs/server-all.jar "$TEMP_DIR/$PACKAGE_NAME/bin/application.jar"
elif [ -f "./server/build/libs/server-${VERSION}-all.jar" ]; then
    cp ./server/build/libs/server-${VERSION}-all.jar "$TEMP_DIR/$PACKAGE_NAME/bin/application.jar"
else
    echo -e "${RED}Error: Could not find application JAR file${NC}"
    echo -e "${YELLOW}Looking for JAR files in server/build/libs/:${NC}"
    ls -la ./server/build/libs/ || echo "Directory not found"
    exit 1
fi

# Copy configuration files
echo -e "${YELLOW}Copying configuration files...${NC}"
cp docker-compose.yml "$TEMP_DIR/$PACKAGE_NAME/"
cp Dockerfile "$TEMP_DIR/$PACKAGE_NAME/"
cp .env.example "$TEMP_DIR/$PACKAGE_NAME/.env"
cp server/src/main/resources/application.yaml "$TEMP_DIR/$PACKAGE_NAME/config/application.yaml"

# Copy database scripts and migrations
echo -e "${YELLOW}Copying database scripts...${NC}"
cp -r scripts/db/* "$TEMP_DIR/$PACKAGE_NAME/scripts/db/"
cp -r server/src/main/resources/db/migration/* "$TEMP_DIR/$PACKAGE_NAME/migrations/"
cp init-db/*.sql "$TEMP_DIR/$PACKAGE_NAME/init-db/" 2>/dev/null || echo "No init-db files to copy"

# Create startup script
echo -e "${YELLOW}Creating startup scripts...${NC}"
cat > "$TEMP_DIR/$PACKAGE_NAME/start.sh" << 'EOF'
#!/bin/bash

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}Starting Ktor Application${NC}"

# Check if .env exists
if [ ! -f .env ]; then
    echo -e "${YELLOW}Warning: .env file not found. Creating from .env.example${NC}"
    cp .env.example .env
    echo -e "${YELLOW}Please edit .env file with your configuration${NC}"
    exit 1
fi

# Load environment variables
export $(cat .env | grep -v '^#' | xargs)

# Start with Docker Compose
echo -e "${GREEN}Starting services with Docker Compose...${NC}"
docker-compose up -d

echo -e "${GREEN}Application started successfully!${NC}"
echo -e "Access the application at: http://localhost:${KTOR_PORT:-8080}"
echo ""
echo -e "To view logs, run: ${YELLOW}docker-compose logs -f${NC}"
echo -e "To stop, run: ${YELLOW}./stop.sh${NC}"
EOF

chmod +x "$TEMP_DIR/$PACKAGE_NAME/start.sh"

# Create stop script
cat > "$TEMP_DIR/$PACKAGE_NAME/stop.sh" << 'EOF'
#!/bin/bash

set -e

# Colors
GREEN='\033[0;32m'
NC='\033[0m'

echo -e "${GREEN}Stopping Ktor Application${NC}"

docker-compose down

echo -e "${GREEN}Application stopped successfully!${NC}"
EOF

chmod +x "$TEMP_DIR/$PACKAGE_NAME/stop.sh"

# Create standalone run script (without Docker)
cat > "$TEMP_DIR/$PACKAGE_NAME/run-standalone.sh" << 'EOF'
#!/bin/bash

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}Running Ktor Application (Standalone)${NC}"

# Check if .env exists
if [ ! -f .env ]; then
    echo -e "${YELLOW}Warning: .env file not found. Using defaults${NC}"
else
    export $(cat .env | grep -v '^#' | xargs)
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${YELLOW}Java not found. Please install Java 21 or higher${NC}"
    exit 1
fi

# Run the application
java ${JAVA_OPTS:--Xmx512m -Xms256m} \
    -Ddatabase.embedded=${DATABASE_EMBEDDED:-true} \
    -Ddatabase.url="${DATABASE_URL:-jdbc:h2:mem:library}" \
    -Ddatabase.user="${DATABASE_USER:-sa}" \
    -Ddatabase.password="${DATABASE_PASSWORD:-}" \
    -Djwt.secret="${JWT_SECRET}" \
    -Djwt.issuer="${JWT_ISSUER:-library-application}" \
    -Djwt.audience="${JWT_AUDIENCE:-library-users}" \
    -Djwt.validity="${JWT_VALIDITY:-3600000}" \
    -jar bin/application.jar
EOF

chmod +x "$TEMP_DIR/$PACKAGE_NAME/run-standalone.sh"

# Create README
cat > "$TEMP_DIR/$PACKAGE_NAME/README.md" << 'EOF'
# Ktor Application Deployment Package

This package contains everything needed to run the Ktor application locally.

## Contents

- `bin/application.jar` - The application JAR file
- `docker-compose.yml` - Docker Compose configuration
- `Dockerfile` - Docker image configuration
- `.env` - Environment configuration (configure before running)
- `config/application.yaml` - Application configuration
- `start.sh` - Script to start the application with Docker
- `stop.sh` - Script to stop the application
- `run-standalone.sh` - Script to run without Docker (requires Java 21+)

## Quick Start

### Using Docker (Recommended)

1. Configure your environment:
   ```bash
   # Edit .env file with your settings
   nano .env
   ```

2. Start the application:
   ```bash
   ./start.sh
   ```

3. Access the application at `http://localhost:8080`

4. Stop the application:
   ```bash
   ./stop.sh
   ```

### Using Standalone (No Docker)

1. Ensure Java 21+ is installed
2. Configure `.env` file
3. Run:
   ```bash
   ./run-standalone.sh
   ```

## Environment Configuration

Edit the `.env` file to configure:
- Database connection
- JWT secret and settings
- Server port
- Java memory settings

**IMPORTANT:** Change the `JWT_SECRET` in production!

## Database

The Docker Compose setup includes PostgreSQL. The database will be initialized automatically.

For standalone mode, the application uses an embedded H2 database by default.

## Logs

- Docker mode: `docker-compose logs -f`
- Standalone mode: Logs are written to console and `logs/` directory

## Health Check

Check application health:
```bash
curl http://localhost:8080/health
```

## Troubleshooting

### Port already in use
Change `KTOR_PORT` in `.env` file

### Database connection failed
Ensure PostgreSQL is running: `docker-compose ps`

### Permission denied
Make scripts executable: `chmod +x *.sh`

## Support

For issues and questions, please refer to the project documentation.
EOF

# Create init-db sample script
cat > "$TEMP_DIR/$PACKAGE_NAME/init-db/01-init.sql" << 'EOF'
-- Database initialization script
-- Add your database initialization SQL here

-- Example: Create extensions if needed
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tables will be created automatically by the application
EOF

# Create deployment instructions
cat > "$TEMP_DIR/$PACKAGE_NAME/DEPLOYMENT.md" << 'EOF'
# Deployment Instructions

## Prerequisites

- Docker and Docker Compose installed
- Or Java 21+ for standalone deployment

## Deployment Steps

1. Extract the deployment package:
   ```bash
   tar -xzf deployment-*.tar.gz
   cd deployment-*
   ```

2. Configure environment variables:
   ```bash
   cp .env.example .env
   nano .env
   ```

3. **IMPORTANT:** Update these values in `.env`:
   - `JWT_SECRET` - Use a strong random secret (minimum 256 bits)
   - `DB_PASSWORD` - Set a strong database password
   - Other settings as needed

4. Start the application:
   ```bash
   ./start.sh
   ```

5. Verify the deployment:
   ```bash
   curl http://localhost:8080/health
   ```

## Production Considerations

1. **Security:**
   - Change all default passwords
   - Use strong JWT secret
   - Configure firewall rules
   - Enable HTTPS/TLS

2. **Database:**
   - Regular backups
   - Configure volume persistence
   - Monitor disk usage

3. **Monitoring:**
   - Set up log aggregation
   - Configure alerts
   - Monitor resource usage

4. **Updates:**
   - Test updates in staging first
   - Backup database before updates
   - Plan for zero-downtime deployments

## Backup and Restore

### Backup Database
```bash
docker exec ktor-postgres pg_dump -U postgres library > backup.sql
```

### Restore Database
```bash
docker exec -i ktor-postgres psql -U postgres library < backup.sql
```

## Scaling

To run multiple instances:
1. Use a load balancer (nginx, traefik)
2. Ensure database is shared
3. Configure session persistence if needed
EOF

# Create package archive
echo -e "${YELLOW}Creating compressed archive...${NC}"
cd "$TEMP_DIR"
tar -czf "${PACKAGE_NAME}.tar.gz" "$PACKAGE_NAME"
mv "${PACKAGE_NAME}.tar.gz" "../../${OUTPUT_DIR}/"
cd - > /dev/null

# Cleanup
echo -e "${YELLOW}Cleaning up...${NC}"
rm -rf "$TEMP_DIR"

# Summary
PACKAGE_PATH="${OUTPUT_DIR}/${PACKAGE_NAME}.tar.gz"
PACKAGE_SIZE=$(du -h "$PACKAGE_PATH" | cut -f1)

echo -e "${GREEN}======================================${NC}"
echo -e "${GREEN}Deployment Package Created!${NC}"
echo -e "${GREEN}======================================${NC}"
echo -e "Package: ${YELLOW}${PACKAGE_PATH}${NC}"
echo -e "Size: ${YELLOW}${PACKAGE_SIZE}${NC}"
echo ""
echo -e "To deploy:"
echo -e "  1. Extract: ${YELLOW}tar -xzf ${PACKAGE_NAME}.tar.gz${NC}"
echo -e "  2. Configure: ${YELLOW}cd ${PACKAGE_NAME} && nano .env${NC}"
echo -e "  3. Start: ${YELLOW}./start.sh${NC}"
echo ""
