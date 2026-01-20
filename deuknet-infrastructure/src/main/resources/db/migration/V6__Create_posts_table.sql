-- Posts Table
-- Stores blog posts with author type (real/anonymous) support
CREATE TABLE posts (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    author_id UUID NOT NULL,
    author_type VARCHAR(20) NOT NULL CHECK (author_type IN ('REAL', 'ANONYMOUS')),
    category_id UUID,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PRIVATE', 'PUBLIC', 'ARCHIVED', 'DELETED')),
    thumbnail_image_url VARCHAR(500),
    view_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
