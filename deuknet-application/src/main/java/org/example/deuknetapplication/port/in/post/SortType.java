package org.example.deuknetapplication.port.in.post;

/**
 * 게시글 정렬 타입
 */
public enum SortType {
    /**
     * 최신순 정렬
     * - createdAt 내림차순 정렬
     */
    RECENT,

    /**
     * 정확도순 정렬 (검색어 매칭 중심)
     * - Elasticsearch _score 기준 정렬 (최소 스코어 2.0)
     */
    RELEVANCE,

    /**
     * 인기순 정렬
     * - 인기도 = (likeCount * 3 + viewCount * 1)
     * - 30일 이상 경과 시 0.75배 시간 감쇠 적용
     */
    POPULAR
}
