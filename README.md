# Job-Application Project: Library Management System - RESTful API with ktor and Kotlin

A modern RESTful web service for managing an online library application built with Kotlin and Ktor.

## Features

- **User Management**: Customer registration and authentication
- **Category Management**: Organize books into categories
- **Book Management**: Full book catalog with CRUD operations
- **JWT Authentication**: Secure token-based authentication
- **Email Validation**: RFC-compliant email validation
- **Password Hashing**: BCrypt password hashing for security
- **Public & Authenticated Endpoints**: Anonymous users can read, authenticated users can write

## Technical Stack

- **Framework**: Ktor 3.5.0
- **Language**: Kotlin 2.3.21
- **Database**: PostgreSQL / H2 (embedded for dev/test)
- **ORM**: Exposed
- **Authentication**: JWT (JSON Web Tokens)
- **Testing**: Kotest + MockK
- **API Documentation**: OpenAPI/Swagger
- **Build Tool**: Gradle with Kotlin DSL
- **Monitoring**: OpenTelemetry

## Quick Start

### Prerequisites

- JDK 25 or higher
- Docker and Docker Compose (optional)
- Gradle 8.x (included via wrapper)

### Running with Docker (Recommended)

```bash
# 1. Copy and configure environment
cp .env.example .env

# 2. Start services
docker-compose up -d

# 3. Access the application
open http://localhost:8080/swagger
```

### Running Standalone

```bash
# 1. Build the project
./gradlew build

# 2. Run the server
./gradlew :server:run

# 3. Access at http://localhost:8080
```

### Using the Development CLI

```bash
# Show all available commands
./dev help

# Start development
./dev db:start    # Start database
./dev test        # Run tests
./dev start       # Start server

# Run tests with coverage
./dev test:coverage

# Debug mode
./dev debug
```

## API Documentation

Once running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger
- **Alternative UI**: http://localhost:8080/api-docs
- **OpenAPI Spec**: http://localhost:8080/openapi

For detailed API documentation, see [API-DOCUMENTATION.md](./API-DOCUMENTATION.md)

## Project Structure

```
server/
├── src/
│   ├── main/kotlin/com/schwarzdigital/
│   │   ├── auth/                 # Authentication & JWT
│   │   ├── database/             # Database configuration
│   │   ├── domain/
│   │   │   ├── models/           # Domain models
│   │   │   └── repositories/     # Data access layer
│   │   ├── routes/               # API endpoints
│   │   ├── utils/                # Utilities
│   │   └── main.kt               # Application entry point
│   ├── resources/
│   │   ├── db/migration/         # Flyway migrations
│   │   └── openapi/              # OpenAPI specification
│   └── test/kotlin/              # Unit & integration tests
```

## API Endpoints Summary

### Authentication (Public)
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login and get JWT token

### Categories
- `GET /categories` - Get all categories (public)
- `GET /categories/{id}` - Get category by ID (public)
- `POST /categories` - Create category (🔒 authenticated)
- `PUT /categories/{id}` - Update category (🔒 authenticated)
- `DELETE /categories/{id}` - Delete category (🔒 authenticated)

### Books
- `GET /books` - Get all books (public)
- `GET /books/{id}` - Get book by ID (public)
- `GET /books/category/{categoryId}` - Get books by category (public)
- `POST /books` - Create book (🔒 authenticated)
- `PUT /books/{id}` - Update book (🔒 authenticated)
- `DELETE /books/{id}` - Delete book (🔒 authenticated)

### Customers (All Authenticated)
- `GET /customers` - Get all customers (🔒)
- `GET /customers/{id}` - Get customer by ID (🔒)
- `PUT /customers/{id}` - Update customer (🔒)
- `DELETE /customers/{id}` - Delete customer (🔒)

## Configuration

Edit `server/src/main/resources/application.yaml`:

```yaml
database:
  embedded: true  # Set to false for PostgreSQL
  url: "jdbc:postgresql://localhost:5432/library"
  user: "postgres"
  password: "postgres"

jwt:
  secret: "your-secret-key-change-in-production-min-256-bits-long"
  issuer: "library-application"
  audience: "library-users"
  validity: "3600000"  # 1 hour in milliseconds
```

For Docker deployment, use `.env` file (copy from `.env.example`).

## Testing

```bash
# Run all tests
./gradlew test

# Run tests with coverage
./dev test:coverage

# Run specific test class
./gradlew test --tests "ValidatorTest"
```

The project includes comprehensive tests:
- **Unit Tests**: Validator, JWT Service, Repositories
- **Integration Tests**: End-to-end API testing

## Security

