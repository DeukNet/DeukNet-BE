package org.example.deuknetinfrastructure.external.search.port;

import lombok.RequiredArgsConstructor;
import org.example.deuknetapplication.dto.search.CommentSearchResponse;
import org.example.deuknetapplication.port.out.search.CommentSearchPort;
import org.example.deuknetinfrastructure.external.search.adapter.CommentSearchAdapter;
import org.example.deuknetinfrastructure.external.search.document.CommentDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 댓글 검색 Port Adapter
 */
@Component
@RequiredArgsConstructor
public class CommentSearchPortAdapter implements CommentSearchPort {

    private final CommentSearchAdapter commentSearchAdapter;

    @Override
    public Optional<CommentSearchResponse> findById(UUID id) {
        return commentSearchAdapter.findById(id).map(this::toResponse);
    }

    @Override
    public List<CommentSearchResponse> findByPostId(UUID postId, int page, int size) {
        return commentSearchAdapter.findByPostId(postId, page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentSearchResponse> findTopLevelCommentsByPostId(UUID postId, int page, int size) {
        return commentSearchAdapter.findTopLevelCommentsByPostId(postId, page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentSearchResponse> findRepliesByParentId(UUID parentCommentId, int page, int size) {
        return commentSearchAdapter.findRepliesByParentId(parentCommentId, page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentSearchResponse> findByAuthorId(UUID authorId, int page, int size) {
        return commentSearchAdapter.findByAuthorId(authorId, page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentSearchResponse> searchByContent(String keyword, int page, int size) {
        return commentSearchAdapter.searchByContent(keyword, page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentSearchResponse> searchWithFilters(String keyword, UUID postId, UUID authorId,
                                                         Boolean isReply, int page, int size) {
        return commentSearchAdapter.searchWithFilters(keyword, postId, authorId, isReply, page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long countByPostId(UUID postId) {
        return commentSearchAdapter.countByPostId(postId);
    }

    private CommentSearchResponse toResponse(CommentDocument document) {
        UUID parentCommentId = document.getParentCommentId() != null
                ? UUID.fromString(document.getParentCommentId())
                : null;

        return new CommentSearchResponse(
                document.getId(),
                UUID.fromString(document.getPostId()),
                document.getContent(),
                UUID.fromString(document.getAuthorId()),
                document.getAuthorUsername(),
                document.getAuthorDisplayName(),
                null, // avatarUrl is not in CommentDocument
                parentCommentId,
                document.getIsReply(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
