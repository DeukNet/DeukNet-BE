package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.out.external.search.PostSearchPort;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
import org.example.deuknetdomain.domain.post.AuthorType;
import org.example.deuknetdomain.domain.post.PostStatus;
import org.example.deuknetdomain.domain.post.exception.PostNotFoundException;
import org.example.deuknetdomain.domain.reaction.Reaction;
import org.example.deuknetdomain.domain.reaction.ReactionType;
import org.example.deuknetdomain.domain.reaction.TargetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPostService 단위 테스트")
class GetPostServiceTest {

    @Mock
    private PostSearchPort postSearchPort;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ReactionRepository reactionRepository;

    @Mock
    private CurrentUserPort currentUserPort;

    @Mock
    private org.example.deuknetapplication.port.out.repository.UserRepository userRepository;

    @InjectMocks
    private GetPostService getPostService;

    private UUID testPostId;
    private UUID testUserId;
    private PostDetailProjection testProjection;

    @BeforeEach
    void setUp() {
        testPostId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        UUID otherAuthorId = UUID.randomUUID(); // 다른 사용자 ID

        testProjection = PostDetailProjection.builder()
                .id(testPostId)
                .title("Test Post")
                .content("Test Content")
                .authorId(otherAuthorId)  // testUserId와 다른 ID
                .authorType(AuthorType.REAL.name())
                .status(PostStatus.PUBLIC.name())
                .categoryId(null)
                .viewCount(0L)
                .commentCount(0L)
                .likeCount(0L)
                .dislikeCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // User mock 설정 (lenient로 변경하여 사용되지 않는 경우에도 에러 방지)
        org.example.deuknetdomain.domain.user.User mockUser = org.example.deuknetdomain.domain.user.User.restore(
                otherAuthorId,
                UUID.randomUUID(),
                "testuser",
                "Test User",
                "Test bio",
                "https://example.com/avatar.jpg",
                org.example.deuknetdomain.domain.user.UserRole.USER,
                false
        );
        lenient().when(userRepository.findById(otherAuthorId)).thenReturn(Optional.of(mockUser));
    }

    @Test
    @DisplayName("Elasticsearch에서 게시물을 찾으면 PostgreSQL 조회를 하지 않는다")
    void whenPostFoundInElasticsearch_thenDoNotQueryPostgres() {
        // Given
        PostSearchResponse elasticsearchResponse = new PostSearchResponse(testProjection);
        when(postSearchPort.findById(testPostId)).thenReturn(Optional.of(elasticsearchResponse));
        when(currentUserPort.getCurrentUserId()).thenReturn(testUserId);
        when(reactionRepository.findByTargetIdAndUserId(testPostId, testUserId))
                .thenReturn(List.of());

        // When
        PostSearchResponse result = getPostService.getPostById(testPostId, false);

        // Then
        assertThat(result).isNotNull();
        verify(postSearchPort).findById(testPostId);
        verify(postRepository, never()).findDetailById(any());
    }

    @Test
    @DisplayName("Elasticsearch에 없으면 PostgreSQL에서 조회한다")
    void whenPostNotFoundInElasticsearch_thenQueryPostgres() {
        // Given
        when(postSearchPort.findById(testPostId)).thenReturn(Optional.empty());
        when(postRepository.findDetailById(testPostId)).thenReturn(Optional.of(testProjection));
        when(currentUserPort.getCurrentUserId()).thenReturn(testUserId);
        when(reactionRepository.findByTargetIdAndUserId(testPostId, testUserId))
                .thenReturn(List.of());

        // When
        PostSearchResponse result = getPostService.getPostById(testPostId, false);

        // Then
        assertThat(result).isNotNull();
        verify(postSearchPort).findById(testPostId);
        verify(postRepository).findDetailById(testPostId);
    }

