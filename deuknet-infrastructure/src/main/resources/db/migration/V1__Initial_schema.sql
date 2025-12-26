-- Initial Database Schema for DeukNet
-- This migration creates the foundational tables for the application

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    auth_credential_id UUID NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100),
    avatar_url VARCHAR(500),
    bio TEXT,
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ADMIN'))
);

-- Auth Credentials Table
CREATE TABLE IF NOT EXISTS auth_credentials (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    auth_provider VARCHAR(20) NOT NULL CHECK (auth_provider IN ('GOOGLE', 'GITHUB', 'LOCAL'))
);

-- Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    thumbnail_image_url VARCHAR(500),
    parent_category_id UUID,
    owner_id UUID
);

-- Posts Table
CREATE TABLE IF NOT EXISTS posts (
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

-- Comments Table
CREATE TABLE IF NOT EXISTS comments (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL,
    parent_comment_id UUID,
    content TEXT NOT NULL,
    author_id UUID NOT NULL,
    author_type VARCHAR(20) NOT NULL CHECK (author_type IN ('REAL', 'ANONYMOUS')),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Reactions Table
CREATE TABLE IF NOT EXISTS reactions (
    id UUID PRIMARY KEY,
    target_type VARCHAR(20) NOT NULL CHECK (target_type IN ('POST', 'COMMENT')),
    target_id UUID NOT NULL,
    user_id UUID NOT NULL,
    reaction_type VARCHAR(20) NOT NULL CHECK (reaction_type IN ('LIKE', 'DISLIKE', 'VIEW')),
    created_at TIMESTAMP NOT NULL
);

-- Outbox Events Table (for CDC Event Sourcing)
CREATE TABLE IF NOT EXISTS outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload_type VARCHAR(255) NOT NULL,
    payload TEXT,
    status VARCHAR(20) NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,
    occurred_on TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP,

    -- Legacy columns (deprecated, for backward compatibility)
    aggregatetype VARCHAR(255) NOT NULL,
    aggregateid VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    timestamp BIGINT NOT NULL
);

-- Debezium Offset Storage Table (for CDC)
CREATE TABLE IF NOT EXISTS debezium_offset_storage (
    id VARCHAR(255) PRIMARY KEY,
    offset_key TEXT NOT NULL,
    offset_value TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create Publication for Debezium CDC (conditionally)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_publication WHERE pubname = 'deuknet_outbox_publication') THEN
        CREATE PUBLICATION deuknet_outbox_publication FOR ALL TABLES WITH (publish = 'insert, update, delete, truncate');
    END IF;
END $$;

-- Create Indexes for Performance
CREATE INDEX IF NOT EXISTS idx_posts_author_id ON posts(author_id);
CREATE INDEX IF NOT EXISTS idx_posts_category_id ON posts(category_id);
CREATE INDEX IF NOT EXISTS idx_posts_status ON posts(status);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_comments_post_id ON comments(post_id);
CREATE INDEX IF NOT EXISTS idx_comments_parent_comment_id ON comments(parent_comment_id);
CREATE INDEX IF NOT EXISTS idx_comments_author_id ON comments(author_id);

CREATE INDEX IF NOT EXISTS idx_reactions_target ON reactions(target_type, target_id);
CREATE INDEX IF NOT EXISTS idx_reactions_user_id ON reactions(user_id);

CREATE INDEX IF NOT EXISTS idx_outbox_events_status ON outbox_events(status);
CREATE INDEX IF NOT EXISTS idx_outbox_events_created_at ON outbox_events(created_at);

CREATE INDEX IF NOT EXISTS idx_categories_parent ON categories(parent_category_id);
CREATE INDEX IF NOT EXISTS idx_categories_owner ON categories(owner_id);
