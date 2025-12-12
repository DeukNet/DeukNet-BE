package org.example.deuknetapplication.port.in.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 게시글 검색 요청 (모든 조건은 AND로 결합)
 * 항상 PUBLISHED 상태만 조회
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
     * 정렬 타입 (RECENT: 최신순, POPULAR: 인기순)
     */
    @Builder.Default
    private SortType sortType = SortType.RECENT;

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
     * 익명 게시물 포함 여부
     * - true: 익명 게시물 포함 (본인 글 조회 시)
     * - false: 실명 게시물만 조회 (다른 유저 글 조회 시)
     */
    @Builder.Default
    private boolean includeAnonymous = false;
}
