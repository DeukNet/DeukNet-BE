-- Comments Table
-- Stores post comments with nested comment support
CREATE TABLE comments (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL,
    parent_comment_id UUID,
    content TEXT NOT NULL,
    author_id UUID NOT NULL,
    author_type VARCHAR(20) NOT NULL CHECK (author_type IN ('REAL', 'ANONYMOUS')),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
