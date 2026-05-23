# Database Migrations and Seeding

This directory contains database migration and seeding scripts for the library application.

## Overview

The application uses **Flyway** for database migrations with PostgreSQL in production and H2 for testing/development.

## Migration Files

Migrations are located in `server/src/main/resources/db/migration/`:

- `V1__create_initial_schema.sql` - Creates tables, indexes, triggers, and constraints
- `V2__seed_initial_data.sql` - Seeds initial categories, books, and demo users

## Database Schema

### Tables

#### `customers`
- `id` - Primary key (auto-increment)
- `name` - Customer name
- `email` - Unique email address
- `password_hash` - BCrypt hashed password
- `created_at` - Timestamp
- `updated_at` - Timestamp (auto-updated via trigger)

#### `categories`
- `id` - Primary key (auto-increment)
- `name` - Unique category name
- `description` - Category description
- `created_at` - Timestamp
- `updated_at` - Timestamp (auto-updated via trigger)

#### `books`
- `id` - Primary key (auto-increment)
- `title` - Book title
- `author` - Book author
- `publisher` - Publisher name
- `publishing_year` - Year of publication
- `category_id` - Foreign key to categories
- `created_at` - Timestamp
- `updated_at` - Timestamp (auto-updated via trigger)

### Indexes

- `idx_customers_email` - Fast customer lookup by email
- `idx_categories_name` - Fast category lookup by name
- `idx_books_title` - Search books by title
- `idx_books_author` - Search books by author
- `idx_books_category_id` - Filter books by category
- `idx_books_publishing_year` - Filter books by year

### Triggers

Automatic `updated_at` timestamp triggers on all tables.

## Scripts

### `migrate.sh`

Runs database migrations.

```bash
./scripts/db/migrate.sh
```

**What it does:**
1. Loads environment from `.env`
2. Connects to PostgreSQL
3. Creates database if it doesn't exist
4. Runs all pending Flyway migrations
5. Shows migration status

**Requirements:**
- PostgreSQL running
- `psql` command available
- Correct credentials in `.env`

### `seed.sh`

Seeds the database with sample data.

```bash
./scripts/db/seed.sh
```

**What it does:**
1. Checks database connection
2. Verifies tables exist
3. Inserts sample categories (10 categories)
4. Inserts sample books (~30 books)
5. Inserts demo users (5 users)
6. Shows statistics

**Sample Data:**
- **Categories:** Fiction, Non-Fiction, Science, Technology, History, Biography, Self-Help, Children, Mystery, Fantasy
- **Books:** Classic and modern books across all categories
- **Demo Users:** All with password `password123`
  - demo@example.com
  - john.doe@example.com
  - jane.smith@example.com
  - alice@example.com
  - bob@example.com

**Note:** Uses `ON CONFLICT DO NOTHING` to prevent duplicate entries.

### `reset.sh`

⚠️ **DESTRUCTIVE** - Completely resets the database.

```bash
./scripts/db/reset.sh
```

**What it does:**
1. Drops the entire database
2. Recreates the database
3. Runs all migrations
4. Seeds with sample data

**Use cases:**
- Development environment reset
- Testing fresh state
- Recovering from migration issues

**⚠️ WARNING:** This will **DELETE ALL DATA**. Never use in production!

## Usage Guide

### Initial Setup

```bash
# 1. Start PostgreSQL (via Docker)
docker-compose up -d postgres

# 2. Run migrations
./scripts/db/migrate.sh

# 3. Seed data (optional)
./scripts/db/seed.sh
```

### Development Workflow

```bash
# Reset database to clean state
./scripts/db/reset.sh

# Run only migrations (without seed)
./scripts/db/migrate.sh

# Seed additional data
./scripts/db/seed.sh
```

### Adding New Migrations

1. Create a new SQL file in `server/src/main/resources/db/migration/`
2. Follow naming convention: `V{version}__{description}.sql`
   - Example: `V3__add_user_roles.sql`
3. Write migration SQL
4. Run `./scripts/db/migrate.sh`

**Example migration:**

```sql
-- V3__add_user_roles.sql

CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE customers ADD COLUMN role_id INTEGER;
ALTER TABLE customers ADD CONSTRAINT fk_customers_role
    FOREIGN KEY (role_id) REFERENCES roles(id);
```

### Rollback Migrations

Flyway doesn't support automatic rollback for free version. To rollback:

1. Create a new migration to undo changes
2. Or reset the database: `./scripts/db/reset.sh`

## Environment Configuration

Configure database connection in `.env`:

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=library
DB_USER=postgres
DB_PASSWORD=postgres
```

## Flyway Configuration

Flyway is configured in `DatabaseFactory.kt`:

- **Locations:** `classpath:db/migration`
- **Baseline on migrate:** true
- **Validate on migrate:** true
- **Baseline version:** 0

## Manual Database Operations

### Connect to PostgreSQL

```bash
# Via Docker
docker exec -it ktor-postgres psql -U postgres -d library

# Via psql
psql -h localhost -U postgres -d library
```

### Common Queries

```sql
-- Show all tables
\dt

-- Show table structure
\d customers
\d categories
\d books

-- Count records
SELECT 'Customers' as table, COUNT(*) FROM customers
UNION ALL
SELECT 'Categories', COUNT(*) FROM categories
UNION ALL
SELECT 'Books', COUNT(*) FROM books;

-- Show books with categories
SELECT 
    b.title,
    b.author,
    c.name as category,
    b.publishing_year
FROM books b
JOIN categories c ON b.category_id = c.id
ORDER BY b.title;

-- Books per category
SELECT 
    c.name,
    COUNT(b.id) as book_count
FROM categories c
LEFT JOIN books b ON c.id = b.category_id
GROUP BY c.name
ORDER BY book_count DESC;
```

## Troubleshooting

### Migration Failed

```bash
# Check Flyway schema history
psql -U postgres -d library -c "SELECT * FROM flyway_schema_history;"

# Reset and try again
./scripts/db/reset.sh
```

### Connection Refused

```bash
# Check if PostgreSQL is running
docker-compose ps

# Start PostgreSQL
docker-compose up -d postgres

# Check logs
docker-compose logs postgres
```

### Permission Denied on Scripts

```bash
chmod +x scripts/db/*.sh
```

### Database Already Exists Error

```bash
# Drop database manually
psql -U postgres -c "DROP DATABASE library;"

# Or use reset script
./scripts/db/reset.sh
```

## CI/CD Integration

Migrations run automatically in CI/CD:

```yaml
- name: Run tests
  run: ./gradlew test
  env:
    DATABASE_URL: jdbc:postgresql://localhost:5432/library_test
```

The application runs migrations on startup when using PostgreSQL.

## Best Practices

1. **Never modify existing migrations** - Create new ones instead
2. **Test migrations locally** before committing
3. **Keep migrations small** and focused
4. **Use transactions** where appropriate
5. **Add rollback documentation** for complex migrations
6. **Version control** all migration files
7. **Back up production data** before running migrations
8. **Use descriptive names** for migrations

## Production Deployment

For production:

1. **Backup database first**
2. **Test migrations in staging**
3. **Run migrations before deploying code**
4. **Monitor migration execution**
5. **Have rollback plan ready**

```bash
# Production migration (example)
DATABASE_URL=jdbc:postgresql://prod-host:5432/library \
DATABASE_USER=prod_user \
DATABASE_PASSWORD=$PROD_PASSWORD \
./scripts/db/migrate.sh
```

## Additional Resources

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Exposed Framework](https://github.com/JetBrains/Exposed)
