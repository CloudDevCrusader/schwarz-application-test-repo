#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}Database Seed Script${NC}"
echo -e "${BLUE}======================================${NC}"

# Load environment variables if .env exists
if [ -f .env ]; then
    echo -e "${YELLOW}Loading environment from .env file...${NC}"
    export $(cat .env | grep -v '^#' | xargs)
else
    echo -e "${YELLOW}Warning: .env file not found, using defaults${NC}"
fi

# Database configuration with defaults
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-library}
DB_USER=${DB_USER:-postgres}
DB_PASSWORD=${DB_PASSWORD:-postgres}

echo ""
echo -e "${GREEN}Database Configuration:${NC}"
echo -e "  Host: ${BLUE}${DB_HOST}${NC}"
echo -e "  Port: ${BLUE}${DB_PORT}${NC}"
echo -e "  Database: ${BLUE}${DB_NAME}${NC}"
echo -e "  User: ${BLUE}${DB_USER}${NC}"
echo ""

# Set password for psql
export PGPASSWORD=$DB_PASSWORD

# Check database connection
echo -e "${YELLOW}Checking database connection...${NC}"
if ! psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c '\q' 2>/dev/null; then
    echo -e "${RED}ERROR: Cannot connect to database${NC}"
    echo -e "${YELLOW}Run './scripts/db/migrate.sh' first to create the database${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Database connection successful${NC}"
echo ""

# Check if tables exist
echo -e "${YELLOW}Checking if tables exist...${NC}"
TABLE_COUNT=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "
    SELECT COUNT(*) FROM information_schema.tables 
    WHERE table_schema = 'public' 
    AND table_name IN ('customers', 'categories', 'books');
")

if [ "$TABLE_COUNT" -lt 3 ]; then
    echo -e "${RED}ERROR: Required tables not found${NC}"
    echo -e "${YELLOW}Run './scripts/db/migrate.sh' first to create the schema${NC}"
    exit 1
fi

echo -e "${GREEN}✓ All required tables exist${NC}"
echo ""

# Ask for confirmation
echo -e "${YELLOW}This will insert sample data into the database.${NC}"
echo -e "${YELLOW}Existing data will not be overwritten (using ON CONFLICT clauses).${NC}"
read -p "Do you want to continue? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}Seeding cancelled${NC}"
    exit 0
fi

echo ""
echo -e "${BLUE}======================================${NC}"
echo -e "${BLUE}Seeding Database${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""

# Seed data
echo -e "${YELLOW}Inserting categories...${NC}"
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME << 'EOF'
INSERT INTO categories (name, description) VALUES
    ('Fiction', 'Fictional literature including novels, short stories, and poetry'),
    ('Non-Fiction', 'Factual books including biographies, history, and science'),
    ('Science', 'Books about scientific topics and research'),
    ('Technology', 'Books about technology, programming, and computing'),
    ('History', 'Historical books and documentaries'),
    ('Biography', 'Biographies and autobiographies of notable people'),
    ('Self-Help', 'Self-improvement and motivational books'),
    ('Children', 'Books for children and young adults'),
    ('Mystery', 'Mystery and detective novels'),
    ('Fantasy', 'Fantasy and magical realism')
ON CONFLICT (name) DO NOTHING;
EOF

echo -e "${GREEN}✓ Categories inserted${NC}"
echo ""