    @Test
    @DisplayName("PostgreSQL에도 없으면 PostNotFoundException을 던진다")
    void whenPostNotFoundAnywhere_thenThrowException() {
        // Given
        when(postSearchPort.findById(testPostId)).thenReturn(Optional.empty());
        when(postRepository.findDetailById(testPostId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> getPostService.getPostById(testPostId, false))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("사용자가 LIKE를 누른 경우: hasUserLiked=true, hasUserDisliked=false")
    void whenUserHasLiked_thenHasUserLikedIsTrue() {
        // Given
        PostSearchResponse elasticsearchResponse = new PostSearchResponse(testProjection);
        when(postSearchPort.findById(testPostId)).thenReturn(Optional.of(elasticsearchResponse));
        when(currentUserPort.getCurrentUserId()).thenReturn(testUserId);

        Reaction likeReaction = Reaction.create(ReactionType.LIKE, TargetType.POST, testPostId, testUserId);
        when(reactionRepository.findByTargetIdAndUserId(testPostId, testUserId))
                .thenReturn(List.of(likeReaction));

        // When
        PostSearchResponse result = getPostService.getPostById(testPostId, false);

        // Then
        assertThat(result.getHasUserLiked()).isTrue();
        assertThat(result.getHasUserDisliked()).isFalse();
        assertThat(result.getIsAuthor()).isFalse(); // authorId != testUserId
    }

    @Test
    @DisplayName("현재 사용자가 작성자인 경우: isAuthor=true")
    void whenCurrentUserIsAuthor_thenIsAuthorIsTrue() {
        // Given
        PostDetailProjection authorProjection = PostDetailProjection.builder()
                .id(testPostId)
                .title("Test Post")
                .content("Test Content")
                .authorId(testUserId) // 현재 사용자가 작성자
                .authorType(AuthorType.REAL.name())
                .status(PostStatus.PUBLIC.name())
                .categoryId(null)
                .viewCount(0L)
                .commentCount(0L)
                .likeCount(0L)
                .dislikeCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PostSearchResponse elasticsearchResponse = new PostSearchResponse(authorProjection);
        when(postSearchPort.findById(testPostId)).thenReturn(Optional.of(elasticsearchResponse));
        when(currentUserPort.getCurrentUserId()).thenReturn(testUserId);
        when(reactionRepository.findByTargetIdAndUserId(testPostId, testUserId))
                .thenReturn(List.of());

        // When
        PostSearchResponse result = getPostService.getPostById(testPostId, false);

        // Then
        assertThat(result.getIsAuthor()).isTrue();
        assertThat(result.getAuthorId()).isEqualTo(testUserId);
    }

    @Test
    @DisplayName("사용자가 DISLIKE를 누른 경우: hasUserLiked=false, hasUserDisliked=true")
    void whenUserHasDisliked_thenHasUserDislikedIsTrue() {
        // Given
        PostSearchResponse elasticsearchResponse = new PostSearchResponse(testProjection);
        when(postSearchPort.findById(testPostId)).thenReturn(Optional.of(elasticsearchResponse));
        when(currentUserPort.getCurrentUserId()).thenReturn(testUserId);

        Reaction dislikeReaction = Reaction.create(ReactionType.DISLIKE, TargetType.POST, testPostId, testUserId);
        when(reactionRepository.findByTargetIdAndUserId(testPostId, testUserId))
                .thenReturn(List.of(dislikeReaction));

        // When
        PostSearchResponse result = getPostService.getPostById(testPostId, false);

        // Then
        assertThat(result.getHasUserLiked()).isFalse();
        assertThat(result.getHasUserDisliked()).isTrue();
    }

    @Test
    @DisplayName("사용자가 아무 반응도 하지 않은 경우: 모두 false")
    void whenUserHasNoReaction_thenBothAreFalse() {
        // Given
        PostSearchResponse elasticsearchResponse = new PostSearchResponse(testProjection);
        when(postSearchPort.findById(testPostId)).thenReturn(Optional.of(elasticsearchResponse));
        when(currentUserPort.getCurrentUserId()).thenReturn(testUserId);
        when(reactionRepository.findByTargetIdAndUserId(testPostId, testUserId))
                .thenReturn(List.of());

        // When
        PostSearchResponse result = getPostService.getPostById(testPostId, false);

        // Then
        assertThat(result.getHasUserLiked()).isFalse();
        assertThat(result.getHasUserDisliked()).isFalse();
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 경우: 모두 false")
    void whenUserNotAuthenticated_thenBothAreFalse() {
        // Given
        PostSearchResponse elasticsearchResponse = new PostSearchResponse(testProjection);
        when(postSearchPort.findById(testPostId)).thenReturn(Optional.of(elasticsearchResponse));
        when(currentUserPort.getCurrentUserId()).thenThrow(new RuntimeException("Not authenticated"));

        // When
        PostSearchResponse result = getPostService.getPostById(testPostId, false);

        // Then
        assertThat(result.getHasUserLiked()).isFalse();
        assertThat(result.getHasUserDisliked()).isFalse();
        assertThat(result.getIsAuthor()).isFalse(); // 비인증 사용자는 작성자가 아님
        verify(reactionRepository, never()).findByTargetIdAndUserId(any(), any());
    }

    @Test
    @DisplayName("PostgreSQL 폴백 시에도 사용자 reaction이 제대로 설정된다")
    void whenFallingBackToPostgres_thenUserReactionIsStillEnriched() {
        // Given
        when(postSearchPort.findById(testPostId)).thenReturn(Optional.empty());
        when(postRepository.findDetailById(testPostId)).thenReturn(Optional.of(testProjection));
        when(currentUserPort.getCurrentUserId()).thenReturn(testUserId);

        Reaction likeReaction = Reaction.create(ReactionType.LIKE, TargetType.POST, testPostId, testUserId);
        when(reactionRepository.findByTargetIdAndUserId(testPostId, testUserId))
                .thenReturn(List.of(likeReaction));

        // When
        PostSearchResponse result = getPostService.getPostById(testPostId, false);

        // Then
        assertThat(result.getHasUserLiked()).isTrue();
        assertThat(result.getHasUserDisliked()).isFalse();
        verify(postRepository).findDetailById(testPostId);
    }
}
