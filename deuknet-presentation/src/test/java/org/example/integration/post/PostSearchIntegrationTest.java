package org.example.integration.post;

import org.example.deuknetapplication.port.in.post.PostSearchRequest;
import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.in.post.SearchPostUseCase;
import org.example.deuknetapplication.port.out.external.search.PostProjectionCommandPort;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.seedwork.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
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
    private PostProjectionCommandPort postProjectionCommandPort;

    @Autowired
    private SearchPostUseCase searchPostUseCase;

    @Test
    void ID로_게시글_조회() {
        // Given
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        PostDetailProjection projection = PostDetailProjection.builder()
                .id(postId)
                .title("테스트 게시글")
                .content("테스트 내용입니다")
                .authorId(authorId)
                .authorUsername("testuser")
                .authorDisplayName("테스트 작성자")
                .authorAvatarUrl(null)
                .status("PUBLISHED")
                .viewCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .categoryIds(List.of(UUID.randomUUID()))
                .categoryNames(List.of("테스트 카테고리"))
                .commentCount(0L)
                .likeCount(0L)
                .dislikeCount(0L)
                .build();

        postProjectionCommandPort.save(projection);
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

        postProjectionCommandPort.save(createProjection(UUID.randomUUID(), "Post 1", "Content 1", authorId1));
        postProjectionCommandPort.save(createProjection(UUID.randomUUID(), "Post 2", "Content 2", authorId1));
        postProjectionCommandPort.save(createProjection(UUID.randomUUID(), "Post 3", "Content 3", authorId2));

        waitForElasticsearch();

        // When - authorId1의 게시글만 검색
        PostSearchRequest request = PostSearchRequest.builder()
                .authorId(authorId1)
                .build();

        List<PostSearchResponse> results = searchPostUseCase.search(request).getContent();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getAuthorId().equals(authorId1));
    }

    @Test
    void 카테고리로_필터링_검색() {
        // Given
        UUID categoryId1 = UUID.randomUUID();
        UUID categoryId2 = UUID.randomUUID();

        postProjectionCommandPort.save(createProjectionWithCategory(UUID.randomUUID(), "Post 1", categoryId1));
        postProjectionCommandPort.save(createProjectionWithCategory(UUID.randomUUID(), "Post 2", categoryId1));
        postProjectionCommandPort.save(createProjectionWithCategory(UUID.randomUUID(), "Post 3", categoryId2));

        waitForElasticsearch();

        // When
        PostSearchRequest request = PostSearchRequest.builder()
                .categoryId(categoryId1)
                .build();

        List<PostSearchResponse> results = searchPostUseCase.search(request).getContent();

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
        postProjectionCommandPort.save(createProjectionWithAll(
                UUID.randomUUID(), "Matching Post", "PUBLISHED", authorId, categoryId));

        // 조건에 맞지 않는 게시글들
        postProjectionCommandPort.save(createProjectionWithAll(
                UUID.randomUUID(), "Wrong Author", "PUBLISHED", UUID.randomUUID(), categoryId));
        postProjectionCommandPort.save(createProjectionWithAll(
                UUID.randomUUID(), "Wrong Category", "PUBLISHED", authorId, UUID.randomUUID()));
        postProjectionCommandPort.save(createProjectionWithAll(
                UUID.randomUUID(), "Draft Status", "DRAFT", authorId, categoryId));

        waitForElasticsearch();

        // When - 모든 조건을 만족하는 게시글만 검색
        PostSearchRequest request = PostSearchRequest.builder()
                .authorId(authorId)
                .categoryId(categoryId)
                .status("PUBLISHED")
                .build();

        List<PostSearchResponse> results = searchPostUseCase.search(request).getContent();

        // Then - 1개만 조회되어야 함
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Matching Post");
    }

    @Test
    void 인기_게시글_조회() {
        // Given
        UUID authorId = UUID.randomUUID();

        PostDetailProjection popularPost = PostDetailProjection.builder()
                .id(UUID.randomUUID())
                .title("인기 게시글")
                .content("Content")
                .authorId(authorId)
                .authorUsername("user" + authorId.toString().substring(0, 8))
                .authorDisplayName("Author " + authorId)
                .authorAvatarUrl(null)
                .status("PUBLISHED")
                .categoryIds(List.of(UUID.randomUUID()))
                .categoryNames(List.of("Category"))
                .viewCount(0L)
                .commentCount(0L)
                .likeCount(100L)
                .dislikeCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PostDetailProjection normalPost = PostDetailProjection.builder()
                .id(UUID.randomUUID())
                .title("일반 게시글")
                .content("Content")
                .authorId(authorId)
                .authorUsername("user" + authorId.toString().substring(0, 8))
                .authorDisplayName("Author " + authorId)
                .authorAvatarUrl(null)
                .status("PUBLISHED")
                .categoryIds(List.of(UUID.randomUUID()))
                .categoryNames(List.of("Category"))
                .viewCount(0L)
                .commentCount(0L)
                .likeCount(10L)
                .dislikeCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        postProjectionCommandPort.save(popularPost);
        postProjectionCommandPort.save(normalPost);

        waitForElasticsearch();

        // When
        List<PostSearchResponse> results = searchPostUseCase.findPopularPosts(0, 20, null, null).getContent();

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getLikeCount())
                .isGreaterThanOrEqualTo(results.get(results.size() - 1).getLikeCount());
    }

    @Test
    void 키워드_검색() {
        // Given - 고유한 키워드 사용
        String uniqueKeyword = "Kubernetes" + UUID.randomUUID().toString().substring(0, 8);

        postProjectionCommandPort.save(createProjection(UUID.randomUUID(), uniqueKeyword + " Tutorial", "Learn " + uniqueKeyword, UUID.randomUUID()));
        postProjectionCommandPort.save(createProjection(UUID.randomUUID(), "Java Basics", uniqueKeyword + " is great", UUID.randomUUID()));
        postProjectionCommandPort.save(createProjection(UUID.randomUUID(), "Python Guide", "Python tutorial", UUID.randomUUID()));

        waitForElasticsearch();

        // When - 고유 키워드로 검색 (제목 + 내용)
        PostSearchRequest request = PostSearchRequest.builder()
                .keyword(uniqueKeyword)
                .build();

        List<PostSearchResponse> results = searchPostUseCase.search(request).getContent();

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

        postProjectionCommandPort.save(createProjection(UUID.randomUUID(), "Java Guide", "Java tutorial", authorId1));
        postProjectionCommandPort.save(createProjection(UUID.randomUUID(), "Java Advanced", "Advanced Java", authorId1));
        postProjectionCommandPort.save(createProjection(UUID.randomUUID(), "Java Basics", "Basic Java", authorId2));

        waitForElasticsearch();

        // When - "Java" 키워드 + authorId1
        PostSearchRequest request = PostSearchRequest.builder()
                .keyword("Java")
                .authorId(authorId1)
                .build();

        List<PostSearchResponse> results = searchPostUseCase.search(request).getContent();

        // Then - authorId1의 Java 게시글만 (2개)
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getAuthorId().equals(authorId1));
    }

    @Test
    void 카테고리_AND_상태_필터링() {
        // Given
        UUID categoryId = UUID.randomUUID();

        postProjectionCommandPort.save(createProjectionWithCategoryAndStatus(UUID.randomUUID(), "Post 1", categoryId, "PUBLISHED"));
        postProjectionCommandPort.save(createProjectionWithCategoryAndStatus(UUID.randomUUID(), "Post 2", categoryId, "PUBLISHED"));
        postProjectionCommandPort.save(createProjectionWithCategoryAndStatus(UUID.randomUUID(), "Post 3", categoryId, "DRAFT"));
        postProjectionCommandPort.save(createProjectionWithCategoryAndStatus(UUID.randomUUID(), "Post 4", UUID.randomUUID(), "PUBLISHED"));

        waitForElasticsearch();

        // When - categoryId + PUBLISHED
        PostSearchRequest request = PostSearchRequest.builder()
                .categoryId(categoryId)
                .status("PUBLISHED")
                .build();

        List<PostSearchResponse> results = searchPostUseCase.search(request).getContent();

        // Then - 해당 카테고리의 PUBLISHED만 (2개)
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r ->
            r.getCategoryIds().contains(categoryId) && r.getStatus().equals("PUBLISHED"));
    }

    @Test
    void 키워드_AND_카테고리_AND_상태_필터링() {
        // Given
        UUID categoryId = UUID.randomUUID();

        postProjectionCommandPort.save(createProjectionWithCategoryAndStatus(UUID.randomUUID(), "Spring Tutorial", categoryId, "PUBLISHED"));
        postProjectionCommandPort.save(createProjectionWithCategoryAndStatus(UUID.randomUUID(), "Spring Advanced", categoryId, "DRAFT"));
        postProjectionCommandPort.save(createProjectionWithCategoryAndStatus(UUID.randomUUID(), "Java Tutorial", categoryId, "PUBLISHED"));
        postProjectionCommandPort.save(createProjectionWithCategoryAndStatus(UUID.randomUUID(), "Spring Guide", UUID.randomUUID(), "PUBLISHED"));

        waitForElasticsearch();

        // When - "Spring" + categoryId + "PUBLISHED"
        PostSearchRequest request = PostSearchRequest.builder()
                .keyword("Spring")
                .categoryId(categoryId)
                .status("PUBLISHED")
                .build();

        List<PostSearchResponse> results = searchPostUseCase.search(request).getContent();

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
        postProjectionCommandPort.save(createProjectionFull(UUID.randomUUID(), "Spring Boot Guide", "Complete Spring tutorial",
            authorId, categoryId, "PUBLISHED"));

        // 일부 조건만 만족
        postProjectionCommandPort.save(createProjectionFull(UUID.randomUUID(), "Spring Advanced", "Advanced Spring",
            UUID.randomUUID(), categoryId, "PUBLISHED")); // 다른 작성자
        postProjectionCommandPort.save(createProjectionFull(UUID.randomUUID(), "Spring Basics", "Basic Spring",
            authorId, UUID.randomUUID(), "PUBLISHED")); // 다른 카테고리
        postProjectionCommandPort.save(createProjectionFull(UUID.randomUUID(), "Spring Tips", "Spring tips",
            authorId, categoryId, "DRAFT")); // DRAFT 상태
        postProjectionCommandPort.save(createProjectionFull(UUID.randomUUID(), "Java Guide", "Java tutorial",
            authorId, categoryId, "PUBLISHED")); // 키워드 불일치

        waitForElasticsearch();

        // When - 키워드 + 작성자 + 카테고리 + 상태 모두 필터링
        PostSearchRequest request = PostSearchRequest.builder()
                .keyword("Spring")
                .authorId(authorId)
                .categoryId(categoryId)
                .status("PUBLISHED")
                .build();

        List<PostSearchResponse> results = searchPostUseCase.search(request).getContent();

        // Then - 모든 조건을 만족하는 1개만
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Spring Boot Guide");
        assertThat(results.get(0).getAuthorId()).isEqualTo(authorId);
        assertThat(results.get(0).getCategoryIds()).contains(categoryId);
        assertThat(results.get(0).getStatus()).isEqualTo("PUBLISHED");
    }

    private PostDetailProjection createProjection(UUID postId, String title, String content, UUID authorId) {
        return PostDetailProjection.builder()
                .id(postId)
                .title(title)
                .content(content)
                .authorId(authorId)
                .authorUsername("user" + authorId.toString().substring(0, 8))
                .authorDisplayName("Author " + authorId)
                .authorAvatarUrl(null)
                .status("PUBLISHED")
                .categoryIds(List.of(UUID.randomUUID()))
                .categoryNames(List.of("Category"))
                .viewCount(0L)
                .commentCount(0L)
                .likeCount(0L)
                .dislikeCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private PostDetailProjection createProjectionWithCategory(UUID postId, String title, UUID categoryId) {
        return PostDetailProjection.builder()
                .id(postId)
                .title(title)
                .content("Content")
                .authorId(UUID.randomUUID())
                .authorUsername("testuser")
                .authorDisplayName("Test Author")
                .authorAvatarUrl(null)
                .status("PUBLISHED")
                .categoryIds(List.of(categoryId))
                .categoryNames(List.of("Category"))
                .viewCount(0L)
                .commentCount(0L)
                .likeCount(0L)
                .dislikeCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private PostDetailProjection createProjectionWithAll(UUID postId, String title, String status, UUID authorId, UUID categoryId) {
        return PostDetailProjection.builder()
                .id(postId)
                .title(title)
                .content("Content")
                .authorId(authorId)
                .authorUsername("testuser")
                .authorDisplayName("Test Author")
                .authorAvatarUrl(null)
                .status(status)
                .categoryIds(List.of(categoryId))
                .categoryNames(List.of("Category"))
                .viewCount(0L)
                .commentCount(0L)
                .likeCount(0L)
                .dislikeCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private PostDetailProjection createProjectionWithCategoryAndStatus(UUID postId, String title, UUID categoryId, String status) {
        return PostDetailProjection.builder()
                .id(postId)
                .title(title)
                .content("Content")
                .authorId(UUID.randomUUID())
                .authorUsername("testuser")
                .authorDisplayName("Test Author")
                .authorAvatarUrl(null)
                .status(status)
                .categoryIds(List.of(categoryId))
                .categoryNames(List.of("Category"))
                .viewCount(0L)
                .commentCount(0L)
                .likeCount(0L)
                .dislikeCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private PostDetailProjection createProjectionFull(UUID postId, String title, String content,
                                                       UUID authorId, UUID categoryId, String status) {
        return PostDetailProjection.builder()
                .id(postId)
                .title(title)
                .content(content)
                .authorId(authorId)
                .authorUsername("testuser")
                .authorDisplayName("Test Author")
                .authorAvatarUrl(null)
                .status(status)
                .categoryIds(List.of(categoryId))
                .categoryNames(List.of("Category"))
                .viewCount(0L)
                .commentCount(0L)
                .likeCount(0L)
                .dislikeCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private void waitForElasticsearch() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
