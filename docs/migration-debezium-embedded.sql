-- Debezium Embedded Engine을 위한 마이그레이션 스크립트

-- 1. Debezium Offset Storage 테이블 생성
CREATE TABLE IF NOT EXISTS debezium_offset_storage (
    id VARCHAR(255) PRIMARY KEY,
    offset_key TEXT,
    offset_value TEXT
);

-- 2. PostgreSQL에 WAL 레벨 설정 확인
-- 이미 wal_level = logical로 설정되어 있어야 합니다.
SHOW wal_level;  -- 결과: logical

-- 3. Publication 생성 (Outbox 테이블용)
CREATE PUBLICATION IF NOT EXISTS deuknet_outbox_publication
FOR TABLE outbox_events;

-- 4. Replication Slot 생성 (Debezium이 자동 생성하지만 수동으로도 가능)
-- SELECT pg_create_logical_replication_slot('deuknet_outbox_slot', 'pgoutput');

-- 5. 현재 Publication 확인
SELECT * FROM pg_publication;

-- 6. Publication에 포함된 테이블 확인
SELECT * FROM pg_publication_tables WHERE pubname = 'deuknet_outbox_publication';

-- 7. Replication Slot 확인
SELECT slot_name, plugin, slot_type, database, active
FROM pg_replication_slots;
