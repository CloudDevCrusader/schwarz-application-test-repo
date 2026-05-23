-- V2__seed_initial_data.sql
-- Seed initial data for development and testing

-- Insert default categories
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

-- Insert sample books
INSERT INTO books (title, author, publisher, publishing_year, category_id) VALUES
    -- Fiction
    ('The Great Gatsby', 'F. Scott Fitzgerald', 'Scribner', 1925, (SELECT id FROM categories WHERE name = 'Fiction')),
    ('1984', 'George Orwell', 'Secker & Warburg', 1949, (SELECT id FROM categories WHERE name = 'Fiction')),
    ('To Kill a Mockingbird', 'Harper Lee', 'J.B. Lippincott & Co.', 1960, (SELECT id FROM categories WHERE name = 'Fiction')),
    
    -- Technology
    ('Clean Code', 'Robert C. Martin', 'Prentice Hall', 2008, (SELECT id FROM categories WHERE name = 'Technology')),
    ('The Pragmatic Programmer', 'Andrew Hunt, David Thomas', 'Addison-Wesley', 1999, (SELECT id FROM categories WHERE name = 'Technology')),
    ('Design Patterns', 'Gang of Four', 'Addison-Wesley', 1994, (SELECT id FROM categories WHERE name = 'Technology')),
    
    -- Science
    ('A Brief History of Time', 'Stephen Hawking', 'Bantam Books', 1988, (SELECT id FROM categories WHERE name = 'Science')),
    ('The Selfish Gene', 'Richard Dawkins', 'Oxford University Press', 1976, (SELECT id FROM categories WHERE name = 'Science')),
    
    -- History
    ('Sapiens', 'Yuval Noah Harari', 'Harper', 2011, (SELECT id FROM categories WHERE name = 'History')),
    ('Guns, Germs, and Steel', 'Jared Diamond', 'W. W. Norton', 1997, (SELECT id FROM categories WHERE name = 'History')),
    
    -- Fantasy
    ('The Hobbit', 'J.R.R. Tolkien', 'George Allen & Unwin', 1937, (SELECT id FROM categories WHERE name = 'Fantasy')),
    ('Harry Potter and the Philosopher''s Stone', 'J.K. Rowling', 'Bloomsbury', 1997, (SELECT id FROM categories WHERE name = 'Fantasy')),
    
    -- Mystery
    ('The Adventures of Sherlock Holmes', 'Arthur Conan Doyle', 'George Newnes', 1892, (SELECT id FROM categories WHERE name = 'Mystery')),
    ('The Girl with the Dragon Tattoo', 'Stieg Larsson', 'Norstedts', 2005, (SELECT id FROM categories WHERE name = 'Mystery')),
    
    -- Self-Help
    ('Atomic Habits', 'James Clear', 'Avery', 2018, (SELECT id FROM categories WHERE name = 'Self-Help')),
    ('The 7 Habits of Highly Effective People', 'Stephen Covey', 'Free Press', 1989, (SELECT id FROM categories WHERE name = 'Self-Help'))
ON CONFLICT DO NOTHING;

-- Insert a demo user (password is 'password123' hashed with bcrypt)
-- Note: In production, users should be created through the application with proper password hashing
INSERT INTO customers (name, email, password_hash) VALUES
    ('Demo User', 'demo@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7Iy/fhPXQDHFmz5GfMLPqOKANWEkPK6'),
    ('John Doe', 'john.doe@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7Iy/fhPXQDHFmz5GfMLPqOKANWEkPK6'),
    ('Jane Smith', 'jane.smith@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7Iy/fhPXQDHFmz5GfMLPqOKANWEkPK6')
ON CONFLICT (email) DO NOTHING;
