# Library Management System - RESTful API

A RESTful web service for managing an online library built with Kotlin and Ktor framework.

## Features

- **Customer Management**: Registration, authentication, and profile management
- **Category Management**: Organize books into categories with book counts
- **Book Management**: Complete CRUD operations for book inventory
- **JWT Authentication**: Secure token-based authentication
- **Email Validation**: RFC-compliant email validation
- **Security**: BCrypt password hashing, authenticated write operations
- **Public Access**: Anonymous users can browse books and categories

## Tech Stack

- **Framework**: Ktor 3.5.0
- **Language**: Kotlin 2.3.21
- **Database**: PostgreSQL (H2 embedded for dev/test)
- **ORM**: Exposed
- **Authentication**: JWT
- **Testing**: Kotest + MockK
- **API Docs**: OpenAPI/Swagger
- **Build**: Gradle with Kotlin DSL

## Quick Start

### Docker (Recommended)

```bash
# Copy environment file
cp .env.example .env

# Start services
docker-compose up -d

# Access at http://localhost:8080
```

### Standalone

```bash
# Build
./gradlew build

# Run
./gradlew :server:run
```

## API Documentation

- **Swagger UI**: http://localhost:8080/swagger
- **OpenAPI Spec**: http://localhost:8080/openapi

### Key Endpoints

#### Authentication (Public)
- `POST /auth/register` - Register new customer
- `POST /auth/login` - Login and receive JWT token

#### Categories
- `GET /categories` - List all (public)
- `GET /categories/{id}` - Get by ID (public)
- `POST /categories` - Create (authenticated)
- `PUT /categories/{id}` - Update (authenticated)
- `DELETE /categories/{id}` - Delete (authenticated)

#### Books
- `GET /books` - List all (public)
- `GET /books/{id}` - Get by ID (public)
- `GET /books/category/{categoryId}` - Get by category (public)
- `POST /books` - Create (authenticated)
- `PUT /books/{id}` - Update (authenticated)
- `DELETE /books/{id}` - Delete (authenticated)

#### Customers (All Authenticated)
- `GET /customers` - List all
- `GET /customers/{id}` - Get by ID
- `PUT /customers/{id}` - Update
- `DELETE /customers/{id}` - Delete

## Configuration

Edit `server/src/main/resources/application.yaml` or use environment variables:

```yaml
database:
  embedded: true  # false for PostgreSQL
  url: "jdbc:postgresql://localhost:5432/library"
  user: "postgres"
  password: "postgres"

jwt:
  secret: "your-secret-key-change-in-production-min-256-bits-long"
  validity: "3600000"  # 1 hour
```

## Testing

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test koverHtmlReport
```

## Validation Rules

**Customer**
- Email: Valid RFC format
- Password: Minimum 6 characters
- Name: 2-255 characters

**Book**
- Title, Author, Publisher: Required
- Publishing Year: 1000-2100
- Category: Must exist

**Category**
- Name, Description: Required
- Cannot delete with existing books

## Security

- BCrypt password hashing (cost factor: 12)
- JWT tokens signed with HMAC256
- RFC-compliant email validation
- Public read, authenticated write access

## Project Structure

```
server/
├── src/main/kotlin/com/schwarzdigital/
│   ├── auth/                 # JWT & Authentication
│   ├── database/             # Database config
│   ├── domain/
│   │   ├── models/           # Data models
│   │   └── repositories/     # Data access
│   ├── routes/               # API endpoints
│   └── utils/                # Utilities
└── src/test/kotlin/          # Tests
```

## Requirements Fulfilled

This implementation completes all practical exercise requirements:

✅ **1. Customer CRUD** - Complete with registration, authentication, and management  
✅ **2. Email Validation** - RFC-compliant validation in `Validator.kt`  
✅ **3. Login with JWT** - `/auth/login` returns token for secured endpoints  
✅ **4. Category & Books CRUD** - Full CRUD operations for both resources  
✅ **5. Tests** - Comprehensive test suite with integration tests  

## Additional Features

- Docker & docker-compose setup
- GitHub Actions CI/CD pipeline
- Database migrations with Flyway
- OpenAPI/Swagger documentation
- Development CLI tools
- Production-ready deployment package

## Documentation

- [API-DOCUMENTATION.md](./API-DOCUMENTATION.md) - Detailed API reference with examples
- [DOCKER-README.md](./DOCKER-README.md) - Docker deployment guide
- [CLI-README.md](./CLI-README.md) - Development CLI tools
- [scripts/db/README.md](./scripts/db/README.md) - Database management

## License

Educational/demonstration purposes.

---

**Built with Kotlin and Ktor** - Practical exercise demonstrating RESTful API development