echo -e "${YELLOW}Inserting books...${NC}"
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME << 'EOF'
INSERT INTO books (title, author, publisher, publishing_year, category_id) VALUES
    -- Fiction
    ('The Great Gatsby', 'F. Scott Fitzgerald', 'Scribner', 1925, (SELECT id FROM categories WHERE name = 'Fiction')),
    ('1984', 'George Orwell', 'Secker & Warburg', 1949, (SELECT id FROM categories WHERE name = 'Fiction')),
    ('To Kill a Mockingbird', 'Harper Lee', 'J.B. Lippincott & Co.', 1960, (SELECT id FROM categories WHERE name = 'Fiction')),
    ('Pride and Prejudice', 'Jane Austen', 'T. Egerton', 1813, (SELECT id FROM categories WHERE name = 'Fiction')),
    ('The Catcher in the Rye', 'J.D. Salinger', 'Little, Brown and Company', 1951, (SELECT id FROM categories WHERE name = 'Fiction')),
    
    -- Technology
    ('Clean Code', 'Robert C. Martin', 'Prentice Hall', 2008, (SELECT id FROM categories WHERE name = 'Technology')),
    ('The Pragmatic Programmer', 'Andrew Hunt, David Thomas', 'Addison-Wesley', 1999, (SELECT id FROM categories WHERE name = 'Technology')),
    ('Design Patterns', 'Gang of Four', 'Addison-Wesley', 1994, (SELECT id FROM categories WHERE name = 'Technology')),
    ('Introduction to Algorithms', 'Thomas H. Cormen', 'MIT Press', 1990, (SELECT id FROM categories WHERE name = 'Technology')),
    ('The Mythical Man-Month', 'Frederick P. Brooks Jr.', 'Addison-Wesley', 1975, (SELECT id FROM categories WHERE name = 'Technology')),
    
    -- Science
    ('A Brief History of Time', 'Stephen Hawking', 'Bantam Books', 1988, (SELECT id FROM categories WHERE name = 'Science')),
    ('The Selfish Gene', 'Richard Dawkins', 'Oxford University Press', 1976, (SELECT id FROM categories WHERE name = 'Science')),
    ('Cosmos', 'Carl Sagan', 'Random House', 1980, (SELECT id FROM categories WHERE name = 'Science')),
    ('The Origin of Species', 'Charles Darwin', 'John Murray', 1859, (SELECT id FROM categories WHERE name = 'Science')),
    
    -- History
    ('Sapiens', 'Yuval Noah Harari', 'Harper', 2011, (SELECT id FROM categories WHERE name = 'History')),
    ('Guns, Germs, and Steel', 'Jared Diamond', 'W. W. Norton', 1997, (SELECT id FROM categories WHERE name = 'History')),
    ('The Silk Roads', 'Peter Frankopan', 'Bloomsbury', 2015, (SELECT id FROM categories WHERE name = 'History')),
    
    -- Fantasy
    ('The Hobbit', 'J.R.R. Tolkien', 'George Allen & Unwin', 1937, (SELECT id FROM categories WHERE name = 'Fantasy')),
    ('Harry Potter and the Philosopher''s Stone', 'J.K. Rowling', 'Bloomsbury', 1997, (SELECT id FROM categories WHERE name = 'Fantasy')),
    ('The Lord of the Rings', 'J.R.R. Tolkien', 'George Allen & Unwin', 1954, (SELECT id FROM categories WHERE name = 'Fantasy')),
    ('A Game of Thrones', 'George R.R. Martin', 'Bantam Spectra', 1996, (SELECT id FROM categories WHERE name = 'Fantasy')),
    
    -- Mystery
    ('The Adventures of Sherlock Holmes', 'Arthur Conan Doyle', 'George Newnes', 1892, (SELECT id FROM categories WHERE name = 'Mystery')),
    ('The Girl with the Dragon Tattoo', 'Stieg Larsson', 'Norstedts', 2005, (SELECT id FROM categories WHERE name = 'Mystery')),
    ('Gone Girl', 'Gillian Flynn', 'Crown Publishing', 2012, (SELECT id FROM categories WHERE name = 'Mystery')),
    
    -- Self-Help
    ('Atomic Habits', 'James Clear', 'Avery', 2018, (SELECT id FROM categories WHERE name = 'Self-Help')),
    ('The 7 Habits of Highly Effective People', 'Stephen Covey', 'Free Press', 1989, (SELECT id FROM categories WHERE name = 'Self-Help')),
    ('How to Win Friends and Influence People', 'Dale Carnegie', 'Simon & Schuster', 1936, (SELECT id FROM categories WHERE name = 'Self-Help')),
    
    -- Biography
    ('Steve Jobs', 'Walter Isaacson', 'Simon & Schuster', 2011, (SELECT id FROM categories WHERE name = 'Biography')),
    ('The Diary of a Young Girl', 'Anne Frank', 'Contact Publishing', 1947, (SELECT id FROM categories WHERE name = 'Biography')),
    
    -- Children
    ('The Very Hungry Caterpillar', 'Eric Carle', 'World Publishing Company', 1969, (SELECT id FROM categories WHERE name = 'Children')),
    ('Charlotte''s Web', 'E.B. White', 'Harper & Brothers', 1952, (SELECT id FROM categories WHERE name = 'Children'))
ON CONFLICT DO NOTHING;
EOF

echo -e "${GREEN}✓ Books inserted${NC}"
echo ""

echo -e "${YELLOW}Inserting demo users...${NC}"
echo -e "${YELLOW}Note: All demo users have password 'password123'${NC}"
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME << 'EOF'
-- Password: password123 (bcrypt hashed)
INSERT INTO customers (name, email, password_hash) VALUES
    ('Demo User', 'demo@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7Iy/fhPXQDHFmz5GfMLPqOKANWEkPK6'),
    ('John Doe', 'john.doe@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7Iy/fhPXQDHFmz5GfMLPqOKANWEkPK6'),
    ('Jane Smith', 'jane.smith@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7Iy/fhPXQDHFmz5GfMLPqOKANWEkPK6'),
    ('Alice Johnson', 'alice@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7Iy/fhPXQDHFmz5GfMLPqOKANWEkPK6'),
    ('Bob Wilson', 'bob@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7Iy/fhPXQDHFmz5GfMLPqOKANWEkPK6')
ON CONFLICT (email) DO NOTHING;
EOF

echo -e "${GREEN}✓ Demo users inserted${NC}"
echo ""

# Show statistics
echo -e "${BLUE}======================================${NC}"
echo -e "${GREEN}✓ Database seeded successfully!${NC}"
echo -e "${BLUE}======================================${NC}"
echo ""

echo -e "${YELLOW}Database Statistics:${NC}"
echo ""

echo -e "${BLUE}Categories:${NC}"
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
    SELECT COUNT(*) as total_categories FROM categories;
"

echo -e "${BLUE}Books:${NC}"
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
    SELECT 
        c.name as category,
        COUNT(b.id) as book_count
    FROM categories c
    LEFT JOIN books b ON c.id = b.category_id
    GROUP BY c.name
    ORDER BY book_count DESC;
"

echo -e "${BLUE}Customers:${NC}"
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
    SELECT COUNT(*) as total_customers FROM customers;
"

echo ""
echo -e "${GREEN}Demo Credentials:${NC}"
echo -e "  Email: ${BLUE}demo@example.com${NC}"
echo -e "  Password: ${BLUE}password123${NC}"

# Clean up
unset PGPASSWORD