- Passwords are hashed using BCrypt (cost factor: 12)
- JWT tokens are signed with HMAC256
- Email validation follows RFC standards
- Authenticated endpoints require valid JWT token
- Public read access for books and categories
- Write operations require authentication

## Validation Rules

### Customer
- **Email**: Valid RFC-compliant email format
- **Password**: Minimum 6 characters
- **Name**: 2-255 characters, cannot be blank

### Book
- **Title, Author, Publisher**: Cannot be blank
- **Publishing Year**: Between 1000 and 2100
- **Category**: Must be assigned to existing category

### Category
- **Name, Description**: Cannot be blank
- Cannot delete category with existing books

## Error Handling

All errors return consistent JSON responses:

```json
{
  "error": "ERROR_CODE",
  "message": "Human readable error message"
}
```

Common error codes:
- `VALIDATION_ERROR`: Invalid input data
- `INVALID_CREDENTIALS`: Login failed
- `EMAIL_EXISTS`: Email already registered
- `NOT_FOUND`: Resource not found
- `UNAUTHORIZED`: Authentication required
- `CONFLICT`: Resource conflict
- `SERVER_ERROR`: Internal server error

## Database Schema

### Customers
- id (PRIMARY KEY)
- name
- email (UNIQUE)
- password_hash
- created_at, updated_at

### Categories
- id (PRIMARY KEY)
- name (UNIQUE)
- description
- created_at, updated_at

### Books
- id (PRIMARY KEY)
- title, author, publisher
- publishing_year
- category_id (FOREIGN KEY → Categories)
- created_at, updated_at

## Documentation

- **[API-DOCUMENTATION.md](./API-DOCUMENTATION.md)** - Complete API reference and examples
- **[OPENAPI-QUICKSTART.md](./OPENAPI-QUICKSTART.md)** - Quick reference for OpenAPI/Swagger
- **[DOCKER-README.md](./DOCKER-README.md)** - Docker and CI/CD setup guide
- **[CLI-README.md](./CLI-README.md)** - Development CLI tools documentation
- **[scripts/db/README.md](./scripts/db/README.md)** - Database management guide

## Development CLI Tools

The project includes a comprehensive CLI for common tasks:

```bash
./dev help              # Show all commands
./dev test:coverage     # Run tests with coverage
./dev start             # Start server
./dev debug             # Start in debug mode
./dev db:start          # Start PostgreSQL
./dev clean:all         # Deep clean
./dev version:bump      # Bump version
```

See [CLI-README.md](./CLI-README.md) for complete documentation.

## CI/CD Pipeline

GitHub Actions workflow automatically:
- ✅ Runs tests with PostgreSQL
- ✅ Builds Docker images (multi-platform)
- ✅ Pushes to GitHub Container Registry
- ✅ Creates deployment packages
- ✅ Runs security scans with Trivy
- ✅ Publishes test results

## Deployment

### Using Docker

```bash
# Build and start
docker-compose up -d

# View logs
docker-compose logs -f

# Stop
docker-compose down
```

### Creating Deployment Package

```bash
./scripts/create-deployment-package.sh
```

This creates `deployment-*.tar.gz` with everything needed for production deployment.

See [DOCKER-README.md](./DOCKER-README.md) for detailed deployment instructions.

## Project Requirements

This implementation fulfills all requirements from the practical exercise:

✅ **1. CRUD operations for Customer resource**
- Register (create), get (read), update, delete endpoints
- Email validation, password hashing, JWT authentication

✅ **2. Email validation**
- RFC-compliant email validation in `Validator.kt`
- Validates format on registration

✅ **3. Login endpoint with token**
- `/auth/login` returns JWT token
- Token used for all authenticated endpoints

✅ **4. CRUD operations for Category and Books**
- Full CRUD for both resources
- Category includes book count
- Books linked to categories via foreign key

✅ **5. Tests for endpoints**
- Comprehensive test suite with Kotest
- Unit tests for all components
- Integration tests for API endpoints

**Additional Features:**
- Docker & docker-compose setup
- GitHub Actions CI/CD
- Database migrations with Flyway
- Database seed data
- OpenAPI/Swagger documentation
- Development CLI tools
- Production-ready deployment package

## Architecture

- **Layered Architecture**: Routes → Repositories → Database
- **Dependency Injection**: Manual DI for simplicity
- **DTOs**: Separate request/response models from domain models
- **Error Handling**: Centralized with StatusPages plugin

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes with tests
4. Ensure tests pass: `./gradlew test`
5. Submit a pull request

## License

This project is for educational/demonstration purposes.

## Authors

Built as a practical exercise for demonstrating RESTful API development with Kotlin and Ktor.

---

For detailed setup and usage instructions, see the documentation files linked above.
