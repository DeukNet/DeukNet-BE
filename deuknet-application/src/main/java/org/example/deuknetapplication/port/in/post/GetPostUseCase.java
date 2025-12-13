package org.example.deuknetapplication.port.in.post;

import java.util.UUID;

/**
 * 게시글 단일 조회 UseCase
 */
public interface GetPostUseCase {
    /**
     * 게시글 ID로 게시글을 조회
     *
     * @param postId 조회할 게시글 ID
     * @param forceCommandModel true: PostgreSQL에서 직접 조회 (생성/수정 직후), false: Elasticsearch 우선 조회 (일반 조회)
     * @return 게시글 상세 정보
     */
    PostSearchResponse getPostById(UUID postId, boolean forceCommandModel);
}
