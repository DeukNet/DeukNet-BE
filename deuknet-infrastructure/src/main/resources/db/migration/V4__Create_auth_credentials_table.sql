-- Auth Credentials Table
-- Stores authentication credentials for users (OAuth, local, etc.)
CREATE TABLE auth_credentials (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    auth_provider VARCHAR(20) NOT NULL CHECK (auth_provider IN ('GOOGLE', 'GITHUB', 'LOCAL'))
);
