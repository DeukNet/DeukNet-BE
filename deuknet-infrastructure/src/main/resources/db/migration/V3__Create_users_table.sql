-- Users Table
-- Stores user profile information
CREATE TABLE users (
    id UUID PRIMARY KEY,
    auth_credential_id UUID NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100),
    avatar_url VARCHAR(500),
    bio TEXT,
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ADMIN'))
);
