package org.example.deuknetinfrastructure.external.search.port;

import co.elastic.clients.elasticsearch._types.SortOrder;
import lombok.RequiredArgsConstructor;
import org.example.deuknetapplication.dto.search.PostDetailSearchResponse;
import org.example.deuknetapplication.port.out.search.PostDetailSearchPort;
import org.example.deuknetinfrastructure.external.search.adapter.PostDetailSearchAdapter;
import org.example.deuknetinfrastructure.external.search.document.PostDetailDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 게시글 검색 Port Adapter
 */
@Component
@RequiredArgsConstructor
public class PostDetailSearchPortAdapter implements PostDetailSearchPort {

    private final PostDetailSearchAdapter postDetailSearchAdapter;

    @Override
    public Optional<PostDetailSearchResponse> findById(UUID id) {
        return postDetailSearchAdapter.findById(id).map(this::toResponse);
    }

    @Override
    public List<PostDetailSearchResponse> searchByTitleAndContent(String keyword, int page, int size) {
        return postDetailSearchAdapter.searchByTitleAndContent(keyword, page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDetailSearchResponse> searchByAuthor(UUID authorId, int page, int size) {
        return postDetailSearchAdapter.searchByAuthor(authorId, page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDetailSearchResponse> searchByCategory(UUID categoryId, int page, int size) {
        return postDetailSearchAdapter.searchByCategory(categoryId, page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDetailSearchResponse> searchByStatus(String status, int page, int size) {
        return postDetailSearchAdapter.searchByStatus(status, page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDetailSearchResponse> searchWithFilters(String keyword, UUID authorId, UUID categoryId,
                                                            String status, int page, int size, String sortField, String sortOrder) {
        SortOrder order = "asc".equalsIgnoreCase(sortOrder) ? SortOrder.Asc : SortOrder.Desc;
        return postDetailSearchAdapter.searchWithFilters(keyword, authorId, categoryId, status, page, size, sortField, order)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDetailSearchResponse> findPopularPosts(int page, int size) {
        return postDetailSearchAdapter.findPopularPosts(page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDetailSearchResponse> findRecentPosts(int page, int size) {
        return postDetailSearchAdapter.findRecentPosts(page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private PostDetailSearchResponse toResponse(PostDetailDocument document) {
        List<UUID> categoryIds = document.getCategoryIds() != null
                ? document.getCategoryIds().stream().map(UUID::fromString).collect(Collectors.toList())
                : List.of();

        return new PostDetailSearchResponse(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                UUID.fromString(document.getAuthorId()),
                document.getAuthorUsername(),
                document.getAuthorDisplayName(),
                document.getStatus(),
                categoryIds,
                document.getCategoryNames(),
                document.getViewCount(),
                document.getCommentCount(),
                document.getLikeCount(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
