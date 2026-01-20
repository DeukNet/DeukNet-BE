-- Rollback V1__Initial_schema.sql
-- This migration drops all tables, indexes, and publications created in V1

-- Drop Indexes first (if they exist)
DROP INDEX IF EXISTS idx_posts_author_id;
DROP INDEX IF EXISTS idx_posts_category_id;
DROP INDEX IF EXISTS idx_posts_status;
DROP INDEX IF EXISTS idx_posts_created_at;

DROP INDEX IF EXISTS idx_comments_post_id;
DROP INDEX IF EXISTS idx_comments_parent_comment_id;
DROP INDEX IF EXISTS idx_comments_author_id;

DROP INDEX IF EXISTS idx_reactions_target;
DROP INDEX IF EXISTS idx_reactions_user_id;

DROP INDEX IF EXISTS idx_outbox_events_status;
DROP INDEX IF EXISTS idx_outbox_events_created_at;

DROP INDEX IF EXISTS idx_categories_parent;
DROP INDEX IF EXISTS idx_categories_owner;

-- Drop Publication (conditionally)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_publication WHERE pubname = 'deuknet_outbox_publication') THEN
        DROP PUBLICATION deuknet_outbox_publication;
    END IF;
END $$;

-- Drop Tables (in correct order due to potential FK dependencies)
DROP TABLE IF EXISTS reactions CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS debezium_offset_storage CASCADE;
DROP TABLE IF EXISTS outbox_events CASCADE;
DROP TABLE IF EXISTS auth_credentials CASCADE;
DROP TABLE IF EXISTS users CASCADE;
