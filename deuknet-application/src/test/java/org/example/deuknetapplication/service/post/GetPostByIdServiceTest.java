package org.example.deuknetapplication.service.post;

import org.example.deuknetapplication.port.in.post.PostSearchResponse;
import org.example.deuknetapplication.port.out.post.PostSearchPort;
import org.example.deuknetapplication.port.out.repository.PostRepository;
import org.example.deuknetapplication.port.out.repository.ReactionRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.post.PostDetailProjection;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPostByIdService 단위 테스트")
class GetPostByIdServiceTest {

    @Mock
    private PostSearchPort postSearchPort;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ReactionRepository reactionRepository;

    @Mock
    private CurrentUserPort currentUserPort;

    @InjectMocks
    private GetPostByIdService getPostByIdService;

    private UUID testPostId;
    private UUID testUserId;
    private PostDetailProjection testProjection;

    @BeforeEach
    void setUp() {
        testPostId = UUID.randomUUID();
        testUserId = UUID.randomUUID();

        testProjection = PostDetailProjection.builder()
                .id(testPostId)
                .title("Test Post")
                .content("Test Content")
                .authorId(UUID.randomUUID())
                .authorUsername("testuser")
                .authorDisplayName("Test User")
                .status(PostStatus.PUBLISHED.name())
                .categoryIds(List.of())
                .categoryNames(List.of())
                .viewCount(0L)
                .commentCount(0L)
                .likeCount(0L)
                .dislikeCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Elasticsearch에서 게시물을 찾으면 PostgreSQL 조회를 하지 않는다")
    void whenPostFoundInElasticsearch_thenDoNotQueryPostgres() {
        // Given
        PostSearchResponse elasticsearchResponse = new PostSearchResponse(testProjection);
        when(postSearchPort.findById(testPostId)).thenReturn(Optional.of(elasticsearchResponse));
        when(currentUserPort.getCurrentUserId()).thenReturn(testUserId);
        when(reactionRepository.findByTargetIdAndUserIdAndReactionType(any(), any(), any()))
                .thenReturn(Optional.empty());

        // When
        PostSearchResponse result = getPostByIdService.getPostById(testPostId);

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
        when(reactionRepository.findByTargetIdAndUserIdAndReactionType(any(), any(), any()))
                .thenReturn(Optional.empty());

        // When
        PostSearchResponse result = getPostByIdService.getPostById(testPostId);

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
        assertThatThrownBy(() -> getPostByIdService.getPostById(testPostId))
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
        when(reactionRepository.findByTargetIdAndUserIdAndReactionType(
                testPostId, testUserId, ReactionType.LIKE))
                .thenReturn(Optional.of(likeReaction));
        when(reactionRepository.findByTargetIdAndUserIdAndReactionType(
                testPostId, testUserId, ReactionType.DISLIKE))
                .thenReturn(Optional.empty());

        // When
        PostSearchResponse result = getPostByIdService.getPostById(testPostId);

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
                .authorUsername("testuser")
                .authorDisplayName("Test User")
                .status(PostStatus.PUBLISHED.name())
                .categoryIds(List.of())
                .categoryNames(List.of())
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
        when(reactionRepository.findByTargetIdAndUserIdAndReactionType(any(), any(), any()))
                .thenReturn(Optional.empty());

        // When
        PostSearchResponse result = getPostByIdService.getPostById(testPostId);

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
        when(reactionRepository.findByTargetIdAndUserIdAndReactionType(
                testPostId, testUserId, ReactionType.LIKE))
                .thenReturn(Optional.empty());
        when(reactionRepository.findByTargetIdAndUserIdAndReactionType(
                testPostId, testUserId, ReactionType.DISLIKE))
                .thenReturn(Optional.of(dislikeReaction));

        // When
        PostSearchResponse result = getPostByIdService.getPostById(testPostId);

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
        when(reactionRepository.findByTargetIdAndUserIdAndReactionType(any(), any(), any()))
                .thenReturn(Optional.empty());

        // When
        PostSearchResponse result = getPostByIdService.getPostById(testPostId);

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
        PostSearchResponse result = getPostByIdService.getPostById(testPostId);

        // Then
        assertThat(result.getHasUserLiked()).isFalse();
        assertThat(result.getHasUserDisliked()).isFalse();
        assertThat(result.getIsAuthor()).isFalse(); // 비인증 사용자는 작성자가 아님
        verify(reactionRepository, never()).findByTargetIdAndUserIdAndReactionType(any(), any(), any());
    }

    @Test
    @DisplayName("PostgreSQL 폴백 시에도 사용자 reaction이 제대로 설정된다")
    void whenFallingBackToPostgres_thenUserReactionIsStillEnriched() {
        // Given
        when(postSearchPort.findById(testPostId)).thenReturn(Optional.empty());
        when(postRepository.findDetailById(testPostId)).thenReturn(Optional.of(testProjection));
        when(currentUserPort.getCurrentUserId()).thenReturn(testUserId);

        Reaction likeReaction = Reaction.create(ReactionType.LIKE, TargetType.POST, testPostId, testUserId);
        when(reactionRepository.findByTargetIdAndUserIdAndReactionType(
                testPostId, testUserId, ReactionType.LIKE))
                .thenReturn(Optional.of(likeReaction));
        when(reactionRepository.findByTargetIdAndUserIdAndReactionType(
                testPostId, testUserId, ReactionType.DISLIKE))
                .thenReturn(Optional.empty());

        // When
        PostSearchResponse result = getPostByIdService.getPostById(testPostId);

        // Then
        assertThat(result.getHasUserLiked()).isTrue();
        assertThat(result.getHasUserDisliked()).isFalse();
        verify(postRepository).findDetailById(testPostId);
    }
}
