# Development CLI Tools

A comprehensive set of shell scripts for managing the Library Management System project.

## Quick Start

The main entry point is the `./dev` command:

```bash
./dev help
```

## Available Commands

### Testing & Quality

- **`./dev test`** - Run all tests
- **`./dev test:coverage`** - Run tests with coverage report (opens HTML report automatically)
- **`./dev test:watch`** - Run tests in watch mode (continuous testing)
- **`./dev lint`** - Check code style
- **`./dev lint:fix`** - Auto-fix linting issues
- **`./dev format`** - Format code

### Server Management

- **`./dev start`** - Start the server
- **`./dev debug`** - Start the server in debug mode (port 5005)
- **`./dev stop`** - Stop the server
- **`./dev restart`** - Restart the server
- **`./dev status`** - Check server status
- **`./dev logs`** - Show server logs (tail -f)

### Build & Deploy

- **`./dev build`** - Build the project
- **`./dev clean`** - Clean build artifacts
- **`./dev package`** - Create deployment package

### Version Management

- **`./dev version`** - Show current version
- **`./dev version:bump`** - Bump patch version (1.0.0 → 1.0.1)
- **`./dev version:major`** - Bump major version (1.0.0 → 2.0.0)
- **`./dev version:minor`** - Bump minor version (1.0.0 → 1.1.0)
- **`./dev version:patch`** - Bump patch version (1.0.0 → 1.0.1)

### Database

- **`./dev db:start`** - Start PostgreSQL database (Docker)
- **`./dev db:stop`** - Stop database
- **`./dev db:reset`** - Reset database (drops all data!)
- **`./dev db:migrate`** - Run database migrations
- **`./dev db:backup`** - Backup database to `backups/` directory
- **`./dev db:shell`** - Open PostgreSQL shell

### Maintenance

- **`./dev clean:all`** - Deep clean (build artifacts, cache, logs, Docker)
- **`./dev clean:cache`** - Clean Gradle cache
- **`./dev clean:logs`** - Clean log files
- **`./dev clean:docker`** - Clean Docker resources

### Other

- **`./dev deps`** - Show dependency tree

## Individual Scripts

All scripts can also be run individually from the `scripts/` directory:

### Test Script (`scripts/test.sh`)

```bash
# Run tests
./scripts/test.sh

# Run with coverage
./scripts/test.sh --coverage

# Watch mode
./scripts/test.sh --watch

# Verbose output
./scripts/test.sh --verbose
```

### Lint Script (`scripts/lint.sh`)

```bash
# Check code style
./scripts/lint.sh

# Auto-fix issues
./scripts/lint.sh --fix

# Format code
./scripts/lint.sh --format

# Check only (no modifications)
./scripts/lint.sh --check
```

### Server Script (`scripts/server.sh`)

```bash
# Start server
./scripts/server.sh start

# Start in debug mode
./scripts/server.sh debug

# Custom port
./scripts/server.sh start --port 9090

# Environment
./scripts/server.sh start --env prod

# Stop server
./scripts/server.sh stop

# Check status
./scripts/server.sh status

# View logs
./scripts/server.sh logs
```

### Version Script (`scripts/version.sh`)

```bash
# Show current version
./scripts/version.sh show

# Bump version
./scripts/version.sh bump major
./scripts/version.sh bump minor
./scripts/version.sh bump patch

# Set specific version
./scripts/version.sh set 2.0.0-SNAPSHOT
```

### Database Script (`scripts/db.sh`)

```bash
# Start database
./scripts/db.sh start

# Stop database
./scripts/db.sh stop

# Reset database (WARNING: deletes all data)
./scripts/db.sh reset

# Run migrations
./scripts/db.sh migrate

# Backup database
./scripts/db.sh backup

# Restore from backup
./scripts/db.sh restore

# Open PostgreSQL shell
./scripts/db.sh shell

# View database logs
./scripts/db.sh logs
```

### Cleanup Script (`scripts/cleanup.sh`)

```bash
# Clean everything
./scripts/cleanup.sh all

# Clean build artifacts
./scripts/cleanup.sh build

# Clean Gradle cache
./scripts/cleanup.sh cache

# Clean log files
./scripts/cleanup.sh logs

# Clean Docker
./scripts/cleanup.sh docker

# Refresh dependencies
./scripts/cleanup.sh deps
```

## Common Workflows

### Starting Development

```bash
# Start the database
./dev db:start

# Run tests to ensure everything works
./dev test

# Start the server
./dev start
```

### Running Tests with Coverage

```bash
# Run tests with coverage (automatically opens HTML report)
./dev test:coverage
```

### Debugging the Server

```bash
# Start in debug mode (port 5005)
./dev debug

# In your IDE, attach to localhost:5005
```

### Bumping Version

```bash
# Show current version
./dev version

# Bump patch version (e.g., 1.0.0 -> 1.0.1)
./dev version:patch

# Bump minor version (e.g., 1.0.1 -> 1.1.0)
./dev version:minor

# Bump major version (e.g., 1.1.0 -> 2.0.0)
./dev version:major
```

### Database Management

```bash
# Start database
./dev db:start

# Run migrations
./dev db:migrate

# Backup database
./dev db:backup

# Reset database (WARNING: deletes all data)
./dev db:reset

# Open database shell
./dev db:shell
```

### Deep Clean

```bash
# Clean everything (build, cache, logs, Docker)
./dev clean:all

# Rebuild
./dev build

# Run tests
./dev test
```

## Requirements

- **Bash** 4.0+
- **Gradle** 9.0+
- **Docker** and **Docker Compose** (for database commands)
- **ktlint** (optional, for linting - can be installed via brew)

## Installation

All scripts are already executable. If you need to make them executable:

```bash
chmod +x dev
chmod +x scripts/*.sh
```

## Configuration

Scripts read configuration from:

- `build.gradle.kts` - Version information
- `docker-compose.yml` - Database configuration
- Environment variables (PORT, ENV, etc.)

## Troubleshooting

### Tests fail to run

```bash
# Clean and rebuild
./dev clean
./dev build
./dev test
```

### Server won't start

```bash
# Check if already running
./dev status

# Stop any running instances
./dev stop

# Check logs
./dev logs

# Start fresh
./dev start
```

### Database connection issues

```bash
# Check if database is running
docker ps

# Restart database
./dev db:restart

# Reset database
./dev db:reset
```

### Coverage reports not generating

Make sure Kover plugin is properly configured in `server/build.gradle.kts`. The scripts will automatically try to add it if missing.

## Contributing

To add new commands:

1. Create a new script in `scripts/` directory
2. Make it executable: `chmod +x scripts/your-script.sh`
3. Add the command to `dev` main CLI
4. Update this README

## License

Same as the main project.
