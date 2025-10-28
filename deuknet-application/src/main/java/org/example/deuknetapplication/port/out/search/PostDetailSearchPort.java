package org.example.deuknetapplication.port.out.search;

import org.example.deuknetapplication.dto.search.PostDetailSearchResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 게시글 검색 Port
 */
public interface PostDetailSearchPort {
    Optional<PostDetailSearchResponse> findById(UUID id);
    List<PostDetailSearchResponse> searchByTitleAndContent(String keyword, int page, int size);
    List<PostDetailSearchResponse> searchByAuthor(UUID authorId, int page, int size);
    List<PostDetailSearchResponse> searchByCategory(UUID categoryId, int page, int size);
    List<PostDetailSearchResponse> searchByStatus(String status, int page, int size);
    List<PostDetailSearchResponse> searchWithFilters(String keyword, UUID authorId, UUID categoryId, String status, int page, int size, String sortField, String sortOrder);
    List<PostDetailSearchResponse> findPopularPosts(int page, int size);
    List<PostDetailSearchResponse> findRecentPosts(int page, int size);
}
