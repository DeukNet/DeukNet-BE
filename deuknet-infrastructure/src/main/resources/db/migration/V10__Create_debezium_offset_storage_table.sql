-- Debezium Offset Storage Table (for CDC)
-- Stores Debezium connector offset for tracking CDC progress
CREATE TABLE debezium_offset_storage (
    id VARCHAR(255) PRIMARY KEY,
    offset_key TEXT NOT NULL,
    offset_value TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
