package org.example.deuknetapplication.port.out.search;

import org.example.deuknetapplication.dto.search.CommentSearchResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 댓글 검색 Port
 */
public interface CommentSearchPort {
    Optional<CommentSearchResponse> findById(UUID id);
    List<CommentSearchResponse> findByPostId(UUID postId, int page, int size);
    List<CommentSearchResponse> findTopLevelCommentsByPostId(UUID postId, int page, int size);
    List<CommentSearchResponse> findRepliesByParentId(UUID parentCommentId, int page, int size);
    List<CommentSearchResponse> findByAuthorId(UUID authorId, int page, int size);
    List<CommentSearchResponse> searchByContent(String keyword, int page, int size);
    List<CommentSearchResponse> searchWithFilters(String keyword, UUID postId, UUID authorId, Boolean isReply, int page, int size);
    long countByPostId(UUID postId);
}
