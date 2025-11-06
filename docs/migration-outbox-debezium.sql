-- Debezium Outbox Pattern 마이그레이션 스크립트
-- 기존 outbox_events 테이블을 Debezium 표준 형식으로 변경

-- 1. 기존 테이블 백업 (선택사항)
CREATE TABLE IF NOT EXISTS outbox_events_backup AS
SELECT * FROM outbox_events;

-- 2. 기존 테이블 삭제
DROP TABLE IF EXISTS outbox_events CASCADE;

-- 3. Debezium 표준 Outbox 테이블 생성
CREATE TABLE outbox (
    id UUID PRIMARY KEY,
    aggregatetype VARCHAR(255) NOT NULL,
    aggregateid VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    payload TEXT,
    timestamp BIGINT NOT NULL
);

-- 4. 인덱스 생성 (성능 최적화)
-- aggregateid로 조회가 많을 경우
CREATE INDEX idx_outbox_aggregateid ON outbox(aggregateid);

-- aggregatetype으로 필터링하는 경우
CREATE INDEX idx_outbox_aggregatetype ON outbox(aggregatetype);

-- timestamp로 정렬하는 경우
CREATE INDEX idx_outbox_timestamp ON outbox(timestamp);

-- 복합 인덱스 (aggregatetype + timestamp)
CREATE INDEX idx_outbox_type_timestamp ON outbox(aggregatetype, timestamp);

-- 5. 코멘트 추가
COMMENT ON TABLE outbox IS 'Debezium Outbox Pattern - CDC 이벤트 발행용 테이블';
COMMENT ON COLUMN outbox.id IS '이벤트 고유 식별자';
COMMENT ON COLUMN outbox.aggregatetype IS 'Aggregate 타입 (예: Post, Comment) - 토픽 라우팅에 사용';
COMMENT ON COLUMN outbox.aggregateid IS 'Aggregate ID (문자열) - Kafka 파티션 키로 사용';
COMMENT ON COLUMN outbox.type IS '이벤트 타입 (예: PostCreated, CommentAdded)';
COMMENT ON COLUMN outbox.payload IS '이벤트 페이로드 (JSON 형식)';
COMMENT ON COLUMN outbox.timestamp IS '이벤트 발생 시각 (epoch milliseconds)';

-- 6. 기존 데이터 마이그레이션 (필요한 경우)
-- 기존 데이터가 있는 경우 아래 쿼리를 사용하여 변환
/*
INSERT INTO outbox (id, aggregatetype, aggregateid, type, payload, timestamp)
SELECT
    id,
    aggregate_type,
    aggregate_id::TEXT,  -- UUID를 문자열로 변환
    event_type,
    payload,
    EXTRACT(EPOCH FROM occurred_on) * 1000  -- LocalDateTime을 epoch millis로 변환
FROM outbox_events_backup
WHERE status = 'PUBLISHED';  -- 발행된 이벤트만 마이그레이션
*/
