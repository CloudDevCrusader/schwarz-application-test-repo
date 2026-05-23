# CLI Tools Summary

A comprehensive shell-based CLI has been created for the Library Management System project.

## What Was Created

### Main Entry Point
- **`./dev`** - Main CLI command that provides a unified interface for all project tasks

### Individual Scripts (in `scripts/` directory)

1. **`scripts/test.sh`** - Test runner with coverage support
   - Run tests
   - Generate coverage reports (HTML & XML)
   - Watch mode for continuous testing
   - Verbose output option

2. **`scripts/lint.sh`** - Code linting and formatting
   - Check code style
   - Auto-fix linting issues
   - Format code
   - Supports ktlint, spotless, and detekt

3. **`scripts/version.sh`** - Version management
   - Show current version
   - Bump major/minor/patch versions
   - Set specific versions
   - Updates all build.gradle.kts files

4. **`scripts/server.sh`** - Server lifecycle management
   - Start/stop/restart server
   - Debug mode with remote debugging (port 5005)
   - Check server status
   - View logs
   - Custom port and environment configuration

5. **`scripts/db.sh`** - Database management
   - Start/stop/restart PostgreSQL (Docker)
   - Reset database (drop and recreate)
   - Run migrations
   - Backup database
   - Open PostgreSQL shell
   - View database logs

6. **`scripts/cleanup.sh`** - Project cleanup and maintenance
   - Deep clean (build artifacts, cache, logs, Docker)
   - Clean specific areas (build, cache, logs, Docker)
   - Refresh dependencies

### Documentation
- **`CLI-README.md`** - Comprehensive documentation for all CLI tools

## Features

### Color-Coded Output
All scripts use color-coded output for better readability:
- 🔵 Blue - Informational messages
- 🟢 Green - Success messages
- 🟡 Yellow - Warnings and in-progress actions
- 🔴 Red - Errors

### Consistent Interface
All scripts follow the same patterns:
- `--help` or `-h` for help
- Clear error messages
- Status feedback
- Confirmation prompts for destructive actions

### Comprehensive Help
Every script and the main CLI has built-in help:
```bash
./dev help
./scripts/test.sh --help
./scripts/server.sh --help
```

## Quick Start

```bash
# Show all available commands
./dev help

# Run tests with coverage
./dev test:coverage

# Start server in debug mode
./dev debug

# Bump version
./dev version:minor

# Start database
./dev db:start

# Clean everything
./dev clean:all
```

## Common Workflows

### Development Workflow
```bash
./dev db:start        # Start database
./dev test            # Run tests
./dev start           # Start server
./dev logs            # View logs
```

### Testing Workflow
```bash
./dev test:coverage   # Run tests with coverage
./dev lint            # Check code style
./dev lint:fix        # Fix linting issues
```

### Release Workflow
```bash
./dev version         # Check current version
./dev test:coverage   # Ensure all tests pass
./dev version:minor   # Bump version
./dev build           # Build project
./dev package         # Create deployment package
```

### Maintenance Workflow
```bash
./dev clean:all       # Deep clean
./dev db:reset        # Reset database
./dev build           # Rebuild
./dev test            # Verify
```

## Integration with Gradle

The CLI integrates seamlessly with Gradle:
- Wraps Gradle commands with better UX
- Adds features not available in Gradle (server management, database tools)
- Maintains compatibility with direct Gradle usage

## Dependencies Added

To support test coverage, the following was added to the project:

### `gradle/libs.versions.toml`
```toml
kover = "0.8.3"
```

### `server/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.kover)
}

testImplementation(ktorLibs.client.contentNegotiation)
testImplementation(ktorLibs.serialization.kotlinx.json)
```

## Bug Fixes Applied

While creating the CLI, the following bugs were fixed:

1. **Customer.kt** - Fixed incorrect `@Serializable` annotation usage
2. **LibraryApiTest.kt** - Added missing import for `ContentNegotiation`
3. **Repository Tests** - Added missing `deleteAll` import

## File Structure

```
.
├── dev                          # Main CLI entry point
├── CLI-README.md               # CLI documentation
└── scripts/
    ├── test.sh                 # Test runner
    ├── lint.sh                 # Linting & formatting
    ├── version.sh              # Version management
    ├── server.sh               # Server management
    ├── db.sh                   # Database management
    ├── cleanup.sh              # Cleanup & maintenance
    └── create-deployment-package.sh  # (existing)
```

## Next Steps

### Recommended Enhancements

1. **Add ktlint** for better linting:
   ```bash
   brew install ktlint
   ```

2. **Configure spotless** in build.gradle.kts for formatting:
   ```kotlin
   plugins {
       id("com.diffplug.spotless") version "6.x.x"
   }
   ```

3. **Add test data seeding** script for the database

4. **Add CI/CD integration** examples using these scripts

5. **Add Docker deployment** scripts

### Usage Tips

- All scripts are idempotent - safe to run multiple times
- Scripts check for prerequisites and provide helpful error messages
- Use `--help` on any command to see detailed options
- The main `./dev` command is the recommended entry point

## Troubleshooting

### Permission Denied
If you get permission errors:
```bash
chmod +x dev scripts/*.sh
```

### Command Not Found
Make sure you're in the project root directory:
```bash
cd /path/to/schwarz-application-test-repo
./dev help
```

### Docker Issues
Ensure Docker is running:
```bash
docker ps
```

## Support

For issues or questions:
1. Run `./dev help` to see all commands
2. Run `<command> --help` for specific command help
3. Check `CLI-README.md` for detailed documentation
4. Review individual script source code in `scripts/` directory
