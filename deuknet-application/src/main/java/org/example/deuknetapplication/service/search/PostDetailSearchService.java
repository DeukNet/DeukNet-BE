package org.example.deuknetapplication.service.search;

import lombok.RequiredArgsConstructor;
import org.example.deuknetapplication.dto.search.PostDetailSearchResponse;
import org.example.deuknetapplication.port.out.search.PostDetailSearchPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Post 검색 서비스
 *
 * Elasticsearch를 활용한 게시글 검색 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class PostDetailSearchService {

    private final PostDetailSearchPort postDetailSearchPort;

    public Optional<PostDetailSearchResponse> findById(UUID id) {
        return postDetailSearchPort.findById(id);
    }

    public List<PostDetailSearchResponse> searchByTitleAndContent(String keyword, int page, int size) {
        return postDetailSearchPort.searchByTitleAndContent(keyword, page, size);
    }

    public List<PostDetailSearchResponse> searchByAuthor(UUID authorId, int page, int size) {
        return postDetailSearchPort.searchByAuthor(authorId, page, size);
    }

    public List<PostDetailSearchResponse> searchByCategory(UUID categoryId, int page, int size) {
        return postDetailSearchPort.searchByCategory(categoryId, page, size);
    }

    public List<PostDetailSearchResponse> searchByStatus(String status, int page, int size) {
        return postDetailSearchPort.searchByStatus(status, page, size);
    }

    public List<PostDetailSearchResponse> searchWithFilters(
            String keyword,
            UUID authorId,
            UUID categoryId,
            String status,
            int page,
            int size,
            String sortField,
            String sortOrder
    ) {
        return postDetailSearchPort.searchWithFilters(
                keyword, authorId, categoryId, status, page, size, sortField, sortOrder
        );
    }

    public List<PostDetailSearchResponse> findPopularPosts(int page, int size) {
        return postDetailSearchPort.findPopularPosts(page, size);
    }

    public List<PostDetailSearchResponse> findRecentPosts(int page, int size) {
        return postDetailSearchPort.findRecentPosts(page, size);
    }
}
