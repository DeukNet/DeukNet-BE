package org.example.deuknetapplication.port.out.repository;

import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetdomain.domain.post.Post;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository {
    Post save(Post post);
    Optional<Post> findById(UUID id);
    void delete(Post post);

    /**
     * 게시글 상세 정보 조회 (QueryDSL)
     * 작성자, 카테고리, 반응 수 등을 조인으로 한 번에 조회
     */
    Optional<PostDetailProjection> findDetailById(UUID id);

    /**
     * 사용자가 좋아요 누른 게시글 조회 (reactions와 JOIN)
     * 페이지네이션 적용, reactions.createdAt 최신순 정렬
     */
    List<PostDetailProjection> findLikedPostsByUserId(UUID userId, int offset, int limit);

    /**
     * 사용자가 좋아요 누른 게시글 총 개수
     */
    long countLikedPostsByUserId(UUID userId);
}
