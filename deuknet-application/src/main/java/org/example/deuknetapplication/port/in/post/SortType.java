package org.example.deuknetapplication.port.in.post;

/**
 * 게시글 정렬 타입
 */
public enum SortType {
    /**
     * 최신순 정렬
     * - 검색어가 있으면: 관련성(_score) 기준 정렬
     * - 검색어가 없으면: createdAt 내림차순 정렬
     */
    RECENT,

    /**
     * 인기순 정렬
     * - 인기도 = (likeCount * 3 + viewCount * 1)
     * - 30일 이상 경과 시 0.75배 시간 감쇠 적용
     */
    POPULAR
}
