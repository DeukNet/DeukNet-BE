package org.example.deuknetinfrastructure.external.messaging.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetinfrastructure.external.search.adapter.PostSearchAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PostEventHandler 단위 테스트
 *
 * PostDetailProjection과 PostCountProjection을 올바르게 구분하고
 * Elasticsearch에 적절한 작업을 수행하는지 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PostEventHandler Unit Test")
class PostEventHandlerTest {

    @Mock
    private PostSearchAdapter mockPostSearchAdapter;

    private PostEventHandler postEventHandler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        postEventHandler = new PostEventHandler(mockPostSearchAdapter, objectMapper);
    }

    @Test
    @DisplayName("POST_CREATED, POST_UPDATED, POST_PUBLISHED 이벤트를 처리할 수 있다")
    void shouldHandlePostEvents() {
        assertThat(postEventHandler.canHandle(EventType.POST_CREATED)).isTrue();
        assertThat(postEventHandler.canHandle(EventType.POST_UPDATED)).isTrue();
        assertThat(postEventHandler.canHandle(EventType.POST_PUBLISHED)).isTrue();
        assertThat(postEventHandler.canHandle(EventType.POST_DELETED)).isTrue();
    }

    @Test
    @DisplayName("REACTION_ADDED 이벤트는 처리할 수 없다")
    void shouldNotHandleReactionEvents() {
        assertThat(postEventHandler.canHandle(EventType.REACTION_ADDED)).isFalse();
        assertThat(postEventHandler.canHandle(EventType.REACTION_REMOVED)).isFalse();
    }

    @Test
    @DisplayName("PostDetailProjection (title 필드 포함) 이벤트는 indexPostDetail을 호출한다")
    void shouldIndexPostDetailForDetailProjection() throws Exception {
        // Given: PostDetailProjection (title 필드 포함)
        String postDetailPayload = """
            {
              "id": "123e4567-e89b-12d3-a456-426614174000",
              "title": "Test Post",
              "content": "Test Content",
              "authorId": "author-123",
              "status": "DRAFT",
              "viewCount": 0,
              "likeCount": 0,
              "dislikeCount": 0,
              "commentCount": 0
            }
            """;

        // When: POST_CREATED 이벤트 처리
        postEventHandler.handle(
                EventType.POST_CREATED,
                "123e4567-e89b-12d3-a456-426614174000",
                postDetailPayload
        );

        // Then: indexPostDetail이 호출되어야 함
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockPostSearchAdapter).indexPostDetail(payloadCaptor.capture());
        verify(mockPostSearchAdapter, never()).updatePostCounts(any());

        assertThat(payloadCaptor.getValue()).contains("Test Post");
    }

    @Test
    @DisplayName("PostCountProjection (viewCount만 포함, title 없음) 이벤트는 updatePostCounts를 호출한다")
    void shouldUpdatePostCountsForCountProjection() throws Exception {
        // Given: PostCountProjection (viewCount 필드 포함, title 없음)
        String postCountPayload = """
            {
              "id": "123e4567-e89b-12d3-a456-426614174000",
              "viewCount": 10,
              "likeCount": 5,
              "dislikeCount": 2,
              "commentCount": 3
            }
            """;

        // When: REACTION_ADDED 이벤트 처리 (EventType은 무관, payload 구조로 판단)
        postEventHandler.handle(
                EventType.POST_CREATED,
                "123e4567-e89b-12d3-a456-426614174000",
                postCountPayload
        );

        // Then: updatePostCounts가 호출되어야 함
        verify(mockPostSearchAdapter).updatePostCounts(postCountPayload);
        verify(mockPostSearchAdapter, never()).indexPostDetail(any());
    }

    @Test
    @DisplayName("POST_DELETED 이벤트는 deletePost를 호출한다")
    void shouldDeletePostForDeletedEvent() throws Exception {
        // Given: POST_DELETED 이벤트
        String aggregateId = "123e4567-e89b-12d3-a456-426614174000";

        // When: POST_DELETED 이벤트 처리
        postEventHandler.handle(
                EventType.POST_DELETED,
                aggregateId,
                "{}" // payload는 사용되지 않음
        );

        // Then: deletePost가 호출되어야 함
        verify(mockPostSearchAdapter).deletePost(aggregateId);
        verify(mockPostSearchAdapter, never()).indexPostDetail(any());
        verify(mockPostSearchAdapter, never()).updatePostCounts(any());
    }

    @Test
    @DisplayName("POST_PUBLISHED 이벤트에서 PostDetailProjection은 indexPostDetail을 호출한다")
    void shouldIndexPostDetailForPublishedEvent() throws Exception {
        // Given: POST_PUBLISHED 이벤트의 PostDetailProjection
        String postDetailPayload = """
            {
              "id": "123e4567-e89b-12d3-a456-426614174000",
              "title": "Published Post",
              "content": "Published Content",
              "status": "PUBLISHED",
              "viewCount": 0
            }
            """;

        // When: POST_PUBLISHED 이벤트 처리
        postEventHandler.handle(
                EventType.POST_PUBLISHED,
                "123e4567-e89b-12d3-a456-426614174000",
                postDetailPayload
        );

        // Then: indexPostDetail이 호출되어야 함
        verify(mockPostSearchAdapter).indexPostDetail(any());
        verify(mockPostSearchAdapter, never()).updatePostCounts(any());
    }

    @Test
    @DisplayName("POST_PUBLISHED 이벤트에서 PostCountProjection은 updatePostCounts를 호출한다")
    void shouldUpdatePostCountsForPublishedCountProjection() throws Exception {
        // Given: POST_PUBLISHED 이벤트의 PostCountProjection
        String postCountPayload = """
            {
              "id": "123e4567-e89b-12d3-a456-426614174000",
              "viewCount": 0,
              "likeCount": 0,
              "dislikeCount": 0,
              "commentCount": 0
            }
            """;

        // When: POST_PUBLISHED 이벤트 처리
        postEventHandler.handle(
                EventType.POST_PUBLISHED,
                "123e4567-e89b-12d3-a456-426614174000",
                postCountPayload
        );

        // Then: updatePostCounts가 호출되어야 함
        verify(mockPostSearchAdapter).updatePostCounts(any());
        verify(mockPostSearchAdapter, never()).indexPostDetail(any());
    }

    @Test
    @DisplayName("title과 viewCount가 둘 다 있는 경우 PostDetailProjection으로 처리한다 (우선순위)")
    void shouldTreatAsDetailProjectionWhenBothFieldsPresent() throws Exception {
        // Given: title과 viewCount가 모두 있는 payload
        String payload = """
            {
              "id": "123e4567-e89b-12d3-a456-426614174000",
              "title": "Test Post",
              "content": "Test Content",
              "viewCount": 10,
              "likeCount": 5
            }
            """;

        // When: 이벤트 처리
        postEventHandler.handle(
                EventType.POST_CREATED,
                "123e4567-e89b-12d3-a456-426614174000",
                payload
        );

        // Then: title이 우선되어 indexPostDetail이 호출되어야 함
        verify(mockPostSearchAdapter).indexPostDetail(any());
        verify(mockPostSearchAdapter, never()).updatePostCounts(any());
    }

    @Test
    @DisplayName("title도 viewCount도 없는 경우 아무 작업도 하지 않는다")
    void shouldDoNothingWhenNeitherFieldPresent() throws Exception {
        // Given: title도 viewCount도 없는 payload
        String payload = """
            {
              "id": "123e4567-e89b-12d3-a456-426614174000",
              "someOtherField": "value"
            }
            """;

        // When: 이벤트 처리
        postEventHandler.handle(
                EventType.POST_CREATED,
                "123e4567-e89b-12d3-a456-426614174000",
                payload
        );

        // Then: 아무 메서드도 호출되지 않아야 함
        verify(mockPostSearchAdapter, never()).indexPostDetail(any());
        verify(mockPostSearchAdapter, never()).updatePostCounts(any());
    }

    @Test
    @DisplayName("POST_UPDATED 이벤트로 PostDetailProjection을 처리한다")
    void shouldHandlePostUpdatedWithDetailProjection() throws Exception {
        // Given: POST_UPDATED 이벤트의 PostDetailProjection
        String payload = """
            {
              "id": "123e4567-e89b-12d3-a456-426614174000",
              "title": "Updated Title",
              "content": "Updated Content",
              "status": "DRAFT"
            }
            """;

        // When: POST_UPDATED 이벤트 처리
        postEventHandler.handle(
                EventType.POST_UPDATED,
                "123e4567-e89b-12d3-a456-426614174000",
                payload
        );

        // Then: indexPostDetail이 호출되어야 함
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockPostSearchAdapter).indexPostDetail(payloadCaptor.capture());

        assertThat(payloadCaptor.getValue()).contains("Updated Title");
    }
}
