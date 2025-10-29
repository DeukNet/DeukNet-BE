package org.example.deuknetapplication.port.in.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 게시글 검색 요청 (모든 조건은 AND로 결합)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSearchRequest {

    /**
     * 검색 키워드 (제목 + 내용 전문 검색)
     */
    private String keyword;

    /**
     * 작성자 ID
     */
    private UUID authorId;

    /**
     * 카테고리 ID
     */
    private UUID categoryId;

    /**
     * 게시글 상태 (PUBLISHED, DRAFT 등)
     */
    private String status;

    /**
     * 페이지 번호 (0부터 시작)
     */
    @Builder.Default
    private int page = 0;

    /**
     * 페이지 크기
     */
    @Builder.Default
    private int size = 10;

    /**
     * 정렬 필드 (createdAt, viewCount, likeCount 등)
     */
    @Builder.Default
    private String sortBy = "createdAt";

    /**
     * 정렬 순서 (asc, desc)
     */
    @Builder.Default
    private String sortOrder = "desc";
}
