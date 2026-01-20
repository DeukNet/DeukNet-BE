-- Create Publication for Debezium CDC
-- This publication enables logical replication for CDC event capture
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_publication WHERE pubname = 'deuknet_outbox_publication') THEN
        CREATE PUBLICATION deuknet_outbox_publication FOR ALL TABLES WITH (publish = 'insert, update, delete, truncate');
    END IF;
END $$;
