package org.example.integration.post;

import org.example.deuknetapplication.port.in.post.PostSearchRequest;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.service.post.PostSearchService;
import org.example.deuknetinfrastructure.external.search.adapter.PostSearchAdapter;
import org.example.deuknetinfrastructure.external.search.document.PostDetailDocument;
import org.example.seedwork.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 게시글 검색 통합 테스트
 *
 * Elasticsearch에 직접 데이터를 저장하고 통합 검색 API로 조회합니다.
 */
class PostSearchIntegrationTest extends AbstractTest {

    @Autowired
    private PostSearchAdapter postSearchAdapter;

    @Autowired
    private PostSearchService postSearchService;

    @Test
    void ID로_게시글_조회() {
        // Given
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        PostDetailDocument document = PostDetailDocument.create(
                postId,
                "테스트 게시글",
                "테스트 내용입니다",
                authorId,
                "testuser",
                "테스트 작성자",
                "PUBLISHED",
                List.of(UUID.randomUUID()),
                List.of("테스트 카테고리"),
                0L,
                0L,
                0L
        );

        postSearchAdapter.save(document);
        waitForElasticsearch();

        // When
        Optional<PostSearchResponse> result = postSearchService.findById(postId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("테스트 게시글");
        assertThat(result.get().getContent()).isEqualTo("테스트 내용입니다");
    }

    @Test
    void 작성자로_필터링_검색() {
        // Given
        UUID authorId1 = UUID.randomUUID();
        UUID authorId2 = UUID.randomUUID();

        postSearchAdapter.save(createDocument(UUID.randomUUID(), "Post 1", "Content 1", authorId1));
        postSearchAdapter.save(createDocument(UUID.randomUUID(), "Post 2", "Content 2", authorId1));
        postSearchAdapter.save(createDocument(UUID.randomUUID(), "Post 3", "Content 3", authorId2));

        waitForElasticsearch();

        // When - authorId1의 게시글만 검색
        PostSearchRequest request = PostSearchRequest.builder()
                .authorId(authorId1)
                .build();

        List<PostSearchResponse> results = postSearchService.search(request);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getAuthorId().equals(authorId1));
    }

    @Test
    void 카테고리로_필터링_검색() {
        // Given
        UUID categoryId1 = UUID.randomUUID();
        UUID categoryId2 = UUID.randomUUID();

        postSearchAdapter.save(createDocumentWithCategory(UUID.randomUUID(), "Post 1", categoryId1));
        postSearchAdapter.save(createDocumentWithCategory(UUID.randomUUID(), "Post 2", categoryId1));
        postSearchAdapter.save(createDocumentWithCategory(UUID.randomUUID(), "Post 3", categoryId2));

        waitForElasticsearch();

        // When
        PostSearchRequest request = PostSearchRequest.builder()
                .categoryId(categoryId1)
                .build();

        List<PostSearchResponse> results = postSearchService.search(request);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getCategoryIds().contains(categoryId1));
    }

    @Test
    void 여러_조건_AND_검색() {
        // Given
        UUID authorId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        // 조건에 맞는 게시글
        postSearchAdapter.save(createDocumentWithAll(
                UUID.randomUUID(), "Matching Post", "PUBLISHED", authorId, categoryId));

        // 조건에 맞지 않는 게시글들
        postSearchAdapter.save(createDocumentWithAll(
                UUID.randomUUID(), "Wrong Author", "PUBLISHED", UUID.randomUUID(), categoryId));
        postSearchAdapter.save(createDocumentWithAll(
                UUID.randomUUID(), "Wrong Category", "PUBLISHED", authorId, UUID.randomUUID()));
        postSearchAdapter.save(createDocumentWithAll(
                UUID.randomUUID(), "Draft Status", "DRAFT", authorId, categoryId));

        waitForElasticsearch();

        // When - 모든 조건을 만족하는 게시글만 검색
        PostSearchRequest request = PostSearchRequest.builder()
                .authorId(authorId)
                .categoryId(categoryId)
                .status("PUBLISHED")
                .build();

        List<PostSearchResponse> results = postSearchService.search(request);

        // Then - 1개만 조회되어야 함
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Matching Post");
    }

    @Test
    void 인기_게시글_조회() {
        // Given
        UUID authorId = UUID.randomUUID();

        PostDetailDocument popularPost = createDocument(UUID.randomUUID(), "인기 게시글", "Content", authorId);
        popularPost.setLikeCount(100L);

        PostDetailDocument normalPost = createDocument(UUID.randomUUID(), "일반 게시글", "Content", authorId);
        normalPost.setLikeCount(10L);

        postSearchAdapter.save(popularPost);
        postSearchAdapter.save(normalPost);

        waitForElasticsearch();

        // When
        List<PostSearchResponse> results = postSearchService.findPopularPosts(0, 10);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getLikeCount())
                .isGreaterThanOrEqualTo(results.get(results.size() - 1).getLikeCount());
    }

    private PostDetailDocument createDocument(UUID postId, String title, String content, UUID authorId) {
        return PostDetailDocument.create(
                postId,
                title,
                content,
                authorId,
                "user" + authorId.toString().substring(0, 8),
                "Author " + authorId,
                "PUBLISHED",
                List.of(UUID.randomUUID()),
                List.of("Category"),
                0L,
                0L,
                0L
        );
    }

    private PostDetailDocument createDocumentWithCategory(UUID postId, String title, UUID categoryId) {
        return PostDetailDocument.create(
                postId,
                title,
                "Content",
                UUID.randomUUID(),
                "testuser",
                "Test Author",
                "PUBLISHED",
                List.of(categoryId),
                List.of("Category"),
                0L,
                0L,
                0L
        );
    }

    private PostDetailDocument createDocumentWithAll(
            UUID postId, String title, String status, UUID authorId, UUID categoryId) {
        return PostDetailDocument.create(
                postId,
                title,
                "Content",
                authorId,
                "testuser",
                "Test Author",
                status,
                List.of(categoryId),
                List.of("Category"),
                0L,
                0L,
                0L
        );
    }

    private void waitForElasticsearch() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
