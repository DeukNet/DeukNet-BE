package org.example.deuknetapplication.port.out.repository;

import org.example.deuknetdomain.domain.post.AuthorType;

import java.util.UUID;

/**
 * 작성자 정보를 동적으로 설정할 수 있는 응답 객체 인터페이스
 * Post, Comment 등의 Response 객체가 구현하여 익명 처리에 사용
 */
public interface AuthorInfoEnrichable {
    UUID getAuthorId();
    void setAuthorId(UUID authorId);

    AuthorType getAuthorType();

    void setAuthorUsername(String username);
    void setAuthorDisplayName(String displayName);
}
