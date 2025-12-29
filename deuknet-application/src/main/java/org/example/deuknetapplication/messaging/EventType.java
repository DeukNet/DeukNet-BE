package org.example.deuknetapplication.messaging;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.deuknetapplication.common.exception.InvalidEventTypeException;

/**
 * Outbox 이벤트 타입
 *
 * 도메인 이벤트와 Outbox 메시지 타입을 중앙에서 관리합니다.
 * 문자열 하드코딩을 방지하고 타입 안정성을 보장합니다.
 */
@Getter
@RequiredArgsConstructor
public enum EventType {

    // Post 관련 이벤트
    POST_CREATED("PostCreated", "Post.Created"),
    POST_UPDATED("PostUpdated", "Post.Updated"),
    POST_PUBLISHED("PostPublished", "Post.Published"),
    POST_DELETED("PostDeleted", "Post.Deleted"),

    // Comment 관련 이벤트
    COMMENT_CREATED("CommentCreated", "Comment.Created"),
    COMMENT_UPDATED("CommentUpdated", "Comment.Updated"),
    COMMENT_DELETED("CommentDeleted", "Comment.Deleted"),

    // Reaction 관련 이벤트
    REACTION_ADDED("ReactionAdded", "Reaction.Added"),
    REACTION_REMOVED("ReactionRemoved", "Reaction.Removed");

    /**
     * Outbox 테이블에 저장되는 타입 값
     */
    private final String typeName;

    /**
     * 도메인 이벤트 클래스명 (선택적)
     */
    private final String domainEventName;

    /**
     * 문자열로부터 EventType 찾기
     *
     * @param typeName Outbox 테이블의 type 컬럼 값
     * @return 해당하는 EventType
     * @throws InvalidEventTypeException 매칭되는 타입이 없을 경우
     */
    public static EventType fromTypeName(String typeName) {
        for (EventType type : values()) {
            if (type.typeName.equals(typeName)) {
                return type;
            }
        }
        throw new InvalidEventTypeException(typeName);
    }

    /**
     * 주어진 타입명이 유효한지 확인
     */
    public static boolean isValid(String typeName) {
        for (EventType type : values()) {
            if (type.typeName.equals(typeName)) {
                return true;
            }
        }
        return false;
    }
}
