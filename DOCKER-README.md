# Docker & CI/CD Setup Guide

This project includes a complete Docker and CI/CD setup for the Ktor application with PostgreSQL database.

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose installed
- Git (for GitHub Actions)
- (Optional) Java 21+ for standalone mode

### Running with Docker

1. **Create environment configuration:**
   ```bash
   cp .env.example .env
   # Edit .env and update values, especially JWT_SECRET and DB_PASSWORD
   ```

2. **Start the application:**
   ```bash
   docker-compose up -d
   ```

3. **Access the application:**
   - Application: http://localhost:8080
   - Health check: http://localhost:8080/health

4. **View logs:**
   ```bash
   docker-compose logs -f
   ```

5. **Stop the application:**
   ```bash
   docker-compose down
   ```

## 📦 Project Structure

```
.
├── Dockerfile                    # Multi-stage Docker build with SDKMAN
├── docker-compose.yml           # Services: Ktor app + PostgreSQL
├── .env.example                 # Environment variables template
├── .github/workflows/ci-cd.yml  # GitHub Actions pipeline
├── scripts/
│   └── create-deployment-package.sh  # Creates deployment archive
└── server/
    └── src/main/resources/
        └── application.yaml     # Ktor configuration
```

## 🐳 Docker Setup

### Dockerfile

The Dockerfile uses a multi-stage build:
- **Builder stage:** Uses SDKMAN to install Java 25 and Kotlin, builds the application
- **Runtime stage:** Minimal image with only Java runtime and the built JAR

### Docker Compose

Services included:
- **postgres:** PostgreSQL 16 database
- **ktor-app:** Your Ktor application

Features:
- Health checks for both services
- Automatic restart policies
- Volume persistence for database
- Network isolation
- Environment-based configuration

## ⚙️ Environment Configuration

Edit `.env` file to configure:

```bash
# Database
DB_NAME=library
DB_USER=postgres
DB_PASSWORD=your-secure-password  # CHANGE THIS!
DB_PORT=5432

# JWT (JSON Web Token)
JWT_SECRET=your-256-bit-secret-key  # CHANGE THIS!
JWT_ISSUER=library-application
JWT_AUDIENCE=library-users
JWT_VALIDITY=3600000  # 1 hour in milliseconds

# Server
KTOR_PORT=8080
KTOR_ENV=production

# Java
JAVA_OPTS=-Xmx512m -Xms256m
```

**⚠️ IMPORTANT:** Always change `JWT_SECRET` and `DB_PASSWORD` in production!

## 🔄 CI/CD Pipeline

The GitHub Actions workflow (`.github/workflows/ci-cd.yml`) includes:

### Jobs

1. **Test**
   - Runs on push/PR to main/develop
   - Sets up PostgreSQL test database
   - Installs dependencies via SDKMAN
   - Runs all tests
   - Publishes test results
   - Caches Gradle dependencies

2. **Build**
   - Builds Docker image
   - Pushes to GitHub Container Registry
   - Multi-platform support (amd64, arm64)
   - Generates SBOM (Software Bill of Materials)
   - Tags: latest, branch name, SHA, semantic versions

3. **Deploy Package**
   - Creates deployment archive
   - Includes application JAR, docker-compose, scripts
   - Uploads as artifact (90 days retention)
   - Creates GitHub Release for tagged versions

4. **Security Scan**
   - Scans codebase with Trivy
   - Uploads results to GitHub Security

### Workflow Triggers

- Push to `main` or `develop` branches
- Pull requests to `main` or `develop`
- Manual workflow dispatch

## 📤 Deployment Package

### Creating a Deployment Package

```bash
./scripts/create-deployment-package.sh
```

This creates a `deployment-*.tar.gz` file containing:
- Application JAR
- Docker Compose configuration
- Dockerfile
- Environment template
- Startup/shutdown scripts
- Documentation

### Package Contents

```
deployment-*/
├── bin/
│   └── application.jar          # Built application
├── config/
│   └── application.yaml         # App configuration
├── init-db/
│   └── 01-init.sql             # Database init scripts
├── logs/                        # Log directory
├── docker-compose.yml           # Docker Compose config
├── Dockerfile                   # Docker build config
├── .env                         # Environment variables
├── start.sh                     # Start script
├── stop.sh                      # Stop script
├── run-standalone.sh            # Run without Docker
├── README.md                    # Quick start guide
└── DEPLOYMENT.md                # Deployment instructions
```

