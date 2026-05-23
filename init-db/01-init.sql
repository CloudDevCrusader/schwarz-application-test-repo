-- 01-init.sql
-- Docker PostgreSQL initialization script
-- This runs automatically when the PostgreSQL container is first created

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Set timezone
SET timezone = 'UTC';

-- Log initialization
DO $$
BEGIN
    RAISE NOTICE 'PostgreSQL initialization complete';
    RAISE NOTICE 'Database: %', current_database();
    RAISE NOTICE 'Version: %', version();
END $$;
