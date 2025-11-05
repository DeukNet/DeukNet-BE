package org.example.deuknetapplication.port.out.repository;

import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetdomain.domain.post.Post;

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
}
