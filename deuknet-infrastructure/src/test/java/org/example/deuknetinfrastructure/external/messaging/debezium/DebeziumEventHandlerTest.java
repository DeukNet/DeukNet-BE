package org.example.deuknetinfrastructure.external.messaging.debezium;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.deuknetapplication.messaging.EventType;
import org.example.deuknetinfrastructure.external.messaging.handler.EventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * DebeziumEventHandler 단위 테스트
 *
 * Debezium Outbox Event Router가 변환한 CDC 이벤트를
 * 제대로 파싱하고 적절한 EventHandler에 위임하는지 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DebeziumEventHandler Unit Test")
class DebeziumEventHandlerTest {

    @Mock
    private EventHandler mockEventHandler;

    private DebeziumEventHandler debeziumEventHandler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        debeziumEventHandler = new DebeziumEventHandler(
                List.of(mockEventHandler),
                objectMapper
        );
    }

    @Test
    @DisplayName("Debezium 이벤트에서 eventType과 aggregateId를 제대로 추출한다")
    void shouldExtractEventTypeAndAggregateId() throws Exception {
        // Given: Debezium Outbox Event Router가 변환한 JSON
        // additional.placement로 eventType과 aggregateId가 envelope에 추가됨
        String cdcEvent = """
            {
              "payload": {
                "eventType": "PostCreated",
                "aggregateId": "123e4567-e89b-12d3-a456-426614174000",
                "payload": {
                  "id": "123e4567-e89b-12d3-a456-426614174000",
                  "title": "Test Post",
                  "content": "Test Content",
                  "status": "DRAFT"
                }
              }
            }
            """;

        when(mockEventHandler.canHandle(EventType.POST_CREATED)).thenReturn(true);

        // When: 이벤트 처리
        debeziumEventHandler.handleEvent("test-key", cdcEvent);

        // Then: 올바른 EventType과 aggregateId로 handler가 호출되어야 함
        ArgumentCaptor<EventType> eventTypeCaptor = ArgumentCaptor.forClass(EventType.class);
        ArgumentCaptor<String> aggregateIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        verify(mockEventHandler).handle(
                eventTypeCaptor.capture(),
                aggregateIdCaptor.capture(),
                payloadCaptor.capture()
        );

        assertThat(eventTypeCaptor.getValue()).isEqualTo(EventType.POST_CREATED);
        assertThat(aggregateIdCaptor.getValue()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
        assertThat(payloadCaptor.getValue()).contains("Test Post");
    }

    @Test
    @DisplayName("envelope이 null인 경우 조용히 스킵한다")
    void shouldSkipIfEnvelopeIsNull() throws Exception {
        // Given: envelope이 null인 CDC 이벤트
        String cdcEvent = """
            {
              "payload": null
            }
            """;

        // When: 이벤트 처리
        debeziumEventHandler.handleEvent("test-key", cdcEvent);

        // Then: handler가 호출되지 않아야 함
        verify(mockEventHandler, never()).handle(any(), any(), any());
    }

    @Test
    @DisplayName("알 수 없는 eventType인 경우 경고 로그를 남기고 스킵한다")
    void shouldSkipUnknownEventType() throws Exception {
        // Given: 알 수 없는 eventType을 가진 CDC 이벤트
        String cdcEvent = """
            {
              "payload": {
                "eventType": "UnknownEventType",
                "aggregateId": "123e4567-e89b-12d3-a456-426614174000",
                "payload": {}
              }
            }
            """;

        // When & Then: 예외가 발생하지 않아야 함
        assertThatCode(() -> debeziumEventHandler.handleEvent("test-key", cdcEvent))
                .doesNotThrowAnyException();

        verify(mockEventHandler, never()).handle(any(), any(), any());
    }

    @Test
    @DisplayName("처리할 수 있는 handler가 없는 경우 경고 로그를 남긴다")
    void shouldLogWarningIfNoHandlerFound() throws Exception {
        // Given: POST_CREATED 이벤트이지만 처리할 handler가 없음
        String cdcEvent = """
            {
              "payload": {
                "eventType": "PostCreated",
                "aggregateId": "123e4567-e89b-12d3-a456-426614174000",
                "payload": {}
              }
            }
            """;

        when(mockEventHandler.canHandle(EventType.POST_CREATED)).thenReturn(false);

        // When: 이벤트 처리
        debeziumEventHandler.handleEvent("test-key", cdcEvent);

        // Then: handler가 호출되지 않아야 함
        verify(mockEventHandler, never()).handle(any(), any(), any());
        verify(mockEventHandler).canHandle(EventType.POST_CREATED);
    }

    @Test
    @DisplayName("POST_PUBLISHED 이벤트를 제대로 처리한다")
    void shouldHandlePostPublishedEvent() throws Exception {
        // Given: POST_PUBLISHED 이벤트
        String cdcEvent = """
            {
              "payload": {
                "eventType": "PostPublished",
                "aggregateId": "123e4567-e89b-12d3-a456-426614174000",
                "payload": {
                  "id": "123e4567-e89b-12d3-a456-426614174000",
                  "title": "Published Post",
                  "status": "PUBLISHED"
                }
              }
            }
            """;

        when(mockEventHandler.canHandle(EventType.POST_PUBLISHED)).thenReturn(true);

        // When: 이벤트 처리
        debeziumEventHandler.handleEvent("test-key", cdcEvent);

        // Then: POST_PUBLISHED 타입으로 handler가 호출되어야 함
        ArgumentCaptor<EventType> eventTypeCaptor = ArgumentCaptor.forClass(EventType.class);

        verify(mockEventHandler).handle(
                eventTypeCaptor.capture(),
                eq("123e4567-e89b-12d3-a456-426614174000"),
                any()
        );

        assertThat(eventTypeCaptor.getValue()).isEqualTo(EventType.POST_PUBLISHED);
    }

    @Test
    @DisplayName("REACTION_ADDED 이벤트를 제대로 처리한다")
    void shouldHandleReactionAddedEvent() throws Exception {
        // Given: REACTION_ADDED 이벤트
        String cdcEvent = """
            {
              "payload": {
                "eventType": "ReactionAdded",
                "aggregateId": "123e4567-e89b-12d3-a456-426614174000",
                "payload": {
                  "id": "123e4567-e89b-12d3-a456-426614174000",
                  "likeCount": 5
                }
              }
            }
            """;

        when(mockEventHandler.canHandle(EventType.REACTION_ADDED)).thenReturn(true);

        // When: 이벤트 처리
        debeziumEventHandler.handleEvent("test-key", cdcEvent);

        // Then: REACTION_ADDED 타입으로 handler가 호출되어야 함
        ArgumentCaptor<EventType> eventTypeCaptor = ArgumentCaptor.forClass(EventType.class);

        verify(mockEventHandler).handle(
                eventTypeCaptor.capture(),
                eq("123e4567-e89b-12d3-a456-426614174000"),
                any()
        );

        assertThat(eventTypeCaptor.getValue()).isEqualTo(EventType.REACTION_ADDED);
    }

    @Test
    @DisplayName("잘못된 JSON 형식인 경우 예외를 로깅하고 계속 진행한다")
    void shouldLogExceptionForInvalidJson() throws Exception {
        // Given: 잘못된 JSON
        String invalidJson = "{ invalid json }";

        // When & Then: 예외가 던져지지 않아야 함 (내부적으로 로깅만)
        assertThatCode(() -> debeziumEventHandler.handleEvent("test-key", invalidJson))
                .doesNotThrowAnyException();

        verify(mockEventHandler, never()).handle(any(), any(), any());
    }

    @Test
    @DisplayName("여러 handler 중 처리 가능한 첫 번째 handler에게 위임한다")
    void shouldDelegateToFirstCapableHandler() throws Exception {
        // Given: 3개의 handler가 있고, 두 번째 handler만 처리 가능
        EventHandler handler1 = mock(EventHandler.class);
        EventHandler handler2 = mock(EventHandler.class);
        EventHandler handler3 = mock(EventHandler.class);

        debeziumEventHandler = new DebeziumEventHandler(
                List.of(handler1, handler2, handler3),
                objectMapper
        );

        String cdcEvent = """
            {
              "payload": {
                "eventType": "PostCreated",
                "aggregateId": "123e4567-e89b-12d3-a456-426614174000",
                "payload": {}
              }
            }
            """;

        when(handler1.canHandle(EventType.POST_CREATED)).thenReturn(false);
        when(handler2.canHandle(EventType.POST_CREATED)).thenReturn(true);
        // handler3는 호출되지 않으므로 stubbing 제거

        // When: 이벤트 처리
        debeziumEventHandler.handleEvent("test-key", cdcEvent);

        // Then: handler2만 호출되고 handler3는 호출되지 않아야 함
        verify(handler1).canHandle(EventType.POST_CREATED);
        verify(handler2).canHandle(EventType.POST_CREATED);
        verify(handler2).handle(any(), any(), any());
        verify(handler3, never()).canHandle(any());
        verify(handler3, never()).handle(any(), any(), any());
    }
}
