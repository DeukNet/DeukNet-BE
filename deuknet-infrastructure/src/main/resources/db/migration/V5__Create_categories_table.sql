-- Categories Table
-- Stores post categories with hierarchical structure support
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    thumbnail_image_url VARCHAR(500),
    parent_category_id UUID,
    owner_id UUID
);
