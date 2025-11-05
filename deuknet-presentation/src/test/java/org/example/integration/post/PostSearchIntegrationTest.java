package org.example.integration.post;

import org.example.deuknetapplication.port.in.post.PostSearchRequest;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.in.post.SearchPostUseCase;
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
    private SearchPostUseCase searchPostUseCase;

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
                0L,
                0L
        );

        postSearchAdapter.save(document);
        waitForElasticsearch();

        // When
        Optional<PostSearchResponse> result = searchPostUseCase.findById(postId);

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

        List<PostSearchResponse> results = searchPostUseCase.search(request);

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

        List<PostSearchResponse> results = searchPostUseCase.search(request);

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

        List<PostSearchResponse> results = searchPostUseCase.search(request);

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
        List<PostSearchResponse> results = searchPostUseCase.findPopularPosts(0, 10);

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
                0L,
                0L
        );
    }

    @Test
    void 키워드_검색() {
        // Given - 고유한 키워드 사용
        String uniqueKeyword = "Kubernetes" + UUID.randomUUID().toString().substring(0, 8);

        postSearchAdapter.save(createDocument(UUID.randomUUID(), uniqueKeyword + " Tutorial", "Learn " + uniqueKeyword, UUID.randomUUID()));
        postSearchAdapter.save(createDocument(UUID.randomUUID(), "Java Basics", uniqueKeyword + " is great", UUID.randomUUID()));
        postSearchAdapter.save(createDocument(UUID.randomUUID(), "Python Guide", "Python tutorial", UUID.randomUUID()));

        waitForElasticsearch();

        // When - 고유 키워드로 검색 (제목 + 내용)
        PostSearchRequest request = PostSearchRequest.builder()
                .keyword(uniqueKeyword)
                .build();

        List<PostSearchResponse> results = searchPostUseCase.search(request);

        // Then - 해당 키워드가 포함된 2개만 검색
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r ->
            r.getTitle().contains(uniqueKeyword) || r.getContent().contains(uniqueKeyword));
    }

    @Test
    void 키워드_AND_작성자_필터링() {
        // Given
        UUID authorId1 = UUID.randomUUID();
        UUID authorId2 = UUID.randomUUID();

        postSearchAdapter.save(createDocument(UUID.randomUUID(), "Java Guide", "Java tutorial", authorId1));
        postSearchAdapter.save(createDocument(UUID.randomUUID(), "Java Advanced", "Advanced Java", authorId1));
        postSearchAdapter.save(createDocument(UUID.randomUUID(), "Java Basics", "Basic Java", authorId2));

        waitForElasticsearch();

        // When - "Java" 키워드 + authorId1
        PostSearchRequest request = PostSearchRequest.builder()
                .keyword("Java")
                .authorId(authorId1)
                .build();

        List<PostSearchResponse> results = searchPostUseCase.search(request);

        // Then - authorId1의 Java 게시글만 (2개)
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getAuthorId().equals(authorId1));
    }

    @Test
    void 카테고리_AND_상태_필터링() {
        // Given
        UUID categoryId = UUID.randomUUID();

        postSearchAdapter.save(createDocumentWithCategoryAndStatus(UUID.randomUUID(), "Post 1", categoryId, "PUBLISHED"));
        postSearchAdapter.save(createDocumentWithCategoryAndStatus(UUID.randomUUID(), "Post 2", categoryId, "PUBLISHED"));
        postSearchAdapter.save(createDocumentWithCategoryAndStatus(UUID.randomUUID(), "Post 3", categoryId, "DRAFT"));
        postSearchAdapter.save(createDocumentWithCategoryAndStatus(UUID.randomUUID(), "Post 4", UUID.randomUUID(), "PUBLISHED"));

        waitForElasticsearch();

        // When - categoryId + PUBLISHED
        PostSearchRequest request = PostSearchRequest.builder()
                .categoryId(categoryId)
                .status("PUBLISHED")
                .build();

        List<PostSearchResponse> results = searchPostUseCase.search(request);

        // Then - 해당 카테고리의 PUBLISHED만 (2개)
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r ->
            r.getCategoryIds().contains(categoryId) && r.getStatus().equals("PUBLISHED"));
    }

    @Test
    void 키워드_AND_카테고리_AND_상태_필터링() {
        // Given
        UUID categoryId = UUID.randomUUID();

        postSearchAdapter.save(createDocumentWithCategoryAndStatus(UUID.randomUUID(), "Spring Tutorial", categoryId, "PUBLISHED"));
        postSearchAdapter.save(createDocumentWithCategoryAndStatus(UUID.randomUUID(), "Spring Advanced", categoryId, "DRAFT"));
        postSearchAdapter.save(createDocumentWithCategoryAndStatus(UUID.randomUUID(), "Java Tutorial", categoryId, "PUBLISHED"));
        postSearchAdapter.save(createDocumentWithCategoryAndStatus(UUID.randomUUID(), "Spring Guide", UUID.randomUUID(), "PUBLISHED"));

        waitForElasticsearch();

        // When - "Spring" + categoryId + "PUBLISHED"
        PostSearchRequest request = PostSearchRequest.builder()
                .keyword("Spring")
                .categoryId(categoryId)
                .status("PUBLISHED")
                .build();

        List<PostSearchResponse> results = searchPostUseCase.search(request);

        // Then - 모든 조건 만족하는 1개만
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Spring Tutorial");
    }

    @Test
    void 모든_필터_조합_검색() {
        // Given
        UUID authorId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        // 모든 조건 만족
        postSearchAdapter.save(createDocumentFull(UUID.randomUUID(), "Spring Boot Guide", "Complete Spring tutorial",
            authorId, categoryId, "PUBLISHED"));

        // 일부 조건만 만족
        postSearchAdapter.save(createDocumentFull(UUID.randomUUID(), "Spring Advanced", "Advanced Spring",
            UUID.randomUUID(), categoryId, "PUBLISHED")); // 다른 작성자
        postSearchAdapter.save(createDocumentFull(UUID.randomUUID(), "Spring Basics", "Basic Spring",
            authorId, UUID.randomUUID(), "PUBLISHED")); // 다른 카테고리
        postSearchAdapter.save(createDocumentFull(UUID.randomUUID(), "Spring Tips", "Spring tips",
            authorId, categoryId, "DRAFT")); // DRAFT 상태
        postSearchAdapter.save(createDocumentFull(UUID.randomUUID(), "Java Guide", "Java tutorial",
            authorId, categoryId, "PUBLISHED")); // 키워드 불일치

        waitForElasticsearch();

        // When - 키워드 + 작성자 + 카테고리 + 상태 모두 필터링
        PostSearchRequest request = PostSearchRequest.builder()
                .keyword("Spring")
                .authorId(authorId)
                .categoryId(categoryId)
                .status("PUBLISHED")
                .build();

        List<PostSearchResponse> results = searchPostUseCase.search(request);

        // Then - 모든 조건을 만족하는 1개만
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Spring Boot Guide");
        assertThat(results.get(0).getAuthorId()).isEqualTo(authorId);
        assertThat(results.get(0).getCategoryIds()).contains(categoryId);
        assertThat(results.get(0).getStatus()).isEqualTo("PUBLISHED");
    }

    private PostDetailDocument createDocumentWithCategoryAndStatus(UUID postId, String title, UUID categoryId, String status) {
        return PostDetailDocument.create(
                postId,
                title,
                "Content",
                UUID.randomUUID(),
                "testuser",
                "Test Author",
                status,
                List.of(categoryId),
                List.of("Category"),
                0L,
                0L,
                0L,
                0L
        );
    }

    private PostDetailDocument createDocumentFull(UUID postId, String title, String content,
                                                   UUID authorId, UUID categoryId, String status) {
        return PostDetailDocument.create(
                postId,
                title,
                content,
                authorId,
                "testuser",
                "Test Author",
                status,
                List.of(categoryId),
                List.of("Category"),
                0L,
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
