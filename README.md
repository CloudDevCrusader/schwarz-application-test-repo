# Library Management System - RESTful API

A modern RESTful web service for managing an--*-        ^   ^   nize books into categories
- **Book Management**: Full book catalog with CRUD operations
- **JWT Authentication**: Secure token-based authentication
- **Email Validation**: RFC-compliant email validation
- **Password Hashing**: BCrypt password hashing for security
- **Public & Authenticated Endpoints**: Anonymous users can read, authenticated users can write

### Technical Stack
0- **Framework**: Ktor 3.5.0
- **Language**: Kotlin 2.3.21
- **Database**: PostgreSQL / H2 (embedded for dev/test)
- **ORM**: Exposed
- **Authentication**: JWT (JSON Web Tokens)
- **Testing**: Kotest + MockK
- **API Documentation**: OpenAPI/Swagger
- **Build Tool**: Gradle with Kotlin DSL
- **Monitoring**: OpenTelemetry

## Project Structure

```
server/
├── src/
│   ├── main/kotlin/com/schwarzdigitale/
│   │   ├── auth/                 # Authentication & JWT
│   │   │   ├── AuthConfig.kt
│   │   │   ├── AuthModels.kt
│   │   │   └── JwtService.kt
│   │   ├── database/             # Database configuration
│   │   │   └── DatabaseFactory.kt
│   │   ├── domain/
│   │   │   ├── models/           # Domain models
│   │   │   │   ├── Book.kt
│   │   │   │   ├── Category.kt
│   │   │   │   └── Customer.kt
│   │   │   └── repositories/     # Data access layer
│   │   │       ├── BookRepository.kt
│   │   │       ├── CategoryRepository.kt
│   │   │       └── CustomerRepository.kt
│   │   ├── routes/               # API endpoints
│   │   │   ├── AuthRoutes.kt
│   │   │   ├── BookRoutes.kt
│   │   │   ├── CategoryRoutes.kt
│   │   │   └── CustomerRoutes.kt
│   │   ├── utils/                # Utilities
│   │   │   └── Validator.kt
│   │   ├── Database.kt
│   │   ├── Routing.kt
│   │   ├── StatusPages.kt        # Error handling
│   │   └── main.kt
│   └── test/kotlin/              # Unit & integration tests
│       ├── auth/
│       ├── domain/repositories/
│       ├── utils/
│       └── LibraryApiTest.kt
```

## API Endpoints

### Authentication (Public)

#### Register
```
POST /auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}

Response: 201 Created
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

#### Login
```
POST /auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "customer": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

### Categories

#### Get All Categories (Public)
```
GET /categories
```

#### Get Category by ID (Public)
```
GET /categories/{id}
```

#### Create Category (Authenticated)
```
POST /categories
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Science Fiction",
  "description": "Science fiction and futuristic books"
}
```

#### Update Category (Authenticated)
```
PUT /categories/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Updated Name",
  "description": "Updated description"
}
```

#### Delete Category (Authenticated)
```
DELETE /categories/{id}
Authorization: Bearer {token}
```

### Books

#### Get All Books (Public)
```
GET /books
```

#### Get Book by ID (Public)
```
GET /books/{id}
```

#### Get Books by Category (Public)
```
GET /books/category/{categoryId}
```

#### Create Book (Authenticated)
```
POST /books
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "1984",
  "author": "George Orwell",
  "publisher": "Secker & Warburg",
  "publishingYear": 1949,
  "categoryId": 1
}
```

#### Update Book (Authenticated)
```
PUT /books/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Updated Title",
  "publishingYear": 1950
}
```

#### Delete Book (Authenticated)
```
DELETE /books/{id}
Authorization: Bearer {token}
```

### Customers (All Authenticated)

#### Get All Customers
```
GET /customers
Authorization: Bearer {token}
```

#### Get Customer by ID
```
GET /customers/{id}
Authorization: Bearer {token}
```

#### Update Customer
```
PUT /customers/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "New Name",
  "email": "newemail@example.com"
}
```

#### Delete Customer
```
DELETE /customers/{id}
Authorization: Bearer {token}
```

## Getting Started

### Prerequisites
- JDK 21 or higher
- Gradle 8.x (included via wrapper)
- PostgreSQL (optional - H2 is used by default)

### Configuration

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

### Running the Application

```bash
# Build the project
./gradlew build

# Run the server
./gradlew :server:run

# The server starts at http://localhost:8080
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests with reports
./gradlew test --info

# Run specific test class
./gradlew test --tests "ValidatorTest"
```

## Testing

The project includes comprehensive tests using Kotest framework:

### Unit Tests
- **ValidatorTest**: Email, password, and name validation
- **JwtServiceTest**: JWT token generation and verification
- **CustomerRepositoryTest**: Customer CRUD operations
- **CategoryRepositoryTest**: Category management with book counts
- **BookRepositoryTest**: Book management with category relationships

### Integration Tests
- **LibraryApiTest**: End-to-end API testing with authentication flows

## API Documentation

Once the server is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger
- **OpenAPI Spec**: http://localhost:8080/openapi

## Security

- Passwords are hashed using BCrypt (cost factor: 12)
- JWT tokens are signed with HMAC256
- Email validation follows RFC standards
- Authenticated endpoints require valid JWT token
- Public read access for books and categories
- Write operations require authentication

## Validation Rules

### Email
- Must be valid email format
- RFC-compliant validation

### Password
- Minimum 6 characters

### Customer Name
- Minimum 2 characters
- Maximum 255 characters
- Cannot be blank

### Book
- Title, author, publisher cannot be blank
- Publishing year must be between 1000 and 2100
- Must be assigned to existing category

### Category
- Name and description cannot be blank
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
- `CONFLICT`: Resource conflict (e.g., deleting category with books)
- `SERVER_ERROR`: Internal server error

## Database Schema

### Customers
- id (PRIMARY KEY)
- name
- email (UNIQUE)
- password_hash
- created_at
- updated_at

### Categories
- id (PRIMARY KEY)
- name (UNIQUE)
- description
- created_at
- updated_at

### Books
- id (PRIMARY KEY)
- title
- author
- publisher
- publishing_year
- category_id (FOREIGN KEY → Categories)
- created_at
- updated_at

## Development

### Code Style
- Kotlin official coding conventions
- Consistent formatting with Kotlin DSL
- Repository pattern for data access
- DTOs for API contracts

### Architecture
- **Layered Architecture**: Routes → Repositories → Database
- **Dependency Injection**: Manual DI for simplicity
- **DTOs**: Separate request/response models from domain models
- **Error Handling**: Centralized with StatusPages plugin

## Future Enhancements

From the kick-off requirements, these features could be added:

- [ ] Docker & docker-compose setup
- [ ] GitHub Actions CI/CD
- [ ] Database migrations with Flyway
- [ ] Database seeds
- [ ] E2E tests
- [ ] Git commit hooks for linting/testing
- [ ] UML diagrams
- [ ] OAuth2 integration
- [ ] Rate limiting
- [ ] Pagination for list endpoints
- [ ] Book search functionality
- [ ] Book borrowing/lending system

## License

This project is for educational/demonstration purposes.

## Authors

Built as a practical exercise for demonstrating RESTful API development with Kotlin and Ktor.
