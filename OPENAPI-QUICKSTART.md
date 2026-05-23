# OpenAPI/Swagger Quick Reference

## Access Points

| Resource | URL | Description |
|----------|-----|-------------|
| Swagger UI | http://localhost:8080/swagger | Interactive API documentation |
| Swagger UI (Alt) | http://localhost:8080/api-docs | Alternative Swagger UI endpoint |
| OpenAPI Spec | http://localhost:8080/openapi | Raw OpenAPI YAML specification |

## Quick Start

### 1. Start the Application

```bash
docker-compose up -d
```

### 2. Open Swagger UI

Visit: http://localhost:8080/swagger

### 3. Authenticate

1. Click **"Authorize"** button
2. Login to get token:
   ```bash
   curl -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"demo@example.com","password":"password123"}'
   ```
3. Copy the `token` value from response
4. In Swagger UI, paste: `Bearer YOUR_TOKEN`
5. Click **"Authorize"** then **"Close"**

### 4. Try Endpoints

1. Click any endpoint to expand
2. Click **"Try it out"**
3. Fill in parameters
4. Click **"Execute"**

## Common cURL Examples

### Register New User
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Doe",
    "email": "jane@example.com",
    "password": "SecurePass123"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo@example.com",
    "password": "password123"
  }'
```

### Get All Books (Public)
```bash
curl http://localhost:8080/books
```

### Create Book (Authenticated)
```bash
curl -X POST http://localhost:8080/books \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "publisher": "Prentice Hall",
    "publishingYear": 2008,
    "categoryId": 4
  }'
```

### Get All Categories (Public)
```bash
curl http://localhost:8080/categories
```

### Create Category (Authenticated)
```bash
curl -X POST http://localhost:8080/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Science Fiction",
    "description": "Sci-fi and futuristic novels"
  }'
```

## File Locations

| File | Path | Purpose |
|------|------|---------|
| OpenAPI Spec | `server/src/main/resources/openapi/documentation.yaml` | API specification |
| HTTP Config | `server/src/main/kotlin/Http.kt` | OpenAPI/Swagger setup |
| Documentation | `API-DOCUMENTATION.md` | Full API documentation |

## API Tags

- **Authentication** - Register, login
- **Books** - Book CRUD operations
- **Categories** - Category management
- **Customers** - Customer profiles
- **Health** - Health checks

## Authentication Requirements

| Endpoint | Public | Authenticated |
|----------|--------|---------------|
| GET /books | ✅ | - |
| POST /books | - | 🔒 |
| GET /categories | ✅ | - |
| POST /categories | - | 🔒 |
| ALL /customers/* | - | 🔒 |

## Status Codes

- **200** OK - Success
- **201** Created - Resource created
- **400** Bad Request - Validation error
- **401** Unauthorized - Auth required
- **404** Not Found - Resource not found
- **409** Conflict - Duplicate resource
- **500** Server Error - Internal error

## Demo Credentials

```
Email: demo@example.com
Password: password123
```

## Importing into Tools

### Postman
1. Import → Link → `http://localhost:8080/openapi`

### Insomnia
1. Import From → URL → `http://localhost:8080/openapi`

### Swagger Editor
1. Visit https://editor.swagger.io
2. File → Import URL → `http://localhost:8080/openapi`

## Files Created

✅ `server/src/main/resources/openapi/documentation.yaml` - Complete OpenAPI 3.0.3 specification  
✅ `server/src/main/kotlin/Http.kt` - Updated with Swagger UI configuration  
✅ `API-DOCUMENTATION.md` - Comprehensive API documentation  
✅ `.env.example` - Updated with API metadata

## Features Included

✅ Complete OpenAPI 3.0.3 specification  
✅ Swagger UI at `/swagger` and `/api-docs`  
✅ JWT authentication schema  
✅ All endpoints documented  
✅ Request/response schemas  
✅ Example values  
✅ Error response formats  
✅ Tag-based organization  
✅ Security requirements marked  

---

For detailed documentation, see `API-DOCUMENTATION.md`
