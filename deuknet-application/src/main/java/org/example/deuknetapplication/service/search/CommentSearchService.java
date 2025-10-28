package org.example.deuknetapplication.service.search;

import lombok.RequiredArgsConstructor;
import org.example.deuknetapplication.dto.search.CommentSearchResponse;
import org.example.deuknetapplication.port.out.search.CommentSearchPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Comment 검색 서비스
 *
 * Elasticsearch를 활용한 댓글 검색 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class CommentSearchService {

    private final CommentSearchPort commentSearchPort;

    public Optional<CommentSearchResponse> findById(UUID id) {
        return commentSearchPort.findById(id);
    }

    public List<CommentSearchResponse> findByPostId(UUID postId, int page, int size) {
        return commentSearchPort.findByPostId(postId, page, size);
    }

    public List<CommentSearchResponse> findTopLevelCommentsByPostId(UUID postId, int page, int size) {
        return commentSearchPort.findTopLevelCommentsByPostId(postId, page, size);
    }

    public List<CommentSearchResponse> findRepliesByParentId(UUID parentCommentId, int page, int size) {
        return commentSearchPort.findRepliesByParentId(parentCommentId, page, size);
    }

    public List<CommentSearchResponse> findByAuthorId(UUID authorId, int page, int size) {
        return commentSearchPort.findByAuthorId(authorId, page, size);
    }

    public List<CommentSearchResponse> searchByContent(String keyword, int page, int size) {
        return commentSearchPort.searchByContent(keyword, page, size);
    }

    public List<CommentSearchResponse> searchWithFilters(
            String keyword,
            UUID postId,
            UUID authorId,
            Boolean isReply,
            int page,
            int size
    ) {
        return commentSearchPort.searchWithFilters(keyword, postId, authorId, isReply, page, size);
    }

    public long countByPostId(UUID postId) {
        return commentSearchPort.countByPostId(postId);
    }
}
