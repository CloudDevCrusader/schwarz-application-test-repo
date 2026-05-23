# API Documentation - OpenAPI/Swagger

This project includes comprehensive API documentation using OpenAPI 3.0.3 specification with Swagger UI.

## Accessing the API Documentation

Once the application is running, you can access the API documentation at:

### Swagger UI (Interactive Documentation)

- **Primary:** http://localhost:8080/swagger
- **Alternative:** http://localhost:8080/api-docs

The Swagger UI provides an interactive interface where you can:
- Browse all available endpoints
- View request/response schemas
- Test API endpoints directly from the browser
- See authentication requirements
- View example requests and responses

### OpenAPI Specification (Raw YAML)

- **OpenAPI Spec:** http://localhost:8080/openapi

This endpoint serves the raw OpenAPI YAML specification that can be:
- Imported into API testing tools (Postman, Insomnia)
- Used for code generation
- Shared with frontend developers
- Used in CI/CD pipelines

## API Overview

### Authentication

The API uses JWT (JSON Web Token) for authentication.

**Getting Started:**

1. **Register** a new account:
   ```bash
   curl -X POST http://localhost:8080/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "name": "John Doe",
       "email": "john@example.com",
       "password": "SecurePassword123"
     }'
   ```

2. **Login** to get JWT token:
   ```bash
   curl -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "email": "john@example.com",
       "password": "SecurePassword123"
     }'
   ```

3. **Use the token** in subsequent requests:
   ```bash
   curl -X POST http://localhost:8080/books \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -d '{
       "title": "1984",
       "author": "George Orwell",
       "publisher": "Secker & Warburg",
       "publishingYear": 1949,
       "categoryId": 1
     }'
   ```

### Demo Credentials

For testing, you can use these pre-seeded accounts:

```
Email: demo@example.com
Password: password123
```

## API Endpoints

### Health Endpoints
- `GET /` - Welcome message
- `GET /health` - Health check

### Authentication
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login and get JWT token

### Books (Public Read, Authenticated Write)
- `GET /books` - Get all books (public)
- `GET /books/{id}` - Get book by ID (public)
- `GET /books/category/{categoryId}` - Get books by category (public)
- `POST /books` - Create new book (🔒 authenticated)
- `PUT /books/{id}` - Update book (🔒 authenticated)
- `DELETE /books/{id}` - Delete book (🔒 authenticated)

### Categories (Public Read, Authenticated Write)
- `GET /categories` - Get all categories (public)
- `GET /categories/{id}` - Get category by ID (public)
- `POST /categories` - Create new category (🔒 authenticated)
- `PUT /categories/{id}` - Update category (🔒 authenticated)
- `DELETE /categories/{id}` - Delete category (🔒 authenticated)

### Customers (All Authenticated)
- `GET /customers` - Get all customers (🔒 authenticated)
- `GET /customers/{id}` - Get customer by ID (🔒 authenticated)
- `PUT /customers/{id}` - Update customer (🔒 authenticated)
- `DELETE /customers/{id}` - Delete customer (🔒 authenticated)

## Using Swagger UI

### 1. Authorize Your Requests

1. Click the **"Authorize"** button at the top right of the Swagger UI
2. Enter your JWT token in the format: `Bearer YOUR_JWT_TOKEN`
3. Click **"Authorize"**
4. Click **"Close"**

Now all requests will include your authentication token.

### 2. Try Out Endpoints

1. Click on any endpoint to expand it
2. Click **"Try it out"**
3. Fill in the required parameters/request body
4. Click **"Execute"**
5. View the response below

### 3. View Response Schemas

Each endpoint shows:
- Request body schema with examples
- Possible response codes
- Response body schemas
- Example values

## Example API Workflows

### Complete Book Management Workflow

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"password123"}' \
  | jq -r '.token')

# 2. Get all categories
curl http://localhost:8080/categories | jq

# 3. Get books in a category
curl http://localhost:8080/books/category/1 | jq

# 4. Create a new book (authenticated)
curl -X POST http://localhost:8080/books \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "The Pragmatic Programmer",
    "author": "Andrew Hunt, David Thomas",
    "publisher": "Addison-Wesley",
    "publishingYear": 1999,
    "categoryId": 4
  }' | jq

# 5. Update a book (authenticated)
curl -X PUT http://localhost:8080/books/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Updated Title",
    "publishingYear": 2024
  }' | jq

# 6. Delete a book (authenticated)
curl -X DELETE http://localhost:8080/books/1 \
  -H "Authorization: Bearer $TOKEN" | jq
```

## Importing into API Clients

### Postman

1. Open Postman
2. Click **Import**
3. Choose **Link**
4. Enter: `http://localhost:8080/openapi`
5. Click **Import**

