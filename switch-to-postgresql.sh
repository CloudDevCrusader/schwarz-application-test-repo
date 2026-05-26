#!/bin/bash

# Script to switch from H2 to PostgreSQL database
# Run this script once OrbStack is fully started

set -e

echo "═══════════════════════════════════════════════════════════════════"
echo "🔄 Switching to PostgreSQL Database"
echo "═══════════════════════════════════════════════════════════════════"
echo ""

# Check if Docker is available
if ! docker ps > /dev/null 2>&1; then
    echo "❌ ERROR: Docker is not running!"
    echo "Please start OrbStack and try again."
    exit 1
fi

echo "✅ Docker is running"
echo ""

# Start PostgreSQL container
echo "📦 Starting PostgreSQL container..."
docker-compose up -d postgres

# Wait for PostgreSQL to be healthy
echo "⏳ Waiting for PostgreSQL to be ready..."
for i in {1..30}; do
    if docker exec ktor-postgres pg_isready -U postgres -d library > /dev/null 2>&1; then
        echo "✅ PostgreSQL is ready!"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ PostgreSQL didn't start in time"
        echo "Check logs: docker-compose logs postgres"
        exit 1
    fi
    echo "   Waiting... ($i/30)"
    sleep 1
done
echo ""

# Check PostgreSQL status
echo "📊 PostgreSQL Status:"
docker ps | grep postgres
echo ""

echo "═══════════════════════════════════════════════════════════════════"
echo "✅ PostgreSQL is ready!"
echo "═══════════════════════════════════════════════════════════════════"
echo ""
echo "Configuration has been updated to use PostgreSQL (embedded: false)"
echo ""
echo "Next steps:"
echo "1. Stop the current Ktor server (if running)"
echo "2. Restart with: ./gradlew :server:run"
echo "3. Check that migrations run successfully"
echo ""
echo "Database Details:"
echo "  Container: ktor-postgres"
echo "  Host: localhost"
echo "  Port: 5432"
echo "  Database: library"
echo "  User: postgres"
echo "  Password: postgres"
echo ""
echo "To connect to the database:"
echo "  docker exec -it ktor-postgres psql -U postgres -d library"
echo ""
