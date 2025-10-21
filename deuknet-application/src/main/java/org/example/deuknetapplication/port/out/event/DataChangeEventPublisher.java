package org.example.deuknetapplication.port.out.event;

import org.example.deuknetdomain.common.seedwork.Projection;

import java.util.UUID;

/**
 * 데이터 변경 이벤트 발행 포트
 *
 * Application 레이어에서 정의하는 아웃바운드 포트입니다.
 * 데이터 변경 사항을 외부 시스템에 전파하여 데이터 동기화를 수행합니다.
 *
 * 사용 목적:
 * - CQRS 읽기 모델 동기화
 * - 검색 엔진(Elasticsearch) 인덱스 업데이트
 * - 알림/통계/추천 시스템 데이터 동기화
 * - 다른 서비스로 데이터 변경 전파
 *
 * 비즈니스 요구사항:
 * - 데이터 변경이 커밋되면 이벤트도 반드시 발행되어야 함 (트랜잭션 일관성)
 * - 이벤트는 최소 1회 전달이 보장되어야 함 (At-least-once delivery)
 * - 같은 엔티티의 이벤트는 발생 순서가 보장되어야 함
 *
 * Infrastructure 구현 방식 (Application은 몰라도 됨):
 * - Transactional Outbox Pattern
 * - Change Data Capture (CDC)
 * - Event Sourcing
 */
public interface DataChangeEventPublisher {

    /**
     * 데이터 변경 이벤트 발행
     *
     * 데이터 변경 사항을 외부 시스템에 전파합니다.
     * 현재 트랜잭션과 함께 안전하게 처리됩니다.
     *
     * @param eventType 이벤트 타입 (예: "PostCreated", "CommentAdded")
     * @param aggregateId 변경된 엔티티의 ID
     * @param projection 변경 데이터를 담은 Projection 객체
     */
    void publish(String eventType, UUID aggregateId, Projection projection);
}