### Insomnia

1. Open Insomnia
2. Click **Create** → **Import From**
3. Choose **URL**
4. Enter: `http://localhost:8080/openapi`
5. Click **Fetch and Import**

### VS Code (REST Client Extension)

Create a `.http` file:

```http
@baseUrl = http://localhost:8080
@token = YOUR_JWT_TOKEN

### Login
POST {{baseUrl}}/auth/login
Content-Type: application/json

{
  "email": "demo@example.com",
  "password": "password123"
}

### Get all books
GET {{baseUrl}}/books

### Create book
POST {{baseUrl}}/books
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "publisher": "Prentice Hall",
  "publishingYear": 2008,
  "categoryId": 4
}
```

## Code Generation

You can generate client SDKs using the OpenAPI specification:

### Using OpenAPI Generator

```bash
# Generate TypeScript client
npx @openapitools/openapi-generator-cli generate \
  -i http://localhost:8080/openapi \
  -g typescript-axios \
  -o ./generated/typescript-client

# Generate Python client
npx @openapitools/openapi-generator-cli generate \
  -i http://localhost:8080/openapi \
  -g python \
  -o ./generated/python-client

# Generate Java client
npx @openapitools/openapi-generator-cli generate \
  -i http://localhost:8080/openapi \
  -g java \
  -o ./generated/java-client
```

## Response Status Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 200 | OK | Request successful |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Validation error or invalid input |
| 401 | Unauthorized | Missing or invalid authentication |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource already exists (e.g., duplicate email) |
| 500 | Internal Server Error | Server error |

## Error Response Format

All errors follow this format:

```json
{
  "code": "ERROR_CODE",
  "message": "Human-readable error message"
}
```

**Example error codes:**
- `VALIDATION_ERROR` - Invalid input data
- `INVALID_CREDENTIALS` - Wrong email/password
- `EMAIL_EXISTS` - Email already registered
- `NOT_FOUND` - Resource not found
- `INVALID_ID` - Invalid ID parameter
- `SERVER_ERROR` - Internal server error

## Data Models

### Book
```json
{
  "id": 1,
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "publisher": "Scribner",
  "publishingYear": 1925,
  "categoryId": 1,
  "categoryName": "Fiction"
}
```

### Category
```json
{
  "id": 1,
  "name": "Fiction",
  "description": "Fictional literature including novels, short stories, and poetry",
  "bookCount": 5
}
```

### Customer
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com"
}
```

## Customizing the Documentation

The OpenAPI specification is located at:
```
server/src/main/resources/openapi/documentation.yaml
```

To modify:
1. Edit the YAML file
2. Restart the application
3. Refresh Swagger UI

### Common Customizations

**Change API info:**
```yaml
info:
  title: Your API Title
  version: 2.0.0
  description: Your API description
```

**Add new endpoint:**
```yaml
paths:
  /your-endpoint:
    get:
      tags:
        - YourTag
      summary: Your summary
      responses:
        '200':
          description: Success
```

**Modify authentication:**
```yaml
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
```

## Testing in CI/CD

You can validate the OpenAPI spec in CI/CD:

```bash
# Install validator
npm install -g @apidevtools/swagger-cli

# Validate specification
swagger-cli validate server/src/main/resources/openapi/documentation.yaml
```

## Additional Resources

- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)
- [Ktor OpenAPI Plugin](https://ktor.io/docs/openapi.html)
- [OpenAPI Generator](https://openapi-generator.tech/)

## Troubleshooting

### Swagger UI not loading

1. Check application is running: `http://localhost:8080/health`
2. Verify the YAML file exists at `server/src/main/resources/openapi/documentation.yaml`
3. Check application logs for errors
4. Clear browser cache

### Authentication not working in Swagger UI

1. Make sure you logged in and got a token
2. Click "Authorize" button in Swagger UI
3. Enter token in format: `Bearer YOUR_TOKEN` (not just the token)
4. Click "Authorize" then "Close"
5. Try the request again

### CORS errors

The API allows all origins by default. In production, configure CORS in `Http.kt`:

```kotlin
install(CORS) {
    allowHost("your-frontend-domain.com")
    allowMethod(HttpMethod.Options)
    allowMethod(HttpMethod.Get)
    allowMethod(HttpMethod.Post)
    allowMethod(HttpMethod.Put)
    allowMethod(HttpMethod.Delete)
    allowHeader(HttpHeaders.Authorization)
    allowHeader(HttpHeaders.ContentType)
}
```

---

**Happy API exploring!** 🚀

For questions or issues, refer to the main project documentation or create an issue in the repository.
