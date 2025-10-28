package org.example.deuknetapplication.dto.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 리액션 카운트 검색 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReactionCountSearchResponse {
    private UUID targetId;
    private Long likeCount;
    private Long loveCount;
    private Long laughCount;
    private Long wowCount;
    private Long sadCount;
    private Long angryCount;
    private Long totalCount;
    private Map<String, Long> reactionCounts;
    private LocalDateTime lastEventTimestamp;
}