### Deploying the Package

1. **Extract:**
   ```bash
   tar -xzf deployment-*.tar.gz
   cd deployment-*
   ```

2. **Configure:**
   ```bash
   nano .env
   # Update JWT_SECRET, DB_PASSWORD, and other settings
   ```

3. **Start:**
   ```bash
   ./start.sh
   ```

4. **Verify:**
   ```bash
   curl http://localhost:8080/health
   ```

## 🔧 Development Workflow

### Local Development

```bash
# Using Docker (recommended)
docker-compose up

# Using Gradle
./gradlew :server:run

# Running tests
./gradlew test

# Building
./gradlew build
```

### Building Docker Image Manually

```bash
docker build -t ktor-app:latest .
```

### Running Specific Services

```bash
# Only database
docker-compose up postgres

# Only application
docker-compose up ktor-app
```

## 🗄️ Database Management

### Accessing PostgreSQL

```bash
# Using Docker
docker exec -it ktor-postgres psql -U postgres -d library

# Using psql (if installed locally)
psql -h localhost -U postgres -d library
```

### Database Backup

```bash
docker exec ktor-postgres pg_dump -U postgres library > backup.sql
```

### Database Restore

```bash
docker exec -i ktor-postgres psql -U postgres library < backup.sql
```

### Database Migrations

Database schema is managed by Exposed ORM and created automatically on application startup.

## 📊 Monitoring & Logs

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f ktor-app
docker-compose logs -f postgres

# Last 100 lines
docker-compose logs --tail=100 ktor-app
```

### Health Checks

```bash
# Application health
curl http://localhost:8080/health

# Database health
docker exec ktor-postgres pg_isready -U postgres
```

## 🔒 Security Considerations

### Production Checklist

- [ ] Change `JWT_SECRET` to a strong random value (min 256 bits)
- [ ] Change `DB_PASSWORD` to a strong password
- [ ] Configure HTTPS/TLS (use reverse proxy like nginx)
- [ ] Set up firewall rules
- [ ] Enable Docker secrets for sensitive data
- [ ] Configure log rotation
- [ ] Set up monitoring and alerts
- [ ] Regular security updates
- [ ] Database backups
- [ ] Limit container resources

### Using Docker Secrets (Production)

```yaml
# docker-compose.yml
secrets:
  jwt_secret:
    file: ./secrets/jwt_secret.txt
  db_password:
    file: ./secrets/db_password.txt

services:
  ktor-app:
    secrets:
      - jwt_secret
      - db_password
```

## 🧪 Testing

### Running Tests in CI

Tests run automatically on GitHub Actions with:
- PostgreSQL test database
- Full test suite execution
- Test result publishing
- Coverage reporting

### Local Testing with Docker

```bash
# Start test database
docker-compose -f docker-compose.test.yml up -d

# Run tests
./gradlew test

# Cleanup
docker-compose -f docker-compose.test.yml down
```

## 🐛 Troubleshooting

### Port Already in Use

```bash
# Change port in .env
KTOR_PORT=8081
```

### Database Connection Failed

```bash
# Check if PostgreSQL is running
docker-compose ps

# View database logs
docker-compose logs postgres

# Restart database
docker-compose restart postgres
```

### Application Won't Start

```bash
# Check logs
docker-compose logs ktor-app

# Rebuild image
docker-compose build --no-cache

# Reset everything
docker-compose down -v
docker-compose up --build
```

### Out of Memory

```bash
# Increase Java heap in .env
JAVA_OPTS=-Xmx1024m -Xms512m

# Increase Docker memory limit in docker-compose.yml
deploy:
  resources:
    limits:
      memory: 1G
```

## 📚 Additional Resources

- [Ktor Documentation](https://ktor.io/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [SDKMAN Documentation](https://sdkman.io/)
- [GitHub Actions Documentation](https://docs.github.com/actions)

## 🤝 Contributing

When contributing, ensure:
1. Tests pass locally and in CI
2. Docker build succeeds
3. Documentation is updated
4. Environment variables are documented in `.env.example`

## 📝 License

See LICENSE file for details.
