package org.example.deuknetinfrastructure.external.messaging.handler;

import org.example.deuknetapplication.messaging.EventType;

/**
 * CDC 이벤트 처리 전략 인터페이스
 *
 * Strategy 패턴을 사용하여 각 도메인 이벤트를 독립적으로 처리합니다.
 */
public interface EventHandler {

    /**
     * 이 핸들러가 처리할 수 있는 이벤트 타입인지 확인
     *
     * @param eventType 이벤트 타입
     * @return 처리 가능 여부
     */
    boolean canHandle(EventType eventType);

    /**
     * 이벤트 처리
     *
     * @param eventType 이벤트 타입
     * @param aggregateId Aggregate ID
     * @param payloadJson 이벤트 페이로드 (JSON)
     * @throws Exception 처리 중 오류 발생 시
     */
    void handle(EventType eventType, String aggregateId, String payloadJson) throws Exception;
}
