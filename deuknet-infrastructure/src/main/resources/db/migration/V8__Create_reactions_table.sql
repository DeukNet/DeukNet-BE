-- Reactions Table
-- Stores user reactions (like, dislike, view) for posts and comments
CREATE TABLE reactions (
    id UUID PRIMARY KEY,
    target_type VARCHAR(20) NOT NULL CHECK (target_type IN ('POST', 'COMMENT')),
    target_id UUID NOT NULL,
    user_id UUID NOT NULL,
    reaction_type VARCHAR(20) NOT NULL CHECK (reaction_type IN ('LIKE', 'DISLIKE', 'VIEW')),
    created_at TIMESTAMP NOT NULL
);
